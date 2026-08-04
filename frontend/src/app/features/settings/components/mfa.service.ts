import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

/**
 * MFA Service
 * Handles all MFA-related API calls for the frontend
 */
@Injectable({
  providedIn: 'root'
})
export class MFAService {
  private apiBase = '/v1/auth/mfa';
  private mfaStatusSubject = new BehaviorSubject<any>(null);
  public mfaStatus$ = this.mfaStatusSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadMFAStatus();
  }

  /**
   * Load current MFA status for user
   */
  loadMFAStatus(): void {
    this.getMFAStatus().subscribe(
      status => this.mfaStatusSubject.next(status),
      error => console.error('Failed to load MFA status:', error)
    );
  }

  /**
   * Setup TOTP - Get QR code and secret
   */
  setupTOTP(tenantId: string): Observable<any> {
    return this.http.post<any>(`${this.apiBase}/totp/setup`, { tenantId })
      .pipe(
        tap(response => {
          if (response) {
            // Store setup ID temporarily in session storage for verification
            sessionStorage.setItem('mfa_setup_id', response.mfaSetupId);
          }
        }),
        catchError(error => {
          console.error('TOTP setup failed:', error);
          throw error;
        })
      );
  }

  /**
   * Verify TOTP code
   */
  verifyTOTP(tenantId: string, mfaSetupId: string, verificationCode: string): Observable<any> {
    return this.http.post<any>(`${this.apiBase}/totp/verify`, {
      tenantId,
      mfaSetupId,
      verificationCode
    })
      .pipe(
        tap(response => {
          if (response.status === 'VERIFIED') {
            // Clear temporary setup ID
            sessionStorage.removeItem('mfa_setup_id');
            // Reload MFA status
            this.loadMFAStatus();
          }
        }),
        catchError(error => {
          console.error('TOTP verification failed:', error);
          throw error;
        })
      );
  }

  /**
   * Get MFA status for current user
   */
  getMFAStatus(): Observable<any> {
    return this.http.get<any>(`${this.apiBase}/status`)
      .pipe(
        catchError(error => {
          console.error('Failed to get MFA status:', error);
          // Return default status if endpoint not available
          return of({
            mfaEnabled: false,
            devices: [],
            setupRequired: false
          });
        })
      );
  }

  /**
   * List all MFA devices for user
   */
  listDevices(tenantId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiBase}/devices`, {
      params: { tenantId }
    })
      .pipe(
        catchError(error => {
          console.error('Failed to list MFA devices:', error);
          return of([]);
        })
      );
  }

  /**
   * Remove MFA device
   */
  removeDevice(deviceId: string, tenantId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiBase}/devices/${deviceId}`, {
      params: { tenantId }
    })
      .pipe(
        tap(() => {
          // Reload MFA status
          this.loadMFAStatus();
        }),
        catchError(error => {
          console.error('Failed to remove device:', error);
          throw error;
        })
      );
  }

  /**
   * Disable MFA for user
   */
  disableMFA(tenantId: string): Observable<any> {
    return this.http.post<any>(`${this.apiBase}/disable`, { tenantId })
      .pipe(
        tap(() => {
          // Reload MFA status
          this.loadMFAStatus();
        }),
        catchError(error => {
          console.error('Failed to disable MFA:', error);
          throw error;
        })
      );
  }

  /**
   * Export backup codes
   */
  exportBackupCodes(codes: string[]): void {
    const content = codes.join('\n');
    const element = document.createElement('a');
    element.setAttribute('href', `data:text/plain;charset=utf-8,${encodeURIComponent(content)}`);
    element.setAttribute('download', `datashield-mfa-backup-codes-${new Date().toISOString().split('T')[0]}.txt`);
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  }

  /**
   * Copy text to clipboard
   */
  copyToClipboard(text: string): Promise<void> {
    return navigator.clipboard.writeText(text);
  }

  /**
   * Validate TOTP code format
   */
  isValidTOTPCode(code: string): boolean {
    return /^\d{6}$/.test(code.trim());
  }

  /**
   * Open authenticator app link
   */
  openAuthenticatorApp(appType: 'google' | 'authy' | 'microsoft'): void {
    const urls = {
      google: 'https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2',
      authy: 'https://authy.com/download/',
      microsoft: 'https://www.microsoft.com/en-us/account/authenticator'
    };
    window.open(urls[appType], '_blank');
  }
}
