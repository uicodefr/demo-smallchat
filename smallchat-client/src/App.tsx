import React from 'react';
import './App.scss';
import { Route, Switch } from 'react-router-dom';
import NotFound from './component/NotFound';
import { Channel } from './component/Channel';
import { Login } from './component/Login';
import { Menu } from './component/Menu';
import { AuthenticationService } from './service/auth/authentication.service';
import Spinner from 'react-bootstrap/Spinner';
import { WebSocketService } from './service/chat/websocket.service';

interface Props {}
interface State {
  init: boolean;
}

export class App extends React.Component<Props, State> {
  private authenticationService: AuthenticationService;
  private webSocketService: WebSocketService;

  constructor(props: Props) {
    super(props);

    this.authenticationService = AuthenticationService.get();
    this.webSocketService = WebSocketService.get();

    this.state = {
      init: false
    };
  }

  componentDidMount() {
    this.authenticationService.loadUser().finally(() => {
      this.setState({
        init: true
      });
      this.webSocketService.connectWebSocket();
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
          <Route path="/c/:channelId" component={Channel} />
          <Route path="/login" component={Login} />
          <Route component={NotFound} />
        </Switch>
      </div>
    );
  }
}
