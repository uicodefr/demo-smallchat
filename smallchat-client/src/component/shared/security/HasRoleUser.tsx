import React from 'react';
import { UserModel } from '../../../model/global/user.model';
import { AuthenticationService } from '../../../service/auth/authentication.service';
import { Subscription } from 'rxjs';

interface Props {
  userRole?: string;
  not?: boolean;
}
interface State {
  currentUser: UserModel;
}

export class HasRoleUser extends React.Component<Props, State> {
  static defaultProps = {
    userRole: 'USER',
    not: false
  };

  private authenticationService: AuthenticationService;

  private currentUserSubscription: Subscription;

  constructor(props) {
    super(props);

    this.authenticationService = AuthenticationService.get();

    this.state = {
      currentUser: this.authenticationService.getCurrentUser()
    };
  }

  componentDidMount() {
    this.currentUserSubscription = this.authenticationService.getCurrentUserObservable().subscribe(currentUser => {
      this.setState({ currentUser: currentUser });
    });
  }

  componentWillUnmount() {
    if (this.currentUserSubscription) {
      this.currentUserSubscription.unsubscribe();
    }
  }

  render() {
    let hasRole = this.state.currentUser && this.state.currentUser.roles.includes(this.props.userRole);
    if (this.props.not) {
      hasRole = !hasRole;
    }

    if (hasRole) {
      return this.props.children;
    } else {
      return null;
    }
  }
}
