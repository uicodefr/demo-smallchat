import React from 'react';
import { Login } from './Login';
import { render, fireEvent } from '@testing-library/react';
import { AppDi } from '../App.di';
import { RestClientServiceMock } from '../service/util/rest-client.service.mock';
import { myDi } from '../util/my-di';
import { AuthenticationService } from '../service/auth/authentication.service';

describe('Login', () => {
  let mockRestClientService = new RestClientServiceMock();
  AppDi.registerForUnitTest([
    {
      provide: 'RestClientService',
      useValue: mockRestClientService,
    },
  ]);

  test('Loads and works', async () => {
    const loginRender = render(<Login />);
    expect(loginRender.getByTestId('login-button')).toBeTruthy();
    expect(loginRender.queryByTestId('logout-button')).toBeFalsy();

    const authenticationService = myDi.get<AuthenticationService>('AuthenticationService');
    authenticationService.loadUser();

    await loginRender.findByText(/You are connected as/);
    expect(loginRender.getByTestId('logout-button')).toBeTruthy();
    expect(loginRender.queryByTestId('login-button')).toBeFalsy();

    fireEvent.click(loginRender.getByTestId('logout-button'));
    await loginRender.findByTestId('login-button');
    expect(mockRestClientService.logout).toHaveBeenCalledTimes(1);
    expect(authenticationService.getCurrentUser()).toBeFalsy();
  });
});
