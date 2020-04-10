import { CountLikesModel } from '../../model/global/count-likes.model';
import { UrlConstant } from '../../const/url-constant';
import { RestClientService } from '../util/rest-client.service';
import { myDi } from '../../util/my-di';

export class GlobalService {
  private restClientService: RestClientService;

  public constructor() {
    this.restClientService = myDi.get(RestClientService);
  }

  public countLike(): Promise<CountLikesModel> {
    return this.restClientService.get(UrlConstant.Global.LIKE_COUNT);
  }

  public addLike() {
    return this.restClientService.post(UrlConstant.Global.LIKE, null);
  }
}
