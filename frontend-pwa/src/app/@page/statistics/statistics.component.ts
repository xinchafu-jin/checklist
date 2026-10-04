import {
  AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit, QueryList, ViewChildren
} from '@angular/core';
import {DatePipe, DecimalPipe} from '@angular/common';
import {ActivatedRoute, RouterLink} from '@angular/router';
import Chart from 'chart.js/auto';
import {SurveyApiService} from '../../@service/survey-api.service';
import {QuestionStat, QuizStat, QuizSubmission, SubmissionAnswer} from '../../@model/survey-model';

@Component({
  selector: 'app-statistics',
  imports: [DecimalPipe, DatePipe, RouterLink],
  templateUrl: './statistics.component.html',
})
export class StatisticsComponent implements OnInit, AfterViewInit, OnDestroy {
  private surveyApiService = inject(SurveyApiService);
  private route = inject(ActivatedRoute);
  quizStat?: QuizStat;
  quizSubmissions?: QuizSubmission[];

  activeTab: 'stat' | 'people' = 'stat';

  /**
   * 兩支 API 各自記錄失敗，不共用一個欄位。
   * 統計掛了、人員還在的話，沒理由把還能看的那個 tab 一起收掉。
   */
  statError = '';
  subError = '';

  private charts: Chart[] = [];

  /**
   * 圖表色票。
   * 這裡是唯一來源 —— 圓餅圖的扇形、圖例的色塊、長條圖的長條都呼叫 colorAt()，
   * 靠同一個 index 對應，避免 CSS 和 TS 各寫一份而對不上。
   */
  private readonly palette = [
    '#29382f',
    '#985036',
    '#606a63',
    '#7a8b6f',
    '#c08b5c',
    '#43584c',
    '#b5654a',
    '#8f9c88',
  ];

  /** 「未作答」專用色，刻意不取自 palette —— 它不是一個選項，視覺上要能一眼分辨 */
  readonly unansweredColor = '#d8d3c6';

  colorAt(index: number): string {
    return this.palette[index % this.palette.length];
  }

  /**
   * 這題有幾個人沒作答。
   * 單選題才算得準（一人最多一票，總票數不會超過填答人數）；
   * 多選題一人可投多票，相減沒有意義，所以直接回 0。
   */
  unansweredOf(q: QuestionStat): number {
    if (q.questionType !== 'SINGLE') {
      return 0;
    }
    const voted = (q.optionStats ?? []).reduce((sum, o) => sum + o.selectedCount, 0);
    return Math.max((this.quizStat?.totalRespondents ?? 0) - voted, 0);
  }

  /** 未作答佔總填答人數的比例，給圖例顯示用 */
  unansweredPercent(q: QuestionStat): number {
    const total = this.quizStat?.totalRespondents ?? 0;
    if (!total) {
      return 0;
    }
    return (this.unansweredOf(q) / total) * 100;
  }

  /**
   * 把一題的作答結果轉成要顯示的文字。
   * 簡答題直接回傳文字；選擇題把勾選的選項用頓號串起來。
   */
  answerText(a: SubmissionAnswer): string {
    if (a.questionType === 'TEXT') {
      if (a.answerText) {
        return a.answerText;
      }
      return '';
    }
    const picked: string[] = [];
    const opts = a.options;
    if (opts) {
      for (let i = 0; i < opts.length; i++) {
        if (opts[i].isSelected) {
          picked.push(opts[i].optionText);
        }
      }
    }
    return picked.join('、');
  }

  isUnanswered(a: SubmissionAnswer): boolean {
    return !this.answerText(a);
  }

  //id 對應問卷題目
  ngOnInit() {
    const ids = Number(this.route.snapshot.paramMap.get('id'));
    this.surveyApiService.getQuizStatistics(ids).subscribe({
      next: res => this.quizStat = res,
      error: () => this.statError = '讀不到統計資料，可能問卷不存在或伺服器沒有回應。',
    })
    this.surveyApiService.getQuizSubmissions(ids).subscribe({
      next: res => this.quizSubmissions = res,
      error: () => this.subError = '讀不到作答紀錄，可能問卷不存在或伺服器沒有回應。',
    })
  }

  @ViewChildren('chartCanvas') canvasRefs!: QueryList<ElementRef<HTMLCanvasElement>>;

  /*
   * canvas 放在 @if 裡，資料到了、或從「人員」切回「統計」時才會出現，
   * 所以要聽 changes 重畫，不能只在 ngAfterViewInit 畫一次。
   */
  ngAfterViewInit() {
    if (this.canvasRefs.length) {
      this.drawChart();
    }
    this.canvasRefs.changes.subscribe(() => this.drawChart());
  }

  ngOnDestroy() {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  /**
   * 只有單選題進 canvas。
   * 多選題改由 template 用 DOM 畫長條，跟文字對齊比較容易。
   */
  private drawChart() {
    this.charts.forEach(c => c.destroy());
    this.charts = [];

    /**
     * template 只在「單選題且有選項」時才畫 <canvas #chartCanvas>，
     * 所以這個篩選結果的順序，跟 canvasRefs 的順序是一對一的。
     */
    const chartQuestions = (this.quizStat?.questionStats ?? []).filter(
      q => q.questionType === 'SINGLE' && (q.optionStats?.length ?? 0) > 0
    );
    const canvases = this.canvasRefs?.toArray() ?? [];

    for (let i = 0; i < chartQuestions.length; i++) {
      const q = chartQuestions[i];
      const el = canvases[i]?.nativeElement;
      if (!el?.getContext('2d')) {
        continue;
      }

      const opts = q.optionStats ?? [];
      const labels = opts.map(o => o.optionText);
      const values = opts.map(o => o.selectedCount);
      const colors = opts.map((_, j) => this.colorAt(j));

      // 選填題會有人跳過。不補這一塊的話，3 票會被撐成整圓，看起來像人人都答了。
      const unanswered = this.unansweredOf(q);
      if (unanswered > 0) {
        labels.push('未作答');
        values.push(unanswered);
        colors.push(this.unansweredColor);
      }

      this.charts.push(new Chart(el, {
        type: 'doughnut',
        data: {
          labels,
          datasets: [{
            data: values,
            backgroundColor: colors,
            borderColor: '#ffffff',
            borderWidth: 2,
            hoverOffset: 6,
          }],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '58%',
          plugins: {
            // 圖例已經用 DOM 畫了，canvas 內不再重複一份
            legend: {display: false},
          },
        },
      }));
    }
  }
}
