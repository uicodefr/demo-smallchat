import { UrlConstant } from '../../const/url-constant';
import { CountLikesModel } from '../../model/global/count-likes.model';
import { UserModel } from '../../model/global/user.model';

export class RestClientServiceMock {
  public get = jest.fn((url) => {
    switch (url) {
      case UrlConstant.User.CURRENT_USER:
        return Promise.resolve({ username: 'usertest', roles: ['USER'] } as UserModel);
      case UrlConstant.Global.LIKE_COUNT:
        return Promise.resolve({ count: 42 } as CountLikesModel);
      default:
        throw Error('invalid get call with url : ' + url);
    }
  });

  logout = jest.fn().mockResolvedValue(null);
}
