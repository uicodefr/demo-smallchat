import './Menu.scss';
import React from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import Badge from 'react-bootstrap/Badge';
import { NavDropdown } from 'react-bootstrap';
import { GlobalInfo } from './menu/GlobalInfo';
import { GlobalService } from '../service/global/global.service';
import { AuthenticationService } from '../service/auth/authentication.service';
import { HasRoleUser } from './shared/security/HasRoleUser';
import { myDi } from '../util/my-di';
import { UserModel } from '../model/global/user.model';
import { Subscription } from 'rxjs';

interface Props {}
interface State {
  likes: number;
  currentUser: UserModel | null;
}

export class Menu extends React.Component<Props, State> {
  private globalService: GlobalService;
  private authenticationService: AuthenticationService;

  private currentUserSubscription: Subscription;

  constructor(props: Props) {
    super(props);
    this.state = {
      likes: 0,
      currentUser: null,
    };

    this.globalService = myDi.get('GlobalService');
    this.authenticationService = myDi.get('AuthenticationService');

    this.handleClickLike = this.handleClickLike.bind(this);
    this.handleClickLogout = this.handleClickLogout.bind(this);
  }

  componentDidMount() {
    this.currentUserSubscription = this.authenticationService.getCurrentUserObservable().subscribe((currentUser) => {
      this.setState({ currentUser: currentUser });
    });

    this.loadLikes();
  }

  componentWillUnmount() {
    if (this.currentUserSubscription) {
      this.currentUserSubscription.unsubscribe();
    }
  }

  loadLikes() {
    this.globalService.countLike().then((likesCount) => {
      this.setState({
        likes: likesCount.count,
      });
    });
  }

  handleClickLike(event: React.MouseEvent<any, any>) {
    this.globalService.addLike().then(() => {
      this.loadLikes();
    });
  }

  handleClickLogout(event: React.MouseEvent<any, any>) {
    this.authenticationService.logout();
  }

  render() {
    return (
      <Navbar className="MenuScreen" bg="primary" variant="dark" expand="lg">
        <LinkContainer to="/">
          <Navbar.Brand>Small Chat</Navbar.Brand>
        </LinkContainer>
        <Nav className="mr-auto">
          <Nav.Link className="fa fa-thumbs-up fa-lg" onClick={this.handleClickLike}></Nav.Link>
          <Badge pill className="likeBadge">
            {this.state.likes}
          </Badge>
        </Nav>
        <Nav>
          <GlobalInfo />
          <HasRoleUser>
            <NavDropdown
              id="user-menu-dropdown"
              alignRight
              title={
                <>
                  <i className="fa fa-user-circle fa-lg mr-2"></i>
                  {this.state.currentUser ? this.state.currentUser.username : 'Signed in'}
                </>
              }
            >
              <LinkContainer to="/">
                <NavDropdown.Item onClick={this.handleClickLogout}>
                  <i className="fa fa-sign-out-alt mr-2"></i>
                  Logout
                </NavDropdown.Item>
              </LinkContainer>
            </NavDropdown>
          </HasRoleUser>
          <HasRoleUser not={true}>
            <LinkContainer to="/signin">
              <Nav.Link>Sign in</Nav.Link>
            </LinkContainer>
          </HasRoleUser>
        </Nav>
      </Navbar>
    );
  }
}
