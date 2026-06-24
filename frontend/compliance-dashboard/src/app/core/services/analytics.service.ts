import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

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

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private baseUrl = environment.services.analytics;

  constructor(private http: HttpClient) {}

  getMetrics(tenantId: string): Observable<ComplianceMetric[]> {
    return this.http.get<ComplianceMetric[]>(
      `${this.baseUrl}/metrics/${tenantId}`
    );
  }

  getComplianceScore(tenantId: string): Observable<ComplianceScore> {
    return this.http.get<ComplianceScore>(
      `${this.baseUrl}/score/${tenantId}`
    );
  }

  getAlerts(): Observable<ComplianceMetric[]> {
    return this.http.get<ComplianceMetric[]>(`${this.baseUrl}/alerts`);
  }

  getDashboardStats(tenantId: string): Observable<DashboardStats> {
    // Aggregated call to multiple services
    return this.http.get<DashboardStats>(
      `${this.baseUrl}/dashboard/${tenantId}`
    );
  }
}
