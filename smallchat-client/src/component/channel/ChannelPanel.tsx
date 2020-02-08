import React from 'react';
import Jumbotron from 'react-bootstrap/Jumbotron';
import { ChannelModel } from '../../model/chat/channel.model';
import './ChannelPanel.scss';
import Spinner from 'react-bootstrap/Spinner';

interface Props {
  channelId: number;
}
interface State {
  loading: boolean;
  channel: ChannelModel;
}

export class ChannelPanel extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loading: true,
      channel: null
    };
  }

  componentDidUpdate(prevProps) {
    if (this.props.channelId !== prevProps.channelId) {
      this.loadChannel();
    }
  }

  private loadChannel() {
    const channelId = this.props.channelId;
    this.setState({
      loading: true
    });
  }

  render() {
    return (
      <Jumbotron className="ChannelPanel">
        {this.state.loading ? (
          <div className="loading">
            <Spinner animation="border" variant="secondary" />
          </div>
        ) : !this.state.channel ? (
          <div className="noChannel">No Channel for the id "{this.props.channelId}"</div>
        ) : (
          <>
            <div className="channelHeader">
              <span className="channelId"> {this.state.channel.id} </span>
              <h1> {this.state.channel.name} </h1>
              <span className="channelDescription"> {this.state.channel.description} </span>
            </div>
            <div className="channelShow"></div>
            <div className="channelSend"></div>
          </>
        )}
      </Jumbotron>
    );
  }
}
