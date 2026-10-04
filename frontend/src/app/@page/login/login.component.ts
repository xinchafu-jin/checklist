import {Component, inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {CurrentUserService} from '../../@service/current-user.service';
import {SurveyApiService} from '../../@service/survey-api.service';
import {LoginRequest, UserRole} from '../../@model/survey-model';

@Component({
  selector: 'app-login',
  imports: [MatCardModule, MatButtonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  currentUserService = inject(CurrentUserService);
  surveyApiService = inject(SurveyApiService);
  fb = inject(FormBuilder);
  router = inject(Router);
  loginError = '';
  isSubmitting = false;
  form = this.fb.group({
    account: ['', Validators.required],
    password: ['', Validators.required],
  });

  login() {
    this.loginError = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const req: LoginRequest = {
      email: this.form.controls.account.value ?? "",
      password: this.form.controls.password.value ?? ""
    }
    this.isSubmitting = true;
    this.surveyApiService.login(req).subscribe({
      next:(res)=>{
        this.currentUserService.set(res);
        if(res.role === 'ADMIN'){
          this.router.navigate(['/home/dashBoard']);
        }else {
          this.router.navigate(['/front-home/front-dashboard'])
        }
      },
      error:(err)=>{
        this.isSubmitting= false;
        this.loginError = err.error?.message ?? '登入失敗，請稍後再試。';
    },
    })
  }
}
