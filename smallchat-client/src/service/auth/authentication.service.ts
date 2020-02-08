import { UrlConstant } from '../../const/url-constant';
import { UserModel } from '../../model/global/user.model';
import { Observable, BehaviorSubject } from 'rxjs';
import { RestClientService } from '../util/rest-client.service';

export class AuthenticationService {
  private static readonly INSTANCE = new AuthenticationService();

  private restClientService: RestClientService;
  private userSubject = new BehaviorSubject<UserModel>(null);

  private constructor() {
    this.restClientService = RestClientService.get();
  }
  public static get(): AuthenticationService {
    return this.INSTANCE;
  }

  public login(username: string, password: string): Promise<UserModel> {
    let formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);

    const requestInit = { method: 'POST', body: formData } as RequestInit;
    return window
      .fetch(UrlConstant.LOGIN, requestInit)
      .then(response => {
        if (response.status !== 200) {
          this.userSubject.next(null);
          return null;
        } else {
          return response.json().then(object => {
            this.userSubject.next(object);
            return object;
          });
        }
      })
      .catch(error => {
        this.userSubject.next(null);
        return null;
      });
  }

  public logout(): Promise<void> {
    return new Promise((resolve, reject) => {
      const requestInit = { method: 'POST' } as RequestInit;
      window.fetch(UrlConstant.LOGOUT, requestInit).finally(() => {
        this.userSubject.next(null);
        resolve();
      });
    });
  }

  public getCurrentUser(): UserModel {
    return this.userSubject.getValue();
  }

  public getCurrentUserObservable(): Observable<UserModel> {
    return this.userSubject.asObservable();
  }

  public loadUser(): Promise<UserModel> {
    return this.restClientService.get<UserModel>(UrlConstant.User.CURRENT_USER).then(user => {
      this.userSubject.next(user);
      return user;
    });
  }
}
