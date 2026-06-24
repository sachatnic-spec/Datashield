import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-8">
      <h1 class="text-2xl font-bold">Compliance Reports</h1>
      <p class="text-gray-600 mt-2">Board-ready PDF/Excel exports</p>
      <div class="card mt-6">
        <p>Coming soon: Executive summary, DPDP compliance certificate, scheduled reports</p>
      </div>
    </div>
  `
})
export class ReportsComponent {}
