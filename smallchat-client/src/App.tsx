import React from 'react';
import './App.scss';
import Menu from './component/Menu';
import { Route, Switch } from 'react-router-dom';
import Channel from './component/Channel';
import Login from './component/Login';
import NotFound from './component/NotFound';
import { GlobalInfoContextType } from './context/GlobalInfoContext';
import { UserContextType } from './context/UserContext';
import { withAutoContext } from './util/hoc.util';
import { UserApi } from './api/user.api';

interface Props {
  globalInfoContext: GlobalInfoContextType,
  userContext: UserContextType
}
interface State {
  init: boolean
}

export class App extends React.Component<Props, State> {

  private userApi: UserApi;

  constructor(props: Props) {
    super(props);
    this.state = {
      init: false
    };

    this.userApi = new UserApi(this.props.globalInfoContext);
  }

  componentDidMount() {
    this.userApi.getCurrentUser().then(user => {
      if (user) {
        this.props.userContext.setCurrentUser(user);
      }
    }).finally(() => {
      this.setState({
        init: true
      });
    });
  }

  render() {
    if (!this.state.init) {
      return (
        <div id="App">
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

export default withAutoContext(App, ['globalInfoContext', 'userContext']);
