import { UrlConstant } from '../../const/url-constant';
import { ChatStateModel } from '../../model/chat/chat-state.model';
import { ChannelModel } from '../../model/chat/channel.model';
import { RestClientService } from '../util/rest-client.service';
import { myDi } from '../../util/my-di';

export class ChatStateService {
  private restClientService: RestClientService;

  public constructor() {
    this.restClientService = myDi.get('RestClientService');
  }

  public getChatState(): Promise<ChatStateModel> {
    return this.restClientService.get(UrlConstant.ChatState.STATE);
  }

  public createChannel(channel: ChannelModel): Promise<ChannelModel> {
    return this.restClientService.post(UrlConstant.ChatState.CHANNEL, channel);
  }

  public updateChannel(channel: ChannelModel): Promise<ChannelModel> {
    return this.restClientService.patch(UrlConstant.ChatState.CHANNEL + '/' + channel.id, channel);
  }

  public deleteChannel(channelId: string): Promise<void> {
    return this.restClientService.delete(UrlConstant.ChatState.CHANNEL + '/' + channelId);
  }
}
