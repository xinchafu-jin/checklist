import {Component, inject} from '@angular/core';
import {MatToolbarModule} from '@angular/material/toolbar';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import {MatMenuModule} from
    '@angular/material/menu';
import {MatButtonModule} from
    '@angular/material/button';
import {CurrentUserService} from '../../@service/current-user.service';

@Component({
  selector: 'app-home',
  imports: [
    RouterLinkActive,
    RouterOutlet,
    RouterLink,
    MatToolbarModule,
    MatMenuModule, MatButtonModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  router = inject(Router);
  currentUserService = inject(CurrentUserService);

  logout() {
    this.currentUserService.clear();
    this.router.navigate(['/']);
  }

  navTitle = [
    {
      title: '我的問卷',
      path: '/home/mySurvey',
    },
    {
      title: '問卷清單',
      path: '/home/surveyMenu',
    },
  ];
}
