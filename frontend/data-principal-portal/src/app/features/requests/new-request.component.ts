import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';

@Component({
  selector: 'app-new-request',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50">
      <header class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <a routerLink="/requests" class="text-blue-600 text-sm mb-2 inline-block hover:underline">← Back to Requests</a>
          <h1 class="text-2xl font-bold text-gray-900">Submit New Request</h1>
          <p class="text-sm text-gray-600 mt-1">Exercise your rights under DPDP Act 2023</p>
        </div>
      </header>

      <main class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="card">
          <form (ngSubmit)="submitRequest()">
            <!-- Request Type -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 mb-2">Request Type *</label>
              <select [(ngModel)]="requestType" name="type" required class="w-full border border-gray-300 rounded-lg px-4 py-3">
                <option value="">Select request type</option>
                <option value="ACCESS">Data Access - Get a copy of my data</option>
                <option value="CORRECTION">Data Correction - Update incorrect information</option>
                <option value="ERASURE">Data Deletion - Delete my personal data</option>
                <option value="PORTABILITY">Data Portability - Transfer my data</option>
              </select>
            </div>

            <!-- Description -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 mb-2">Description *</label>
              <textarea
                [(ngModel)]="description"
                name="description"
                required
                rows="4"
                class="w-full border border-gray-300 rounded-lg px-4 py-3"
                placeholder="Provide details about your request..."
              ></textarea>
            </div>

            <!-- Contact Information -->
            <div class="grid md:grid-cols-2 gap-4 mb-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Email *</label>
                <input
                  type="email"
                  [(ngModel)]="email"
                  name="email"
                  required
                  class="w-full border border-gray-300 rounded-lg px-4 py-3"
                  placeholder="your@email.com"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Phone</label>
                <input
                  type="tel"
                  [(ngModel)]="phone"
                  name="phone"
                  class="w-full border border-gray-300 rounded-lg px-4 py-3"
                  placeholder="+91 XXXXX XXXXX"
                />
              </div>
            </div>

            <!-- Info Box -->
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <div class="flex gap-3">
                <svg class="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path>
                </svg>
                <div class="text-sm text-blue-800">
                  <p class="font-semibold mb-1">Processing Timeline</p>
                  <p>Your request will be processed within 30 days as per DPDP Act 2023. You will receive updates via email and SMS.</p>
                </div>
              </div>
            </div>

            <!-- Verification -->
            <div class="mb-6">
              <label class="flex items-start gap-3">
                <input
                  type="checkbox"
                  [(ngModel)]="verified"
                  name="verified"
                  required
                  class="mt-1"
                />
                <span class="text-sm text-gray-700">
                  I confirm that I am the data principal and the information provided is accurate. I understand that providing false information may result in rejection of my request.
                </span>
              </label>
            </div>

            <!-- Submit Button -->
            <div class="flex gap-4">
              <button
                type="submit"
                [disabled]="!canSubmit()"
                class="btn btn-primary flex-1"
                [class.opacity-50]="!canSubmit()"
              >
                Submit Request
              </button>
              <a routerLink="/requests" class="btn btn-secondary">Cancel</a>
            </div>
          </form>
        </div>
      </main>
    </div>
  `
})
export class NewRequestComponent {
  requestType = '';
  description = '';
  email = '';
  phone = '';
  verified = false;

  constructor(private router: Router) {}

  canSubmit(): boolean {
    return !!(this.requestType && this.description && this.email && this.verified);
  }

  submitRequest(): void {
    if (!this.canSubmit()) return;

    alert('Request submitted successfully! You will receive a confirmation email shortly.');
    this.router.navigate(['/requests']);
  }
}
