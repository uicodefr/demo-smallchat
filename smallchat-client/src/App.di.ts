import { myDi } from './util/my-di';
import { AuthenticationService } from './service/auth/authentication.service';
import { ChannelService } from './service/chat/channel.service';
import { ChatStateService } from './service/chat/chat-state.service';
import { ChatService } from './service/chat/chat.service';
import { GlobalService } from './service/global/global.service';
import { GlobalInfoService } from './service/util/global-info.service';
import { RestClientService } from './service/util/rest-client.service';

interface RegisterObject<T> {
  provide: string;
  useValue: T;
}

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

  public static registerForUnitTest(mockRegisterArray?: Array<RegisterObject<any>>) {
    this.registerServices();
    myDi.unregister('RestClientService'); // unregister because it makes http call
    myDi.unregister('ChatService'); // unregister because it uses a websocket
    if (mockRegisterArray) {
      for (const registerObject of mockRegisterArray) {
        myDi.registerInstance(registerObject.provide, registerObject.useValue);
      }
    }
    myDi.loadInstances();
  }
}
