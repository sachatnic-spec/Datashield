import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Grievance {
  id: string;
  subject: string;
  category: string;
  status: 'FILED' | 'INVESTIGATING' | 'RESOLVED' | 'ESCALATED';
  filedDate: string;
  slaDeadline: string;
  daysRemaining: number;
}

@Component({
  selector: 'app-my-grievances',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50">
      <header class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div class="flex justify-between items-center">
            <div>
              <a routerLink="/home" class="text-blue-600 text-sm mb-2 inline-block hover:underline">← Back to Home</a>
              <h1 class="text-2xl font-bold text-gray-900">My Grievances</h1>
              <p class="text-sm text-gray-600 mt-1">File and track privacy complaints (30-day SLA)</p>
            </div>
            <button class="btn btn-primary">+ File Grievance</button>
          </div>
        </div>
      </header>

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="card">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Active Grievances</h2>
          
          @if (grievances().length === 0) {
            <div class="text-center py-12">
              <p class="text-gray-600 mb-4">No grievances filed</p>
              <button class="btn btn-primary">File Your First Grievance</button>
            </div>
          }

          <div class="space-y-4">
            @for (grievance of grievances(); track grievance.id) {
              <div class="border border-gray-200 rounded-lg p-4">
                <div class="flex justify-between items-start mb-3">
                  <div class="flex-1">
                    <div class="flex items-center gap-3 mb-2">
                      <h3 class="font-semibold text-gray-900">{{ grievance.subject }}</h3>
                      <span [class]="getStatusBadgeClass(grievance.status)">
                        {{ grievance.status }}
                      </span>
                    </div>
                    <p class="text-sm text-gray-600">Category: {{ grievance.category }}</p>
                  </div>
                </div>

                <!-- SLA Timeline -->
                <div class="bg-gray-50 rounded-lg p-3">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-xs font-medium text-gray-700">SLA Timeline</span>
                    <span [class]="getSLAClass(grievance.daysRemaining)">
                      {{ grievance.daysRemaining }} days remaining
                    </span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2">
                    <div
                      [class]="getSLABarClass(grievance.daysRemaining)"
                      [style.width]="getSLAProgress(grievance.daysRemaining) + '%'"
                      class="h-2 rounded-full transition-all"
                    ></div>
                  </div>
                  <div class="flex justify-between text-xs text-gray-500 mt-1">
                    <span>Filed: {{ grievance.filedDate }}</span>
                    <span>Deadline: {{ grievance.slaDeadline }}</span>
                  </div>
                </div>

                <div class="flex gap-2 mt-3">
                  <button class="text-sm text-blue-600 hover:underline">View Details</button>
                  <button class="text-sm text-blue-600 hover:underline">Add Comment</button>
                </div>
              </div>
            }
          </div>
        </div>
      </main>
    </div>
  `
})
export class MyGrievancesComponent {
  grievances = signal<Grievance[]>([
    {
      id: '1',
      subject: 'DSAR Request Rejected Without Valid Reason',
      category: 'DATA_ACCESS_DENIAL',
      status: 'INVESTIGATING',
      filedDate: '2024-02-01',
      slaDeadline: '2024-03-03',
      daysRemaining: 15
    },
    {
      id: '2',
      subject: 'Unauthorized Data Sharing with Third Party',
      category: 'PRIVACY_VIOLATION',
      status: 'FILED',
      filedDate: '2024-02-20',
      slaDeadline: '2024-03-22',
      daysRemaining: 28
    }
  ]);

  getStatusBadgeClass(status: string): string {
    const classes: Record<string, string> = {
      FILED: 'badge badge-pending',
      INVESTIGATING: 'badge badge-pending',
      RESOLVED: 'badge badge-approved',
      ESCALATED: 'badge badge-rejected'
    };
    return classes[status] || 'badge';
  }

  getSLAClass(days: number): string {
    if (days <= 5) return 'text-xs font-semibold text-red-600';
    if (days <= 10) return 'text-xs font-semibold text-yellow-600';
    return 'text-xs font-semibold text-green-600';
  }

  getSLABarClass(days: number): string {
    if (days <= 5) return 'bg-red-500';
    if (days <= 10) return 'bg-yellow-500';
    return 'bg-green-500';
  }

  getSLAProgress(daysRemaining: number): number {
    return ((30 - daysRemaining) / 30) * 100;
  }
}
