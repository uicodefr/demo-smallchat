import { myDi } from './util/my-di';
import { AuthenticationService } from './service/auth/authentication.service';
import { ChannelService } from './service/chat/channel.service';
import { ChatStateService } from './service/chat/chat-state.service';
import { ChatService } from './service/chat/chat.service';
import { GlobalService } from './service/global/global.service';
import { GlobalInfoService } from './service/util/global-info.service';
import { RestClientService } from './service/util/rest-client.service';

export class AppDi {
  public static register() {
    this.registerServices();
    myDi.loadInstances();
  }

  private static registerServices() {
    myDi.register('AuthenticationService', AuthenticationService);
    myDi.register('ChannelService', ChannelService);
    myDi.register('ChatStateService', ChatStateService);
    myDi.register('ChatService', ChatService);
    myDi.register('GlobalService', GlobalService);
    myDi.register('GlobalInfoService', GlobalInfoService);
    myDi.register('RestClientService', RestClientService);
  }
}
