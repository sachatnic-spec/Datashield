import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface DataRequest {
  id: string;
  type: 'ACCESS' | 'CORRECTION' | 'ERASURE' | 'PORTABILITY';
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'REJECTED';
  submittedDate: string;
  completedDate?: string;
  description: string;
}

@Component({
  selector: 'app-my-requests',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50">
      <header class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div class="flex justify-between items-center">
            <div>
              <a routerLink="/home" class="text-blue-600 text-sm mb-2 inline-block hover:underline">← Back to Home</a>
              <h1 class="text-2xl font-bold text-gray-900">My Data Requests</h1>
              <p class="text-sm text-gray-600 mt-1">Track your DSAR submissions</p>
            </div>
            <a routerLink="/requests/new" class="btn btn-primary">+ New Request</a>
          </div>
        </div>
      </header>

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="card">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Request History</h2>
          
          @if (requests().length === 0) {
            <div class="text-center py-12">
              <p class="text-gray-600 mb-4">No requests submitted yet</p>
              <a routerLink="/requests/new" class="btn btn-primary">Submit Your First Request</a>
            </div>
          }

          <div class="space-y-4">
            @for (request of requests(); track request.id) {
              <div class="border border-gray-200 rounded-lg p-4">
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <div class="flex items-center gap-3 mb-2">
                      <h3 class="font-semibold text-gray-900">{{ getRequestTypeLabel(request.type) }}</h3>
                      <span [class]="getStatusBadgeClass(request.status)">
                        {{ request.status }}
                      </span>
                    </div>
                    <p class="text-sm text-gray-600 mb-2">{{ request.description }}</p>
                    <div class="flex gap-4 text-xs text-gray-500">
                      <span>Submitted: {{ request.submittedDate }}</span>
                      @if (request.completedDate) {
                        <span>Completed: {{ request.completedDate }}</span>
                      }
                    </div>
                  </div>
                  <button class="text-sm text-blue-600 hover:underline">View Details</button>
                </div>
              </div>
            }
          </div>
        </div>
      </main>
    </div>
  `
})
export class MyRequestsComponent {
  requests = signal<DataRequest[]>([
    {
      id: '1',
      type: 'ACCESS',
      status: 'COMPLETED',
      submittedDate: '2024-01-10',
      completedDate: '2024-01-25',
      description: 'Request for copy of all personal data'
    },
    {
      id: '2',
      type: 'ERASURE',
      status: 'IN_PROGRESS',
      submittedDate: '2024-02-15',
      description: 'Delete my account and all associated data'
    }
  ]);

  getRequestTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      ACCESS: 'Data Access Request',
      CORRECTION: 'Data Correction Request',
      ERASURE: 'Data Deletion Request',
      PORTABILITY: 'Data Portability Request'
    };
    return labels[type] || type;
  }

  getStatusBadgeClass(status: string): string {
    const classes: Record<string, string> = {
      PENDING: 'badge badge-pending',
      IN_PROGRESS: 'badge badge-pending',
      COMPLETED: 'badge badge-approved',
      REJECTED: 'badge badge-rejected'
    };
    return classes[status] || 'badge';
  }
}
