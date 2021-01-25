import React from 'react';
import { render, fireEvent } from '@testing-library/react';
import { App } from './App';
import { AppDi } from './App.di';
import { UrlConstant } from './const/url-constant';
import { ChatService } from './service/chat/chat.service';
import { RestClientService } from './service/util/rest-client.service';
import { myDi } from './util/my-di';

jest.mock('./service/util/rest-client.service');
jest.mock('./service/chat/chat.service');

describe('App', () => {
  AppDi.register();
  const restClientService = myDi.get<RestClientService>('RestClientService');
  const chatService = myDi.get<ChatService>('ChatService');

  test('Loads and works', async () => {
    const appRender = render(<App />);
    await appRender.findByText(/Small Chat/);
    expect(appRender.getByText(/Small/)).toBeTruthy();
    expect(appRender.getAllByText(/Channel 1/)).toBeTruthy();
    expect(appRender.getAllByText(/Channel 2/)).toBeTruthy();
    expect(appRender.getAllByText(/usertest/)).toBeTruthy();
    expect(appRender.getAllByText(/otheruser/)).toBeTruthy();

    expect(restClientService.get).toHaveBeenCalledTimes(2);
    expect(restClientService.get).toHaveBeenCalledWith(UrlConstant.User.CURRENT_USER);
    expect(restClientService.get).toHaveBeenCalledWith(UrlConstant.Global.LIKE_COUNT);
    expect(chatService.getChatState).toHaveBeenCalled();
    expect(chatService.getChatStateObservable).toHaveBeenCalled();

    expect(appRender.queryByText(/Create new channel/)).toBeFalsy();
    fireEvent.click(appRender.getByTitle('Settings'));
    await appRender.findByText(/Create new channel/);
    expect(appRender.getAllByText(/Channel 1/)).toBeTruthy();
    expect(appRender.getAllByText(/Channel 2/)).toBeTruthy();
    expect(appRender.getAllByText(/Close/)).toBeTruthy();
  });
});
