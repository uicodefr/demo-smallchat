import { UrlConstant } from '../../const/url-constant';
import { ChatStateModel } from '../../model/chat/chat-state.model';
import { ChannelModel } from '../../model/chat/channel.model';
import { RestClientService } from '../util/rest-client.service';

export class ChatService {
  private static readonly INSTANCE = new ChatService();

  private restClientService: RestClientService = null;

  private constructor() {
    this.restClientService = RestClientService.get();
  }
  public static get(): ChatService {
    return this.INSTANCE;
  }

  public getChatState(): Promise<ChatStateModel> {
    return this.restClientService.get(UrlConstant.Chat.STATE);
  }

  public createChannel(channel: ChannelModel): Promise<ChannelModel> {
    return this.restClientService.post(UrlConstant.Chat.CHANNEL, channel);
  }

  public updateChannel(channel: ChannelModel): Promise<ChannelModel> {
    return this.restClientService.patch(UrlConstant.Chat.CHANNEL + '/' + channel.id, channel);
  }

  public deleteChannel(channelId: string): Promise<void> {
    return this.restClientService.delete(UrlConstant.Chat.CHANNEL + '/' + channelId);
  }
}