import {Disposable} from 'vscode-languageclient';
import {SarosClient, ContactDto, UserJoinedSessionNotification, UserLeftSessionNotification, SessionUserDto, SarosExtension, SarosResultResponse} from '../lsp';
import {messages} from './labels';
import {TreeDataProvider, ExtensionContext, window, EventEmitter, Event, TreeItem, TreeView, commands} from 'vscode';
import {variables} from './variables';

export class SarosSessionProvider implements TreeDataProvider<SessionUserDto> {
	private _users: SessionUserDto[];

	constructor(client: SarosClient, private _context: ExtensionContext) {
	  this._users = [];

	  client.onNotification(UserJoinedSessionNotification.type, (user: SessionUserDto) => {
	    window.showInformationMessage(`'${user.nickname}' joined the session.`);
	    const userIndex = this._users.findIndex((user) => user.id === user.id);
	    if (userIndex < 0) {
	      this._users.push(user);
	    }
	    this.refresh();
	  });

	  client.onNotification(UserLeftSessionNotification.type, (user: SessionUserDto) => {
	    const userIndex = this._users.findIndex((user) => user.id === user.id);

	    if (userIndex < 0) {
	      window.showErrorMessage('Couldn\'t determine user that has left the session.');
	      return;
	    }

	    window.showInformationMessage(`'${user.nickname}' left the session.`);
	    this._users.splice(userIndex, 1);
	    this.refresh();
	  });
	}

	refresh(): void {
	  this._onDidChangeTreeData.fire(undefined);
	}

	clear(): void {
	  this._users = [];
	  this.refresh();
	}

    private _onDidChangeTreeData: EventEmitter<ContactDto | undefined> = new EventEmitter<ContactDto | undefined>();
	readonly onDidChangeTreeData: Event<ContactDto | undefined> = this._onDidChangeTreeData.event;

	getTreeItem(element: SessionUserDto): TreeItem | Thenable<TreeItem> {
	  const contactItem = new TreeItem(element.nickname);

	  contactItem.tooltip = element.id;
	  contactItem.description = element.id;
	  contactItem.contextValue = 'user';
	  contactItem.iconPath = this._context.asAbsolutePath('/media/obj16/contact_saros_obj.png'); // TODO: host?

	  return contactItem;
	}

	getChildren(element?: SessionUserDto | undefined): SessionUserDto[] {
	  if (!element) {
	    const contacts = this._users;

	    const sorted = contacts.sort((a, b) => {
	      return a.nickname > b.nickname ? -1 : 1;
	    });

	    return sorted;
	  }

	  return [];
	}
}

export class SarosSessionView implements Disposable {
    private _provider!: SarosSessionProvider;
    private _viewMain!: TreeView<SessionUserDto>;
    private _viewSub!: TreeView<SessionUserDto>;

    private _isOnline: boolean = false;

    dispose(): void {
      this._viewMain.dispose();
      this._viewSub.dispose();
    }

    constructor(extension: SarosExtension) {
      extension.client.onReady().then(() => {
        this._provider = new SarosSessionProvider(extension.client, extension.context);
        this._viewMain = window.createTreeView('saros-session-main', {treeDataProvider: this._provider});
        this._viewSub = window.createTreeView('saros-session-sub', {treeDataProvider: this._provider});

        this._setSession(false);

        extension.client.onConnectionChanged((isOnline: boolean) => {
          this._isOnline = isOnline;
          this._setSession(false);
        });

        extension.client.onSessionChanged((inSession: boolean) => {
          this._setSession(inSession);
        });
      });
    }

    private _setSession(inSession: boolean): void {
      if (!this._isOnline) {
        this._viewMain.message = messages.NOT_CONNECTED;
      } else if (!inSession) {
        this._viewMain.message = messages.NO_SESSION;
      } else {
        this._viewMain.message = undefined;
      }

      if (!this._isOnline || !inSession) {
        this._provider.clear();
      }

      variables.setSessionActive(inSession);
    }
}
