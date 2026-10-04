import {Injectable} from '@angular/core';
import {UserResponse} from '../@model/survey-model';

@Injectable({
  providedIn: 'root'
})
export class CurrentUserService {
  private readonly KEY = 'currentUser';

  set(user: UserResponse) {
    sessionStorage.setItem(this.KEY, JSON.stringify(user));
  }

  get(): UserResponse | null {
    const raw = sessionStorage.getItem(this.KEY);
    if(!raw){
      return null;
    }
    return JSON.parse(raw);

  }

  clear() {
    sessionStorage.removeItem(this.KEY);
  }

  isAdmin():boolean{
    const user = this.get();
    if(!user){
    return false ;
    }
    return user.role ==='ADMIN';


}

}
