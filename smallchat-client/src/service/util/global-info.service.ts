import { GlobalConstant } from '../../const/global-constant';
import { AlertType } from '../../const/alert-type.const';
import { AlertModel } from '../../model/global/alert.model';
import { Observable, Subject } from 'rxjs';

export class GlobalInfoService {
  private loaderSubject = new Subject<boolean>();
  private alertSubject = new Subject<AlertModel>();

  public getLoaderObservable(): Observable<boolean> {
    return this.loaderSubject.asObservable();
  }

  public getAlertObservable(): Observable<AlertModel> {
    return this.alertSubject.asObservable();
  }

  public notifLoader(displayLoader: boolean) {
    this.loaderSubject.next(displayLoader);
  }

  public showAlert(alertType: AlertType, message: string, duration?: number) {
    if (!duration || isNaN(duration)) {
      duration = GlobalConstant.NOTIFICATION_DELAY;
    }

    const alert = {
      alertType: alertType,
      message: message,
      duration: duration,
    } as AlertModel;

    this.alertSubject.next(alert);
  }
}
