import {Routes} from '@angular/router';

import {LoginComponent} from './@page/login/login.component';
import {RegisterComponent} from './@page/register/register.component';
import {ResetPasswordComponent} from './@page/reset-password/reset-password.component';
import {HomeComponent} from './@page/home/home.component';
import {DashboardComponent} from './@page/dashboard/dashboard.component';
import {SurveyMenuComponent} from './@page/survey-menu/survey-menu.component';
import {FillInComponent} from './@page/fill-in/fill-in.component';
import {authGuard} from './@guard/auth.guard';

export const routes: Routes = [
  {path: '', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'reset-password', component: ResetPasswordComponent},
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [authGuard],
    children: [
      {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
      {path: 'dashboard', component: DashboardComponent},
      {path: 'survey-menu', component: SurveyMenuComponent},
      {path: 'fill-in/:id', component: FillInComponent},
      // chart.js 很大，只有這頁用到 —— lazy load 讓它拆成獨立 chunk，不拖慢首次載入
      {
        path: 'statistics/:id',
        loadComponent: () => import('./@page/statistics/statistics.component').then(m => m.StatisticsComponent),
      },
    ],
  },
  {path: '**', redirectTo: ''},
];
