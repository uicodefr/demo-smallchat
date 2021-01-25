import React from 'react';
import { AppDi } from '../App.di';
import { render, fireEvent, waitFor } from '@testing-library/react';
import { AuthenticationService } from '../service/auth/authentication.service';
import { myDi } from '../util/my-di';
import { Channel } from './Channel';
import { MemoryRouter } from 'react-router-dom';
import { PrivateRoute } from './shared/security/PrivateRoute';
import { ChatService } from '../service/chat/chat.service';

jest.mock('../service/util/rest-client.service');
jest.mock('../service/chat/chat.service');

describe('Channel', () => {
  AppDi.register();
  const chatService = myDi.get<ChatService>('ChatService');

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

    await waitFor(() => {
      fireEvent.click(loginRender.getByTestId('send-button'));
    });
    expect(chatService.sendMessage).toHaveBeenCalledTimes(0);

    await waitFor(() => {
      fireEvent.change(loginRender.getByTestId('message-input'), { target: { value: 'messagetest' } });
      fireEvent.click(loginRender.getByTestId('send-button'));
    });
    expect(chatService.sendMessage).toHaveBeenCalledWith('channeltest', 'messagetest');
  });
});
