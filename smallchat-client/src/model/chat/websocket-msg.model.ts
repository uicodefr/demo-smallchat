export class WebsocketMsgModel {
  public static readonly CHANNEL_PREFIX = '#';
  public static readonly USER_PREFIX = '@';
  public static readonly CHAT_STATE_CHANNEL = 'state';

  channel: string;
  data: any;
}
