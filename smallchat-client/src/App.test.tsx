import React from 'react';
import { render, waitForElement, fireEvent } from '@testing-library/react';
import { App } from './App';
import { AppDi } from './App.di';
import { UrlConstant } from './const/url-constant';
import { RestClientServiceMock } from './service/util/rest-client.service.mock';
import { ChatServiceMock } from './service/chat/chat.service.mock';

describe('App', () => {
  let mockRestClientService = new RestClientServiceMock();
  let mockChatService = new ChatServiceMock();
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

  test('Loads and works', async () => {
    const appRender = render(<App />);
    await waitForElement(() => appRender.getByText(/Small Chat/));
    expect(appRender.getByText(/Small/)).toBeTruthy();
    expect(appRender.getAllByText(/Channel 1/)).toBeTruthy();
    expect(appRender.getAllByText(/Channel 2/)).toBeTruthy();
    expect(appRender.getAllByText(/usertest/)).toBeTruthy();
    expect(appRender.getAllByText(/otheruser/)).toBeTruthy();

    expect(mockRestClientService.get).toHaveBeenCalledTimes(2);
    expect(mockRestClientService.get).toHaveBeenCalledWith(UrlConstant.User.CURRENT_USER);
    expect(mockRestClientService.get).toHaveBeenCalledWith(UrlConstant.Global.LIKE_COUNT);
    expect(mockChatService.getChatState).toHaveBeenCalled();
    expect(mockChatService.getChatStateObservable).toHaveBeenCalled();

    expect(appRender.queryByText(/Create new channel/)).toBeFalsy();
    fireEvent.click(appRender.getByTitle('Settings'));
    await waitForElement(() => appRender.getByText(/Create new channel/));
    expect(appRender.getAllByText(/Channel 1/)).toBeTruthy();
    expect(appRender.getAllByText(/Channel 2/)).toBeTruthy();
    expect(appRender.getAllByText(/Close/)).toBeTruthy();
  });
});
