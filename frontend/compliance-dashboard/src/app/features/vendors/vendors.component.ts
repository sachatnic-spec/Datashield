import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-vendors',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-8">
      <h1 class="text-2xl font-bold">Vendor Risk Management</h1>
      <p class="text-gray-600 mt-2">Processor compliance and DPA tracking</p>
      <div class="card mt-6">
        <p>Coming soon: Risk scoring, DPA lifecycle, vendor assessment dashboard</p>
      </div>
    </div>
  `
})
export class VendorsComponent {}
