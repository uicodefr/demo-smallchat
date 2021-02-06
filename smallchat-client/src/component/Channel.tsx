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
import { ChatService } from '../service/chat/chat.service';
import { AuthenticationService } from '../service/auth/authentication.service';
import { Subscription } from 'rxjs';
import { ChannelPanel } from './channel/ChannelPanel';
import { myDi } from '../util/my-di';

interface Props {
  match: match<{ channelId: string }>; // For Routing
}
interface State {
  chatState: ChatStateModel | null;
  currentUser: UserModel | null;
  selectedChannelId: string;
}

export class Channel extends React.Component<Props, State> {
  private chatService: ChatService;
  private authenticationService: AuthenticationService;

  private chatStateSubscription: Subscription;
  private currentUserSubscription: Subscription;

  constructor(props: Props) {
    super(props);

    this.chatService = myDi.get('ChatService');
    this.authenticationService = myDi.get('AuthenticationService');

    this.state = {
      chatState: this.chatService.getChatState(),
      currentUser: this.authenticationService.getCurrentUser(),
      selectedChannelId: this.props.match?.params?.channelId,
    };
  }

  componentDidMount() {
    this.chatStateSubscription = this.chatService.getChatStateObservable().subscribe((chatState) => {
      this.setState({ chatState: chatState });
    });
    this.currentUserSubscription = this.authenticationService.getCurrentUserObservable().subscribe((currentUser) => {
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

  componentDidUpdate(prevProps: Props) {
    if (this.props.match?.params['channelId'] !== prevProps.match?.params['channelId']) {
      this.setState({
        selectedChannelId: this.props.match?.params['channelId'],
      });
    }
  }

  render() {
    const currentUser = this.state.currentUser;
    const chatState = this.state.chatState;

    return (
      <div className="ChannelScreen">
        <div className="leftPanel">
          <ChannelsCard />

          <Card>
            <Card.Header>
              <i className="fa fa-users mr-2"></i>
              Users
            </Card.Header>
            <Card.Body>
              {chatState ? (
                <ListGroup variant="flush" className="smallItem">
                  {chatState.users.map((user) => (
                    <ListGroup.Item key={user.id} title={user.id}>
                      {user.id === currentUser?.username ? <em>{user.pseudo}</em> : <>{user.pseudo}</>}
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
              <h1>Welcome {currentUser?.username} !</h1>
              {currentUser ? (
                <p>Select a channel to the right to begin chatting with others.</p>
              ) : (
                <>
                  <p>Before sending messages to channels or to users, sign in.</p>
                  <p>
                    <LinkContainer to="/signin">
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
