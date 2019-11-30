import { GlobalInfoContextType } from '../context/GlobalInfoContext';
import { ApiUtil } from '../util/api.util';
import { UserModel } from '../model/global/user.model';
import { UrlConstant } from '../const/url-constant';

export class UserApi {

  constructor(private globalInfoContext: GlobalInfoContextType) {}

  public login(username: string, password: string): Promise<UserModel> {
    let formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);

    const requestInit = { method: 'POST', body: formData } as RequestInit;
    return window
      .fetch(UrlConstant.LOGIN, requestInit)
      .then(response => {
        if (response.status !== 200) {
          return null;
        } else {
          return response.json().then(object => {
            return object;
          });
        }
      })
      .catch(error => {
        return null;
      });
  }

  public logout(): Promise<void> {
    return new Promise((resolve, reject) => {
      const requestInit = { method: 'POST' } as RequestInit;
      window.fetch(UrlConstant.LOGOUT, requestInit).finally(() => {
        resolve();
      });
    });
  }

  public getCurrentUser(): Promise<UserModel> {
    return new ApiUtil(this.globalInfoContext).get(
      UrlConstant.User.CURRENT_USER
    );
  }

}
