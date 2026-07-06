import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-500 to-primary-700 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-md w-full space-y-8">
        <div class="card text-center">
          <div class="mb-6">
            <h1 class="text-3xl font-bold text-gray-900">DataShield India</h1>
            <p class="text-sm text-gray-600 mt-2">DPDP Compliance Platform</p>
          </div>

          <h2 class="text-xl font-semibold text-gray-700 mb-6">DPO Dashboard Login</h2>

          <form (ngSubmit)="onLogin()" class="space-y-4">
            @if (error()) {
              <div class="bg-red-50 text-red-700 p-3 rounded-lg text-sm">
                {{ error() }}
              </div>
            }

            <div>
              <input
                type="email"
                [(ngModel)]="email"
                name="email"
                required
                class="input-field"
                placeholder="Email address"
              />
            </div>

            <div>
              <input
                type="password"
                [(ngModel)]="password"
                name="password"
                required
                class="input-field"
                placeholder="Password"
              />
            </div>

            <div>
              <input
                type="text"
                [(ngModel)]="tenantId"
                name="tenantId"
                required
                class="input-field"
                placeholder="Tenant ID"
              />
            </div>

            <button
              type="submit"
              [disabled]="loading()"
              class="btn-primary w-full"
            >
              {{ loading() ? 'Signing in...' : 'Sign In' }}
            </button>
          </form>

          <div class="mt-6 text-xs text-gray-500">
            <p>Demo: dpo&#64;example.com / demo1234 / default</p>
          </div>
        </div>

        <p class="text-center text-white text-sm">
          © 2026 DataShield India. All rights reserved.
        </p>
      </div>
    </div>
  `
})
export class LoginComponent {
  email = '';
  password = '';
  tenantId = environment.defaultTenantId;
  loading = signal(false);
  error = signal('');

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin(): void {
    if (!this.email || !this.password || !this.tenantId) {
      this.error.set('Please fill in all fields');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.authService.login({ email: this.email, password: this.password, tenantId: this.tenantId })
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => {
          this.loading.set(false);
          this.error.set(err.error?.message || err.message || 'Login failed. Please try again.');
        }
      });
  }
}
