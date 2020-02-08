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
import { ChatService } from '../../service/chat/chat.service';
import { GlobalInfoService } from '../../service/util/global-info.service';
import { WebSocketService } from '../../service/chat/websocket.service';
import { ChatStateModel } from '../../model/chat/chat-state.model';
import { Subscription } from 'rxjs';
import { HasRoleUser } from '../shared/security/HasRoleUser';
import { TransitionGroup, CSSTransition } from 'react-transition-group';

interface Props {}

interface State {
  chatState: ChatStateModel;
  settings: boolean;
  editChannels: Array<ChannelModel>;
  currentChannel: ChannelModel;
  showSaveDialog: boolean;
  showDeleteDialog: boolean;
}

export class ChannelsCard extends React.Component<Props, State> {
  private globalInfoService: GlobalInfoService;
  private chatService: ChatService;
  private webSocketService: WebSocketService;

  private chatStateSubscription: Subscription;

  constructor(props: Props) {
    super(props);

    this.globalInfoService = GlobalInfoService.get();
    this.chatService = ChatService.get();
    this.webSocketService = WebSocketService.get();

    this.state = {
      chatState: this.webSocketService.getChatState(),
      settings: false,
      editChannels: [],
      currentChannel: null,
      showSaveDialog: null,
      showDeleteDialog: false
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
    this.chatStateSubscription = this.webSocketService.getChatStateObservable().subscribe(chatState => {
      if (!chatState) {
        return;
      }

      this.setState({
        chatState: chatState,
        editChannels: [...chatState.channels]
      });
    });
  }

  componentWillUnmount() {
    if (this.chatStateSubscription) {
      this.chatStateSubscription.unsubscribe();
    }
  }

  handleClickSettings(event) {
    if (!this.state.chatState) {
      return;
    }
    if (this.state.settings) {
      this.setState({
        settings: false
      });
    } else {
      this.setState({
        settings: true,
        editChannels: [...this.state.chatState.channels]
      });
    }
  }

  handleClickEdit(channel: ChannelModel) {
    this.setState({
      showSaveDialog: true,
      currentChannel: { ...channel }
    });
  }

  handleClickCreate() {
    this.setState({
      showSaveDialog: true,
      currentChannel: new ChannelModel()
    });
  }

  handleCancelSave() {
    this.setState({
      showSaveDialog: false
    });
  }

  handleConfirmSave(channel: ChannelModel) {
    this.setState({
      showSaveDialog: false
    });

    if (this.state.currentChannel.id) {
      this.chatService.updateChannel(channel).then(() => {
        this.globalInfoService.showAlert(AlertType.SUCCESS, 'Channel "' + channel.id + '" updated');
      });
    } else {
      this.chatService.createChannel(channel).then(() => {
        this.globalInfoService.showAlert(AlertType.SUCCESS, 'Channel "' + channel.id + '" created');
      });
    }
  }

  handleClickDelete(channel: ChannelModel) {
    this.setState({
      showDeleteDialog: true,
      currentChannel: { ...channel }
    });
  }

  handleConfirmDelete(confirm: boolean) {
    this.setState({
      showDeleteDialog: false
    });
    if (confirm) {
      this.chatService.deleteChannel(this.state.currentChannel.id).then(() => {
        this.globalInfoService.showAlert(AlertType.SUCCESS, 'Channel "' + this.state.currentChannel.id + '" deleted');
      });
    }
  }

  render() {
    const chatState = this.state.chatState;
    const buttonClassName = this.state.settings ? 'toogleOn' : '';

    return (
      <Card className="ChannelsCard">
        <Card.Header>
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
                {this.state.settings ? (
                  <TransitionGroup>
                    {this.state.editChannels.map(channel => (
                      <CSSTransition key={channel.id} timeout={300} classNames="transitionFade">
                        <ListGroup.Item title={channel.id} className="smallItem">
                          {channel.name}
                          <span className="channelButtonZone float-right">
                            <Button
                              variant="primary"
                              size="sm"
                              className="fa fa-pencil"
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
                    ))}
                  </TransitionGroup>
                ) : (
                  <TransitionGroup>
                    {chatState.channels.map(channel => (
                      <CSSTransition key={channel.id} timeout={300} classNames="transitionFade">
                        <LinkContainer to={'/c/' + channel.id}>
                          <ListGroup.Item action className="smallItem">
                            {channel.name}
                          </ListGroup.Item>
                        </LinkContainer>
                      </CSSTransition>
                    ))}
                  </TransitionGroup>
                )}
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
