import './Info.scss';
import React from 'react';
import { appInfo } from '../app.info';
import { RestClientService } from '../service/util/rest-client.service';
import { myDi } from '../util/my-di';
import { UrlConstant } from '../const/url-constant';

interface Props {}

interface State {
  serverInfo: any;
}

export class Info extends React.Component<Props, State> {
  private clientInfo = appInfo;

  private restClientService: RestClientService;

  public constructor(props: Props) {
    super(props);

    this.restClientService = myDi.get('RestClientService');

    this.state = {
      serverInfo: null,
    };
  }

  componentDidMount() {
    this.restClientService.get(UrlConstant.Global.INFO).then((info) => this.setState({ serverInfo: info }));
  }

  render() {
    return (
      <div className="info">
        <h1>Client Info</h1>

        <pre>{this.toJson(this.clientInfo)}</pre>

        <h1>Server Info</h1>

        <pre>{this.toJson(this.state.serverInfo)}</pre>
      </div>
    );
  }

  private toJson(object: any): string {
    return JSON.stringify(object, null, 2);
  }
}
