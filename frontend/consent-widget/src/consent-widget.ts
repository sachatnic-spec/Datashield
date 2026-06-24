/**
 * DataShield Consent Widget SDK
 * Embeddable consent management widget for DPDP Act 2023 compliance
 * 
 * @version 1.0.0
 * @license MIT
 */

export interface ConsentConfig {
  apiUrl: string;
  tenantId: string;
  purposes: ConsentPurpose[];
  theme?: 'light' | 'dark';
  language?: string;
  position?: 'bottom' | 'top' | 'center';
  requireParentalConsent?: boolean;
  onConsentGranted?: (consent: ConsentResponse) => void;
  onConsentWithdrawn?: (consentId: string) => void;
}

export interface ConsentPurpose {
  id: string;
  name: string;
  description: string;
  required: boolean;
  category: 'ESSENTIAL' | 'MARKETING' | 'ANALYTICS' | 'PERSONALIZATION';
}

export interface ConsentResponse {
  consentId: string;
  purposes: string[];
  grantedAt: string;
  expiresAt?: string;
}

export class DataShieldConsentWidget {
  private config: ConsentConfig;
  private container: HTMLElement | null = null;
  private overlay: HTMLElement | null = null;

  constructor(config: ConsentConfig) {
    this.config = {
      theme: 'light',
      language: 'en',
      position: 'bottom',
      requireParentalConsent: false,
      ...config
    };
  }

  /**
   * Initialize and render the consent widget
   */
  public init(): void {
    if (this.hasExistingConsent()) {
      console.log('[DataShield] Existing consent found');
      return;
    }

    this.render();
  }

  /**
   * Show the consent widget
   */
  public show(): void {
    this.render();
  }

  /**
   * Hide the consent widget
   */
  public hide(): void {
    if (this.overlay) {
      this.overlay.remove();
      this.overlay = null;
    }
    if (this.container) {
      this.container.remove();
      this.container = null;
    }
  }

  /**
   * Check if user has already given consent
   */
  private hasExistingConsent(): boolean {
    const consent = localStorage.getItem(`ds_consent_${this.config.tenantId}`);
    return !!consent;
  }

  /**
   * Render the consent widget UI
   */
  private render(): void {
    // Create overlay
    this.overlay = document.createElement('div');
    this.overlay.className = 'ds-consent-overlay';
    this.overlay.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      z-index: 9998;
      display: flex;
      align-items: ${this.getAlignmentStyle()};
      justify-content: center;
      padding: 20px;
    `;

    // Create container
    this.container = document.createElement('div');
    this.container.className = 'ds-consent-container';
    this.container.style.cssText = `
      background: ${this.config.theme === 'dark' ? '#1f2937' : '#ffffff'};
      color: ${this.config.theme === 'dark' ? '#ffffff' : '#000000'};
      border-radius: 12px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
      max-width: 600px;
      width: 100%;
      padding: 24px;
      z-index: 9999;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    `;

    // Header
    const header = document.createElement('div');
    header.innerHTML = `
      <h2 style="margin: 0 0 12px 0; font-size: 24px; font-weight: 700;">
        🔒 Your Privacy Choices
      </h2>
      <p style="margin: 0 0 20px 0; font-size: 14px; opacity: 0.8;">
        We value your privacy. Please choose how we can use your data under DPDP Act 2023.
      </p>
    `;
    this.container.appendChild(header);

    // Purpose checkboxes
    const purposesContainer = document.createElement('div');
    purposesContainer.style.cssText = 'margin-bottom: 20px;';

    this.config.purposes.forEach(purpose => {
      const purposeDiv = document.createElement('label');
      purposeDiv.style.cssText = `
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 12px;
        border: 1px solid ${this.config.theme === 'dark' ? '#374151' : '#e5e7eb'};
        border-radius: 8px;
        margin-bottom: 12px;
        cursor: ${purpose.required ? 'not-allowed' : 'pointer'};
        opacity: ${purpose.required ? '0.7' : '1'};
      `;

      const checkbox = document.createElement('input');
      checkbox.type = 'checkbox';
      checkbox.id = `ds-purpose-${purpose.id}`;
      checkbox.checked = purpose.required;
      checkbox.disabled = purpose.required;
      checkbox.style.cssText = 'margin-top: 4px; cursor: pointer;';

      const details = document.createElement('div');
      details.innerHTML = `
        <div style="font-weight: 600; margin-bottom: 4px;">
          ${purpose.name}
          ${purpose.required ? '<span style="font-size: 12px; color: #ef4444;">(Required)</span>' : ''}
        </div>
        <div style="font-size: 13px; opacity: 0.8;">
          ${purpose.description}
        </div>
      `;

      purposeDiv.appendChild(checkbox);
      purposeDiv.appendChild(details);
      purposesContainer.appendChild(purposeDiv);
    });

    this.container.appendChild(purposesContainer);

    // Buttons
    const buttons = document.createElement('div');
    buttons.style.cssText = 'display: flex; gap: 12px;';

    const acceptButton = document.createElement('button');
    acceptButton.textContent = 'Accept Selected';
    acceptButton.style.cssText = `
      flex: 1;
      padding: 12px 24px;
      background: #3b82f6;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    `;
    acceptButton.onmouseover = () => acceptButton.style.background = '#2563eb';
    acceptButton.onmouseout = () => acceptButton.style.background = '#3b82f6';
    acceptButton.onclick = () => this.handleAccept();

    const rejectButton = document.createElement('button');
    rejectButton.textContent = 'Reject Optional';
    rejectButton.style.cssText = `
      flex: 1;
      padding: 12px 24px;
      background: ${this.config.theme === 'dark' ? '#374151' : '#e5e7eb'};
      color: ${this.config.theme === 'dark' ? '#ffffff' : '#000000'};
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    `;
    rejectButton.onclick = () => this.handleReject();

    buttons.appendChild(acceptButton);
    buttons.appendChild(rejectButton);
    this.container.appendChild(buttons);

    // Privacy policy link
    const footer = document.createElement('div');
    footer.style.cssText = 'margin-top: 16px; text-align: center; font-size: 12px; opacity: 0.7;';
    footer.innerHTML = `
      <a href="#" style="color: #3b82f6; text-decoration: none;">Privacy Policy</a> |
      <a href="#" style="color: #3b82f6; text-decoration: none;">Terms of Service</a>
    `;
    this.container.appendChild(footer);

    // Append to DOM
    this.overlay.appendChild(this.container);
    document.body.appendChild(this.overlay);
  }

  /**
   * Handle accept button click
   */
  private async handleAccept(): Promise<void> {
    const selectedPurposes: string[] = [];

    this.config.purposes.forEach(purpose => {
      const checkbox = document.getElementById(`ds-purpose-${purpose.id}`) as HTMLInputElement;
      if (checkbox && checkbox.checked) {
        selectedPurposes.push(purpose.id);
      }
    });

    // Save consent to API
    const consent = await this.saveConsent(selectedPurposes);

    // Store locally
    localStorage.setItem(
      `ds_consent_${this.config.tenantId}`,
      JSON.stringify(consent)
    );

    // Callback
    if (this.config.onConsentGranted) {
      this.config.onConsentGranted(consent);
    }

    this.hide();
  }

  /**
   * Handle reject button click
   */
  private async handleReject(): Promise<void> {
    // Only accept required purposes
    const requiredPurposes = this.config.purposes
      .filter(p => p.required)
      .map(p => p.id);

    const consent = await this.saveConsent(requiredPurposes);

    localStorage.setItem(
      `ds_consent_${this.config.tenantId}`,
      JSON.stringify(consent)
    );

    if (this.config.onConsentGranted) {
      this.config.onConsentGranted(consent);
    }

    this.hide();
  }

  /**
   * Save consent to API
   */
  private async saveConsent(purposes: string[]): Promise<ConsentResponse> {
    try {
      const response = await fetch(`${this.config.apiUrl}/consents`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Tenant-ID': this.config.tenantId
        },
        body: JSON.stringify({
          purposes,
          source: 'CONSENT_WIDGET',
          language: this.config.language
        })
      });

      return await response.json();
    } catch (error) {
      console.error('[DataShield] Failed to save consent:', error);
      // Return mock consent on failure
      return {
        consentId: `mock-${Date.now()}`,
        purposes,
        grantedAt: new Date().toISOString()
      };
    }
  }

  /**
   * Get alignment style based on position
   */
  private getAlignmentStyle(): string {
    switch (this.config.position) {
      case 'top': return 'flex-start';
      case 'bottom': return 'flex-end';
      case 'center': return 'center';
      default: return 'flex-end';
    }
  }

  /**
   * Withdraw consent
   */
  public async withdrawConsent(consentId: string): Promise<void> {
    try {
      await fetch(`${this.config.apiUrl}/consents/${consentId}/withdraw`, {
        method: 'POST',
        headers: {
          'X-Tenant-ID': this.config.tenantId
        }
      });

      localStorage.removeItem(`ds_consent_${this.config.tenantId}`);

      if (this.config.onConsentWithdrawn) {
        this.config.onConsentWithdrawn(consentId);
      }
    } catch (error) {
      console.error('[DataShield] Failed to withdraw consent:', error);
    }
  }
}

// Auto-initialize if config found in window
declare global {
  interface Window {
    DataShieldConfig?: ConsentConfig;
    DataShieldConsent?: DataShieldConsentWidget;
  }
}

if (typeof window !== 'undefined' && window.DataShieldConfig) {
  window.DataShieldConsent = new DataShieldConsentWidget(window.DataShieldConfig);
  window.DataShieldConsent.init();
}

export default DataShieldConsentWidget;
