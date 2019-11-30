import { ChatUserModel } from './chat-user.model';
import { ChannelModel } from './channel.model';

export class ChatStateModel {
  channels: Array<ChannelModel>;
  users: Array<ChatUserModel>;
  updateDate: Date;
}
