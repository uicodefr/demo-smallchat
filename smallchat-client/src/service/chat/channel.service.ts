import { UrlConstant } from '../../const/url-constant';
import { RestClientService } from '../util/rest-client.service';
import { ChannelFullModel } from '../../model/channel/channel-full.model';
import { myDi } from '../../util/my-di';

export class ChannelService {
  private restClientService: RestClientService;

  public constructor() {
    this.restClientService = myDi.get('RestClientService');
  }

  public getChannel(channelId: string): Promise<ChannelFullModel> {
    return this.restClientService.get(UrlConstant.Channel.CHANNEL + channelId);
  }

  public connect(channelId: string): Promise<ChannelFullModel> {
    return this.restClientService.post(UrlConstant.Channel.CHANNEL + channelId + UrlConstant.Channel.CONNECT, null);
  }

  public disconnect(channelId: string): Promise<void> {
    return this.restClientService.post(UrlConstant.Channel.CHANNEL + channelId + UrlConstant.Channel.DISCONNECT, null);
  }
}
