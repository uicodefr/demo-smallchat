import React from "react";
import { ChatStateModel } from "../model/chat/chat-state.model";
import { GlobalInfoContextType } from "./GlobalInfoContext";
import { withAutoContext } from "../util/hoc.util";
import { ChatApi } from "../api/chat.api";
import { WebsocketMsgModel } from "../model/chat/websocket-msg.model";
import { GlobalConstant } from "../const/global-constant";
import { UserContextType } from "./UserContext";

export interface WebSocketContextType {
  chatState: ChatStateModel,

  connect(channel: string),
  disconnect(channel: string),
  sendMessage(channel: string, message: string)
}

export const WebSocketContext = React.createContext({

} as WebSocketContextType);

interface Props {
  globalInfoContext: GlobalInfoContextType,
  userContext: UserContextType
}

class WebSocketContextComponent extends React.Component<Props, WebSocketContextType> {

  private webSocket: WebSocket;

  componentDidMount() {
    this.reconnectWebSocket();
  }

  componentDidUpdate(prevProps) {
    if (this.props.userContext?.currentUser !== prevProps.userContext?.currentUser) {
      this.reconnectWebSocket();
    }
  }

  private reconnectWebSocket() {
    if (this.webSocket) {
      this.webSocket.close();
    }

    this.connectWebSocket();
    new ChatApi(this.props.globalInfoContext).getChatState().then(chatState => {
      this.setState({ chatState: chatState });
    });
  }

  private connectWebSocket() {
    this.webSocket = new WebSocket('ws://localhost:8080/websocket');
    this.webSocket.onerror = eventError => {
      console.error('WebSocket error', eventError);
    }

    this.webSocket.onopen = eventOpen => {
      this.webSocket.send('ping');
    }

    this.webSocket.onmessage = eventMessage => {
      const message = JSON.parse(eventMessage.data) as WebsocketMsgModel;
      if (message.channel.startsWith('#')) {
        console.warn('Receive message from channel' + message.data);
      } else if (message.channel === GlobalConstant.CHAT_STATE_CHANNEL) {
        this.setState({ chatState: message.data });
      } else {
        console.error('Message unknown : ' + message);
      }
    }

    this.webSocket.onclose = eventClose => {
      console.info('WebSocket closed');
    }
  }

  render() {
    return (
      <WebSocketContext.Provider value={this.state}>
        {this.props.children}
      </WebSocketContext.Provider>
    );
  }

}

export default withAutoContext(WebSocketContextComponent, ['globalInfoContext', 'userContext']);
