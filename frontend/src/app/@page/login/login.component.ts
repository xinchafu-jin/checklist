import {Component, inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {CurrentUserService} from '../../@service/current-user.service';
import {SurveyApiService} from '../../@service/survey-api.service';
import {LoginRequest} from '../../@model/survey-model';

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
        /*
         * 後台只給 ADMIN。前台已拆到 frontend-pwa，
         * USER 不寫進 sessionStorage —— 寫了也只會被 adminGuard 擋回登入頁。
         */
        if (res.role !== 'ADMIN') {
          this.isSubmitting = false;
          this.loginError = '此帳號沒有後台權限，請改用現場檢點 App 登入。';
          return;
        }
        this.currentUserService.set(res);
        this.router.navigate(['/home/dashBoard']);
      },
      error:(err)=>{
        this.isSubmitting= false;
        this.loginError = err.error?.message ?? '登入失敗，請稍後再試。';
    },
    })
  }
}
