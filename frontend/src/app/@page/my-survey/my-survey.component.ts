import {AfterViewInit, Component, inject, OnInit, QueryList, ViewChildren} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatTabsModule} from '@angular/material/tabs';
import {MatDialog} from '@angular/material/dialog';
import {RouterLink} from '@angular/router';
import {SurveyApiService} from '../../@service/survey-api.service';
import {CurrentUserService} from '../../@service/current-user.service';
import {ReleaseComponent} from '../release/release.component';
import {statusAdj, Survey} from '../../@model/survey-model';

@Component({
  selector: 'app-my-survey',
  imports: [
    CommonModule,
    MatTabsModule,
    MatPaginatorModule,
    MatTableModule,
    RouterLink,
  ],
  standalone: true,
  templateUrl: './my-survey.component.html',
  styleUrl: './my-survey.component.scss'
})
export class MySurveyComponent implements OnInit, AfterViewInit {

  surveyApiService = inject(SurveyApiService);
  currentUser = inject(CurrentUserService);
  dialog = inject(MatDialog);

  // 篩掉別人的問卷之後，四個 tab 空掉的原因會有兩種：
  // 「你還沒建過」與「根本沒載到資料」。沒有這個欄位就分不出來。
  loadError = '';

  displayedColumns: string[] = ['id', 'name', 'updateAt', 'actions'];
  underwayTitle: string [] = ['id', 'name', 'star', 'end'];
  endedColumns: string[] = ['id', 'name', 'star', 'end', 'result'];

  selectMode = false;
  selectIds = new Set<number>();

  pendingDelete = false;   // 已按下刪除，等待二次確認
  isDeleting = false;      // 刪除中，擋連點
  deleteError = '';

  saving = new MatTableDataSource<Survey>();
  underway = new MatTableDataSource<Survey>();
  comingEnd = new MatTableDataSource<Survey>();
  endSurvey = new MatTableDataSource<Survey>();

  // 四個 tab 各一個分頁器，順序即 template 中出現的順序
  @ViewChildren(MatPaginator) paginators!: QueryList<MatPaginator>;

  ngAfterViewInit() {
    const [p1, p2, p3, p4] =
      this.paginators.toArray();
    this.saving.paginator = p1;
    this.underway.paginator = p2;
    this.comingEnd.paginator = p3;
    this.endSurvey.paginator = p4;
  }

  ngOnInit() {
    this.upData();
  }


  openRelease(id: number) {
    /*
     * 原本是 window.open 開新分頁，改成 Material Dialog。
     *
     * sessionStorage 每個分頁各存一份，新分頁拿不到 currentUser，
     * release 那條路由上的 authGuard 會把它彈回登入頁 —— 每次都會。
     *
     * 換成 dialog 之後還少了兩個毛病：
     * 不用 setInterval 每 0.5 秒問「視窗關了沒」（使用者不關就一直跑，
     * 元件銷毀時也沒人 clearInterval），改由 afterClosed() 主動通知。
     */
    const dialogRef = this.dialog.open(ReleaseComponent, {
      data: {id},
      width: '600px',
      maxHeight: '80vh',
    });

    dialogRef.afterClosed().subscribe((published) => {
      // 只有發佈成功才帶 true 回來；按取消不重抓，省一次請求
      if (published) {
        this.upData();
      }
    });
  }

  toggleSelectMode() {
    this.selectMode = true;
  }

  exitSelectMode() {
    this.selectMode = false;
    this.selectIds.clear();
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

  /**
   * 按下確認鍵先跳出確認條，不直接刪。
   * 刪除是不可逆的，多選時使用者不見得記得自己勾了哪幾份 —— 要有機會反悔。
   */
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

    this.surveyApiService.deleteSurvey(Array.from(this.selectIds)).subscribe({
      next: () => {
        this.isDeleting = false;
        this.pendingDelete = false;
        this.exitSelectMode();
        this.upData();
      },
      error: (err) => {
        this.isDeleting = false;
        // 失敗時不退出選取模式 —— 使用者的勾選要留著，才能直接重試
        this.deleteError = err.error?.message ?? '刪除失敗，請稍後再試。';
      },
    });
  }

  upData() {
    this.loadError = '';

    const myEmail = this.currentUser.get()?.email ?? '';
    if (!myEmail) {
      // 沒有 email 就無從比對，這時候把全部問卷列出來是錯的
      this.loadError = '找不到登入資訊，請重新登入後再試。';
      this.saving.data = [];
      this.underway.data = [];
      this.comingEnd.data = [];
      this.endSurvey.data = [];
      return;
    }

    this.surveyApiService.getAllSurveys().subscribe({
      next: (res) => {
        const savingRows: Survey[] = [];
        const underwayRows: Survey[] = [];
        const comingEndRows: Survey[] = [];
        const endedRows: Survey[] = [];

        for (let i = 0; i < res.length; i++) {
          const survey = res[i];
          if (survey.creatorEmail !== myEmail) {
            continue;              // 別人建的，四個 tab 都不該出現
          }

          const status = statusAdj(survey);
          if (status === '儲存中') {
            savingRows.push(survey);
          } else if (status === '尚未開始') {
            underwayRows.push(survey);
          } else if (status === '進行中') {
            comingEndRows.push(survey);
          } else if (status === '已結束') {
            endedRows.push(survey);
          }
        }

        this.saving.data = savingRows;
        this.underway.data = underwayRows;
        this.comingEnd.data = comingEndRows;
        this.endSurvey.data = endedRows;
      },
      error: (err) => {
        this.loadError = err.error?.message ?? '讀不到問卷資料，請確認伺服器是否啟動。';
      }
    })
  }
}
