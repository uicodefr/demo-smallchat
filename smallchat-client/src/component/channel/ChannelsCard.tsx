import './ChannelsCard.scss';
import React from 'react';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import ListGroup from 'react-bootstrap/ListGroup';
import { ChannelModel } from '../../model/chat/channel.model';
import { ConfirmDialogCommon } from '../shared/dialog/ConfirmDialog.common';
import { SaveChannelDialog } from './dialog/SaveChannelDialog';
import { AlertType } from '../../const/alert-type.const';
import { LinkContainer } from 'react-router-bootstrap';
import { ChatStateService } from '../../service/chat/chat-state.service';
import { GlobalInfoService } from '../../service/util/global-info.service';
import { ChatService } from '../../service/chat/chat.service';
import { ChatStateModel } from '../../model/chat/chat-state.model';
import { ChannelStateModel } from '../../model/chat/channel-state.model';
import { Subscription } from 'rxjs';
import { HasRoleUser } from '../shared/security/HasRoleUser';
import { TransitionGroup, CSSTransition } from 'react-transition-group';
import Badge from 'react-bootstrap/Badge';
import { myDi } from '../../util/my-di';

interface Props {}

interface State {
  chatState: ChatStateModel | null;
  settings: boolean;
  editChannels: Array<ChannelStateModel>;
  currentChannel: ChannelStateModel | null;
  showSaveDialog: boolean;
  showDeleteDialog: boolean;
}

export class ChannelsCard extends React.Component<Props, State> {
  private globalInfoService: GlobalInfoService;
  private chatStateService: ChatStateService;
  private chatService: ChatService;

  private chatStateSubscription: Subscription;

  constructor(props: Props) {
    super(props);

    this.globalInfoService = myDi.get('GlobalInfoService');
    this.chatStateService = myDi.get('ChatStateService');
    this.chatService = myDi.get('ChatService');

    this.state = {
      chatState: this.chatService.getChatState(),
      settings: false,
      editChannels: [],
      currentChannel: null,
      showSaveDialog: false,
      showDeleteDialog: false,
    };

    this.handleClickSettings = this.handleClickSettings.bind(this);
    this.handleClickEdit = this.handleClickEdit.bind(this);
    this.handleClickCreate = this.handleClickCreate.bind(this);
    this.handleCancelSave = this.handleCancelSave.bind(this);
    this.handleConfirmSave = this.handleConfirmSave.bind(this);
    this.handleClickDelete = this.handleClickDelete.bind(this);
    this.handleConfirmDelete = this.handleConfirmDelete.bind(this);
  }

  componentDidMount() {
    this.chatStateSubscription = this.chatService.getChatStateObservable().subscribe((chatState) => {
      if (!chatState) {
        return;
      }

      this.setState({
        chatState: chatState,
        editChannels: [...chatState.channels],
      });
    });
  }

  componentWillUnmount() {
    if (this.chatStateSubscription) {
      this.chatStateSubscription.unsubscribe();
    }
  }

  handleClickSettings(event: React.MouseEvent) {
    if (!this.state.chatState) {
      return;
    }
    if (this.state.settings) {
      this.setState({
        settings: false,
      });
    } else {
      this.setState({
        settings: true,
        editChannels: [...this.state.chatState.channels],
      });
    }
  }

  handleClickEdit(channel: ChannelStateModel) {
    this.setState({
      showSaveDialog: true,
      currentChannel: { ...channel },
    });
  }

  handleClickCreate() {
    this.setState({
      showSaveDialog: true,
      currentChannel: new ChannelStateModel(),
    });
  }

  handleCancelSave() {
    this.setState({
      showSaveDialog: false,
    });
  }

  handleConfirmSave(channel: ChannelModel) {
    this.setState({
      showSaveDialog: false,
    });

    if (this.state.currentChannel?.id) {
      this.chatStateService.updateChannel(channel).then(() => {
        this.globalInfoService.showAlert(AlertType.SUCCESS, 'Channel "' + channel.id + '" updated');
      });
    } else {
      this.chatStateService.createChannel(channel).then(() => {
        this.globalInfoService.showAlert(AlertType.SUCCESS, 'Channel "' + channel.id + '" created');
      });
    }
  }

  handleClickDelete(channel: ChannelStateModel) {
    this.setState({
      showDeleteDialog: true,
      currentChannel: { ...channel },
    });
  }

  handleConfirmDelete(confirm: boolean) {
    this.setState({
      showDeleteDialog: false,
    });
    if (confirm && this.state.currentChannel) {
      this.chatStateService.deleteChannel(this.state.currentChannel.id).then(() => {
        this.globalInfoService.showAlert(AlertType.SUCCESS, 'Channel "' + this.state.currentChannel?.id + '" deleted');
      });
    }
  }

  render() {
    const chatState = this.state.chatState;
    const buttonClassName = this.state.settings ? 'toogleOn' : '';

    return (
      <Card className="ChannelsCard">
        <Card.Header>
          <i className="far fa-comments mr-2"></i>
          Channels
          <HasRoleUser>
            <Button
              className={'fa fa-cog float-right ' + buttonClassName}
              variant="primary"
              title="Settings"
              size="sm"
              onClick={this.handleClickSettings}
            ></Button>
          </HasRoleUser>
        </Card.Header>
        <Card.Body>
          {chatState ? (
            <>
              <ListGroup variant="flush">
                <TransitionGroup component={null}>
                  {this.state.settings
                    ? this.state.editChannels.map((channel) => (
                        <CSSTransition key={channel.id} timeout={300} classNames="transitionFade">
                          <ListGroup.Item title={channel.id} className="smallItem">
                            {channel.name}
                            <span className="channelButtonZone float-right">
                              <Button
                                variant="primary"
                                size="sm"
                                className="fa fa-pen"
                                title="Edit"
                                onClick={() => this.handleClickEdit(channel)}
                              ></Button>
                              <Button
                                variant="danger"
                                size="sm"
                                className="fa fa-trash"
                                title="Delete"
                                onClick={() => this.handleClickDelete(channel)}
                              ></Button>
                            </span>
                          </ListGroup.Item>
                        </CSSTransition>
                      ))
                    : chatState.channels.map((channel) => (
                        <CSSTransition key={channel.id} timeout={300} classNames="transitionFade">
                          <LinkContainer to={'/c/' + channel.id}>
                            <ListGroup.Item action className="smallItem">
                              {channel.subscribed ? (
                                <>
                                  <strong>{channel.name}</strong>
                                  {channel.unreadMessages ? (
                                    <Badge className="badgeUnread" pill variant="warning">
                                      {channel.unreadMessages}
                                    </Badge>
                                  ) : null}
                                </>
                              ) : (
                                channel.name
                              )}
                            </ListGroup.Item>
                          </LinkContainer>
                        </CSSTransition>
                      ))}
                </TransitionGroup>
              </ListGroup>

              {this.state.settings ? (
                <div className="generalButtonZone">
                  <Button variant="primary" size="sm" onClick={this.handleClickCreate}>
                    Create new channel
                  </Button>
                  <Button variant="secondary" onClick={this.handleClickSettings}>
                    Close
                  </Button>
                </div>
              ) : null}
            </>
          ) : (
            <p> - </p>
          )}
        </Card.Body>

        {this.state.currentChannel ? (
          <>
            <SaveChannelDialog
              channel={this.state.currentChannel}
              show={this.state.showSaveDialog}
              onCancel={this.handleCancelSave}
              onSave={this.handleConfirmSave}
            ></SaveChannelDialog>

            <ConfirmDialogCommon
              confirmTitle="Delete channel"
              confirmLabel="Are you sure to delete this channel ?"
              detailLabel={this.state.currentChannel.name}
              show={this.state.showDeleteDialog}
              onConfirm={this.handleConfirmDelete}
            ></ConfirmDialogCommon>
          </>
        ) : null}
      </Card>
    );
  }
}
