import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AnalyticsService, DashboardStats, ComplianceMetric } from '../../core/services/analytics.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50">
      <!-- Header -->
      <header class="bg-white shadow-sm border-b border-gray-200">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">Compliance Dashboard</h1>
            <p class="text-sm text-gray-600">DPDP Act 2023 Monitoring</p>
          </div>
          <div class="flex items-center gap-4">
            <span class="text-sm text-gray-600">{{ currentUser()?.firstName }} {{ currentUser()?.lastName }}</span>
            <button (click)="logout()" class="btn-secondary text-sm">Logout</button>
          </div>
        </div>
      </header>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- Compliance Score Card -->
        <div class="card mb-8 bg-gradient-to-r from-primary-500 to-primary-700 text-white">
          <div class="flex justify-between items-center">
            <div>
              <h2 class="text-lg font-semibold opacity-90">Overall Compliance Score</h2>
              <p class="text-5xl font-bold mt-2">{{ stats()?.complianceScore || 0 }}%</p>
              <p class="text-sm opacity-75 mt-2">Last updated: {{ currentTime }}</p>
            </div>
            <div class="text-right">
              <div class="text-sm opacity-75 mb-2">Status</div>
              <div [ngClass]="{
                'bg-green-500': (stats()?.complianceScore || 0) >= 90,
                'bg-yellow-500': (stats()?.complianceScore || 0) >= 70 && (stats()?.complianceScore || 0) < 90,
                'bg-red-500': (stats()?.complianceScore || 0) < 70
              }" class="px-4 py-2 rounded-full text-white font-semibold">
                {{ getComplianceStatus(stats()?.complianceScore || 0) }}
              </div>
            </div>
          </div>
        </div>

        <!-- Key Metrics Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <!-- Total Consents -->
          <div class="card hover:shadow-md transition-shadow">
            <div class="flex justify-between items-start">
              <div>
                <p class="text-sm text-gray-600">Total Consents</p>
                <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats()?.totalConsents || 0 }}</p>
                <p class="text-xs text-green-600 mt-1">
                  {{ stats()?.activeConsents || 0 }} active
                </p>
              </div>
              <div class="bg-green-100 p-3 rounded-lg">
                <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
            </div>
          </div>

          <!-- Grievances -->
          <div class="card hover:shadow-md transition-shadow">
            <div class="flex justify-between items-start">
              <div>
                <p class="text-sm text-gray-600">Pending Grievances</p>
                <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats()?.pendingGrievances || 0 }}</p>
                <p class="text-xs text-red-600 mt-1">
                  {{ stats()?.slaBreaches || 0 }} SLA breaches
                </p>
              </div>
              <div class="bg-yellow-100 p-3 rounded-lg">
                <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
            </div>
          </div>

          <!-- Breaches -->
          <div class="card hover:shadow-md transition-shadow">
            <div class="flex justify-between items-start">
              <div>
                <p class="text-sm text-gray-600">Open Breaches</p>
                <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats()?.openBreaches || 0 }}</p>
                <p class="text-xs text-gray-600 mt-1">Requires action</p>
              </div>
              <div class="bg-red-100 p-3 rounded-lg">
                <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                </svg>
              </div>
            </div>
          </div>

          <!-- Critical Vendors -->
          <div class="card hover:shadow-md transition-shadow">
            <div class="flex justify-between items-start">
              <div>
                <p class="text-sm text-gray-600">Critical Risk Vendors</p>
                <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats()?.criticalVendors || 0 }}</p>
                <p class="text-xs text-gray-600 mt-1">Needs review</p>
              </div>
              <div class="bg-purple-100 p-3 rounded-lg">
                <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                </svg>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Alerts -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div class="card">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">Recent Alerts</h3>
            <div class="space-y-3">
              @if (alerts().length === 0) {
                <p class="text-sm text-gray-500 text-center py-8">No alerts at this time</p>
              }
              @for (alert of alerts(); track alert.id) {
                <div class="flex items-start gap-3 p-3 rounded-lg" [ngClass]="{
                  'bg-red-50': alert.status === 'CRITICAL',
                  'bg-yellow-50': alert.status === 'WARNING',
                  'bg-green-50': alert.status === 'OK'
                }">
                  <div class="flex-1">
                    <p class="text-sm font-medium text-gray-900">{{ alert.metricType }}</p>
                    <p class="text-xs text-gray-600 mt-1">{{ alert.complianceSection }}</p>
                  </div>
                  <span [ngClass]="{
                    'badge-danger': alert.status === 'CRITICAL',
                    'badge-warning': alert.status === 'WARNING',
                    'badge-success': alert.status === 'OK'
                  }">
                    {{ alert.status }}
                  </span>
                </div>
              }
            </div>
          </div>

          <div class="card">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
            <div class="space-y-2">
              <a routerLink="/grievances" class="block p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
                <p class="text-sm font-medium text-gray-900">View Grievances</p>
                <p class="text-xs text-gray-600">Track 30-day SLA compliance</p>
              </a>
              <a routerLink="/breaches" class="block p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
                <p class="text-sm font-medium text-gray-900">Breach Management</p>
                <p class="text-xs text-gray-600">72-hour DPBI notification</p>
              </a>
              <a routerLink="/vendors" class="block p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
                <p class="text-sm font-medium text-gray-900">Vendor Risk Assessment</p>
                <p class="text-xs text-gray-600">Review processor compliance</p>
              </a>
              <a routerLink="/reports" class="block p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
                <p class="text-sm font-medium text-gray-900">Generate Reports</p>
                <p class="text-xs text-gray-600">Board-ready compliance reports</p>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  stats = signal<DashboardStats | null>(null);
  alerts = signal<ComplianceMetric[]>([]);
  currentTime = new Date().toLocaleString();

  constructor(
    private analyticsService: AnalyticsService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  currentUser = this.authService.currentUser;

  loadDashboardData(): void {
    const tenantId = this.authService.getTenantId();
    
    this.analyticsService.getDashboardStats(tenantId).subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error('Failed to load stats', err)
    });

    this.analyticsService.getAlerts().subscribe({
      next: (data) => this.alerts.set(data.slice(0, 5)),
      error: (err) => console.error('Failed to load alerts', err)
    });
  }

  getComplianceStatus(score: number): string {
    if (score >= 90) return 'EXCELLENT';
    if (score >= 70) return 'GOOD';
    if (score >= 50) return 'NEEDS IMPROVEMENT';
    return 'CRITICAL';
  }

  logout(): void {
    this.authService.logout();
  }
}
