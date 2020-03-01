export class ChannelMessage {
  id: string;
  channelId: string;
  message: string;
  user: string;
  date: number;

  // decorator variable which indicate if the message is sent by the current user
  sentByCurrentUser = false;
}
