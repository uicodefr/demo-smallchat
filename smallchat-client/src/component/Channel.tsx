import React from 'react';
import './Channel.scss';
import Card from 'react-bootstrap/Card';
import Jumbotron from 'react-bootstrap/Jumbotron';
import Button from 'react-bootstrap/Button';
import ListGroup from 'react-bootstrap/ListGroup';
import { LinkContainer } from 'react-router-bootstrap';
import { match } from 'react-router-dom';
import { ChannelsCard } from './channel/ChannelsCard';
import { ChatStateModel } from '../model/chat/chat-state.model';
import { UserModel } from '../model/global/user.model';
import { WebSocketService } from '../service/chat/websocket.service';
import { AuthenticationService } from '../service/auth/authentication.service';
import { Subscription } from 'rxjs';
import { ChannelPanel } from './channel/ChannelPanel';

interface Props {
  match: match; // For Routing
}
interface State {
  chatState: ChatStateModel;
  currentUser: UserModel;
  selectedChannelId: string;
}

export class Channel extends React.Component<Props, State> {
  private webSocketService: WebSocketService;
  private authenticationService: AuthenticationService;

  private chatStateSubscription: Subscription;
  private currentUserSubscription: Subscription;

  constructor(props: Props) {
    super(props);

    this.webSocketService = WebSocketService.get();
    this.authenticationService = AuthenticationService.get();

    this.state = {
      chatState: this.webSocketService.getChatState(),
      currentUser: this.authenticationService.getCurrentUser(),
      selectedChannelId: this.props.match?.params['channelId']
    };
  }

  componentDidMount() {
    this.chatStateSubscription = this.webSocketService.getChatStateObservable().subscribe(chatState => {
      this.setState({ chatState: chatState });
    });
    this.currentUserSubscription = this.authenticationService.getCurrentUserObservable().subscribe(currentUser => {
      this.setState({ currentUser: currentUser });
    });
  }

  componentWillUnmount() {
    if (this.chatStateSubscription) {
      this.chatStateSubscription.unsubscribe();
    }
    if (this.currentUserSubscription) {
      this.currentUserSubscription.unsubscribe();
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.match?.params['channelId'] !== prevProps.match?.params['channelId']) {
      this.setState({
        selectedChannelId: this.props.match?.params['channelId']
      });
    }
  }

  render() {
    const chatState = this.state.chatState;

    return (
      <div className="ChannelScreen">
        <div className="leftPanel">
          <ChannelsCard />

          <Card>
            <Card.Header>Users</Card.Header>
            <Card.Body>
              {chatState ? (
                <ListGroup variant="flush" className="smallItem">
                  {chatState.users.map(user => (
                    <ListGroup.Item key={user.id} title={user.id}>
                      {user.pseudo}
                    </ListGroup.Item>
                  ))}
                </ListGroup>
              ) : (
                <p> - </p>
              )}
            </Card.Body>
          </Card>
        </div>

        <div className="mainPanel">
          {this.state.selectedChannelId ? (
            <ChannelPanel channelId={this.state.selectedChannelId} />
          ) : (
            <Jumbotron className="helloPanel">
              <h1>Welcome !</h1>
              {this.state.currentUser ? (
                <p>Select a channel or a user to the right to begin chatting with others.</p>
              ) : (
                <>
                  <p>Before sending messages to channels or to users, sign in.</p>
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
