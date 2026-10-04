import {Component, inject} from '@angular/core';
import {
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {SurveyApiService} from '../../@service/survey-api.service';
import {CurrentUserService} from '../../@service/current-user.service';
import {SurveyRequest} from '../../@model/survey-model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-custom-survey',
  imports: [ReactiveFormsModule],
  templateUrl: './custom-survey.component.html',
  styleUrl: './custom-survey.component.scss',
})
export class CustomSurveyComponent {
  today = new Date().toISOString().split('T')[0];
  router = inject(Router);
  surveyApiService = inject(SurveyApiService);
  currentUser = inject(CurrentUserService);
  fb = inject(FormBuilder);
  isSubmitting = false;   // 送出中，用來擋連點
  errorMessage = '';      // 後端回傳的錯誤訊息
  form = this.fb.group({
    title: ['', Validators.required],       // 大標題
    description: ['', Validators.required], // 簡介
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    isPublished: [false, Validators.required],
    questions: this.fb.array([]),           // 題目
  });

  get questionsArray(): FormArray {
    return this.form.get('questions') as FormArray;
  }

  addQuestion() {
    const questionGroup = this.fb.group({
      title: ['', Validators.required],
      type: ['', Validators.required],
      isRequired: [false],
      questionNum: [this.questionsArray.length + 1, Validators.required],
      options: this.fb.array([]),
    });
    this.questionsArray.push(questionGroup);
  }

  removeQuestion(questionIndex: number) {
    this.questionsArray.removeAt(questionIndex);
    this.questionsArray.controls.forEach((control, index) => {
      control.get('questionNum')?.setValue(index + 1);
    });
  }

  getOption(questionIndex: number) {
    return this.questionsArray.at(questionIndex).get('options') as FormArray;
  }

  onQuestionTypeChange(questionIndex: number) {
    const questionType = this.questionsArray.at(questionIndex).get('type')?.value;
    const options = this.getOption(questionIndex);

    if (
      (questionType === 'SINGLE' || questionType === 'MULTI') &&
      options.length === 0
    ) {
      this.addOption(questionIndex);
    }
  }

  addOption(questionIndex: number) {
    const options = this.getOption(questionIndex);
    const optionsGroup = this.fb.group({
      optionCode: [String(options.length + 1), Validators.required],
      optionText: ['', Validators.required],
    });
    options.push(optionsGroup);
  }

  removeOption(questionIndex: number, optionIndex: number) {
    const options = this.getOption(questionIndex);
    options.removeAt(optionIndex);
    options.controls.forEach((control, index) => {
      control.get('optionCode')?.setValue(String(index + 1));
    });
  }

  subForm() {
    if (this.form.invalid || this.isSubmitting) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';

    /*
     * 製作者取自登入資訊，不放在表單裡讓人填。
     * 沒登入的話後端也會擋，但錯誤訊息是英文的，
     * 在這裡先擋一次才能給看得懂的中文。
     */
    const creatorEmail = this.currentUser.get()?.email;
    if (!creatorEmail) {
      this.errorMessage = '請先登入再建立問卷。';
      return;
    }

    this.isSubmitting = true;
    this.surveyApiService
      .createSurvey({
        ...this.form.getRawValue(),
        creatorEmail,
      } as SurveyRequest)
      .subscribe({
        next: () => this.router.navigate(['/home/mySurvey']),
        error: (err) => {
          this.isSubmitting = false;
          this.errorMessage = err.error?.message ?? '建立問卷失敗，請稍後再試。';
        },
      });
  }
}
