import React from 'react';
import { UserContext } from '../../../context/UserContext';

export function HasRoleUser(props) {

  const userContext = React.useContext(UserContext);
  if (
    userContext.currentUser &&
    userContext.currentUser.roles.includes(props.userRole)
  ) {
    return props.children;
  } else {
    return null;
  }

}
