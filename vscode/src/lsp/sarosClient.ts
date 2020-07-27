import {
  LanguageClient,
  LanguageClientOptions,
} from 'vscode-languageclient';
import {
  ConnectedStateNotification,
  SessionStateNotification,
} from './sarosProtocol';
import {LanguageServerOptions} from './sarosServer';

type Callback<T> = (p: T) => void;

/**
 * Custom language client for the Saros protocol.
 *
 * @export
 * @class SarosClient
 * @extends {LanguageClient}
 */
export class SarosClient extends LanguageClient {
    private _connectionChangedListeners: Callback<boolean>[] = [];
    private _sessionChangedListeners: Callback<boolean>[] = [];

    /**
     * Creates an instance of SarosClient.
     *
     * @param {LanguageServerOptions} serverOptions The server options
     * @param {LanguageClientOptions} clientOptions The client options
     * @memberof SarosClient
     */
    constructor(serverOptions: LanguageServerOptions,
        clientOptions: LanguageClientOptions) {
      super('saros', 'Saros Server', serverOptions, clientOptions, true);

      this.onReady().then(() => {
        this.onNotification(ConnectedStateNotification.type, (isOnline) => {
          this._connectionChangedListeners.forEach(
              (callback) => callback(isOnline.result),
          );
        });

        this.onNotification(SessionStateNotification.type, (inSession) => {
          this._sessionChangedListeners.forEach(
              (callback) => callback(inSession.result),
          );
        });
      });
    }

    /**
     * Registers a callback for the connection changed event.
     *
     * @param {Callback<boolean>} callback The callback to execute
     * @memberof SarosClient
     */
    public onConnectionChanged(callback: Callback<boolean>) {
      this._connectionChangedListeners.push(callback);
    }

    /**
     * Registers a callback for the session changed event.
     *
     * @param {Callback<boolean>} callback The callback to execute
     * @memberof SarosClient
     */
    public onSessionChanged(callback: Callback<boolean>) {
      this._sessionChangedListeners.push(callback);
    }
}
