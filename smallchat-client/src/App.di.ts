import { myDi } from './util/my-di';
import { AuthenticationService } from './service/auth/authentication.service';
import { ChannelService } from './service/chat/channel.service';
import { ChatService } from './service/chat/chat.service';
import { ChatStateService } from './service/chat/chat-state.service';
import { GlobalService } from './service/global/global.service';
import { GlobalInfoService } from './service/util/global-info.service';
import { RestClientService } from './service/util/rest-client.service';

export class AppDi {
  public static registerServices() {
    myDi.register(AuthenticationService);
    myDi.register(ChannelService);
    myDi.register(ChatStateService);
    myDi.register(ChatService);
    myDi.register(GlobalService);
    myDi.register(GlobalInfoService);
    myDi.register(RestClientService);
  }
}
