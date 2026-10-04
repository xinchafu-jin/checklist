import {Component, inject} from '@angular/core';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {CurrentUserService} from '../../../@service/current-user.service';
import {MatButton, MatButtonModule} from '@angular/material/button';
import {MatMenu, MatMenuItem, MatMenuModule} from '@angular/material/menu';
import {MatToolbar, MatToolbarModule} from '@angular/material/toolbar';

@Component({
  selector: 'app-front-home',
  imports: [
    RouterLinkActive,
    RouterOutlet,
    RouterLink,
    MatToolbarModule,
    MatMenuModule, MatButtonModule
  ],
  templateUrl: './front-home.component.html',
  styleUrl: './front-home.component.scss'
})
export class FrontHomeComponent {

  router = inject(Router);
  currentUserService = inject(CurrentUserService);

  logout() {
    this.currentUserService.clear();
    this.router.navigate(['/']);
  }

  navTitle = [
    {
      title: '我的問卷',
      path: '/front-home/front-dashboard',
    },
    {
      title: '問卷清單',
      path: '/front-home/front-surveymenu',
    },
  ];
}
