import { ChannelModel } from '../chat/channel.model';
import { ChannelMessage } from './channel-message';

export class ChannelFullModel extends ChannelModel {
  messages: Array<ChannelMessage>;
}
