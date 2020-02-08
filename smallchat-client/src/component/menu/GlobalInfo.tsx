import React from 'react';
import './GlobalInfo.scss';
import Spinner from 'react-bootstrap/Spinner';
import Alert from 'react-bootstrap/Alert';
import { AlertType } from '../../const/alert-type.const';
import { AlertModel } from '../../model/global/alert.model';
import { GlobalInfoService } from '../../service/util/global-info.service';
import { Subscription } from 'rxjs';
import { TransitionGroup, CSSTransition } from 'react-transition-group';

interface Props {}
interface State {
  displayLoader: boolean;
  alerts: Array<AlertModel>;
}

export class GlobalInfo extends React.Component<Props, State> {
  private globalInfoService: GlobalInfoService;

  private loaderSubscription: Subscription;
  private alertSubscription: Subscription;

  constructor(props: Props) {
    super(props);
    this.state = {
      displayLoader: false,
      alerts: []
    };

    this.globalInfoService = GlobalInfoService.get();

    this.closeAlert = this.closeAlert.bind(this);
  }

  componentDidMount() {
    this.loaderSubscription = this.globalInfoService.getLoaderObservable().subscribe(displayLoader => {
      this.setState({
        displayLoader: displayLoader
      });
    });

    this.alertSubscription = this.globalInfoService.getAlertObservable().subscribe(alert => {
      const newAlerts = [...this.state.alerts, alert];
      this.setState({
        alerts: newAlerts
      });

      if (alert.duration && alert.duration > 0) {
        window.setTimeout(() => {
          this.closeAlert(alert);
        }, alert.duration);
      }
    });
  }

  componentWillUnmount() {
    if (this.loaderSubscription) {
      this.loaderSubscription.unsubscribe();
    }
    if (this.alertSubscription) {
      this.alertSubscription.unsubscribe();
    }
  }

  closeAlert(alert: AlertModel) {
    const newAlerts = [...this.state.alerts];
    const alertIndex = newAlerts.indexOf(alert);
    if (alertIndex > -1) {
      newAlerts.splice(alertIndex, 1);
      this.setState({
        alerts: newAlerts
      });
    }
  }

  private getAlertVariant(alertType: AlertType) {
    switch (alertType) {
      case AlertType.SUCCESS:
        return 'success';
      case AlertType.INFO:
        return 'info';
      case AlertType.WARNING:
        return 'warning';
      case AlertType.DANGER:
        return 'danger';
      default:
        return 'primary';
    }
  }

  render() {
    return (
      <div className="GlobalInfo">
        {this.state.displayLoader && (
          <Spinner className="loadingSpinner" animation="border" role="status">
            <span className="sr-only">Loading...</span>
          </Spinner>
        )}

        <div className="alertZone">
          <TransitionGroup>
            {this.state.alerts.map((alert, idx) => (
              <CSSTransition key={idx} timeout={300} classNames="transitionFade">
                <Alert
                  className="alert"
                  variant={this.getAlertVariant(alert.alertType)}
                  onClose={() => this.closeAlert(alert)}
                  dismissible
                >
                  {alert.message}
                </Alert>
              </CSSTransition>
            ))}
          </TransitionGroup>
        </div>
      </div>
    );
  }
}
