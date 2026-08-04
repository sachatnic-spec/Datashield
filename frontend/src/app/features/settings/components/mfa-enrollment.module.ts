import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ToastrModule } from 'ngx-toastr';

import { MFAEnrollmentComponent } from './mfa-enrollment.component';
import { MFAService } from './mfa.service';

@NgModule({
  declarations: [
    MFAEnrollmentComponent
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ToastrModule.forRoot({
      timeOut: 3000,
      positionClass: 'toast-top-right',
      preventDuplicates: true
    })
  ],
  providers: [MFAService],
  exports: [MFAEnrollmentComponent]
})
export class MFAEnrollmentModule { }
