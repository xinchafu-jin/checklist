import {AfterViewInit, Component, DestroyRef, inject, OnInit, QueryList, ViewChildren} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {provideNativeDateAdapter} from '@angular/material/core';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatTabsModule} from '@angular/material/tabs';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {SurveyApiService} from '../../../@service/survey-api.service';
import {statusAdj, Survey} from '../../../@model/survey-model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-front-surveymenu',
  /*
   * 用「模組」而不是個別 directive。
   *
   * IDE 自動 import 補的是一顆一顆的 directive（MatTable、MatCell…），
   * 少補一個就整塊失效而且不會報錯 —— 這次少了 MatHeaderCellDef
   * 與 MatInput，表格與日期欄位就整個不見了。
   * 模組會把該有的一次帶齊。
   */
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatTabsModule,
    ReactiveFormsModule,
    RouterLink,
    FormsModule,
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './front-surveymenu.component.html',
  styleUrl: './front-surveymenu.component.scss'
})
export class FrontSurveymenuComponent implements OnInit, AfterViewInit {

  private destroyRef = inject(DestroyRef);
  surveyApiSurvey = inject(SurveyApiService);

  // 三個 tab 各一個分頁器，順序即 template 中出現的順序
  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;

  surveys: Survey[] = [];

  /*
   * 三個 tab 各自一份資料。
   *
   * 不共用同一個 dataSource 是為了 tab 的滑動動畫 ——
   * Material 是把各 tab 的內容並排、切換時橫移，
   * 內容如果都指向同一份資料，滑過去看到的會是一樣的東西。
   *
   * 沒有「儲存中」——那是未發布的草稿，只有製作者自己看得到。
   */
  notStart = new MatTableDataSource<Survey>();
  active = new MatTableDataSource<Survey>();
  ended = new MatTableDataSource<Survey>();

  keyword: string = "";

  /** 通過關鍵字與日期、且前台看得到的總數（三個 tab 加總） */
  filteredTotal = 0;

  /** 日期區間篩選，兩個欄位任一改變都重跑 applyFilters() */
  readonly range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  /*
   * 狀態欄拿掉了 —— tab 本身就是狀態，同一頁講兩次是多餘的。
   *
   * 統計欄給「進行中」與「已結束」兩個 tab（與 my-survey 一致）：
   * 進行中的問卷雖然數字還會變，但看即時進度本來就是有用的；
   * 儲存中與尚未開始則是一份填答都不可能有，那一欄沒有意義。
   */
  readonly baseColumns: string[] = ['id', 'Name', 'Creator', 'StartTime', 'EndTime'];
  readonly withStatColumns: string[] = [...this.baseColumns, 'Result'];

  ngOnInit() {
    this.surveyApiSurvey.getAllSurveys().subscribe((res) => {
      this.surveys = res;
      // 不直接塞 dataSource —— 要先過關鍵字、日期與 tab 三道條件
      this.applyFilters();
    })
    this.range.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.applyFilters();
    });
  }

  ngAfterViewInit() {
    const [p1, p2, p3] = this.paginators.toArray();
    this.notStart.paginator = p1;
    this.active.paginator = p2;
    this.ended.paginator = p3;
  }

  changeInput(event: Event) {
    const inputEvent = event as InputEvent;
    if (inputEvent.isComposing) {
      return;
    }
    const inputElement = event.target as HTMLInputElement;
    this.keyword = inputElement.value.trim().normalize('NFKC').toLowerCase();
    this.applyFilters();
  }

  applyFilters() {
    const startDate = this.range.value.start;
    const endDate = this.range.value.end;
    const tidyData: Survey[] = [];
    for (let i = 0; i < this.surveys.length; i++) {
      const survey = this.surveys[i];

      let isNameMatch = false;
      if (!this.keyword) {
        isNameMatch = true;
      } else if (survey.title && survey.title.toLowerCase().indexOf(this.keyword) > -1) {
        isNameMatch = true;
      }
      // 日期
      let isDateMatch = true;
      if (survey.startDate) {
        const surveyDate = new Date(survey.startDate);

        if (startDate && surveyDate < new Date(startDate)) {
          isDateMatch = false;
        }

        if (endDate) {
          const endDateTime = new Date(endDate);
          endDateTime.setHours(23, 59, 59, 999);

          if (surveyDate > endDateTime) {
            isDateMatch = false;
          }
        }
      }

      if (isNameMatch && isDateMatch) {
        tidyData.push(survey);
      }
    }

    /*
     * 通過關鍵字與日期的問卷，再依狀態分到三個 tab。
     *
     * 「儲存中」直接跳過，連 filteredTotal 都不算 ——
     * 那是還沒發布的草稿，只有製作者自己該看得到。
     */
    const notStartRows: Survey[] = [];
    const activeRows: Survey[] = [];
    const endedRows: Survey[] = [];
    let visibleTotal = 0;

    for (const survey of tidyData) {
      const status = statusAdj(survey);

      if (status === '尚未開始') {
        notStartRows.push(survey);
      } else if (status === '進行中') {
        activeRows.push(survey);
      } else if (status === '已結束') {
        endedRows.push(survey);
      } else {
        continue;      // 儲存中，前台不顯示
      }

      visibleTotal++;
    }

    this.notStart.data = notStartRows;
    this.active.data = activeRows;
    this.ended.data = endedRows;
    this.filteredTotal = visibleTotal;

    // 篩選條件變了就回到第一頁，否則停在一個可能已經沒有資料的頁碼上
    this.resetPages();
  }

  private resetPages() {
    const sources = [this.notStart, this.active, this.ended];
    for (const source of sources) {
      if (source.paginator) {
        source.paginator.firstPage();
      }
    }
  }

}
