import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';

/**
 * 給彈窗顯示用的單題摘要。
 *
 * 這裡刻意不放 FormGroup 或 SurveyQuestion —— 彈窗只負責把文字排出來，
 * 「使用者選的選項 id 對應到哪個選項文字」由 fill-in 換算好再送進來。
 * 彈窗不懂表單結構，之後表單怎麼改都不會動到這個元件。
 */
export interface AnswerPreviewItem {
  questionNum: number;
  title: string;
  answerText: string;
  answered: boolean;      // 非必填題可以留白，留白的要標示出來而不是顯示空字串
}

export interface AnswerPreviewData {
  surveyTitle: string;
  items: AnswerPreviewItem[];
}

@Component({
  selector: 'app-answer-preview',
  imports: [MatDialogModule],
  templateUrl: './answer-preview.component.html',
  styleUrl: './answer-preview.component.scss'
})
export class AnswerPreviewComponent {
  private dialogRef = inject(MatDialogRef<AnswerPreviewComponent>);
  data = inject<AnswerPreviewData>(MAT_DIALOG_DATA);

  // 帶 true 回去代表「確認送出」，fill-in 收到才真的呼叫 API
  confirm() {
    this.dialogRef.close(true);
  }

  // 沒帶值回去 = 返回修改，fill-in 什麼都不做
  cancel() {
    this.dialogRef.close();
  }
}
