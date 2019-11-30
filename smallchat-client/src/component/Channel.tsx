import React from 'react';
import './Channel.scss';
import { UserContextType } from '../context/UserContext';
import { withAutoContext } from '../util/hoc.util';
import Card from 'react-bootstrap/Card';
import Jumbotron from 'react-bootstrap/Jumbotron';
import Button from 'react-bootstrap/Button';
import ListGroup from 'react-bootstrap/ListGroup'
import { LinkContainer } from 'react-router-bootstrap';
import { WebSocketContextType } from '../context/WebSocketContext';
import ChannelsCard from './channel/ChannelsCard';
import { match } from 'react-router-dom';
import { ChannelModel } from '../model/chat/channel.model';

interface Props {
  userContext: UserContextType,
  webSocketContext: WebSocketContextType
  match: match
}
interface State {
  channel: ChannelModel
}

class Channel extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      channel: null
    };
    this.loadChannel();
  }

  componentDidUpdate(prevProps) {
    if (this.props.match?.params['channelId'] !== prevProps.match?.params['channelId'] ||
      this.props.webSocketContext?.chatState !== prevProps.webSocketContext?.chatState) {
      this.loadChannel();
    }
  }

  private loadChannel() {
    const channelId = this.props.match?.params['channelId'];
    const chatStateChannels = this.props.webSocketContext?.chatState?.channels;
    if (!channelId || !chatStateChannels) {
      this.setState({
        channel: null
      });
    } else {
      const channel = this.props.webSocketContext.chatState.channels
        .find(channel => channel.id === channelId);
      this.setState({
        channel: channel
      });
    }
  }

  render() {
    const chatState = this.props.webSocketContext?.chatState;

    return (
      <div className="ChannelScreen">

        <div className="leftPanel">
          <ChannelsCard />

          <Card>
            <Card.Header>Users</Card.Header>
            <Card.Body>
              {chatState ? (
                <ListGroup variant="flush">
                  {chatState.users.map(user => 
                    <ListGroup.Item key={user.id} title={user.id}>
                      {user.pseudo}
                    </ListGroup.Item>
                  )}
                </ListGroup>
              ):(
                <p> - </p>
              )}
            </Card.Body>
          </Card>

        </div>

        <div className="mainPanel">
          {this.state.channel ? (
            <Jumbotron className="channelPanel">
              <div className="channelHeader">
                <span className="channelId"> {this.state.channel.id} </span>
                <h1> {this.state.channel.name} </h1>
                <span className="channelDescription"> {this.state.channel.description} </span>
              </div>
              <div className="channelShow">

              </div>
              <div className="channelSend">
              </div>
            </Jumbotron>
          ):(
            <Jumbotron className="helloPanel">
              <h1>Welcome !</h1>
              {this.props.userContext.currentUser ? (
                <p>
                  Select a channel or a user to the right to begin chatting with others.
                </p>
              ):(
                <>
                  <p>
                    Before sending messages to channels or to users, sign in.
                  </p>
                  <p>
                    <LinkContainer to="/login">
                      <Button variant="primary">Sign In</Button>
                    </LinkContainer>
                  </p>
                </>
              )}
            </Jumbotron>
          )}
        </div>
      </div>
    );
  }
}

export default withAutoContext(Channel, ['userContext', 'webSocketContext']);
