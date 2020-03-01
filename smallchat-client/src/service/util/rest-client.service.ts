import axios, { AxiosInstance } from 'axios';
import { AlertType } from '../../const/alert-type.const';
import { GlobalInfoService } from './global-info.service';

export class RestClientService {
  private static readonly INSTANCE = new RestClientService();

  private globalInfoService: GlobalInfoService = null;
  private axiosInstance: AxiosInstance = null;

  private constructor() {
    this.globalInfoService = GlobalInfoService.get();

    this.axiosInstance = axios.create();

    this.axiosInstance.interceptors.request.use(
      config => {
        this.globalInfoService.notifLoader(true);
        config.headers = {
          'content-type': 'application/json'
        };
        return config;
      },
      error => {
        return Promise.reject(error);
      }
    );

    this.axiosInstance.interceptors.response.use(
      response => {
        this.globalInfoService.notifLoader(false);
        return response;
      },
      error => {
        this.globalInfoService.notifLoader(false);
        this.globalInfoService.showAlert(AlertType.DANGER, '' + error, 0);
        return Promise.reject(error);
      }
    );
  }
  public static get(): RestClientService {
    return this.INSTANCE;
  }

  public get<T>(url: string): Promise<T> {
    return this.axiosInstance.get(url).then(response => response.data);
  }

  public post<T>(url: string, body: object): Promise<T> {
    return this.axiosInstance.post(url, body).then(response => response.data);
  }

  public patch<T>(url: string, body: object): Promise<T> {
    return this.axiosInstance.patch(url, body).then(response => response.data);
  }

  public delete<T>(url: string): Promise<T> {
    return this.axiosInstance.delete(url).then(response => response.data);
  }
}
