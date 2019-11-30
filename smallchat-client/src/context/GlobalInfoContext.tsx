import React from 'react';
import { AlertType } from '../const/alert-type.const';
import { AlertModel } from '../model/global/alert.model';
import { GlobalConstant } from '../const/global-constant';

export interface GlobalInfoContextType {
  notifLoader(displayLoader: boolean);
  showAlert(alertType: AlertType, message: string, duration?: number);
}

export interface GlobalInfoDisplayContextType {
  displayLoader: boolean;
  alertList: Array<AlertModel>;
  dismisAlert(alertModel: AlertModel);
}

export const GlobalInfoContext = React.createContext({
  notifLoader: (displayLoader) => { },
  showAlert: (alertType: AlertType, message: string, duration?: number) => { }
} as GlobalInfoContextType);

export const GlobalInfoDisplayContext = React.createContext({
  displayLoader: false,
  alertList: [],
  dismisAlert: (alertModel: AlertModel) => { }
});

interface Props {
}

interface State {
  notify: GlobalInfoContextType
  display: GlobalInfoDisplayContextType
}

export class GlobalInfoContextComponent extends React.Component<Props, State> {

  constructor(props) {
    super(props);

    this.state = {
      notify: {
        notifLoader: this.notifLoader.bind(this),
        showAlert: this.showAlert.bind(this)
      },
      display: {
        displayLoader: false,
        alertList: [],
        dismisAlert: this.dismisAlert.bind(this)
      }
    }
  }

  notifLoader(displayLoader) {
    this.setState((state) => {
      return { display: { ...state.display, displayLoader: displayLoader } }
    });
  }

  showAlert(alertType: AlertType, message: string, duration?: number) {
    if (isNaN(duration)) {
      duration = GlobalConstant.NOTIFICATION_DELAY;
    }

    const alert = {
      alertType: alertType,
      message: message
    } as AlertModel;

    this.setState((state) => {
      state.display.alertList.push(alert);
      return { display: { ...state.display } };
    });

    if (duration > 0) {
      window.setTimeout(() => {
        this.dismisAlert(alert);
      }, duration);
    }
  }

  dismisAlert(alertModel: AlertModel) {
    this.setState((state) => {
      const index = state.display.alertList.indexOf(alertModel);
      if (index > -1) {
        state.display.alertList.splice(index, 1);
      }
      return { display: { ...state.display } };
    });
  }

  render() {
    return (
      <GlobalInfoContext.Provider value={this.state.notify}>
        <GlobalInfoDisplayContext.Provider value={this.state.display}>
          {this.props.children}
        </GlobalInfoDisplayContext.Provider>
      </GlobalInfoContext.Provider>
    );
  }
}