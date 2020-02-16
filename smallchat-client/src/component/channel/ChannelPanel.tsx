import React from 'react';
import Jumbotron from 'react-bootstrap/Jumbotron';
import './ChannelPanel.scss';
import Spinner from 'react-bootstrap/Spinner';
import { ChannelService } from '../../service/chat/channel.service';
import { WebSocketService } from '../../service/chat/websocket.service';
import { ChannelFullModel } from '../../model/channel/channel-full.model';
import Button from 'react-bootstrap/Button';
import { Formik, FormikValues } from 'formik';
import Form from 'react-bootstrap/Form';
import { WebsocketMsgModel } from '../../model/chat/websocket-msg.model';

interface Props {
  channelId: string;
}
interface State {
  loading: boolean;
  channel: ChannelFullModel;
}

export class ChannelPanel extends React.Component<Props, State> {
  private channelService: ChannelService;
  private webSocketService: WebSocketService;

  constructor(props: Props) {
    super(props);

    this.channelService = ChannelService.get();
    this.webSocketService = WebSocketService.get();

    this.state = {
      loading: true,
      channel: null
    };

    this.handleSubmitSend = this.handleSubmitSend.bind(this);
  }

  componentDidMount() {
    this.loadChannel();
  }

  componentDidUpdate(prevProps) {
    if (this.props.channelId !== prevProps.channelId) {
      this.loadChannel();
    }
  }

  private loadChannel() {
    const channelId = this.props.channelId;
    this.setState({
      loading: true
    });

    this.channelService
      .getChannel(channelId)
      .then(channel => {
        this.setState({
          loading: false,
          channel: channel
        });
      })
      .catch(error => {
        this.setState({
          loading: false,
          channel: null
        });
      });
  }

  handleSubmitSend(formSendValues: FormikValues) {
    if (!this.state.channel) {
      return;
    }

    const channel = WebsocketMsgModel.CHANNEL_PREFIX + this.state.channel.id;
    this.webSocketService.sendMessage(channel, formSendValues['messageTxt']);
  }

  render() {
    const initValues = {};
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
              <h1> {this.state.channel.name} </h1>
              <span className="channelDescription"> {this.state.channel.description} </span>
            </div>
            <div className="channelShow">Messages Length : {this.state.channel.messages.length}</div>
            <div className="channelSend">
              <Formik onSubmit={this.handleSubmitSend} initialValues={initValues}>
                {({ values, errors, handleChange, handleBlur, handleSubmit }) => (
                  <Form onSubmit={handleSubmit}>
                    <Form.Group controlId="formMessageTxt">
                      <Form.Control
                        type="text"
                        name="messageTxt"
                        value={values.id}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        isInvalid={!!errors.id}
                      />
                    </Form.Group>

                    <Button variant="primary" type="submit">
                      SEND
                    </Button>
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
