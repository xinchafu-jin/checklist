import {Component, AfterViewInit, ViewChild, OnInit, inject} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatTableModule} from '@angular/material/table';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {RouterLink} from '@angular/router';
import {forkJoin, Observable, of} from 'rxjs';
import {SurveyApiService} from '../../@service/survey-api.service';
import {CurrentUserService} from '../../@service/current-user.service';
import {statusAdj, Survey,} from '../../@model/survey-model';

@Component({
  selector: 'app-dash-board',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    RouterLink,
  ],
  templateUrl: './dash-board.component.html',
  styleUrl: './dash-board.component.scss'
})
export class DashBoardComponent implements OnInit, AfterViewInit {
  survey: Survey[] = [];
  quantity: number = 0;

  // AuthGuard 保證進得來就一定登入過，所以這裡取一次就夠，不用每次變更偵測都讀
  userName = '';

  loaded = false;         // 資料已到
  loadError = '';         // 載入失敗，整頁的數字都不可信

  // 所有問卷
  surveysCount: number = 0;
  draftCount: number = 0;
  notStartCount: number = 0;
  activeCount: number = 0;
  endedCount: number = 0;

  // 我的問卷（依 creatorEmail 比對，沒登入就全部是 0）
  mySurveysCount: number = 0;
  myDraftCount: number = 0;
  myNotStartCount: number = 0;
  myActiveCount: number = 0;
  myEndedCount: number = 0;

  displayedColumns: string[] = ['survey'];
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  dataSource = new MatTableDataSource<Survey>([]);

  private surveyApi = inject(SurveyApiService);
  private currentUser = inject(CurrentUserService);

  ngOnInit() {
    this.userName = this.currentUser.get()?.name ?? '';
    this.loadSurveys();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
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
    // 沒登入就拿不到 email，「我的問卷」那四張卡會全部是 0
    const myEmail = this.currentUser.get()?.email ?? '';

    /*
     * 待作答清單只收「進行中」而且「我還沒答過」的。
     * 尚未開始的還不能填、已結束的填不了，
     * 兩者留在清單裡都會給出一顆點下去是死路的「前往作答」；
     * 已經答過的點進去填完，送出時才會撞上一人一份的限制。
     */
    const answerable: Survey[] = [];

    // 每次重抓都要先歸零，否則按重整會一直往上加
    this.surveysCount = 0;
    this.draftCount = 0;
    this.notStartCount = 0;
    this.activeCount = 0;
    this.endedCount = 0;

    this.mySurveysCount = 0;
    this.myDraftCount = 0;
    this.myNotStartCount = 0;
    this.myActiveCount = 0;
    this.myEndedCount = 0;

    // 兩組計數在同一輪迴圈裡算完，statusAdj 每份問卷只算一次
    for (let i = 0; i < this.survey.length; i++) {
      const survey = this.survey[i];
      const status = statusAdj(survey);

      let isMine = false;
      if (myEmail && survey.creatorEmail === myEmail) {
        isMine = true;
      }

      this.surveysCount++;
      if (isMine) {
        this.mySurveysCount++;
      }

      if (status === '儲存中') {
        this.draftCount++;
        if (isMine) {
          this.myDraftCount++;
        }
      } else if (status === '尚未開始') {
        this.notStartCount++;
        if (isMine) {
          this.myNotStartCount++;
        }
      } else if (status === '進行中') {
        /*
         * activeCount 不扣掉已作答的 —— 那張卡問的是
         * 「系統裡有幾份問卷正在進行」，是問卷的狀態，
         * 跟我答過沒有無關。扣掉的話兩個人會看到不同的數字。
         */
        this.activeCount++;
        if (!answeredIds.has(survey.id)) {
          answerable.push(survey);
        }
        if (isMine) {
          this.myActiveCount++;
        }
      } else if (status === '已結束') {
        this.endedCount++;
        if (isMine) {
          this.myEndedCount++;
        }
      }
    }

    // 清單與數字用同一份資料，才不會出現「尚未作答 43 份」但只列出 5 筆
    this.dataSource.data = answerable;
    this.quantity = answerable.length;
  }
}
