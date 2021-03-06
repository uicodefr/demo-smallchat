import React from 'react';
import './App.scss';
import { Route, Switch, BrowserRouter } from 'react-router-dom';
import { NotFound } from './component/NotFound';
import { Channel } from './component/Channel';
import { Login } from './component/Login';
import { Menu } from './component/Menu';
import { Info } from './component/Info';
import { AuthenticationService } from './service/auth/authentication.service';
import Spinner from 'react-bootstrap/Spinner';
import { ChatService } from './service/chat/chat.service';
import { PrivateRoute } from './component/shared/security/PrivateRoute';
import { myDi } from './util/my-di';
import { appInfo } from './app.info';

interface Props {}
interface State {
  init: boolean;
}

export class App extends React.Component<Props, State> {
  private chatService: ChatService;
  private authenticationService: AuthenticationService;
  private appVersion = appInfo.version;

  constructor(props: Props) {
    super(props);

    this.chatService = myDi.get('ChatService');
    this.authenticationService = myDi.get('AuthenticationService');

    this.state = {
      init: false,
    };
  }

  componentDidMount() {
    this.authenticationService.loadUser().finally(() => {
      this.setState({
        init: true,
      });
      this.chatService.connect();
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
      <BrowserRouter basename={`${process.env.PUBLIC_URL}/`}>
        <div id="App">
          <Menu></Menu>
          <Switch>
            <Route exact path="/" component={Channel} />
            <PrivateRoute path="/c/:channelId" component={Channel} />
            <Route path="/signin" component={Login} />
            <Route path="/info" component={Info} />
            <Route component={NotFound} />
          </Switch>
          <div style={{ display: 'none' }}>version: '{this.appVersion}'</div>
        </div>
      </BrowserRouter>
    );
  }
}
