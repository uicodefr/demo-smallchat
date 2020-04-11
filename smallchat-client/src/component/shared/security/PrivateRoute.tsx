import { Route, Redirect } from 'react-router-dom';
import React from 'react';
import { AuthenticationService } from '../../../service/auth/authentication.service';
import { myDi } from '../../../util/my-di';

const PrivateRoute = ({ component: Component, ...otherProps }: any) => {
  const isLogin = () => {
    return !!myDi.get<AuthenticationService>('AuthenticationService').getCurrentUser();
  };

  return (
    <Route {...otherProps} render={(props) => (isLogin() ? <Component {...props} /> : <Redirect to="/signin" />)} />
  );
};

export default PrivateRoute;
