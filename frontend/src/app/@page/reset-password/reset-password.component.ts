import {Component, inject} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {SurveyApiService} from '../../@service/survey-api.service';
import {ResetPasswordRequest} from '../../@model/survey-model';

@Component({
  selector: 'app-reset-password',
  imports: [MatCardModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent {
  surveyApiService = inject(SurveyApiService);
  fb = inject(FormBuilder);

  errorMessage = '';
  isSubmitting = false;

  /**
   * 成功之後不導頁，改用這個旗標把整頁換成完成畫面。
   * 導回登入頁的話畫面看起來跟沒做一樣，使用者會不確定成功了沒。
   */
  isDone = false;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    phone: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
  });

  submit() {
    if (this.isSubmitting) {
      return;
    }
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // 兩次新密碼一致與否是前端自己的檢查，後端只收一個 newPassword
    const newPassword = this.form.controls.newPassword.value ?? '';
    const confirmPassword = this.form.controls.confirmPassword.value ?? '';
    if (newPassword !== confirmPassword) {
      this.errorMessage = '兩次輸入的新密碼不一致。';
      return;
    }

    const req: ResetPasswordRequest = {
      email: this.form.controls.email.value ?? '',
      phone: this.form.controls.phone.value ?? '',
      newPassword: newPassword,
    };

    this.isSubmitting = true;

    this.surveyApiService.resetPassword(req).subscribe({
      next: () => {
        this.isDone = true;
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message ?? '重設失敗，請稍後再試。';
      },
    });
  }
}
