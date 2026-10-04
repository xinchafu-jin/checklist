import {Component, inject, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {SurveyApiService} from '../../@service/survey-api.service';
import {CurrentUserService} from '../../@service/current-user.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AnswerPreviewComponent, AnswerPreviewItem} from './answer-preview/answer-preview.component';
import {FillAnswer, FillRequest, Survey, SurveyQuestion} from '../../@model/survey-model';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';

@Component({
  selector: 'app-fill-in',
  imports: [
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './fill-in.component.html',
  styleUrl: './fill-in.component.scss'
})
export class FillInComponent implements OnInit {
  surveyId!: number;
  survey?: Survey;
  loadError = '';         // 載入失敗，整頁不可用（與 errorMessage 是兩回事）
  fb = inject(FormBuilder);
  surveyApiService = inject(SurveyApiService);
  route = inject(ActivatedRoute)
  router = inject(Router)
  currentUser = inject(CurrentUserService);
  dialog = inject(MatDialog);
  isSubmitting = false;   // 送出中，用來擋連點
  errorMessage = '';      // 後端回傳的錯誤訊息

  ngOnInit() {
    this.surveyId = Number(this.route.snapshot.paramMap.get("id"));
    this.surveyApiService.getSurveyDetail(this.surveyId).subscribe({
      next: (survey) => {
        this.survey = survey;
        this.buildAnswers(survey.questions ?? []);
      },
      error: () => {
        this.loadError = '查無這份問卷，可能已被刪除，或伺服器沒有回應。';
      }
    })
  }

  answersArray(): FormArray {
    return this.form.get('answers') as FormArray;
  }

  optionChecks(qi: number): FormArray {
    return this.answersArray().at(qi).get('optionChecks') as FormArray;
  }

  form = this.fb.group({
    answers: this.fb.array([]),
  });

  // ============================== 看題目 對應給填答 ======================================================
  private buildAnswers(q: SurveyQuestion[]) {
    this.answersArray().clear();
    q.forEach((q) => this.answersArray().push(this.buildAnswer(q)));

  }

  private buildAnswer(q: SurveyQuestion): FormGroup {
    return this.fb.group({
      ansText: ["", q.isRequired && q.type === 'TEXT' ? Validators.required : []
      ],
      singleOption: [null, q.isRequired && q.type === 'SINGLE' ? Validators.required : []],
      optionChecks: this.fb.array(
        (q.options ?? []).map(() => this.fb.control(false)),
        q.isRequired && q.type === 'MULTI' ? atLeastOneChecked : []
      ),
    })
  }
  showError(qi:number):boolean{
    const  g = this.answersArray().at(qi);
    return g.invalid && g.touched;
  }

  // ====================================================================================

  subForm() {
    if (this.form.invalid || this.isSubmitting) {
      this.form.markAllAsTouched();
      this.errorMessage =
        '有必填題尚未完成，請檢查標示紅字的題目。';
      return;
    }
    this.errorMessage = '';      // ← 這裡

    // 後端 quiz_response.user_email 有外鍵指向 user 表。
    // 送空字串會撞 quiz_response_ibfk_2，回一整串 SQL 錯誤，
    // 使用者只會看到看不懂的訊息 —— 不如在這裡先擋下來。
    const userEmail = this.currentUser.get()?.email ?? '';
    if (!userEmail) {
      this.errorMessage = '找不到登入資訊，請重新登入後再送出。';
      return;
    }

    const answers: FillAnswer[] = (this.survey?.questions ?? []).map((q, qi) => {
      const group = this.answersArray().at(qi);
      if (q.type === 'TEXT') {
        return {questionId: q.id, answerText: group.get('ansText')?.value ?? ''};
      }
      if (q.type === 'SINGLE') {
        const id = group.get('singleOption')?.value ?? "";
        return {questionId: q.id, optionIds: id ? [id] : []}
      }
      const checks = this.optionChecks(qi).value as boolean[];
      return {
        questionId: q.id,
        optionIds: q.options.filter((_, oi) => checks[oi]).map((o) => o.id),
      }
    })
    const payload: FillRequest = {
      quizId: this.surveyId!,
      userEmail,
      answers,
    }

    /*
     * 送出是不可逆的 —— quiz_response 上有 (quiz_id, user_email) 的唯一約束，
     * 一人一份只能答一次，送出去就改不了也重填不了。
     * 所以先開唯讀彈窗讓使用者看一眼自己填了什麼，確認才真的送。
     */
    const dialogRef = this.dialog.open(AnswerPreviewComponent, {
      data: {
        surveyTitle: this.survey?.title ?? '',
        items: this.buildPreview(),
      },
      width: '600px',
      maxHeight: '80vh',
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      // 只有按「確認送出」才帶 true 回來，按「返回修改」什麼都不做
      if (confirmed) {
        this.send(payload);
      }
    });
  }

  /**
   * 真正呼叫 API 的那一段。
   * 從 subForm() 拆出來，是因為現在中間隔了一個彈窗 ——
   * isSubmitting 要等使用者確認之後才亮，不然彈窗還開著送出鈕就先鎖住了。
   */
  private send(payload: FillRequest) {
    this.isSubmitting = true;
    this.surveyApiService.fillQuiz(payload).subscribe({
      next: () => {
        this.router.navigate(['/home/surveyMenu'])
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message ?? '送出失敗，請稍後再試。';
      },
    })
  }

  /**
   * 把表單裡的值換算成給人看的文字。
   *
   * 表單存的是選項 id 與 boolean 陣列，直接顯示會是一串數字；
   * 這裡在送進彈窗之前先換成選項文字，彈窗就不必懂表單結構。
   */
  private buildPreview(): AnswerPreviewItem[] {
    const items: AnswerPreviewItem[] = [];
    const questions = this.survey?.questions ?? [];

    for (let qi = 0; qi < questions.length; qi++) {
      const q = questions[qi];
      const group = this.answersArray().at(qi);
      let answerText = '';

      if (q.type === 'TEXT') {
        answerText = (group.get('ansText')?.value ?? '').trim();

      } else if (q.type === 'SINGLE') {
        const optionId = group.get('singleOption')?.value;
        for (const option of q.options) {
          if (option.id === optionId) {
            answerText = option.optionText;
          }
        }

      } else {
        // MULTI：勾了幾個就串幾個
        const checks = this.optionChecks(qi).value as boolean[];
        const picked: string[] = [];
        for (let oi = 0; oi < q.options.length; oi++) {
          if (checks[oi]) {
            picked.push(q.options[oi].optionText);
          }
        }
        answerText = picked.join('、');
      }

      items.push({
        questionNum: q.questionNum,
        title: q.title,
        answerText,
        // 非必填題可以留白，彈窗要顯示「未作答」而不是一片空白
        answered: answerText.length > 0,
      });
    }

    return items;
  }
}

function atLeastOneChecked(control: AbstractControl): ValidationErrors | null {
  const values = control.value as boolean[];
  return values.some(v => v) ? null : {required: true};
}
