import { ChatUserModel } from './chat-user.model';
import { ChannelStateModel } from './channel-state.model';

export class ChatStateModel {
  channels: Array<ChannelStateModel>;
  users: Array<ChatUserModel>;
  updateDate: Date;
}
