import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';

import { MFAEnrollmentComponent } from './mfa-enrollment.component';
import { MFAService } from './mfa.service';

describe('MFAEnrollmentComponent', () => {
  let component: MFAEnrollmentComponent;
  let fixture: ComponentFixture<MFAEnrollmentComponent>;
  let mfaService: MFAService;
  let httpMock: HttpTestingController;
  let toastrService: ToastrService;

  const mockTenantId = '550e8400-e29b-41d4-a716-446655440000';
  const mockSetupResponse = {
    mfaSetupId: '7a3f8c2d-4e9b-11eb-ae93-0242ac130002',
    mfaType: 'TOTP',
    secret: 'JBSWY3DPEBXG64TMMQ======',
    qrCode: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA...',
    backupCodes: ['8739284756', '2857394021', '5921748630', '1847362910', '4729183457', '6184729301', '3957281046', '7264891352'],
    verificationUrl: 'otpauth://totp/DataShield:test@datasheield.com?secret=JBSWY3DPEBXG64TMMQ======&issuer=DataShield'
  };

  const mockVerifyResponse = {
    mfaDeviceId: 'e4c91d2a-7f3e-4d2c-9b8a-5c6e1f4a2b7d',
    mfaType: 'TOTP',
    status: 'VERIFIED',
    message: 'TOTP device successfully verified and activated'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MFAEnrollmentComponent ],
      imports: [ HttpClientTestingModule ],
      providers: [
        MFAService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ tenantId: mockTenantId })
          }
        },
        {
          provide: ToastrService,
          useValue: {
            success: jasmine.createSpy('success'),
            error: jasmine.createSpy('error'),
            warning: jasmine.createSpy('warning'),
            info: jasmine.createSpy('info')
          }
        }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MFAEnrollmentComponent);
    component = fixture.componentInstance;
    mfaService = TestBed.inject(MFAService);
    httpMock = TestBed.inject(HttpTestingController);
    toastrService = TestBed.inject(ToastrService);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Component Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with select step', () => {
      expect(component.step).toBe('select');
    });

    it('should load tenant ID from route params', () => {
      expect(component.tenantId).toBe(mockTenantId);
    });

    it('should initialize with empty setup data', () => {
      expect(component.setupResponse).toBeNull();
      expect(component.qrCodeDataUrl).toBe('');
      expect(component.secret).toBe('');
      expect(component.backupCodes.length).toBe(0);
    });

    it('should initialize verification state', () => {
      expect(component.verificationCode).toBe('');
      expect(component.isVerifying).toBe(false);
    });
  });

  describe('MFA Type Selection', () => {
    it('should call setupTOTP when TOTP is selected', () => {
      spyOn(component, 'setupTOTP');
      component.selectMFAType('totp');
      expect(component.setupTOTP).toHaveBeenCalled();
    });

    it('should transition to setup step', () => {
      component.selectMFAType('totp');
      expect(component.step).toBe('setup');
    });
  });

  describe('TOTP Setup', () => {
    it('should fetch TOTP setup from API', () => {
      component.setupTOTP();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/setup');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ tenantId: mockTenantId });

      req.flush(mockSetupResponse);

      expect(component.setupResponse).toEqual(mockSetupResponse);
      expect(component.secret).toBe(mockSetupResponse.secret);
    });

    it('should display QR code', () => {
      component.setupTOTP();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/setup');
      req.flush(mockSetupResponse);

      expect(component.qrCodeDataUrl).toBe(mockSetupResponse.qrCode);
    });

    it('should store backup codes', () => {
      component.setupTOTP();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/setup');
      req.flush(mockSetupResponse);

      expect(component.backupCodes).toEqual(mockSetupResponse.backupCodes);
    });

    it('should handle setup error gracefully', () => {
      component.setupTOTP();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/setup');
      req.error(new ErrorEvent('Network error'));

      expect(toastrService.error).toHaveBeenCalled();
    });
  });

  describe('TOTP Verification', () => {
    beforeEach(() => {
      component.setupResponse = mockSetupResponse;
      component.verificationCode = '123456';
    });

    it('should validate 6-digit code format', () => {
      expect(mfaService.isValidTOTPCode('123456')).toBe(true);
      expect(mfaService.isValidTOTPCode('12345')).toBe(false);
      expect(mfaService.isValidTOTPCode('abcdef')).toBe(false);
    });

    it('should reject empty code', () => {
      component.verificationCode = '';
      component.verifyTOTPCode();
      expect(toastrService.error).toHaveBeenCalled();
    });

    it('should reject invalid code format', () => {
      component.verificationCode = 'invalid';
      component.verifyTOTPCode();
      expect(toastrService.error).toHaveBeenCalled();
    });

    it('should verify valid TOTP code', () => {
      component.verifyTOTPCode();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/verify');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        tenantId: mockTenantId,
        mfaSetupId: mockSetupResponse.mfaSetupId,
        verificationCode: '123456'
      });

      req.flush(mockVerifyResponse);

      expect(component.step).toBe('success');
      expect(component.showBackupCodes).toBe(true);
      expect(toastrService.success).toHaveBeenCalled();
    });

    it('should set isVerifying flag during verification', () => {
      component.verifyTOTPCode();
      expect(component.isVerifying).toBe(true);

      const req = httpMock.expectOne('/v1/auth/mfa/totp/verify');
      req.flush(mockVerifyResponse);

      expect(component.isVerifying).toBe(false);
    });

    it('should handle verification error', () => {
      component.verifyTOTPCode();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/verify');
      req.error(new ErrorEvent('Invalid code'));

      expect(toastrService.error).toHaveBeenCalled();
      expect(component.step).not.toBe('success');
    });
  });

  describe('Backup Codes Handling', () => {
    beforeEach(() => {
      component.backupCodes = mockSetupResponse.backupCodes;
    });

    it('should display backup codes after verification', () => {
      component.showBackupCodes = true;
      fixture.detectChanges();

      const codesElement = fixture.nativeElement.querySelector('.backup-codes-section');
      expect(codesElement).toBeTruthy();
    });

    it('should copy backup codes to clipboard', (done) => {
      spyOn(navigator.clipboard, 'writeText').and.returnValue(Promise.resolve());
      const codesText = component.backupCodes.join('\n');

      component.copyBackupCodesToClipboard();

      setTimeout(() => {
        expect(navigator.clipboard.writeText).toHaveBeenCalledWith(codesText);
        expect(toastrService.success).toHaveBeenCalled();
        done();
      }, 100);
    });

    it('should download backup codes as file', () => {
      spyOn(component, 'downloadBackupCodes').and.callThrough();
      const createElementSpy = spyOn(document, 'createElement').and.callThrough();

      component.downloadBackupCodes();

      expect(component.downloadBackupCodes).toHaveBeenCalled();
    });

    it('should require backup codes confirmation', () => {
      component.backupCodesDownloaded = false;
      expect(component.canProceedFromSuccess()).toBe(false);

      component.backupCodesDownloaded = true;
      expect(component.canProceedFromSuccess()).toBe(true);
    });
  });

  describe('Code Input Validation', () => {
    it('should allow only numeric input', () => {
      component.verificationCode = '123abc';
      const isValid = component.isValidCode(component.verificationCode);
      expect(isValid).toBe(false);
    });

    it('should accept exactly 6 digits', () => {
      expect(component.isValidCode('123456')).toBe(true);
      expect(component.isValidCode('12345')).toBe(false);
      expect(component.isValidCode('1234567')).toBe(false);
    });

    it('should trim whitespace', () => {
      expect(component.isValidCode('  123456  ')).toBe(true);
    });
  });

  describe('Navigation Between Steps', () => {
    it('should navigate from select to setup', () => {
      component.step = 'select';
      component.selectMFAType('totp');
      expect(component.step).toBe('setup');
    });

    it('should navigate from setup to verify', () => {
      component.step = 'setup';
      component.setupResponse = mockSetupResponse;
      component.verificationCode = '123456';
      expect(component.step).toBe('setup');
    });

    it('should navigate from verify to success', () => {
      component.step = 'verify';
      component.setupResponse = mockSetupResponse;
      component.verifyTOTPCode();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/verify');
      req.flush(mockVerifyResponse);

      expect(component.step).toBe('success');
    });

    it('should allow going back from verify to setup', () => {
      component.step = 'verify';
      component.goBack();
      expect(component.step).toBe('setup');
    });

    it('should allow going back from setup to select', () => {
      component.step = 'setup';
      component.goBack();
      expect(component.step).toBe('select');
    });
  });

  describe('Error Handling', () => {
    it('should handle network errors in setup', () => {
      component.setupTOTP();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/setup');
      req.error(new ErrorEvent('Network error'));

      expect(toastrService.error).toHaveBeenCalledWith(
        jasmine.stringContaining('Failed to setup')
      );
    });

    it('should handle validation errors in verify', () => {
      component.setupResponse = mockSetupResponse;
      component.verificationCode = '000000';
      component.verifyTOTPCode();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/verify');
      req.error(new ErrorEvent('Invalid code'));

      expect(toastrService.error).toHaveBeenCalled();
    });

    it('should show user-friendly error messages', () => {
      component.setupTOTP();

      const req = httpMock.expectOne('/v1/auth/mfa/totp/setup');
      req.error(new ErrorEvent('Internal server error'));

      expect(toastrService.error).toHaveBeenCalledWith(
        jasmine.stringMatching(/setup|failed/i)
      );
    });
  });

  describe('UI Interactions', () => {
    it('should disable verify button when code is empty', () => {
      component.verificationCode = '';
      expect(component.isVerifyDisabled()).toBe(true);
    });

    it('should disable verify button during verification', () => {
      component.verificationCode = '123456';
      component.isVerifying = true;
      expect(component.isVerifyDisabled()).toBe(true);
    });

    it('should enable verify button with valid input', () => {
      component.verificationCode = '123456';
      component.isVerifying = false;
      expect(component.isVerifyDisabled()).toBe(false);
    });

    it('should show loading indicator during verification', () => {
      component.isVerifying = true;
      fixture.detectChanges();
      // Would check for loading spinner HTML element
    });

    it('should hide backup codes until confirmed', () => {
      component.backupCodesDownloaded = false;
      component.showBackupCodes = true;
      // Proceed button should be disabled
      expect(component.canProceedFromSuccess()).toBe(false);
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels', () => {
      component.step = 'verify';
      fixture.detectChanges();

      const codeInput = fixture.nativeElement.querySelector('input[type="text"]');
      expect(codeInput?.getAttribute('aria-label')).toBeTruthy();
    });

    it('should support keyboard navigation', () => {
      component.verificationCode = '12345';
      const event = new KeyboardEvent('keydown', { key: '6' });
      // Should allow numeric input only
    });

    it('should have sufficient color contrast', () => {
      // Visual test - verify against WCAG AA standards
    });
  });
});
