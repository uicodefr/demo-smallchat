import { Login } from './Login';
import { render, fireEvent } from '@testing-library/react';
import { AppDi } from '../App.di';
import { myDi } from '../util/my-di';
import { AuthenticationService } from '../service/auth/authentication.service';
import { RestClientService } from '../service/util/rest-client.service';

jest.mock('../service/util/rest-client.service');
jest.mock('../service/chat/chat.service');

describe('Login', () => {
  AppDi.register();
  const restClientService = myDi.get<RestClientService>('RestClientService');

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
    expect(restClientService.logout).toHaveBeenCalledTimes(1);
    expect(authenticationService.getCurrentUser()).toBeFalsy();
  });
});
