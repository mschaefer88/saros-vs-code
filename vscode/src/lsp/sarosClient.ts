import { LanguageClient, LanguageClientOptions, StreamInfo } from "vscode-languageclient";
import { ConnectedStateNotification, SessionStateNotification } from "./sarosProtocol";

type Callback<T> = (p: T) => void;

/**
 * Custom language client for Saros protocol.
 *
 * @export
 * @class SarosClient
 * @extends {LanguageClient}
 */
export class SarosClient extends LanguageClient {

    private _connectionChangedListeners: Callback<boolean>[] = [];
    private _sessionChangedListeners: Callback<boolean>[] = [];

    constructor(serverOptions: (() => Promise<StreamInfo>), clientOptions: LanguageClientOptions) {
        super('saros', 'Saros Server', serverOptions, clientOptions, true);//TODO: get from config

        this.onReady().then(() => {
            this.onNotification(ConnectedStateNotification.type, isOnline => {
                this._connectionChangedListeners.forEach(callback => callback(isOnline.result));
            });
    
            this.onNotification(SessionStateNotification.type, inSession => {
                this._sessionChangedListeners.forEach(callback => callback(inSession.result));
            });
        });
    }

    public onConnectionChanged(callback: Callback<boolean>) {
        this._connectionChangedListeners.push(callback);
    }

    public onSessionChanged(callback: Callback<boolean>) {
        this._sessionChangedListeners.push(callback);
    }
}