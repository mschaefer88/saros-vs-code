import {Disposable, window, StatusBarItem, StatusBarAlignment, commands, Command} from 'vscode';
import {SarosExtension, GetAllAccountRequest, AccountDto, events, SarosClient} from '../lsp';

export class SarosAccountView implements Disposable {
    private _statusBarItem: StatusBarItem;
    private _sarosClient: SarosClient;

    constructor(extension: SarosExtension) {
      this._sarosClient = extension.client;
      this._statusBarItem = window.createStatusBarItem(StatusBarAlignment.Left);

      extension.subscribe(events.DefaultAccountChanged, (account: AccountDto) => this._refreshAccount());
      extension.subscribe(events.AccountRemoved, (account: AccountDto) => this._refreshAccount());

      this._statusBarItem.command = 'saros.account.setDefault';
      this._setAccount(undefined);

      extension.client.onReady().then(() => {
        this._statusBarItem.show();

        extension.client.sendRequest(GetAllAccountRequest.type, null).then((result) => {
          const accounts = result.result;
          const defaultAccount = accounts.find((account) => account.isDefault);
          this._setAccount(defaultAccount);
        });
      });
    }

    private _refreshAccount() {
      this._sarosClient.sendRequest(GetAllAccountRequest.type, null).then((result) => {
        const accounts = result.result;
        const defaultAccount = accounts.find((account) => account.isDefault);
        this._setAccount(defaultAccount);
      });
    }

    private _setAccount(defaultAccount: AccountDto | undefined) {
      this._statusBarItem.text = `$(account) Saros: ${defaultAccount?.username || 'n/A'}`;
      this._statusBarItem.tooltip = `Domain: ${defaultAccount?.domain} (click to change)`;
    }

    dispose() {
      this._statusBarItem.dispose();
    }
}
