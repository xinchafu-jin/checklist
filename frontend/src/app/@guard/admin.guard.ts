import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {CurrentUserService} from '../@service/current-user.service';

/** 只允許 ADMIN 進入後台管理頁面。 */
export const adminGuard: CanActivateFn = () => {
  const currentUserService = inject(CurrentUserService);
  const router = inject(Router);

  return currentUserService.isAdmin()
    ? true
    : router.createUrlTree(['/front-home']);
};
