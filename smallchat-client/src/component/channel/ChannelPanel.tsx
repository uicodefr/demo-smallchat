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

interface Props {
  channelId: string;
}
interface State {
  loading: boolean;
  channel: ChannelFullModel;
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
      channel: null
    };

    this.handleSendSubmit = this.handleSendSubmit.bind(this);
  }

  componentDidMount() {
    this.loadChannel();
    this.scrollChannelShowToBottom();
  }

  componentWillUnmount() {
    if (this.channelSubscription) {
      this.channelSubscription.unsubscribe();
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.channelId !== prevProps.channelId) {
      this.loadChannel();
    }
    this.scrollChannelShowToBottom();
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

  handleSendSubmit(formSendValues: FormikValues, { setFieldValue }) {
    const message = formSendValues['messageTxt'].trim();
    if (!this.state.channel || !message) {
      return;
    }

    this.webSocketService.sendMessage(this.state.channel.id, message);
    setFieldValue('messageTxt', '');
  }

  scrollChannelShowToBottom() {
    if (this.channelShowBottomRef.current) {
      this.channelShowBottomRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }

  render() {
    const initValues = { messageTxt: '' };
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
              <span className="channelId"> {this.state.channel.id} </span>
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
                          isInvalid={!!errors.id}
                        />
                      </Form.Group>
                    </div>

                    <div className="buttonZone">
                      <Button variant="primary" type="submit">
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
