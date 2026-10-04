import {Routes} from '@angular/router';

import {LoginComponent} from './@page/login/login.component';
import {HomeComponent} from './@page/home/home.component';
import {DashBoardComponent} from './@page/dash-board/dash-board.component';
import {SurveyMenuComponent} from './@page/survey-menu/survey-menu.component';
import {MySurveyComponent} from './@page/my-survey/my-survey.component';
import {CustomSurveyComponent} from './@page/custom-survey/custom-survey.component';
import {StatisticsComponent} from './@page/statistics/statistics.component';
import {FillInComponent} from './@page/fill-in/fill-in.component';
import {EditComponent} from './@page/edit/edit.component';
import {RegisterComponent} from './@page/register/register.component';
import {ResetPasswordComponent} from './@page/reset-password/reset-password.component';
import {authGuard} from './@guard/auth.guard';
import {adminGuard} from './@guard/admin.guard';
import {FrontHomeComponent} from './front/@page/front-home/front-home.component';
import {FrontSurveymenuComponent} from './front/@page/front-surveymenu/front-surveymenu.component';
import {FrontDashboardComponent} from './front/@page/front-dashboard/front-dashboard.component';


export const routes: Routes = [
  {
    path: '',
    component: LoginComponent,
  },
  {path: 'register', component: RegisterComponent},
  {path: 'reset-password', component: ResetPasswordComponent},
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [authGuard],
    children: [
      {path: '', redirectTo: 'dashBoard', pathMatch: 'full'},
      {path: 'dashBoard', component: DashBoardComponent, canActivate: [adminGuard]},
      {
        path: 'surveyMenu', component: SurveyMenuComponent, canActivate: [adminGuard]
      },
      {path: 'mySurvey', component: MySurveyComponent, canActivate: [adminGuard]},
      {path: 'customSurvey', component: CustomSurveyComponent, canActivate: [adminGuard]},
      {path: 'statistics/:id', component: StatisticsComponent, canActivate: [adminGuard]},
      {path: 'fillIn/:id', component: FillInComponent},
      {path: 'edit/:id', component: EditComponent, canActivate: [adminGuard]}
    ],
  },
  {
    path: 'front-home', component: FrontHomeComponent,
    canActivate: [authGuard],
    children: [
      // adminGuard 把 USER 彈回來時只會給 /front-home，沒有這條就是一片空白
      {path: '', redirectTo: 'front-dashboard', pathMatch: 'full'},
      {path: 'front-surveymenu', component: FrontSurveymenuComponent},
      {path: 'front-dashboard', component: FrontDashboardComponent},
      {path: 'statistics/:id', component: StatisticsComponent}
    ]
  },
  // release 不再有自己的路由 —— 它改成 my-survey 開的 Material Dialog
];
