import React from 'react';
import './App.scss';
import { Route, Switch } from 'react-router-dom';
import NotFound from './component/NotFound';
import { Channel } from './component/Channel';
import { Login } from './component/Login';
import { Menu } from './component/Menu';
import { AuthenticationService } from './service/auth/authentication.service';
import Spinner from 'react-bootstrap/Spinner';
import { ChatService } from './service/chat/chat.service';
import PrivateRoute from './component/shared/security/PrivateRoute';
import { myDi } from './util/my-di';

interface Props {}
interface State {
  init: boolean;
}

export class App extends React.Component<Props, State> {
  private authenticationService: AuthenticationService;
  private chatService: ChatService;

  constructor(props: Props) {
    super(props);

    this.authenticationService = myDi.get(AuthenticationService);
    this.chatService = myDi.get(ChatService);

    this.state = {
      init: false,
    };
  }

  componentDidMount() {
    this.authenticationService.loadUser().finally(() => {
      this.setState({
        init: true,
      });
      this.chatService.connectWebSocket();
    });
  }

  render() {
    if (!this.state.init) {
      return (
        <div id="App" className="init">
          <Spinner animation="grow" variant="primary" />
        </div>
      );
    }

    return (
      <div id="App">
        <Menu></Menu>
        <Switch>
          <Route exact path="/" component={Channel} />
          <PrivateRoute path="/c/:channelId" component={Channel} />
          <Route path="/signin" component={Login} />
          <Route component={NotFound} />
        </Switch>
        <div style={{ display: 'none' }}>version: '0.1.5-SNAPSHOT'</div>
      </div>
    );
  }
}
