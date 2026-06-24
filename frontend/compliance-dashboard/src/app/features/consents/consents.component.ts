import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-consents',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-8">
      <h1 class="text-2xl font-bold">Consent Management</h1>
      <p class="text-gray-600 mt-2">Track and manage user consents</p>
      <div class="card mt-6">
        <p>Coming soon: Consent lifecycle tracking, purpose management, and withdrawal handling</p>
      </div>
    </div>
  `
})
export class ConsentsComponent {}
