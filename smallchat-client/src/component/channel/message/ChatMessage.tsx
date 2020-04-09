import React from 'react';
import './ChatMessage.scss';
import { ChannelMessage, MessageCode } from '../../../model/channel/channel-message';

interface Props {
  message: ChannelMessage;
}
interface State {}

export class ChatMessage extends React.Component<Props, State> {
  getClassNameForMessage(message: ChannelMessage) {
    if (message.code === MessageCode.MSG) {
      if (message.sentByCurrentUser) {
        return 'ChatMessage sentMessage';
      } else {
        return 'ChatMessage receivedMessage';
      }
    } else {
      if (message.code === MessageCode.DELETED) {
        return 'ChatMessage importantTechnicalMessage';
      } else {
        return 'ChatMessage technicalMessage';
      }
    }
  }

  formatDate(timestamp: number) {
    const date = new Date(timestamp);
    return Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      hour12: false,
      minute: '2-digit',
      second: '2-digit'
    }).format(date);
  }

  render() {
    return this.props.message.code === MessageCode.MSG ? (
      <div className={this.getClassNameForMessage(this.props.message)}>
        <div>
          <span className="user">{this.props.message.user}</span>
          <span className="date">{this.formatDate(this.props.message.date)}</span>
        </div>
        <div className="message">{this.props.message.message}</div>
      </div>
    ) : (
      <div className={this.getClassNameForMessage(this.props.message)}>
        <span className="message">{this.props.message.message}</span>
        <span className="date">{this.formatDate(this.props.message.date)}</span>
      </div>
    );
  }
}
