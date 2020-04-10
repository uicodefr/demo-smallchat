import { UrlConstant } from '../../const/url-constant';
import { UserModel } from '../../model/global/user.model';
import { Observable, BehaviorSubject } from 'rxjs';
import { RestClientService } from '../util/rest-client.service';
import axios from 'axios';
import { ChannelService } from '../chat/channel.service';
import { myDi } from '../../util/my-di';
import { ChatService } from '../chat/chat.service';

export class AuthenticationService {
  private restClientService: RestClientService;
  private channelService: ChannelService;

  private userSubject = new BehaviorSubject<UserModel | null>(null);

  public constructor() {
    this.restClientService = myDi.get(RestClientService);
    this.channelService = myDi.get(ChannelService);
  }

  public login(username: string, password: string): Promise<UserModel | null> {
    let formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);

    return axios
      .post(UrlConstant.LOGIN, formData)
      .then((response) => {
        let user = null;
        if (response.status === 200) {
          user = response.data;
          myDi.get(ChatService).connectWebSocket();
        }
        this.userSubject.next(user);
        return user;
      })
      .catch((error) => {
        this.userSubject.next(null);
        return null;
      });
  }

  public logout(): Promise<void> {
    return new Promise((resolve, reject) => {
      axios.post(UrlConstant.LOGOUT, null).finally(() => {
        this.userSubject.next(null);
        resolve();
      });
    });
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
