import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface ComplianceMetric {
  id: string;
  tenantId: string;
  metricType: string;
  metricValue: number;
  unit: string;
  status: 'OK' | 'WARNING' | 'CRITICAL';
  complianceSection: string;
  measuredAt: string;
}

export interface ComplianceScore {
  tenantId: string;
  overallScore: number;
  grievanceSLAScore: number;
  retentionScore: number;
  vendorRiskScore: number;
  dsarScore: number;
  lastUpdated: string;
}

export interface DashboardStats {
  totalConsents: number;
  activeConsents: number;
  pendingGrievances: number;
  slaBreaches: number;
  openBreaches: number;
  criticalVendors: number;
  complianceScore: number;
}

const EMPTY_STATS: DashboardStats = {
  totalConsents: 0, activeConsents: 0, pendingGrievances: 0,
  slaBreaches: 0, openBreaches: 0, criticalVendors: 0, complianceScore: 0
};

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private baseUrl = environment.services.analytics;

  constructor(private http: HttpClient, private authService: AuthService) {}

  getMetrics(tenantId: string): Observable<ComplianceMetric[]> {
    return this.http.get<ComplianceMetric[]>(`${this.baseUrl}/analytics/metrics/${tenantId}`)
      .pipe(catchError(() => of([])));
  }

  getComplianceScore(tenantId: string): Observable<ComplianceScore> {
    return this.http.get<ComplianceScore>(`${this.baseUrl}/analytics/score/${tenantId}`)
      .pipe(catchError(() => of({ tenantId, overallScore: 0, grievanceSLAScore: 0, retentionScore: 0, vendorRiskScore: 0, dsarScore: 0, lastUpdated: new Date().toISOString() })));
  }

  getAlerts(): Observable<ComplianceMetric[]> {
    return this.http.get<ComplianceMetric[]>(`${this.baseUrl}/analytics/alerts`)
      .pipe(catchError(() => of([])));
  }

  getDashboardStats(tenantId: string): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.baseUrl}/analytics/dashboard/${tenantId}`)
      .pipe(catchError(() => of(EMPTY_STATS)));
  }
}
