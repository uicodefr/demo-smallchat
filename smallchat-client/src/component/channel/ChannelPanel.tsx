import React from 'react';
import Jumbotron from 'react-bootstrap/Jumbotron';
import './ChannelPanel.scss';
import Spinner from 'react-bootstrap/Spinner';
import { ChatService } from '../../service/chat/chat.service';
import { ChannelFullModel } from '../../model/channel/channel-full.model';
import Button from 'react-bootstrap/Button';
import { Formik, FormikValues } from 'formik';
import Form from 'react-bootstrap/Form';
import { Subscription } from 'rxjs';
import { ChatMessage } from './message/ChatMessage';
import { Redirect } from 'react-router-dom';
import { myDi } from '../../util/my-di';

interface Props {
  channelId: string;
}
interface State {
  loading: boolean;
  channel: ChannelFullModel | null;
  redirectAfterDisconnect: boolean;
}

export class ChannelPanel extends React.Component<Props, State> {
  private chatService: ChatService;
  private channelSubscription: Subscription;

  private channelShowBottomRef = React.createRef<HTMLDivElement>();

  constructor(props: Props) {
    super(props);

    this.chatService = myDi.get('ChatService');

    this.state = {
      loading: true,
      channel: null,
      redirectAfterDisconnect: false,
    };

    this.cleanUnreadMessages = this.cleanUnreadMessages.bind(this);
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
    if (this.channelShowBottomRef?.current?.scrollIntoView) {
      this.channelShowBottomRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }

  private loadChannel() {
    if (!this.props.channelId) {
      return;
    }

    const channelId = this.props.channelId;
    this.setState({
      loading: true,
    });

    if (this.channelSubscription) {
      this.channelSubscription.unsubscribe();
    }

    this.channelSubscription = this.chatService.getChannelObservable(channelId).subscribe(
      (channel) => {
        this.setState({
          loading: false,
          channel: channel,
        });
      },
      (error) => {
        this.setState({
          loading: false,
          channel: null,
        });
      }
    );

    this.chatService.connectToChannel(channelId);
  }

  public cleanUnreadMessages() {
    this.chatService.cleanUnreadMessages(this.props.channelId);
  }

  public handleSendSubmit(
    formSendValues: FormikValues,
    options: { setFieldValue: (field: string, value: any, shouldValidate?: boolean) => void }
  ) {
    const message = formSendValues['messageTxt'].trim();
    if (!this.state.channel || this.state.channel.deleted || !message) {
      return;
    }

    this.chatService.sendMessage(this.state.channel.id, message);
    this.cleanUnreadMessages();
    options.setFieldValue('messageTxt', '');
  }

  public handleDisconnect(event: React.MouseEvent) {
    if (!this.state.channel || this.state.channel.deleted) {
      this.setState({ redirectAfterDisconnect: true });
      return;
    }
    this.chatService.disconnectToChannel(this.props.channelId).then(() => {
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
                <Button variant="secondary" size="sm" title="Leave channel" onClick={this.handleDisconnect}>
                  Leave
                </Button>
              </span>
              <h1 className="channelName"> {this.state.channel.name} </h1>
              <span className="channelDescription"> {this.state.channel.description} </span>
            </div>

            <div className="channelShow">
              {this.state.channel.messages.map((message) => (
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
                          onClick={this.cleanUnreadMessages}
                          data-testid="message-input"
                        />
                      </Form.Group>
                    </div>

                    <div className="buttonZone">
                      <Button variant="primary" type="submit" disabled={disabledForm} data-testid="send-button">
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
