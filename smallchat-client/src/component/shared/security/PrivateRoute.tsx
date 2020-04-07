import { Route, Redirect } from 'react-router-dom';
import React from 'react';
import { AuthenticationService } from '../../../service/auth/authentication.service';

const PrivateRoute = ({ component: Component, ...otherProps }: any) => {
  const isLogin = () => {
    return !!AuthenticationService.get().getCurrentUser();
  };

  return <Route {...otherProps} render={props => (isLogin() ? <Component {...props} /> : <Redirect to="/signin" />)} />;
};

export default PrivateRoute;
