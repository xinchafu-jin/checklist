import {Component, inject, OnInit} from '@angular/core';
import {FormArray, FormBuilder, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {SurveyApiService} from '../../@service/survey-api.service';
import {Survey} from '../../@model/survey-model';

@Component({
  selector: 'app-edit',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './edit.component.html',
  styleUrl: './edit.component.scss'
})
export class EditComponent implements OnInit {
  ids?: number;
  loaded = false;         // 資料已到，可以顯示表單
  loadError = '';         // 載入失敗，整頁不可用（與 errorMessage 是兩回事）
  router = inject(Router);
  route = inject(ActivatedRoute);
  surveyApiService = inject(SurveyApiService);
  fb = inject(FormBuilder);
  survey?: Survey;
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

  ngOnInit() {
    this.ids = Number(this.route.snapshot.paramMap.get("id"));
    this.surveyApiService.getSurveyDetail(this.ids).subscribe({
      next: (survey) => {
        this.fillFrom(survey);
        this.loaded = true;
      },
      error: () => this.loadError = '查無這份問卷，可能已被刪除，或伺服器沒有回應。',
    })
  }

  fillFrom(survey: Survey) {
    this.form.patchValue({
      title: survey.title,
      description: survey.description,
      startDate: survey.startDate?.slice(0, 10),
      endDate: survey.endDate?.slice(0, 10),
      isPublished: survey.isPublished,
    });
    (survey.questions ?? []).forEach((q) => {
      const questionGroup = this.fb.group({
        title: [q.title, Validators.required],
        type: [q.type, Validators.required],
        isRequired: [q.isRequired ?? true],   // 後端預設 true，這裡跟著 true
        questionNum: [q.questionNum, Validators.required],
        options: this.fb.array([]),
      });
      const optionsArray = questionGroup.get('options') as FormArray;
      (q.options ?? []).forEach((o) => {
        optionsArray.push(this.fb.group({
          optionCode: [o.optionCode, Validators.required],
          optionText: [o.optionText, Validators.required],
        }));
      });
      this.questionsArray.push(questionGroup);
    });
  }

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
    this.isSubmitting = true;
    this.surveyApiService
      .updateSurvey({...this.form.getRawValue(), id: this.ids} as Survey)
      .subscribe({
        next: () => this.router.navigate(['/home/mySurvey']),
        error: (err) => {
          this.isSubmitting = false;
          this.errorMessage = err.error?.message ?? '儲存問卷失敗，請稍後再試。';
        },
      });
  }

}
