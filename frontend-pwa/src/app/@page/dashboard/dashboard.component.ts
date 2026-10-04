import {Component, inject, OnInit} from '@angular/core';
import {RouterLink} from '@angular/router';
import {forkJoin, Observable, of} from 'rxjs';
import {statusAdj, Survey} from '../../@model/survey-model';
import {SurveyApiService} from '../../@service/survey-api.service';
import {CurrentUserService} from '../../@service/current-user.service';
import {PagerComponent} from '../../@component/pager/pager.component';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, PagerComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {

  survey: Survey[] = [];
  answerable: Survey[] = [];

  // AuthGuard 保證進得來就一定登入過，所以這裡取一次就夠，不用每次變更偵測都讀
  userName = '';

  loaded = false;         // 資料已到
  loadError = '';         // 載入失敗，整頁的數字都不可信

  readonly pageSize = 3;
  page = 0;

  private surveyApi = inject(SurveyApiService);
  private currentUser = inject(CurrentUserService);

  get pagedSurveys(): Survey[] {
    const start = this.page * this.pageSize;
    return this.answerable.slice(start, start + this.pageSize);
  }

  ngOnInit() {
    this.userName = this.currentUser.get()?.name ?? '';
    this.loadSurveys();
  }

  loadSurveys() {
    const myEmail = this.currentUser.get()?.email ?? '';

    /*
     * 沒登入就不打 /answered —— 後端擋空 email 會回 400，
     * 而 forkJoin 只要有一支失敗就整組進 error，
     * 連問卷列表都會跟著消失。這種情況直接當作「一份都沒答過」。
     */
    let answered$: Observable<number[]>;
    if (myEmail) {
      answered$ = this.surveyApi.getAnsweredQuizIds(myEmail);
    } else {
      answered$ = of([]);
    }

    // 兩支都回來才進 next，任一支失敗就進 error
    forkJoin([
      this.surveyApi.getAllSurveys(),
      answered$,
    ]).subscribe({
      next: ([surveys, answeredIds]) => {
        this.survey = surveys;
        // 用 Set 而不是陣列：下面每份問卷都要問一次「答過沒」
        this.updateDashBoardData(new Set(answeredIds));
        this.loaded = true;
      },
      error: () => {
        this.loadError = '讀不到問卷資料，請確認伺服器是否啟動。';
      }
    });
  }

  updateDashBoardData(answeredIds: Set<number>) {
    /*
     * 前台只關心「還等著我填的問卷」，不做任何統計。
     *
     * 收進清單的條件有兩個：
     * 狀態是「進行中」（尚未開始的還不能填、已結束的填不了），
     * 而且我還沒答過（答過的點進去填完，送出時才會撞上一人一份的限制）。
     */
    const answerable: Survey[] = [];

    for (const survey of this.survey) {
      if (statusAdj(survey) !== '進行中') {
        continue;
      }
      if (answeredIds.has(survey.id)) {
        continue;
      }
      answerable.push(survey);
    }

    // 清單與數字用同一份資料，才不會出現「尚未作答 43 份」但只列出 5 筆
    this.answerable = answerable;
    this.page = 0;
  }
}
