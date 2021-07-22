import axios, { AxiosInstance } from 'axios';
import { AlertType } from '../../const/alert-type.const';
import { GlobalInfoService } from './global-info.service';
import { myDi } from '../../util/my-di';
import { UserModel } from '../../model/global/user.model';
import { UrlConstant } from '../../const/url-constant';

export class RestClientService {
  private axiosInstance: AxiosInstance;

  private globalInfoService: GlobalInfoService;

  public constructor() {
    this.globalInfoService = myDi.get('GlobalInfoService');
    this.axiosInstance = axios.create();

    this.axiosInstance.interceptors.request.use(
      (config) => {
        this.globalInfoService.notifLoader(true);
        config.headers = {
          'content-type': 'application/json',
        };
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    this.axiosInstance.interceptors.response.use(
      (response) => {
        this.globalInfoService.notifLoader(false);
        return response;
      },
      (error) => {
        this.globalInfoService.notifLoader(false);
        this.globalInfoService.showAlert(AlertType.DANGER, '' + error, 0);
        return Promise.reject(error);
      }
    );
  }

  public get<T>(url: string): Promise<T> {
    return this.axiosInstance.get(url).then((response) => response.data);
  }

  public post<T>(url: string, body: object | null): Promise<T> {
    return this.axiosInstance.post(url, body).then((response) => response.data);
  }

  public patch<T>(url: string, body: object | null): Promise<T> {
    return this.axiosInstance.patch(url, body).then((response) => response.data);
  }

  public delete<T>(url: string): Promise<T> {
    return this.axiosInstance.delete(url).then((response) => response.data);
  }

  public login(username: string, password: string): Promise<UserModel | null> {
    const formData = new FormData();
    formData.append('username', username);
    formData.append('password', password);

    return axios
      .post(UrlConstant.LOGIN, formData)
      .then((response) => {
        let user = null;
        if (response.status === 200) {
          user = response.data;
        }
        return user;
      })
      .catch((error) => {
        return null;
      });
  }

  public logout(): Promise<void> {
    return new Promise((resolve, reject) => {
      axios
        .post(UrlConstant.LOGOUT, null)
        .catch((error) => this.globalInfoService.showAlert(AlertType.DANGER, '' + error, 0))
        .finally(() => {
          resolve();
        });
    });
  }
}
