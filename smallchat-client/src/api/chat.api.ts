import { GlobalInfoContextType } from '../context/GlobalInfoContext';
import { ChatStateModel } from '../model/chat/chat-state.model';
import { ApiUtil } from '../util/api.util';
import { UrlConstant } from '../const/url-constant';
import { ChannelModel } from '../model/chat/channel.model';

export class ChatApi {

  constructor(private globalInfoContext: GlobalInfoContextType) {}

  public getChatState(): Promise<ChatStateModel> {
    return new ApiUtil(this.globalInfoContext).get(UrlConstant.Chat.STATE);
  }

  public createChannel(channel: ChannelModel): Promise<ChannelModel> {
    return new ApiUtil(this.globalInfoContext).post(
      UrlConstant.Chat.CHANNEL,
      channel
    );
  }

  public updateChannel(channel: ChannelModel): Promise<ChannelModel> {
    return new ApiUtil(this.globalInfoContext).patch(
      UrlConstant.Chat.CHANNEL + '/' + channel.id,
      channel
    );
  }

  public deleteChannel(channelId: string): Promise<void> {
    return new ApiUtil(this.globalInfoContext).delete(
      UrlConstant.Chat.CHANNEL + '/' + channelId
    );
  }

}
