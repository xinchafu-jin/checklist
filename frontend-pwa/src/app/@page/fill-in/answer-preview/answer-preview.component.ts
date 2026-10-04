import {Component, EventEmitter, Input, Output} from '@angular/core';

/**
 * 給確認視窗顯示用的單題摘要。
 *
 * 這裡刻意不放 FormGroup 或 SurveyQuestion —— 視窗只負責把文字排出來，
 * 「使用者選的選項 id 對應到哪個選項文字」由 fill-in 換算好再送進來。
 * 視窗不懂表單結構，之後表單怎麼改都不會動到這個元件。
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

/**
 * 後台用 MatDialog 開，前台沒有 Material，改成由 fill-in 用 @if 控制的遮罩。
 * 結果不再透過 dialogRef.close() 回傳，而是 confirmed / cancelled 兩個事件。
 */
@Component({
  selector: 'app-answer-preview',
  templateUrl: './answer-preview.component.html',
})
export class AnswerPreviewComponent {
  @Input({required: true}) data!: AnswerPreviewData;

  // 確認送出，fill-in 收到才真的呼叫 API
  @Output() confirmed = new EventEmitter<void>();

  // 返回修改，fill-in 只關掉視窗
  @Output() cancelled = new EventEmitter<void>();
}
