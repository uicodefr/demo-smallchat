import React from 'react';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';

interface Props {
  yesLabel?: string,
  noLabel?: string,
  confirmTitle: string,
  confirmLabel: string,
  detailLabel: string,
  show: boolean
  onConfirm: (confirm: boolean) => void;
}

interface State {
}

export class ConfirmDialogCommon extends React.Component<Props, State> {

  static defaultProps = {
    yesLabel: 'Yes',
    noLabel: 'No'
  }

  constructor(props: Props) {
    super(props);
    this.handleConfirm = this.handleConfirm.bind(this);
  }

  handleConfirm(confirm: boolean) {
    if (this.props.onConfirm) {
      this.props.onConfirm(confirm);
    }
  }

  render() {
    return (
      <Modal show={this.props.show} onHide={() => this.handleConfirm(false)}>
        <Modal.Header>
          <Modal.Title>{this.props.confirmTitle}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {this.props.confirmLabel}
          <br />
          <em>{this.props.detailLabel}</em>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => this.handleConfirm(false)}>
            {this.props.noLabel}
          </Button>
          <Button variant="primary" onClick={() => this.handleConfirm(true)}>
            {this.props.yesLabel}
          </Button>
        </Modal.Footer>
      </Modal>
    );
  }

}