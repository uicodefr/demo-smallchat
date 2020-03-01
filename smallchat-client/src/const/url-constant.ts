export class UrlConstant {
  public static readonly BASE = process.env.REACT_APP_API_BASE_URL;

  public static readonly LOGIN = UrlConstant.BASE + '/login';
  public static readonly LOGOUT = UrlConstant.BASE + '/logout';
  public static readonly WEBSOCKET = UrlConstant.BASE + '/websocket';

  private static readonly GLOBAL_PREFIX = UrlConstant.BASE + '/global';
  public static readonly Global = {
    STATUS: UrlConstant.GLOBAL_PREFIX + '/status',
    PARAMETERS: UrlConstant.GLOBAL_PREFIX + '/parameters',
    LIKE: UrlConstant.GLOBAL_PREFIX + '/likes',
    LIKE_COUNT: UrlConstant.GLOBAL_PREFIX + '/likes.count'
  };

  private static readonly USER_PREFIX = UrlConstant.BASE + '/users';
  public static readonly User = {
    USERS: UrlConstant.USER_PREFIX,
    CURRENT_USER: UrlConstant.USER_PREFIX + '/me'
  };

  private static readonly CHAT_STATE_PREFIX = UrlConstant.BASE + '/chat-state';
  public static readonly ChatState = {
    STATE: UrlConstant.CHAT_STATE_PREFIX + '/',
    CHANNEL: UrlConstant.CHAT_STATE_PREFIX + '/channels'
  };

  private static readonly CHANNEL_PREFIX = UrlConstant.BASE + '/channels';
  public static readonly Channel = {
    CHANNEL: UrlConstant.CHANNEL_PREFIX + '/',
    CONNECT: '/connect'
  };

  public static readonly WebSocket = {
    CONNECTION: UrlConstant.BASE + '/websocket'
  };
}
