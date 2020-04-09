import React from 'react';
import Jumbotron from 'react-bootstrap/Jumbotron';
import './ChannelPanel.scss';
import Spinner from 'react-bootstrap/Spinner';
import { WebSocketService } from '../../service/chat/websocket.service';
import { ChannelFullModel } from '../../model/channel/channel-full.model';
import Button from 'react-bootstrap/Button';
import { Formik, FormikValues } from 'formik';
import Form from 'react-bootstrap/Form';
import { Subscription } from 'rxjs';
import { ChatMessage } from './message/ChatMessage';
import { Redirect } from 'react-router-dom';

interface Props {
  channelId: string;
}
interface State {
  loading: boolean;
  channel: ChannelFullModel | null;
  redirectAfterDisconnect: boolean;
}

export class ChannelPanel extends React.Component<Props, State> {
  private webSocketService: WebSocketService;
  private channelSubscription: Subscription;

  private channelShowBottomRef = React.createRef<HTMLDivElement>();

  constructor(props: Props) {
    super(props);

    this.webSocketService = WebSocketService.get();

    this.state = {
      loading: true,
      channel: null,
      redirectAfterDisconnect: false
    };

    this.handleSendSubmit = this.handleSendSubmit.bind(this);
    this.handleDisconnect = this.handleDisconnect.bind(this);
  }

  public componentDidMount() {
    this.loadChannel();
    this.scrollChannelShowToBottom();
  }

  public componentWillUnmount() {
    if (this.channelSubscription) {
      this.channelSubscription.unsubscribe();
    }
  }

  public componentDidUpdate(prevProps: Props) {
    if (this.props.channelId !== prevProps.channelId) {
      this.loadChannel();
    }
    this.scrollChannelShowToBottom();
  }

  private scrollChannelShowToBottom() {
    if (this.channelShowBottomRef.current) {
      this.channelShowBottomRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }

  private loadChannel() {
    if (!this.props.channelId) {
      return;
    }

    const channelId = this.props.channelId;
    this.setState({
      loading: true
    });

    if (this.channelSubscription) {
      this.channelSubscription.unsubscribe();
    }

    this.channelSubscription = this.webSocketService.getChannelObservable(channelId).subscribe(
      channel => {
        this.setState({
          loading: false,
          channel: channel
        });
      },
      error => {
        this.setState({
          loading: false,
          channel: null
        });
      }
    );
    this.webSocketService.connectToChannel(channelId);
  }

  public handleSendSubmit(
    formSendValues: FormikValues,
    options: { setFieldValue: (field: string, value: any, shouldValidate?: boolean) => void }
  ) {
    const message = formSendValues['messageTxt'].trim();
    if (!this.state.channel || this.state.channel.deleted || !message) {
      return;
    }

    this.webSocketService.sendMessage(this.state.channel.id, message);
    options.setFieldValue('messageTxt', '');
  }

  public handleDisconnect(event: React.MouseEvent) {
    if (!this.state.channel || this.state.channel.deleted) {
      this.setState({ redirectAfterDisconnect: true });
      return;
    }
    this.webSocketService.disconnectToChannel(this.props.channelId).then(() => {
      this.setState({ redirectAfterDisconnect: true });
    });
  }

  public render() {
    if (this.state.redirectAfterDisconnect) {
      return <Redirect to="/" />;
    }

    const initValues = { messageTxt: '' };
    const disabledForm = this.state.channel?.deleted === true;
    return (
      <Jumbotron className="ChannelPanel">
        {this.state.loading ? (
          <div className="loading">
            <Spinner animation="border" variant="secondary" />
          </div>
        ) : !this.state.channel ? (
          <div className="noChannel">No Channel for the id "{this.props.channelId}"</div>
        ) : (
          <>
            <div className="channelHeader">
              <span className="channelDisconnect">
                <Button variant="warning" size="sm" title="Leave channel" onClick={this.handleDisconnect}>
                  Leave
                </Button>
              </span>
              <h1 className="channelName"> {this.state.channel.name} </h1>
              <span className="channelDescription"> {this.state.channel.description} </span>
            </div>

            <div className="channelShow">
              {this.state.channel.messages.map(message => (
                <ChatMessage key={message.id} message={message} />
              ))}
              <div ref={this.channelShowBottomRef}></div>
            </div>

            <div className="channelSend">
              <Formik enableReinitialize onSubmit={this.handleSendSubmit} initialValues={initValues}>
                {({ values, errors, handleChange, handleBlur, handleSubmit }) => (
                  <Form className="formMessage" onSubmit={handleSubmit}>
                    <div className="textZone">
                      <Form.Group controlId="formMessageTxt">
                        <Form.Control
                          type="text"
                          autoComplete="off"
                          name="messageTxt"
                          value={values.messageTxt}
                          onChange={handleChange}
                          onBlur={handleBlur}
                          isInvalid={!!errors.messageTxt}
                          disabled={disabledForm}
                        />
                      </Form.Group>
                    </div>

                    <div className="buttonZone">
                      <Button variant="primary" type="submit" disabled={disabledForm}>
                        Send
                      </Button>
                    </div>
                  </Form>
                )}
              </Formik>
            </div>
          </>
        )}
      </Jumbotron>
    );
  }
}
