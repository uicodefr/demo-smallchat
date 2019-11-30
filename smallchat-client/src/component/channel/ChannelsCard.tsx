import './ChannelsCard.scss';
import { WebSocketContextType } from "../../context/WebSocketContext";
import React from "react";
import Card from "react-bootstrap/Card";
import { HasRoleUser } from "../shared/functional/user.common";
import Button from "react-bootstrap/Button";
import ListGroup from "react-bootstrap/ListGroup";
import { withAutoContext } from "../../util/hoc.util";
import { ChannelModel } from "../../model/chat/channel.model";
import { ConfirmDialogCommon } from '../shared/dialog/ConfirmDialog.common';
import { SaveChannelDialog } from './dialog/SaveChannelDialog';
import { ChatApi } from '../../api/chat.api';
import { GlobalInfoContextType } from '../../context/GlobalInfoContext';
import { AlertType } from '../../const/alert-type.const';
import { LinkContainer } from 'react-router-bootstrap';

interface Props {
  globalInfoContext: GlobalInfoContextType,
  webSocketContext: WebSocketContextType
}

interface State {
  settings: boolean
  editChannels: Array<ChannelModel>,
  currentChannel: ChannelModel,
  showSaveDialog: boolean,
  showDeleteDialog: boolean
}

class ChannelsCard extends React.Component<Props, State> {

  private chatApi: ChatApi;

  constructor(props: Props) {
    super(props);

    this.state = {
      settings: false,
      editChannels: [],
      currentChannel: null,
      showSaveDialog: null,
      showDeleteDialog: false
    };

    this.chatApi = new ChatApi(this.props.globalInfoContext);

    this.handleClickSettings = this.handleClickSettings.bind(this);
    this.handleClickEdit = this.handleClickEdit.bind(this);
    this.handleClickCreate = this.handleClickCreate.bind(this);
    this.handleCancelSave = this.handleCancelSave.bind(this);
    this.handleConfirmSave = this.handleConfirmSave.bind(this);
    this.handleClickDelete = this.handleClickDelete.bind(this);
    this.handleConfirmDelete = this.handleConfirmDelete.bind(this);
  }

  componentDidUpdate(prevProps) {
    if (this.props.webSocketContext?.chatState?.channels !== prevProps.webSocketContext?.chatState?.channels) {
      this.setState({
        editChannels: [...this.props.webSocketContext.chatState.channels]
      });
    }
  }

  handleClickSettings(event) {
    if (!this.props.webSocketContext.chatState) {
      return;
    }
    if (this.state.settings) {
      this.setState({
        settings: false
      });
    } else {
      this.setState({
        settings: true,
        editChannels: [...this.props.webSocketContext.chatState.channels]
      });
    }
  }

  handleClickEdit(channel: ChannelModel) {
    this.setState({
      showSaveDialog: true,
      currentChannel: {...channel}
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
      this.chatApi.updateChannel(channel).then(() => {
        this.props.globalInfoContext.showAlert(AlertType.SUCCESS, 'Channel "' + channel.id + '" updated');
      });
    } else {
      this.chatApi.createChannel(channel).then(() => {
        this.props.globalInfoContext.showAlert(AlertType.SUCCESS, 'Channel "' + channel.id + '" created');
      });
    }
  }

  handleClickDelete(channel: ChannelModel) {
    this.setState({
      showDeleteDialog: true,
      currentChannel: {...channel}
    });
  }

  handleConfirmDelete(confirm: boolean) {
    this.setState({
      showDeleteDialog: false
    });
    if (confirm) {
      this.chatApi.deleteChannel(this.state.currentChannel.id).then(() => {
        this.props.globalInfoContext.showAlert(AlertType.SUCCESS, 'Channel "' + this.state.currentChannel.id + '" deleted');
      });
    }
  }

  render() {
    const chatState = this.props.webSocketContext ? this.props.webSocketContext.chatState : null;
    const buttonClassName = this.state.settings ? 'toogleOn' : '';

    return (
      <Card className="ChannelsCard">
        <Card.Header>
          Channels
          <HasRoleUser userRole="USER">
              <Button className={"fa fa-cog float-right " + buttonClassName}
                variant="primary" title="Settings" size="sm" onClick={this.handleClickSettings}>
              </Button>
          </HasRoleUser>
        </Card.Header>
        <Card.Body>
          {chatState ? (
            <>
            <ListGroup variant="flush">
              {this.state.settings ? (
                this.state.editChannels.map(channel =>
                  <ListGroup.Item key={channel.id} title={channel.id}>
                    {channel.name}
                    <span className="channelButtonZone float-right">
                      <Button variant="primary" size="sm" className="fa fa-pencil" title="Edit" onClick={() => this.handleClickEdit(channel)}>
                      </Button>
                      <Button variant="danger" size="sm" className="fa fa-trash" title="Delete" onClick={() => this.handleClickDelete(channel)}>
                      </Button>
                    </span>
                  </ListGroup.Item>
                )
              ) : (
                chatState.channels.map(channel =>
                  <LinkContainer key={channel.id} to={"/c/" + channel.id}>
                    <ListGroup.Item action>
                      {channel.name}
                    </ListGroup.Item>
                  </LinkContainer>
                )
              )}
            </ListGroup>
            {this.state.settings ? (
              <div className="generalButtonZone">
                <Button variant="primary" size="sm" onClick={this.handleClickCreate}>Create new channel</Button>
                <Button variant="secondary" onClick={this.handleClickSettings}>Close</Button>
              </div>
            ) : null}
            </>
          ) : (
            <p> - </p>
          )}
        </Card.Body>

        {this.state.currentChannel ? (
          <ConfirmDialogCommon confirmTitle="Delete channel" confirmLabel="Are you sure to delete this channel ?"
            detailLabel={this.state.currentChannel.name} show={this.state.showDeleteDialog} onConfirm={this.handleConfirmDelete}>
          </ConfirmDialogCommon>
        ): null}

        {this.state.currentChannel ? (
          <SaveChannelDialog channel={this.state.currentChannel} show={this.state.showSaveDialog}
            onCancel={this.handleCancelSave} onSave={this.handleConfirmSave}>
          </SaveChannelDialog>
        ): null}
        
      </Card>
    );
  }
}

export default withAutoContext(ChannelsCard, ['globalInfoContext', 'webSocketContext']);
