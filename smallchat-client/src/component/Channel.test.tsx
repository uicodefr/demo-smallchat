import React from 'react';
import { RestClientServiceMock } from '../service/util/rest-client.service.mock';
import { AppDi } from '../App.di';
import { render, fireEvent, wait } from '@testing-library/react';
import { AuthenticationService } from '../service/auth/authentication.service';
import { myDi } from '../util/my-di';
import { Channel } from './Channel';
import { ChatServiceMock } from '../service/chat/chat.service.mock';
import { MemoryRouter } from 'react-router-dom';
import { PrivateRoute } from './shared/security/PrivateRoute';

describe('Channel', () => {
  const mockRestClientService = new RestClientServiceMock();
  const mockChatService = new ChatServiceMock();
  AppDi.registerForUnitTest([
    {
      provide: 'RestClientService',
      useValue: mockRestClientService,
    },
    {
      provide: 'ChatService',
      useValue: mockChatService,
    },
  ]);

  beforeEach(() => {
    myDi.get<AuthenticationService>('AuthenticationService').loadUser();
  });

  test('Loads and works', async () => {
    const loginRender = render(
      <MemoryRouter initialEntries={['/c/channeltest']}>
        <PrivateRoute path="/c/:channelId" component={Channel} />
      </MemoryRouter>
    );
    expect(loginRender.getByTestId('send-button')).toBeTruthy();
    expect(loginRender.getByText(/Hello message/)).toBeTruthy();

    await wait(() => {
      fireEvent.click(loginRender.getByTestId('send-button'));
    });
    expect(mockChatService.sendMessage).toHaveBeenCalledTimes(0);

    await wait(() => {
      fireEvent.change(loginRender.getByTestId('message-input'), { target: { value: 'messagetest' } });
      fireEvent.click(loginRender.getByTestId('send-button'));
    });
    expect(mockChatService.sendMessage).toHaveBeenCalledWith('channeltest', 'messagetest');
  });
});
