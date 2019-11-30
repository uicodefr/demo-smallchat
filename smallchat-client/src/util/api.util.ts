import { GlobalInfoContextType } from '../context/GlobalInfoContext';
import { AlertType } from '../const/alert-type.const';

export class ApiUtil {

  constructor(private globalInfoContext: GlobalInfoContextType) {}

  public get<T>(url: string): Promise<T> {
    return this.fetch(url, {});
  }

  public post<T>(url: string, body: object): Promise<T> {
    return this.fetchWithBody('POST', url, body);
  }

  public patch<T>(url: string, body: object): Promise<T> {
    return this.fetchWithBody('PATCH', url, body);
  }

  public delete<T>(url: string): Promise<T> {
    return this.fetch(url, { method: 'DELETE' });
  }

  private fetchWithBody<T>(
    method: string,
    url: string,
    body: object
  ): Promise<T> {
    const requestInit = {
      method: method
    } as RequestInit;
    if (body) {
      requestInit.body = JSON.stringify(body);
    }
    return this.fetch(url, requestInit);
  }

  private fetch<T>(url: string, requestInit: RequestInit): Promise<T> {
    this.globalInfoContext.notifLoader(true);

    requestInit.headers = {
      'content-type': 'application/json'
    };

    return new Promise((resolve, reject) => {
      window
        .fetch(url, requestInit)
        .then(response => {
          // Check Response Code
          if (response.status >= 400) {
            this.handleError(response, url);
            reject(response.status);
          } else {
            response
              .text()
              .then(textBody => {
                if (textBody.length > 0) {
                  const object = JSON.parse(textBody);
                  if (object) {
                    resolve(object);
                  } else {
                    this.handleError('Invalid Json', url);
                    reject('Invalid Json');
                  }
                } else {
                  // Empty body
                  resolve(null);
                }
              })
              .catch(error => {
                // Body error
                this.handleError(error, url);
                reject(error);
              });
          }
        })
        .catch(error => {
          // Http Error
          this.handleError(error, url);
          reject(error);
        })
        .finally(() => {
          this.globalInfoContext.notifLoader(false);
        });
    });
  }

  private handleError(error, url) {
    let errMsg = url + ' - ';
    if (error instanceof Response) {
      errMsg += error.statusText;
    } else if ('string' === typeof error) {
      errMsg += error;
    } else {
      errMsg += 'Unknown error';
      console.error(error);
    }
    this.globalInfoContext.showAlert(
      AlertType.DANGER,
      'Technical Error : ' + errMsg
    );
  }

}
