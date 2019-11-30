export class UrlConstant {

  public static readonly BASE = process.env.REACT_APP_API_BASE_URL;

  public static readonly LOGIN = UrlConstant.BASE + '/login';
  public static readonly LOGOUT = UrlConstant.BASE + '/logout';

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

  private static readonly CHAT_PREFIX = UrlConstant.BASE + '/chat';
  public static readonly Chat = {
    STATE: UrlConstant.CHAT_PREFIX + '/state',
    CHANNEL: UrlConstant.CHAT_PREFIX + '/channels'
  };

  public static readonly WebSocket = {
    CONNECTION: UrlConstant.BASE + '/websocket'
  };

}
