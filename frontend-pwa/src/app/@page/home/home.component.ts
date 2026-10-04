import {Component, inject} from '@angular/core';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {CurrentUserService} from '../../@service/current-user.service';

@Component({
  selector: 'app-home',
  imports: [RouterLinkActive, RouterOutlet, RouterLink],
  templateUrl: './home.component.html',
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
      path: '/home/dashboard',
    },
    {
      title: '問卷清單',
      path: '/home/survey-menu',
    },
  ];
}
