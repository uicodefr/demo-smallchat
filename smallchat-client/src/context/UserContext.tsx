import React from 'react';
import { UserModel } from '../model/global/user.model';

export interface UserContextType {
  currentUser: UserModel,
  setCurrentUser(user: UserModel)
}

export const UserContext = React.createContext({
  currentUser: null,
  setCurrentUser: () => { }
} as UserContextType);

interface Props {
}

export class UserContextComponent extends React.Component<Props, UserContextType> {

  constructor(props) {
    super(props);

    this.state = {
      currentUser: null,
      setCurrentUser: this.setCurrentUser.bind(this)
    }
  }

  setCurrentUser(user) {
    this.setState({
      currentUser: user
    });
  }

  render() {
    return (
      <UserContext.Provider value={this.state}>
        {this.props.children}
      </UserContext.Provider>
    );
  }

}