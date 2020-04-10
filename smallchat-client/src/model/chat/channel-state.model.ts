import { ChannelModel } from './channel.model';

export class ChannelStateModel extends ChannelModel {
  subscribed = false;
  unreadMessages = 0;
}
