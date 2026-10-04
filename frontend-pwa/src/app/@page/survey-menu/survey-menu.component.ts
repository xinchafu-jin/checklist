import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {SurveyApiService} from '../../@service/survey-api.service';
import {statusAdj, Survey} from '../../@model/survey-model';
import {PagerComponent} from '../../@component/pager/pager.component';

type SurveyTab = 'notStart' | 'active' | 'ended';

@Component({
  selector: 'app-survey-menu',
  imports: [ReactiveFormsModule, RouterLink, PagerComponent],
  templateUrl: './survey-menu.component.html',
})
export class SurveyMenuComponent implements OnInit {

  private destroyRef = inject(DestroyRef);
  surveyApiSurvey = inject(SurveyApiService);

  surveys: Survey[] = [];

  /*
   * 三個 tab 各自一份資料。
   * 沒有「儲存中」——那是未發布的草稿，只有製作者自己看得到。
   */
  rows: Record<SurveyTab, Survey[]> = {notStart: [], active: [], ended: []};

  readonly tabs: { key: SurveyTab, label: string }[] = [
    {key: 'notStart', label: '尚未開始'},
    {key: 'active', label: '進行中'},
    {key: 'ended', label: '已結束'},
  ];
  activeTab: SurveyTab = 'notStart';

  readonly pageSize = 5;
  page = 0;

  keyword: string = "";

  /** 通過關鍵字與日期、且前台看得到的總數（三個 tab 加總） */
  filteredTotal = 0;

  /**
   * 日期區間篩選，兩個欄位任一改變都重跑 applyFilters()。
   * 原生 <input type="date"> 給的是 "yyyy-MM-dd" 字串，跟後端的 LocalDate 同格式，
   * 直接比字串就好，不必轉成 Date 再處理時區。
   */
  readonly range = new FormGroup({
    start: new FormControl<string>('', {nonNullable: true}),
    end: new FormControl<string>('', {nonNullable: true}),
  });

  get currentRows(): Survey[] {
    return this.rows[this.activeTab];
  }

  get pagedRows(): Survey[] {
    const start = this.page * this.pageSize;
    return this.currentRows.slice(start, start + this.pageSize);
  }

  /*
   * 統計連結只給「進行中」與「已結束」：
   * 尚未開始的問卷一份填答都不可能有，連過去也沒有意義。
   */
  get showStat(): boolean {
    return this.activeTab !== 'notStart';
  }

  ngOnInit() {
    this.surveyApiSurvey.getAllSurveys().subscribe((res) => {
      this.surveys = res;
      // 不直接顯示 —— 要先過關鍵字、日期與 tab 三道條件
      this.applyFilters();
    })
    this.range.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.applyFilters();
    });
  }

  selectTab(tab: SurveyTab) {
    this.activeTab = tab;
    this.page = 0;
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
    const {start, end} = this.range.getRawValue();
    const tidyData: Survey[] = [];

    for (const survey of this.surveys) {
      let isNameMatch = false;
      if (!this.keyword) {
        isNameMatch = true;
      } else if (survey.title && survey.title.toLowerCase().indexOf(this.keyword) > -1) {
        isNameMatch = true;
      }

      let isDateMatch = true;
      if (survey.startDate) {
        if (start && survey.startDate < start) {
          isDateMatch = false;
        }
        if (end && survey.startDate > end) {
          isDateMatch = false;
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
    const rows: Record<SurveyTab, Survey[]> = {notStart: [], active: [], ended: []};
    let visibleTotal = 0;

    for (const survey of tidyData) {
      const status = statusAdj(survey);

      if (status === '尚未開始') {
        rows.notStart.push(survey);
      } else if (status === '進行中') {
        rows.active.push(survey);
      } else if (status === '已結束') {
        rows.ended.push(survey);
      } else {
        continue;      // 儲存中，前台不顯示
      }

      visibleTotal++;
    }

    this.rows = rows;
    this.filteredTotal = visibleTotal;

    // 篩選條件變了就回到第一頁，否則停在一個可能已經沒有資料的頁碼上
    this.page = 0;
  }
}
