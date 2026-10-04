import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {CurrentUserService} from '../@service/current-user.service';

export const authGuard: CanActivateFn = (route
  , state) => {

  const currentUserService = inject(CurrentUserService);
  const router = inject(Router);
  const user = currentUserService.get();
  if (!user) {
    return router.createUrlTree(['/'])
  }
  return true;
};
