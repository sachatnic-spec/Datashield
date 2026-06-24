import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-8">
      <h1 class="text-2xl font-bold">Settings</h1>
      <p class="text-gray-600 mt-2">Platform configuration and preferences</p>
      <div class="card mt-6">
        <p>Coming soon: Tenant settings, user management, notification preferences</p>
      </div>
    </div>
  `
})
export class SettingsComponent {}
