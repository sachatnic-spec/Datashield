import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-grievances',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-8">
      <h1 class="text-2xl font-bold">Grievance Management</h1>
      <p class="text-gray-600 mt-2">30-day SLA tracking and resolution</p>
      <div class="card mt-6">
        <p>Coming soon: Grievance queue, SLA countdown, escalation workflows</p>
      </div>
    </div>
  `
})
export class GrievancesComponent {}
