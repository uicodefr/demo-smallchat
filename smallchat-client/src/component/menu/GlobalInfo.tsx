import React from 'react';
import './GlobalInfo.scss';
import { withContext } from '../../util/hoc.util';
import { GlobalInfoDisplayContext, GlobalInfoDisplayContextType } from '../../context/GlobalInfoContext';
import Spinner from 'react-bootstrap/Spinner';
import Alert from 'react-bootstrap/Alert'
import { AlertType } from '../../const/alert-type.const';
import { AlertModel } from '../../model/global/alert.model';

interface Props {
  displayContext: GlobalInfoDisplayContextType
}
interface State {
}

class GlobalInfo extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.closeAlert = this.closeAlert.bind(this);
  }

  closeAlert(alert: AlertModel) {
    this.props.displayContext.dismisAlert(alert);
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
        {this.props.displayContext.displayLoader &&
          <Spinner className="loadingSpinner" animation="border" role="status">
            <span className="sr-only">Loading...</span>
          </Spinner>
        }

        <div className="alertZone">
          {this.props.displayContext.alertList.map((alert, idx) =>
            <Alert className="alert" key={idx} variant={this.getAlertVariant(alert.alertType)}
              onClose={() => this.closeAlert(alert)} dismissible>
              {alert.message}
            </Alert>
          )}
        </div>
      </div>
    );
  }

}

export default withContext(GlobalInfo, GlobalInfoDisplayContext, 'displayContext');
