# DataShield Consent Widget SDK

**Embeddable JavaScript SDK for DPDP Act 2023 Consent Management**

## Overview

Lightweight, framework-agnostic consent widget that can be embedded into any website or application for DPDP-compliant consent collection.

## Features

✅ **Lightweight** - <10KB gzipped, no external dependencies  
✅ **Framework Agnostic** - Works with React, Angular, Vue, or vanilla JS  
✅ **DPDP Compliant** - Purpose-specific consent with granular controls  
✅ **Customizable** - Light/dark themes, multiple positions  
✅ **Multi-language** - Ready for 22+ Indian languages  
✅ **Accessible** - WCAG 2.1 AA compliant  
✅ **Mobile Responsive** - Touch-friendly on all devices  
✅ **Fast** - <50ms load time, non-blocking  

## Installation

### NPM

```bash
npm install @datasheild/consent-widget
```

### CDN

```html
<script src="https://cdn.datasheild.in/consent-widget.min.js"></script>
```

## Quick Start

### Script Tag (Global)

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://cdn.datasheild.in/consent-widget.min.js"></script>
  <script>
    window.DataShieldConfig = {
      apiUrl: 'https://api.datasheild.in/v1',
      tenantId: 'your-tenant-id',
      purposes: [
        {
          id: 'essential',
          name: 'Essential Services',
          description: 'Required for platform functionality',
          required: true,
          category: 'ESSENTIAL'
        },
        {
          id: 'marketing',
          name: 'Marketing Communications',
          description: 'Promotional emails and offers',
          required: false,
          category: 'MARKETING'
        }
      ],
      theme: 'light',
      position: 'bottom'
    };
  </script>
</head>
<body>
  <!-- Widget auto-initializes -->
</body>
</html>
```

### ES Module

```typescript
import { DataShieldConsentWidget } from '@datasheild/consent-widget';

const widget = new DataShieldConsentWidget({
  apiUrl: 'https://api.datasheild.in/v1',
  tenantId: 'your-tenant-id',
  purposes: [
    {
      id: 'analytics',
      name: 'Analytics',
      description: 'Help us improve our services',
      required: false,
      category: 'ANALYTICS'
    }
  ],
  onConsentGranted: (consent) => {
    console.log('Consent granted:', consent);
    // Initialize analytics, tracking, etc.
  }
});

widget.init();
```

### React

```tsx
import { useEffect } from 'react';
import { DataShieldConsentWidget } from '@datasheild/consent-widget';

function App() {
  useEffect(() => {
    const widget = new DataShieldConsentWidget({
      apiUrl: 'https://api.datasheild.in/v1',
      tenantId: 'your-tenant-id',
      purposes: [/* ... */],
      onConsentGranted: (consent) => {
        // Handle consent
      }
    });
    
    widget.init();
  }, []);

  return <div>Your App</div>;
}
```

### Angular

```typescript
import { Component, OnInit } from '@angular/core';
import { DataShieldConsentWidget } from '@datasheild/consent-widget';

@Component({
  selector: 'app-root',
  template: '<router-outlet />'
})
export class AppComponent implements OnInit {
  ngOnInit() {
    const widget = new DataShieldConsentWidget({
      apiUrl: 'https://api.datasheild.in/v1',
      tenantId: 'your-tenant-id',
      purposes: [/* ... */],
      onConsentGranted: (consent) => {
        // Handle consent
      }
    });
    
    widget.init();
  }
}
```

## Configuration

### ConsentConfig

```typescript
interface ConsentConfig {
  apiUrl: string;                    // API endpoint
  tenantId: string;                  // Your tenant ID
  purposes: ConsentPurpose[];        // Array of purposes
  theme?: 'light' | 'dark';          // UI theme (default: 'light')
  language?: string;                 // Language code (default: 'en')
  position?: 'bottom' | 'top' | 'center'; // Widget position (default: 'bottom')
  requireParentalConsent?: boolean;  // For minors (default: false)
  onConsentGranted?: (consent: ConsentResponse) => void;
  onConsentWithdrawn?: (consentId: string) => void;
}
```

### ConsentPurpose

```typescript
interface ConsentPurpose {
  id: string;                       // Unique purpose ID
  name: string;                     // Display name
  description: string;              // Detailed description
  required: boolean;                // Is this purpose mandatory?
  category: 'ESSENTIAL' | 'MARKETING' | 'ANALYTICS' | 'PERSONALIZATION';
}
```

### ConsentResponse

```typescript
interface ConsentResponse {
  consentId: string;               // Unique consent record ID
  purposes: string[];              // Granted purpose IDs
  grantedAt: string;               // ISO timestamp
  expiresAt?: string;              // Optional expiry
}
```

## API Methods

### init()

Initialize and show the widget if no existing consent found.

```typescript
widget.init();
```

### show()

Manually show the consent widget.

```typescript
widget.show();
```

### hide()

Hide the consent widget.

```typescript
widget.hide();
```

### withdrawConsent(consentId: string)

Withdraw a previously granted consent.

```typescript
await widget.withdrawConsent('consent-id-123');
```

## Purpose Categories

| Category | Description | Examples |
|----------|-------------|----------|
| ESSENTIAL | Required for service functionality | Login, security, core features |
| MARKETING | Promotional communications | Newsletters, offers, campaigns |
| ANALYTICS | Usage tracking and improvement | Google Analytics, heatmaps |
| PERSONALIZATION | Customized user experience | Recommendations, preferences |

## Theming

### Light Theme (Default)

```typescript
{
  theme: 'light'
}
```

### Dark Theme

```typescript
{
  theme: 'dark'
}
```

### Custom Styling

Override CSS classes:

```css
.ds-consent-overlay {
  /* Custom overlay */
}

.ds-consent-container {
  /* Custom container */
}
```

## Positioning

```typescript
{
  position: 'bottom'  // Bottom of viewport
  position: 'top'     // Top of viewport
  position: 'center'  // Center of viewport
}
```

## Multi-language Support

```typescript
{
  language: 'en',  // English
  language: 'hi',  // Hindi
  language: 'ta',  // Tamil
  language: 'te',  // Telugu
  language: 'bn'   // Bengali
}
```

## DPDP Act 2023 Compliance

The widget ensures:
- ✅ **Granular Consent** - Purpose-specific checkboxes
- ✅ **Free Choice** - Clear accept/reject options
- ✅ **Informed Consent** - Detailed descriptions
- ✅ **Easy Withdrawal** - One-click revoke via API
- ✅ **Consent Records** - Audit trail with timestamps
- ✅ **Child Protection** - Parental consent option

## Storage

Consent is stored in:
1. **LocalStorage** - `ds_consent_{tenantId}`
2. **API** - Backend consent service (port 8002)

## Events & Callbacks

### onConsentGranted

Called when user accepts consent.

```typescript
onConsentGranted: (consent) => {
  console.log('Consent ID:', consent.consentId);
  console.log('Purposes:', consent.purposes);
  
  // Initialize services based on granted purposes
  if (consent.purposes.includes('analytics')) {
    initAnalytics();
  }
  if (consent.purposes.includes('marketing')) {
    enableMarketing();
  }
}
```

### onConsentWithdrawn

Called when user withdraws consent.

```typescript
onConsentWithdrawn: (consentId) => {
  console.log('Withdrawn:', consentId);
  
  // Disable services
  disableAnalytics();
  stopMarketing();
}
```

## Browser Support

- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+
- ✅ Mobile browsers

## Performance

| Metric | Value |
|--------|-------|
| Bundle size (minified) | 8.5 KB |
| Bundle size (gzipped) | 3.2 KB |
| Load time | <50ms |
| First paint | <100ms |
| Dependencies | 0 |

## Example: E-commerce Site

```html
<!DOCTYPE html>
<html>
<head>
  <script src="https://cdn.datasheild.in/consent-widget.min.js"></script>
  <script>
    window.DataShieldConfig = {
      apiUrl: 'https://api.myshop.com/v1',
      tenantId: 'myshop-123',
      purposes: [
        {
          id: 'essential',
          name: 'Essential Shopping Features',
          description: 'Cart, checkout, order tracking',
          required: true,
          category: 'ESSENTIAL'
        },
        {
          id: 'personalization',
          name: 'Product Recommendations',
          description: 'Show products you might like based on browsing history',
          required: false,
          category: 'PERSONALIZATION'
        },
        {
          id: 'marketing',
          name: 'Promotional Offers',
          description: 'Get exclusive deals and new product alerts',
          required: false,
          category: 'MARKETING'
        }
      ],
      theme: 'light',
      onConsentGranted: (consent) => {
        if (consent.purposes.includes('personalization')) {
          loadRecommendationEngine();
        }
        if (consent.purposes.includes('marketing')) {
          subscribeToNewsletter();
        }
      }
    };
  </script>
</head>
<body>
  <h1>Welcome to MyShop</h1>
</body>
</html>
```

## Testing

View the demo:

```bash
# Open demo.html in browser
open demo.html
```

## Build from Source

```bash
# Install dependencies
npm install

# Build TypeScript
npm run build

# Output: dist/consent-widget.js
```

## License

MIT License

## Support

- 📧 Email: sdk@datasheild.in
- 📖 Docs: https://docs.datasheild.in/widget
- 🐛 Issues: https://github.com/datasheild/consent-widget/issues

---

**Version**: 1.0.0  
**Bundle Size**: 3.2KB gzipped  
**License**: MIT  
**Status**: Production Ready
