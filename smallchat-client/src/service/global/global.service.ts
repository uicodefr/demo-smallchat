import { CountLikesModel } from '../../model/global/count-likes.model';
import { UrlConstant } from '../../const/url-constant';
import { RestClientService } from '../util/rest-client.service';

export class GlobalService {
  private static readonly INSTANCE = new GlobalService();

  private restClientService: RestClientService = null;

  private constructor() {
    this.restClientService = RestClientService.get();
  }
  public static get(): GlobalService {
    return this.INSTANCE;
  }

  public countLike(): Promise<CountLikesModel> {
    return this.restClientService.get(UrlConstant.Global.LIKE_COUNT);
  }

  public addLike() {
    return this.restClientService.post(UrlConstant.Global.LIKE, null);
  }
}
