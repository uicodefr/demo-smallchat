export enum MessageCode {
  MSG = 'MSG',
  CONNECT = 'CONNECT',
  DISCONNECT = 'DISCONNECT',
  CREATED = 'CREATED',
  UPDATED = 'UPDATED',
  DELETED = 'DELETED'
}

export class ChannelMessage {
  id: string;
  channelId: string;
  message: string;
  user: string;
  date: number;
  code: MessageCode | null;

  // decorator variable which indicate if the message is sent by the current user
  sentByCurrentUser = false;
}
