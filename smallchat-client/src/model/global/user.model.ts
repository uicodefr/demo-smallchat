export class UserModel {
  public static readonly USERNAME_PATTERN = '^[a-z0-9-_]{2,}$';
  username: string;
  roles: Array<string>;
}
