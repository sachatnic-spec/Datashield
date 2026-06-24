import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <!-- Header -->
      <header class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div class="flex justify-between items-center">
            <div>
              <h1 class="text-3xl font-bold text-gray-900">DataShield India</h1>
              <p class="text-sm text-gray-600 mt-1">My Privacy Portal</p>
            </div>
            <div class="flex gap-4">
              <button class="btn btn-secondary">हिंदी</button>
              <button class="btn btn-primary">Login</button>
            </div>
          </div>
        </div>
      </header>

      <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <!-- Hero Section -->
        <div class="text-center mb-16">
          <h2 class="text-4xl font-bold text-gray-900 mb-4">
            Take Control of Your Personal Data
          </h2>
          <p class="text-xl text-gray-600 max-w-3xl mx-auto">
            Manage your consents, exercise your rights, and track your data privacy requests
            under the DPDP Act 2023
          </p>
        </div>

        <!-- Features Grid -->
        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-8 mb-16">
          <!-- Consents -->
          <div class="card text-center hover:shadow-lg transition-shadow cursor-pointer">
            <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">My Consents</h3>
            <p class="text-sm text-gray-600 mb-4">View and manage your consent preferences</p>
            <a routerLink="/consents" class="text-blue-600 text-sm font-medium hover:underline">
              Manage Consents →
            </a>
          </div>

          <!-- Data Requests -->
          <div class="card text-center hover:shadow-lg transition-shadow cursor-pointer">
            <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Data Requests</h3>
            <p class="text-sm text-gray-600 mb-4">Submit access, correction, or deletion requests</p>
            <a routerLink="/requests/new" class="text-green-600 text-sm font-medium hover:underline">
              New Request →
            </a>
          </div>

          <!-- Grievances -->
          <div class="card text-center hover:shadow-lg transition-shadow cursor-pointer">
            <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Grievances</h3>
            <p class="text-sm text-gray-600 mb-4">File and track your privacy complaints</p>
            <a routerLink="/grievances" class="text-yellow-600 text-sm font-medium hover:underline">
              View Grievances →
            </a>
          </div>

          <!-- Profile -->
          <div class="card text-center hover:shadow-lg transition-shadow cursor-pointer">
            <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
              </svg>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-2">My Profile</h3>
            <p class="text-sm text-gray-600 mb-4">Update your personal information</p>
            <a routerLink="/profile" class="text-purple-600 text-sm font-medium hover:underline">
              View Profile →
            </a>
          </div>
        </div>

        <!-- Your Rights Section -->
        <div class="card bg-gradient-to-r from-blue-600 to-blue-700 text-white">
          <h3 class="text-2xl font-bold mb-6">Your Rights Under DPDP Act 2023</h3>
          <div class="grid md:grid-cols-2 gap-6">
            <div>
              <h4 class="font-semibold mb-2 flex items-center">
                <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                Right to Access
              </h4>
              <p class="text-sm opacity-90">Request a copy of your personal data</p>
            </div>
            <div>
              <h4 class="font-semibold mb-2 flex items-center">
                <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                Right to Correction
              </h4>
              <p class="text-sm opacity-90">Correct inaccurate personal data</p>
            </div>
            <div>
              <h4 class="font-semibold mb-2 flex items-center">
                <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                Right to Erasure
              </h4>
              <p class="text-sm opacity-90">Request deletion of your personal data</p>
            </div>
            <div>
              <h4 class="font-semibold mb-2 flex items-center">
                <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                </svg>
                Right to Grievance Redressal
              </h4>
              <p class="text-sm opacity-90">File a complaint within 30-day SLA</p>
            </div>
          </div>
        </div>
      </main>

      <!-- Footer -->
      <footer class="bg-gray-900 text-white mt-16 py-8">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <p class="text-sm opacity-75">
            © 2026 DataShield India. Compliant with DPDP Act 2023.
          </p>
          <p class="text-xs opacity-50 mt-2">
            For support: support&#64;datasheild.in | Toll-free: 1800-PRIVACY
          </p>
        </div>
      </footer>
    </div>
  `
})
export class HomeComponent implements OnInit {
  ngOnInit(): void {
    console.log('Data Principal Portal - Home loaded');
  }
}
