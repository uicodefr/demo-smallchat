import { UrlConstant } from '../../const/url-constant';
import { UserModel } from '../../model/global/user.model';
import { Observable, BehaviorSubject } from 'rxjs';
import { RestClientService } from '../util/rest-client.service';
import { myDi } from '../../util/my-di';
import { ChatService } from '../chat/chat.service';

export class AuthenticationService {
  private restClientService: RestClientService;

  private userSubject = new BehaviorSubject<UserModel | null>(null);

  public constructor() {
    this.restClientService = myDi.get('RestClientService');
  }

  public login(username: string, password: string): Promise<UserModel | null> {
    return this.restClientService.login(username, password).then((user) => {
      if (user) {
        myDi.get<ChatService>('ChatService').connectWebSocket();
      }
      this.userSubject.next(user);
      return user;
    });
  }

  public logout(): Promise<void> {
    return this.restClientService.logout().finally(() => this.userSubject.next(null));
  }

  public getCurrentUser(): UserModel | null {
    return this.userSubject.getValue();
  }

  public getCurrentUserObservable(): Observable<UserModel | null> {
    return this.userSubject.asObservable();
  }

  public loadUser(): Promise<UserModel> {
    return this.restClientService.get<UserModel>(UrlConstant.User.CURRENT_USER).then((user) => {
      this.userSubject.next(user);
      return user;
    });
  }
}
