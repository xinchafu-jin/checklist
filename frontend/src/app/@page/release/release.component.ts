import {Component, inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {SurveyApiService} from '../../@service/survey-api.service';
import {Survey} from '../../@model/survey-model';

@Component({
  selector: 'app-release',
  imports: [MatDialogModule],
  templateUrl: './release.component.html',
  styleUrl: './release.component.scss'
})
export class ReleaseComponent implements OnInit {
  private surveyApiService = inject(SurveyApiService);

  /*
   * 這一頁原本是 window.open 開的獨立分頁，現在改成 Material Dialog。
   *
   * 換掉的理由是 sessionStorage 每個分頁各存一份：
   * 新分頁拿不到 currentUser，authGuard 會把它彈回登入頁，
   * 發布問卷的視窗每次都變成登入畫面。
   *
   * 順便解決另外兩件事：
   * 不必用 setInterval 每 0.5 秒偵測視窗有沒有被關，
   * 也不必依賴 window.close()（非 script 開啟的視窗關不掉）。
   */
  private dialogRef = inject(MatDialogRef<ReleaseComponent>);
  private data = inject<{ id: number }>(MAT_DIALOG_DATA);

  survey?: Survey;

  loadError = '';         // 載入失敗，整個彈窗不可用
  errorMessage = '';      // 發佈失敗，還能重試
  isSubmitting = false;

  ngOnInit() {
    this.surveyApiService.getSurveyDetail(this.data.id).subscribe({
      next: (survey) => this.survey = survey,
      error: () => this.loadError = '查無這份問卷，可能已被刪除，或伺服器沒有回應。',
    })
  }

  goRelease() {
    if (!this.survey || this.isSubmitting) {
      return;
    }
    this.errorMessage = '';
    this.isSubmitting = true;

    // 不直接改 this.survey —— 發佈失敗時，畫面上的狀態不該先變成已發佈
    this.surveyApiService.updateSurvey({...this.survey, isPublished: true}).subscribe({
      next: () => {
        this.survey!.isPublished = true;
        // 帶 true 回去，讓 my-survey 知道「有發佈成功，該重抓列表」
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message ?? '發佈失敗，請稍後再試。';
      },
    });
  }

  // 取消或載入失敗時關閉；沒帶值回去，my-survey 就不會重抓
  close() {
    this.dialogRef.close();
  }
}
