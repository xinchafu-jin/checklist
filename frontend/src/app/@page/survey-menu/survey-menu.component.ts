import {AfterViewInit, Component, DestroyRef, inject, OnInit, QueryList, ViewChildren} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {provideNativeDateAdapter} from '@angular/material/core';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatTabsModule} from '@angular/material/tabs';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {SurveyApiService} from '../../@service/survey-api.service';
import {statusAdj, Survey} from '../../@model/survey-model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {switchMap} from 'rxjs';

@Component({
  selector: 'app-survey-menu',
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
  templateUrl: './survey-menu.component.html',
  styleUrl: './survey-menu.component.scss',
  providers: [provideNativeDateAdapter()],
})
export class SurveyMenuComponent implements OnInit, AfterViewInit {
  private destroyRef = inject(DestroyRef);
  surveyApiSurvey = inject(SurveyApiService);

  // 四個 tab 各一個分頁器，順序即 template 中出現的順序
  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;

  surveys: Survey[] = [];

  /*
   * 四個 tab 各自一份資料。
   *
   * 不共用同一個 dataSource 是為了 tab 的滑動動畫 ——
   * Material 是把四個 tab 的內容並排、切換時橫移，
   * 四份內容如果指向同一份資料，滑過去看到的會是一樣的東西。
   */
  saving = new MatTableDataSource<Survey>();
  notStart = new MatTableDataSource<Survey>();
  active = new MatTableDataSource<Survey>();
  ended = new MatTableDataSource<Survey>();

  keyword: string = "";

  /** 通過關鍵字與日期的總數（四個 tab 加總），顯示在標題旁 */
  filteredTotal = 0;

  selectMode = false;
  selectIds = new Set<number>();

  pendingDelete = false;   // 已按下刪除，等待二次確認
  isDeleting = false;      // 刪除中，擋連點
  deleteError = '';

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
    const [p1, p2, p3, p4] = this.paginators.toArray();
    this.saving.paginator = p1;
    this.notStart.paginator = p2;
    this.active.paginator = p3;
    this.ended.paginator = p4;
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

    // 通過關鍵字與日期的問卷，再依狀態分到四個 tab
    const savingRows: Survey[] = [];
    const notStartRows: Survey[] = [];
    const activeRows: Survey[] = [];
    const endedRows: Survey[] = [];

    for (const survey of tidyData) {
      const status = statusAdj(survey);

      if (status === '儲存中') {
        savingRows.push(survey);
      } else if (status === '尚未開始') {
        notStartRows.push(survey);
      } else if (status === '進行中') {
        activeRows.push(survey);
      } else if (status === '已結束') {
        endedRows.push(survey);
      }
    }

    this.saving.data = savingRows;
    this.notStart.data = notStartRows;
    this.active.data = activeRows;
    this.ended.data = endedRows;
    this.filteredTotal = tidyData.length;

    // 篩選條件變了就回到第一頁，否則停在一個可能已經沒有資料的頁碼上
    this.resetPages();
  }

  private resetPages() {
    const sources = [this.saving, this.notStart, this.active, this.ended];
    for (const source of sources) {
      if (source.paginator) {
        source.paginator.firstPage();
      }
    }
  }

  toggleSelectMode() {
    this.selectMode = true;
  }

  exitSelectMode() {
    this.selectMode = false;
    this.selectIds.clear();     // 不清的話，下次進選取模式會帶著上次的勾選
    this.pendingDelete = false;
    this.deleteError = '';
  }

  toggleSelectOne(id: number, checked: boolean) {
    if (checked) {
      this.selectIds.add(id);
    } else {
      this.selectIds.delete(id);
    }

  }

  /** 按下確認鍵先跳出確認條，不直接刪 —— 刪除不可逆，要有機會反悔 */
  askDelete() {
    this.deleteError = '';
    if (!this.selectIds.size) {
      this.deleteError = '請先勾選要刪除的問卷。';
      return;
    }
    this.pendingDelete = true;
  }

  cancelDelete() {
    this.pendingDelete = false;
  }

  confirmDelete() {
    if (this.isDeleting) {
      return;
    }
    this.isDeleting = true;
    this.deleteError = '';

    this.surveyApiSurvey.deleteSurvey(Array.from(this.selectIds)).pipe(
      // 刪完接著重抓列表。用 switchMap 串接，不要巢狀 subscribe
      switchMap(() => this.surveyApiSurvey.getAllSurveys())
    ).subscribe({
      next: (res) => {
        this.isDeleting = false;
        this.exitSelectMode();
        this.surveys = res;
        this.applyFilters();      // 沿用目前的關鍵字與日期條件，不要把篩選洗掉
      },
      error: (err) => {
        this.isDeleting = false;
        // 失敗時不退出選取模式 —— 勾選要留著才能直接重試
        this.deleteError = err.error?.message ?? '刪除失敗，請稍後再試。';
      },
    });
  }

}
