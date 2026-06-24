# DataShield India - Data Principal Portal

**End-User Privacy Portal for DPDP Act 2023 Compliance**

## Overview

Angular 21 standalone application for data principals (end-users) to manage their privacy rights, consents, and data requests.

## Features

### 🏠 Home Dashboard
- Overview of privacy rights under DPDP Act 2023
- Quick access to all portal features
- Multi-language support (Hindi, English, Tamil, Telugu, Bengali)

### ✅ My Consents
- View all granted consents
- Withdraw consents with one click
- Filter by status (ACTIVE/WITHDRAWN)
- Purpose and organization details

### 📋 Data Requests (DSAR)
- Submit new requests:
  - **Access**: Get a copy of personal data
  - **Correction**: Update incorrect information
  - **Erasure**: Request data deletion
  - **Portability**: Transfer data to another service
- Track request status (PENDING/IN_PROGRESS/COMPLETED/REJECTED)
- 30-day processing timeline

### ⚖️ Grievances
- File privacy complaints
- 30-day SLA tracking with countdown
- Visual progress indicators
- Status updates (FILED/INVESTIGATING/RESOLVED/ESCALATED)

### 👤 My Profile
- Update personal information
- Language preferences
- Account summary
- Delete account (danger zone)

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Angular 21 (Standalone Components) |
| Styling | TailwindCSS 3.4 |
| State | Angular Signals |
| Forms | Template-driven + Reactive Forms |
| HTTP | HttpClient |
| Routing | Angular Router (lazy loading) |

## Installation & Setup

```bash
cd data-principal-portal

# Install dependencies
npm install

# Start development server (port 4201)
npm start

# Build for production
npm run build
```

## Development Server

Navigate to `http://localhost:4201/`

## API Integration

Connects to backend services:
- **Auth Service** (8001): OTP-based authentication
- **Consent Service** (8002): Consent management
- **Rights Service** (8003): DSAR submissions
- **Grievance Service** (8012): Complaint filing
- **Notification Service** (8005): Email/SMS updates

## Routes

| Path | Component | Description |
|------|-----------|-------------|
| `/home` | HomeComponent | Landing page with feature overview |
| `/consents` | MyConsentsComponent | Consent management |
| `/requests` | MyRequestsComponent | DSAR tracking |
| `/requests/new` | NewRequestComponent | Submit new DSAR |
| `/grievances` | MyGrievancesComponent | Grievance tracking |
| `/profile` | ProfileComponent | Profile management |

## Components

### HomeComponent (350 LOC)
- Hero section with privacy rights overview
- Feature cards for quick navigation
- Footer with support contact

### MyConsentsComponent (200 LOC)
- Consent list with status badges
- Withdraw action with confirmation
- Summary cards (total, active, withdrawn)

### MyRequestsComponent (180 LOC)
- Request history table
- Status-based filtering
- Request type labels and descriptions

### NewRequestComponent (220 LOC)
- Multi-step form for DSAR submission
- Request type selection
- Email/phone verification
- 30-day SLA information

### MyGrievancesComponent (250 LOC)
- Grievance list with SLA countdown
- Visual progress bars
- Color-coded status indicators
- Days remaining calculation

### ProfileComponent (180 LOC)
- Personal information form
- Language selector (5 Indian languages)
- Account summary statistics
- Delete account option

## DPDP Act 2023 Compliance

| Right | Feature | Implementation |
|-------|---------|----------------|
| § 13(a) - Right to Access | Data Requests | Access request submission |
| § 13(b) - Right to Correction | Data Requests | Correction request form |
| § 13(c) - Right to Erasure | Data Requests | Deletion request + account deletion |
| § 13(d) - Right to Portability | Data Requests | Portability request |
| § 14 - Withdrawal of Consent | My Consents | One-click withdrawal |
| § 18 - Grievance Redressal | Grievances | 30-day SLA tracking |

## Styling

TailwindCSS custom classes:

```scss
.btn              // Base button
.btn-primary      // Primary action (blue)
.btn-secondary    // Secondary action (gray)
.card             // White container with shadow
.badge            // Status badge
.badge-pending    // Yellow (pending/in-progress)
.badge-approved   // Green (completed/active)
.badge-rejected   // Red (rejected/withdrawn)
```

## Internationalization (i18n)

Supported languages:
- 🇬🇧 English (en)
- 🇮🇳 हिंदी / Hindi (hi)
- 🇮🇳 தமிழ் / Tamil (ta)
- 🇮🇳 తెలుగు / Telugu (te)
- 🇮🇳 বাংলা / Bengali (bn)

## Accessibility

- Semantic HTML5
- ARIA labels for screen readers
- Keyboard navigation
- Color contrast WCAG 2.1 AA compliant
- Focus indicators
- Error messages

## Mobile Responsiveness

- Mobile-first design
- Responsive breakpoints:
  - Mobile: < 640px
  - Tablet: 640px - 1024px
  - Desktop: > 1024px
- Touch-friendly buttons (min 44x44px)

## Security

- OTP-based authentication (no passwords)
- JWT tokens (future)
- Input validation
- XSS protection (Angular sanitization)
- HTTPS-only in production

## Performance

| Metric | Target | Status |
|--------|--------|--------|
| Initial load | < 2s | 🔨 TBD |
| Route transition | < 200ms | ✅ |
| Form submission | < 500ms | ✅ |
| Bundle size | < 400KB | 🔨 TBD |

## Testing

```bash
# Unit tests
npm test

# E2E tests (future)
npm run e2e
```

## Production Build

```bash
npm run build
# Output: dist/data-principal-portal/
```

## Deployment

- **Static Hosting**: AWS S3 + CloudFront, Netlify, Vercel
- **Server**: Nginx, Apache
- **Container**: Docker (future)

## Environment Variables

`src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8000/api/v1',
  services: {
    auth: 'http://localhost:8001/api/v1',
    consent: 'http://localhost:8002/api/v1',
    rights: 'http://localhost:8003/api/v1',
    grievance: 'http://localhost:8012/api/v1'
  }
};
```

## Support

- 📧 Email: support@datasheild.in
- 📞 Toll-free: 1800-PRIVACY
- 🌐 Website: https://datasheild.in

## License

Proprietary - DataShield India Private Limited

---

**Version**: 1.0.0  
**Last Updated**: 2026-06-24  
**Port**: 4201  
**Status**: Phase 8 - Production Ready
