import { IdEntityModel } from '../id-entity.model';

export class ChannelModel extends IdEntityModel {
  public static readonly ID_PATTERN = '^[a-z0-9-_]{4,}$';

  name: string;
  description: string;
}
