import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

/**
 * MFA Enrollment Component - Angular
 * Handles TOTP setup, QR code scanning, verification, and backup codes
 */
@Component({
  selector: 'app-mfa-enrollment',
  templateUrl: './mfa-enrollment.component.html',
  styleUrls: ['./mfa-enrollment.component.scss']
})
export class MFAEnrollmentComponent implements OnInit {
  
  step: 'select' | 'setup' | 'verify' | 'success' = 'select';
  
  // Setup data
  setupResponse: any = null;
  qrCodeDataUrl: string = '';
  secret: string = '';
  backupCodes: string[] = [];
  
  // Verification data
  verificationCode: string = '';
  isVerifying: boolean = false;
  
  // State
  tenantId: string = '';
  showBackupCodes: boolean = false;
  backupCodesDownloaded: boolean = false;
  
  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.tenantId = params['tenantId'] || '';
    });
  }

  selectMFAType(type: string): void {
    if (type === 'totp') {
      this.setupTOTP();
    }
  }

  setupTOTP(): void {
    const payload = { tenantId: this.tenantId };
    
    this.http.post('/v1/auth/mfa/totp/setup', payload).subscribe({
      next: (response: any) => {
        this.setupResponse = response;
        this.qrCodeDataUrl = response.qrCode;
        this.secret = response.secret;
        this.backupCodes = response.backupCodes;
        this.step = 'setup';
        this.toastr.success('TOTP setup initiated. Scan QR code with your authenticator app.');
      },
      error: (error) => {
        this.toastr.error('Failed to setup TOTP: ' + (error.error?.message || 'Unknown error'));
      }
    });
  }

  verifyTOTPCode(): void {
    if (!this.verificationCode || this.verificationCode.length !== 6) {
      this.toastr.error('Please enter a 6-digit code');
      return;
    }

    this.isVerifying = true;
    const payload = {
      tenantId: this.tenantId,
      mfaSetupId: this.setupResponse.mfaSetupId,
      verificationCode: this.verificationCode
    };

    this.http.post('/v1/auth/mfa/totp/verify', payload).subscribe({
      next: (response: any) => {
        this.isVerifying = false;
        this.step = 'success';
        this.toastr.success('TOTP device verified and activated!');
      },
      error: (error) => {
        this.isVerifying = false;
        this.toastr.error('Invalid code. Please try again: ' + (error.error?.message || ''));
        this.verificationCode = '';
      }
    });
  }

  downloadBackupCodes(): void {
    const content = `DataShield MFA Backup Codes\n` +
                   `Generated: ${new Date().toISOString()}\n` +
                   `\nIMPORTANT: Store these codes in a secure location.\n` +
                   `Each code can be used once if you lose access to your authenticator app.\n\n` +
                   this.backupCodes.map((code, i) => `${i + 1}. ${code}`).join('\n');
    
    const blob = new Blob([content], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `datasheild-backup-codes-${Date.now()}.txt`;
    a.click();
    window.URL.revokeObjectURL(url);
    
    this.backupCodesDownloaded = true;
    this.toastr.success('Backup codes downloaded');
  }

  copyBackupCodesToClipboard(): void {
    const text = this.backupCodes.join('\n');
    navigator.clipboard.writeText(text).then(() => {
      this.toastr.success('Backup codes copied to clipboard');
    });
  }

  startOver(): void {
    this.step = 'select';
    this.setupResponse = null;
    this.verificationCode = '';
    this.showBackupCodes = false;
  }
}
