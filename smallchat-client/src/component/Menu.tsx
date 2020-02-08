import React from 'react';
import './Menu.scss';
import { LinkContainer } from 'react-router-bootstrap';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import Badge from 'react-bootstrap/Badge';
import { GlobalInfo } from './menu/GlobalInfo';
import { GlobalService } from '../service/global/global.service';
import { AuthenticationService } from '../service/auth/authentication.service';
import { HasRoleUser } from './shared/security/HasRoleUser';

interface Props {}
interface State {
  likes: number;
}

export class Menu extends React.Component<Props, State> {
  private globalService: GlobalService;
  private authenticationService: AuthenticationService;

  constructor(props: Props) {
    super(props);
    this.state = {
      likes: null
    };

    this.globalService = GlobalService.get();
    this.authenticationService = AuthenticationService.get();

    this.handleClickLike = this.handleClickLike.bind(this);
    this.handleClickLogout = this.handleClickLogout.bind(this);
  }

  componentDidMount() {
    this.loadLikes();
  }

  loadLikes() {
    this.globalService.countLike().then(likesCount => {
      this.setState({
        likes: likesCount.count
      });
    });
  }

  handleClickLike(event) {
    this.globalService.addLike().then(() => {
      this.loadLikes();
    });
  }

  handleClickLogout(event) {
    this.authenticationService.logout();
  }

  render() {
    return (
      <Navbar className="MenuScreen" bg="primary" variant="dark" expand="lg">
        <LinkContainer to="/">
          <Navbar.Brand>Small Chat</Navbar.Brand>
        </LinkContainer>
        <Nav className="mr-auto">
          <Nav.Link className="fa fa-thumbs-up" onClick={this.handleClickLike}></Nav.Link>
          <Badge pill className="likeBadge">
            {this.state.likes}
          </Badge>
        </Nav>
        <Nav>
          <GlobalInfo />
          <HasRoleUser>
            <Nav.Link onClick={this.handleClickLogout}>Logout</Nav.Link>
          </HasRoleUser>
          <HasRoleUser not={true}>
            <LinkContainer to="/login">
              <Nav.Link>Login</Nav.Link>
            </LinkContainer>
          </HasRoleUser>
        </Nav>
      </Navbar>
    );
  }
}
