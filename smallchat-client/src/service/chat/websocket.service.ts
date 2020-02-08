import { Observable, BehaviorSubject } from 'rxjs';
import { ChatStateModel } from '../../model/chat/chat-state.model';
import { GlobalConstant } from '../../const/global-constant';
import { WebsocketMsgModel } from '../../model/chat/websocket-msg.model';
import { ChatService } from './chat.service';
import { ChannelFullModel } from '../../model/channel/channel-full.model';

export class WebSocketService {
  private static readonly INSTANCE = new WebSocketService();

  private chatService: ChatService = null;

  private webSocket: WebSocket;

  private chatStateSubject = new BehaviorSubject<ChatStateModel>(null);
  private channelSubjectMap = new Map<string, ChannelFullModel>();

  private constructor() {
    this.chatService = ChatService.get();
  }
  public static get(): WebSocketService {
    return this.INSTANCE;
  }

  public getChatState(): ChatStateModel {
    return this.chatStateSubject.getValue();
  }

  public getChatStateObservable(): Observable<ChatStateModel> {
    return this.chatStateSubject.asObservable();
  }

  public connectWebSocket() {
    if (this.webSocket) {
      this.webSocket.close();
    }

    this.connect();
    this.chatService.getChatState().then(chatState => {
      this.chatStateSubject.next(chatState);
    });
  }

  private connect() {
    this.webSocket = new WebSocket('ws://localhost:8080/websocket');
    this.webSocket.onerror = eventError => {
      console.error('WebSocket error', eventError);
    };

    this.webSocket.onopen = eventOpen => {
      this.webSocket.send('ping');
    };

    this.webSocket.onmessage = eventMessage => {
      const message = JSON.parse(eventMessage.data) as WebsocketMsgModel;
      if (message.channel.startsWith('#')) {
        console.warn('Receive message from channel' + message.data);
      } else if (message.channel === GlobalConstant.CHAT_STATE_CHANNEL) {
        this.chatStateSubject.next(message.data);
      } else {
        console.error('Message unknown : ' + message);
      }
    };

    this.webSocket.onclose = eventClose => {
      console.info('WebSocket closed');
    };
  }
}