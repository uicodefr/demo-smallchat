import { Observable, BehaviorSubject } from 'rxjs';
import { ChatStateModel } from '../../model/chat/chat-state.model';
import { WebsocketMsgModel } from '../../model/chat/websocket-msg.model';
import { ChatService } from './chat.service';
import { ChannelFullModel } from '../../model/channel/channel-full.model';
import { ChannelMessage, MessageCode } from '../../model/channel/channel-message';
import { ChannelService } from './channel.service';
import { filter } from 'rxjs/operators';
import { AuthenticationService } from '../auth/authentication.service';
import { UrlConstant } from '../../const/url-constant';

export class WebSocketService {
  private static readonly INSTANCE = new WebSocketService();

  private chatService: ChatService;
  private channelService: ChannelService;
  private authenticationService: AuthenticationService;

  private webSocket: WebSocket;
  private connectedSubject = new BehaviorSubject<boolean>(false);

  private currentUser: string | undefined;
  private chatStateSubject = new BehaviorSubject<ChatStateModel | null>(null);
  private channelSubjectMap = new Map<string, BehaviorSubject<ChannelFullModel | null>>();

  private constructor() {
    this.chatService = ChatService.get();
    this.channelService = ChannelService.get();
    this.authenticationService = AuthenticationService.get();

    this.authenticationService.getCurrentUserObservable().subscribe(user => {
      this.currentUser = user?.username;
    });
  }
  public static get(): WebSocketService {
    return this.INSTANCE;
  }

  public getChatState(): ChatStateModel | null {
    return this.chatStateSubject.getValue();
  }

  public getChatStateObservable(): Observable<ChatStateModel | null> {
    return this.chatStateSubject.asObservable();
  }

  private getChannelSubject(channelId: string): BehaviorSubject<ChannelFullModel | null> {
    let channelSubject = this.channelSubjectMap.get(channelId);
    if (!channelSubject) {
      channelSubject = new BehaviorSubject<ChannelFullModel | null>(null);
      this.channelSubjectMap.set(channelId, channelSubject);
    }
    return channelSubject;
  }

  public getChannelObservable(channelId: string): Observable<ChannelFullModel | null> {
    return this.getChannelSubject(channelId)
      .asObservable()
      .pipe(
        filter(channel => {
          if (!channel) {
            return false;
          }
          this.correctAndImproveChannel(channel);
          return true;
        })
      );
  }

  private correctAndImproveChannel(channel: ChannelFullModel) {
    if (channel.messages) {
      // Delete duplicate messages
      channel.messages = channel.messages.reduce((accumulator, currentValue) => {
        if (!accumulator.find(accumulatorElement => accumulatorElement.id === currentValue.id)) {
          accumulator.push(currentValue);
        }
        return accumulator;
      }, [] as Array<ChannelMessage>);

      // Fill sentByCurrentUser
      channel.messages.forEach(message => {
        message.sentByCurrentUser = message.user === this.currentUser;
      });

      // Order by date
      channel.messages.sort((message1, message2) => message1.date - message2.date);
    }
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
    this.connectedSubject.next(false);
    this.webSocket = new WebSocket(this.convertToWebSocketUrl(UrlConstant.WEBSOCKET));
    this.webSocket.onerror = eventError => {
      console.error('WebSocket error', eventError);
    };

    this.webSocket.onopen = eventOpen => {
      const webSocketMsg = { subject: WebsocketMsgModel.PING_SUBJECT, data: 'ping' } as WebsocketMsgModel;
      this.webSocket.send(JSON.stringify(webSocketMsg));
    };

    this.webSocket.onmessage = eventMessage => {
      const message = JSON.parse(eventMessage.data) as WebsocketMsgModel;

      switch (message.subject) {
        case WebsocketMsgModel.CHANNEL_MESSAGE_SUBJECT:
          this.onReceiveChannelMessage(message.data);
          break;

        case WebsocketMsgModel.CHAT_STATE_SUBJECT:
          this.onReceiveChatState(message.data);
          break;

        case WebsocketMsgModel.PONG_SUBJECT:
          this.connectedSubject.next(true);
          break;

        default:
          console.error('Message unknown : ', message);
      }
    };

    this.webSocket.onclose = eventClose => {
      this.connectedSubject.next(false);
      console.info('WebSocket closed');
    };
  }

  private convertToWebSocketUrl(path: string): string {
    if (process.env.REACT_APP_API_PROXY === 'true') {
      // In development, we use the direct url
      // because the react proxy doesn't handle well the websocket
      return 'ws://localhost:8080' + path;
    } else {
      let webSocketUrl = 'ws:';
      if (window.location.protocol === 'https:') {
        webSocketUrl = 'wss:';
      }
      webSocketUrl += '//' + window.location.host;
      webSocketUrl += path;
      return webSocketUrl;
    }
  }

  private onReceiveChatState(chatState: ChatStateModel) {
    this.chatStateSubject.next(chatState);

    chatState.channels.forEach(channelLight => {
      const channelSubject = this.channelSubjectMap.get(channelLight.id);
      if (channelSubject && channelSubject.value) {
        const channelFull = channelSubject.value;
        channelFull.name = channelLight.name;
        channelFull.description = channelLight.description;
        channelSubject.next(channelFull);
      }
    });
  }

  private onReceiveChannelMessage(channelMessage: ChannelMessage) {
    const channelSubject = this.getChannelSubject(channelMessage.channelId);
    let currentValue = channelSubject.value;
    if (!currentValue) {
      currentValue = new ChannelFullModel();
      currentValue.messages = [];
    }
    if (channelMessage.code === MessageCode.DELETED) {
      currentValue.deleted = true;
    }
    // Add the message in the channelObject
    currentValue.messages.push(channelMessage);
    channelSubject.next(currentValue);

    if (channelMessage.code === MessageCode.DELETED) {
      channelSubject.next(null);
    }
  }

  private whenConnected(): Promise<void> {
    if (this.connectedSubject.value) {
      return Promise.resolve();
    } else {
      return new Promise((resolve, reject) => {
        const subscription = this.connectedSubject.subscribe(connected => {
          if (connected) {
            resolve();
            subscription.unsubscribe();
          }
        });
      });
    }
  }

  public sendMessage(channel: string, message: string) {
    this.whenConnected().then(() => {
      const webSocketMsg = {
        subject: WebsocketMsgModel.CHANNEL_MESSAGE_SUBJECT,
        data: {
          channelId: channel,
          message: message
        }
      } as WebsocketMsgModel;
      this.webSocket.send(JSON.stringify(webSocketMsg));
    });
  }

  public connectToChannel(channelId: string) {
    this.whenConnected().then(() => {
      const channelSubject = this.getChannelSubject(channelId);
      let currentValue = channelSubject.value;

      if (!currentValue || !currentValue.id) {
        this.channelService.connect(channelId).then(channelFull => {
          if (!currentValue) {
            currentValue = new ChannelFullModel();
            currentValue.messages = [];
          }

          // Merge the response (channelFull) to the currentValue
          currentValue = { ...channelFull, messages: currentValue.messages };
          currentValue.messages.push(...channelFull.messages);
          channelSubject.next(currentValue);
        });
      }
    });
  }

  public disconnectToChannel(channelId: string): Promise<void> {
    const channelSubject = this.getChannelSubject(channelId);
    channelSubject.next(null);
    return this.channelService.disconnect(channelId);
  }
}
