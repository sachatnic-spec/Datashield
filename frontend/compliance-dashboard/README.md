# DataShield India - Compliance Dashboard

**DPO Portal for DPDP Act 2023 Compliance Monitoring**

## Overview

Angular 18 standalone application for Data Protection Officers (DPOs) to monitor and manage DPDP compliance across all services.

## Features

- **Real-time Compliance Score** - Overall compliance percentage with status indicators
- **Metric Dashboard** - Consents, grievances, breaches, vendor risk tracking
- **Alert System** - CRITICAL/WARNING/OK status monitoring
- **Multi-Module Navigation** - Consents, Grievances, Breaches, Vendors, Reports, Settings
- **JWT Authentication** - Secure login with token-based auth
- **Responsive UI** - TailwindCSS for mobile-first design

## Tech Stack

- **Framework**: Angular 18 (Standalone Components)
- **Styling**: TailwindCSS 3.x
- **Charts**: Chart.js + ng2-charts (ready for integration)
- **State**: Angular Signals
- **HTTP**: Angular HttpClient with interceptors
- **Routing**: Angular Router with lazy loading

## Project Structure

```
compliance-dashboard/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   ├── guards/          # Route guards
│   │   │   ├── interceptors/    # HTTP interceptors
│   │   │   └── services/        # API services
│   │   ├── features/
│   │   │   ├── auth/            # Login
│   │   │   ├── dashboard/       # Main dashboard
│   │   │   ├── consents/        # Consent management
│   │   │   ├── grievances/      # Grievance tracking
│   │   │   ├── breaches/        # Breach management
│   │   │   ├── vendors/         # Vendor risk
│   │   │   ├── reports/         # Compliance reports
│   │   │   └── settings/        # Configuration
│   │   ├── shared/              # Shared components
│   │   └── app.routes.ts        # Route configuration
│   ├── environments/
│   │   └── environment.ts       # API endpoints
│   └── styles.scss              # Global styles + Tailwind
├── angular.json
├── package.json
├── tailwind.config.js
└── tsconfig.json
```

## Installation & Setup

```bash
# Navigate to project directory
cd compliance-dashboard

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test
```

## Development Server

Run `npm start` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## API Integration

The app connects to backend microservices (ports 8001-8027). Configure endpoints in `src/environments/environment.ts`:

```typescript
services: {
  auth: 'http://localhost:8001/api/v1',
  analytics: 'http://localhost:8013/api/v1',
  // ... other services
}
```

## Authentication

- **Login**: POST `/api/v1/auth/login` (Auth Service - 8001)
- **Demo Credentials**: 
  - Email: `dpo@example.com`
  - Password: `demo123`

JWT token is stored in localStorage and automatically attached to API requests via `authInterceptor`.

## Routes

| Path | Component | Description |
|------|-----------|-------------|
| `/login` | LoginComponent | User authentication |
| `/dashboard` | DashboardComponent | Main compliance dashboard |
| `/consents` | ConsentsComponent | Consent lifecycle |
| `/grievances` | GrievancesComponent | 30-day SLA tracking |
| `/breaches` | BreachesComponent | 72-hour DPBI notification |
| `/vendors` | VendorsComponent | Processor risk assessment |
| `/reports` | ReportsComponent | Board-ready exports |
| `/settings` | SettingsComponent | Platform configuration |

## Styling

TailwindCSS utility classes + custom components:

```scss
.btn-primary      // Primary action buttons
.btn-secondary    // Secondary buttons
.card             // White card with shadow
.input-field      // Form inputs
.badge-success    // Green badge
.badge-warning    // Yellow badge
.badge-danger     // Red badge
.badge-info       // Blue badge
```

## Next Steps

1. **Implement Feature Components**: Add full CRUD operations for each module
2. **Add Charts**: Integrate ng2-charts for trend visualization
3. **WebSocket Integration**: Real-time alert notifications
4. **i18n Support**: Multi-language (Hindi, English, Tamil, etc.)
5. **Advanced Filters**: Date range, status filters, search
6. **Export Functionality**: PDF/Excel report generation
7. **Dark Mode**: Theme switcher
8. **Accessibility**: WCAG 2.1 AA compliance

## Production Build

```bash
npm run build
```

Output: `dist/compliance-dashboard/`

## Testing

```bash
# Unit tests
npm test

# E2E tests (future)
npm run e2e
```

## License

Proprietary - DataShield India Private Limited

---

**Version**: 1.0.0  
**Last Updated**: 2026-06-24  
**Port**: 4200  
**Status**: Phase 8 - MVP Ready
