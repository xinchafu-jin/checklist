import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {CurrentUserService} from '../@service/current-user.service';

/**
 * 只允許 ADMIN 進入後台管理頁面。
 * 前台已拆到 frontend-pwa，USER 在這裡沒有頁面可去，一律送回登入頁。
 */
export const adminGuard: CanActivateFn = () => {
  const currentUserService = inject(CurrentUserService);
  const router = inject(Router);

  return currentUserService.isAdmin()
    ? true
    : router.createUrlTree(['/']);
};
