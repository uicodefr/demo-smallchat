import { GlobalInfoContextType } from '../context/GlobalInfoContext';
import { ApiUtil } from '../util/api.util';
import { CountLikesModel } from '../model/global/count-likes.model';
import { UrlConstant } from '../const/url-constant';

export class GlobalApi {

  constructor(private globalInfoContext: GlobalInfoContextType) {}

  public countLike(): Promise<CountLikesModel> {
    return new ApiUtil(this.globalInfoContext).get(
      UrlConstant.Global.LIKE_COUNT
    );
  }

  public addLike() {
    return new ApiUtil(this.globalInfoContext).post(
      UrlConstant.Global.LIKE,
      null
    );
  }

}
