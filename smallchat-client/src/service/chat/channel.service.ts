import { UrlConstant } from '../../const/url-constant';
import { RestClientService } from '../util/rest-client.service';
import { ChannelFullModel } from '../../model/channel/channel-full.model';

export class ChannelService {
  private static readonly INSTANCE = new ChannelService();

  private restClientService: RestClientService = null;

  private constructor() {
    this.restClientService = RestClientService.get();
  }
  public static get(): ChannelService {
    return this.INSTANCE;
  }

  public getChannel(channelId: string): Promise<ChannelFullModel> {
    return this.restClientService.get(UrlConstant.Channel.CHANNEL + channelId);
  }

  public connect(channelId: string): Promise<ChannelFullModel> {
    return this.restClientService.post(UrlConstant.Channel.CHANNEL + channelId + UrlConstant.Channel.CONNECT, null);
  }
}
