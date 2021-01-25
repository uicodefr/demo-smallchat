import { of } from 'rxjs';
import { ChannelFullModel } from '../../../model/channel/channel-full.model';
import { MessageCode, ChannelMessage } from '../../../model/channel/channel-message';
import { ChatStateModel } from '../../../model/chat/chat-state.model';

export class ChatService {
  public getChatState = jest.fn().mockReturnValue(null);

  public getChatStateObservable = jest.fn().mockReturnValue(
    of({
      channels: [
        { id: 'channel1', name: 'Channel 1' },
        { id: 'channel2', name: 'Channel 2', description: 'Channel 2 description' },
      ],
      users: [
        { id: 'usertest', pseudo: 'usertest' },
        { id: 'otheruser', pseudo: 'otheruser' },
      ],
      updateDate: new Date(),
    } as ChatStateModel)
  );

  public connect = jest.fn();

  public disconnect = jest.fn();

  public getChannelObservable = jest.fn((channelId) => {
    return of({
      id: channelId,
      name: 'Channel ' + channelId,
      messages: [
        {
          id: '1',
          channelId: channelId,
          user: 'oneuser',
          date: new Date().getTime(),
          code: MessageCode.MSG,
          message: 'Hello message',
          sentByCurrentUser: false,
        },
      ] as Array<ChannelMessage>,
    } as ChannelFullModel);
  });

  public connectToChannel = jest.fn();

  public sendMessage = jest.fn();

  public cleanUnreadMessages = jest.fn();
}
