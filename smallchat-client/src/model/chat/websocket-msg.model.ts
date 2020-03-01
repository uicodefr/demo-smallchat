export class WebsocketMsgModel {
  public static readonly CHANNEL_MESSAGE_SUBJECT = 'channel-message';
  public static readonly PING_SUBJECT = 'ping';
  public static readonly CHAT_STATE_SUBJECT = 'state';

  subject: string;
  data: any;
}
