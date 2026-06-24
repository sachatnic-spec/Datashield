import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50">
      <header class="bg-white shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <a routerLink="/home" class="text-blue-600 text-sm mb-2 inline-block hover:underline">← Back to Home</a>
          <h1 class="text-2xl font-bold text-gray-900">My Profile</h1>
          <p class="text-sm text-gray-600 mt-1">Manage your personal information</p>
        </div>
      </header>

      <main class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- Profile Info -->
        <div class="card mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Personal Information</h2>
          <form>
            <div class="grid md:grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Full Name</label>
                <input
                  type="text"
                  [(ngModel)]="profile().name"
                  name="name"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
                <input
                  type="email"
                  [(ngModel)]="profile().email"
                  name="email"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2"
                />
              </div>
            </div>

            <div class="grid md:grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Phone</label>
                <input
                  type="tel"
                  [(ngModel)]="profile().phone"
                  name="phone"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Language</label>
                <select [(ngModel)]="profile().language" name="language" class="w-full border border-gray-300 rounded-lg px-4 py-2">
                  <option value="en">English</option>
                  <option value="hi">हिंदी (Hindi)</option>
                  <option value="ta">தமிழ் (Tamil)</option>
                  <option value="te">తెలుగు (Telugu)</option>
                  <option value="bn">বাংলা (Bengali)</option>
                </select>
              </div>
            </div>

            <button type="button" (click)="saveProfile()" class="btn btn-primary">
              Save Changes
            </button>
          </form>
        </div>

        <!-- Account Summary -->
        <div class="card mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Account Summary</h2>
          <div class="grid md:grid-cols-3 gap-4">
            <div class="text-center p-4 bg-blue-50 rounded-lg">
              <p class="text-2xl font-bold text-blue-600">5</p>
              <p class="text-sm text-gray-600">Active Consents</p>
            </div>
            <div class="text-center p-4 bg-green-50 rounded-lg">
              <p class="text-2xl font-bold text-green-600">3</p>
              <p class="text-sm text-gray-600">Requests Submitted</p>
            </div>
            <div class="text-center p-4 bg-yellow-50 rounded-lg">
              <p class="text-2xl font-bold text-yellow-600">1</p>
              <p class="text-sm text-gray-600">Open Grievances</p>
            </div>
          </div>
        </div>

        <!-- Danger Zone -->
        <div class="card border-red-200 bg-red-50">
          <h2 class="text-lg font-semibold text-red-900 mb-4">Danger Zone</h2>
          <p class="text-sm text-red-700 mb-4">
            Permanently delete your account and all associated data. This action cannot be undone.
          </p>
          <button class="btn bg-red-600 text-white hover:bg-red-700">
            Delete My Account
          </button>
        </div>
      </main>
    </div>
  `
})
export class ProfileComponent {
  profile = signal({
    name: 'John Doe',
    email: 'john.doe@example.com',
    phone: '+91 98765 43210',
    language: 'en'
  });

  saveProfile(): void {
    alert('Profile updated successfully!');
  }
}
