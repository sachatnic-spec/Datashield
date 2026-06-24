import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Consent {
  id: string;
  purpose: string;
  organization: string;
  grantedDate: string;
  status: 'ACTIVE' | 'WITHDRAWN';
  category: string;
}

@Component({
  selector: 'app-my-consents',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50">
      <header class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div class="flex justify-between items-center">
            <div>
              <a routerLink="/home" class="text-blue-600 text-sm mb-2 inline-block hover:underline">← Back to Home</a>
              <h1 class="text-2xl font-bold text-gray-900">My Consents</h1>
              <p class="text-sm text-gray-600 mt-1">Manage your data processing consents</p>
            </div>
          </div>
        </div>
      </header>

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- Summary Cards -->
        <div class="grid md:grid-cols-3 gap-6 mb-8">
          <div class="card">
            <p class="text-sm text-gray-600">Total Consents</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ consents().length }}</p>
          </div>
          <div class="card">
            <p class="text-sm text-gray-600">Active Consents</p>
            <p class="text-3xl font-bold text-green-600 mt-2">
              {{ consents().filter(c => c.status === 'ACTIVE').length }}
            </p>
          </div>
          <div class="card">
            <p class="text-sm text-gray-600">Withdrawn</p>
            <p class="text-3xl font-bold text-red-600 mt-2">
              {{ consents().filter(c => c.status === 'WITHDRAWN').length }}
            </p>
          </div>
        </div>

        <!-- Consents List -->
        <div class="card">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Your Consent History</h2>
          
          @if (consents().length === 0) {
            <div class="text-center py-12">
              <svg class="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
              <p class="text-gray-600">No consents found</p>
            </div>
          }

          <div class="space-y-4">
            @for (consent of consents(); track consent.id) {
              <div class="border border-gray-200 rounded-lg p-4 hover:border-blue-300 transition-colors">
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <div class="flex items-center gap-3 mb-2">
                      <h3 class="font-semibold text-gray-900">{{ consent.purpose }}</h3>
                      <span [class]="consent.status === 'ACTIVE' ? 'badge badge-approved' : 'badge badge-rejected'">
                        {{ consent.status }}
                      </span>
                    </div>
                    <p class="text-sm text-gray-600">{{ consent.organization }}</p>
                    <p class="text-xs text-gray-500 mt-1">Category: {{ consent.category }}</p>
                    <p class="text-xs text-gray-500">Granted: {{ consent.grantedDate }}</p>
                  </div>
                  <div class="flex gap-2">
                    @if (consent.status === 'ACTIVE') {
                      <button 
                        (click)="withdrawConsent(consent.id)"
                        class="text-sm text-red-600 hover:underline"
                      >
                        Withdraw
                      </button>
                    }
                    <button class="text-sm text-blue-600 hover:underline">
                      View Details
                    </button>
                  </div>
                </div>
              </div>
            }
          </div>
        </div>
      </main>
    </div>
  `
})
export class MyConsentsComponent implements OnInit {
  consents = signal<Consent[]>([
    {
      id: '1',
      purpose: 'Marketing Communications',
      organization: 'Acme Corp',
      grantedDate: '2024-01-15',
      status: 'ACTIVE',
      category: 'Marketing'
    },
    {
      id: '2',
      purpose: 'Personal Data Processing',
      organization: 'XYZ Services',
      grantedDate: '2024-02-20',
      status: 'ACTIVE',
      category: 'Essential'
    },
    {
      id: '3',
      purpose: 'Analytics & Tracking',
      organization: 'Data Analytics Inc',
      grantedDate: '2023-12-10',
      status: 'WITHDRAWN',
      category: 'Analytics'
    }
  ]);

  ngOnInit(): void {
    // Load consents from API
  }

  withdrawConsent(consentId: string): void {
    if (confirm('Are you sure you want to withdraw this consent?')) {
      const updated = this.consents().map(c => 
        c.id === consentId ? { ...c, status: 'WITHDRAWN' as const } : c
      );
      this.consents.set(updated);
    }
  }
}
