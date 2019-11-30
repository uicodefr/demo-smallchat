import React from 'react';
import './Menu.scss';
import { LinkContainer } from 'react-router-bootstrap';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import Badge from 'react-bootstrap/Badge';
import GlobalInfo from './menu/GlobalInfo';
import { GlobalInfoContextType } from '../context/GlobalInfoContext';
import { withAutoContext } from '../util/hoc.util';
import { GlobalApi } from '../api/global.api';
import { UserContextType } from '../context/UserContext';
import { UserApi } from '../api/user.api';

interface Props {
  globalInfoContext: GlobalInfoContextType,
  userContext: UserContextType
}
interface State {
  likes: number
}

class Menu extends React.Component<Props, State> {

  private globalApi: GlobalApi;
  private userApi: UserApi;

  constructor(props: Props) {
    super(props);
    this.state = {
      likes: null,
    };

    this.globalApi = new GlobalApi(this.props.globalInfoContext);
    this.userApi = new UserApi(this.props.globalInfoContext);
    this.handleClickLike = this.handleClickLike.bind(this);
    this.handleClickLogout = this.handleClickLogout.bind(this);
  }

  componentDidMount() {
    this.loadLikes();
  }

  loadLikes() {
    this.globalApi.countLike().then(likesCount => {
      this.setState({
        likes: likesCount.count
      });
    });
  }

  handleClickLike(event) {
    this.globalApi.addLike().then(() => {
      this.loadLikes();
    });
  }

  handleClickLogout(event) {
    this.userApi.logout().then(() => {
      this.props.userContext.setCurrentUser(null);
    });
  }

  render() {
    return (
      <Navbar className="MenuScreen" bg="primary" variant="dark" expand="lg">
        <LinkContainer to="/">
          <Navbar.Brand>Small Chat</Navbar.Brand>
        </LinkContainer>
        <Nav className="mr-auto">
          <Nav.Link className="fa fa-thumbs-up" onClick={this.handleClickLike}></Nav.Link>
          <Badge pill className="likeBadge">{this.state.likes}</Badge>
        </Nav>
        <Nav>
          <GlobalInfo />
          {this.props.userContext.currentUser ? (
            <Nav.Link onClick={this.handleClickLogout}>Logout</Nav.Link>
          ) : (
              <LinkContainer to="/login">
                <Nav.Link>Login</Nav.Link>
              </LinkContainer>
            )
          }
        </Nav>
      </Navbar >
    );
  }
}

export default withAutoContext(Menu, ['userContext', 'globalInfoContext']);