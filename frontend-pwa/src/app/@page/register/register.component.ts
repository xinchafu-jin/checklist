import {Component, inject} from '@angular/core';
import {AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {SurveyApiService} from '../../@service/survey-api.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  router = inject(Router);
  surveyApiService = inject(SurveyApiService)
  registerError = '';
  isSubmitting = false;
  fb = inject(FormBuilder);
  form = this.fb.nonNullable.group({
    email: ['', [Validators.email, Validators.required]],
    password: ['', [Validators.minLength(6), Validators.required]],
    secondPassword: ['', [Validators.minLength(6), Validators.required]],
    name: ['', Validators.required],
    phone: ['', [Validators.required, Validators.pattern(/^09\d{8}$/)]],
    age: this.fb.control<number | null>(null),
  }, {validators: passwordMatch});

  subForm() {
    if (this.form.invalid || this.isSubmitting) {
      this.form.markAllAsTouched();
      this.registerError =
        '有必填題尚未完成，請檢查標示紅字的題目。';
      return;
    }

    this.registerError = '';
    this.isSubmitting = true;
    const {secondPassword, ...payload} = this.form.getRawValue();

    this.surveyApiService.register(payload).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.registerError = err.error?.message ?? "建立帳號失敗, 請稍候重試"
      }
    })
  }
}

function passwordMatch(group: AbstractControl): ValidationErrors | null {
  const pw = group.get('password')?.value;
  const pw2 = group.get('secondPassword')?.value;
  if (pw === pw2) {
    return null;
  } else {
    return {passwordMismatch: true};
  }
}
