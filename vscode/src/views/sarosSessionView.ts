import {Disposable} from 'vscode-languageclient';
import {
  SarosClient,
  ContactDto,
  UserJoinedSessionNotification,
  UserLeftSessionNotification,
  SessionUserDto,
  SarosExtension,
} from '../lsp';
import {messages} from './labels';
import {
  TreeDataProvider,
  ExtensionContext,
  window,
  EventEmitter,
  Event,
  TreeItem,
  TreeView,
} from 'vscode';
import {variables} from './variables';

/**
 * Provider for users that are in the session.
 *
 * @export
 * @class SarosContactProvider
 * @implements {TreeDataProvider<ContactDto>}
 */
export class SarosSessionProvider implements TreeDataProvider<SessionUserDto> {
  private _users: SessionUserDto[];

  /**
   * Creates an instance of SarosSessionProvider.
   *
   * @param {SarosClient} client The Saros client
   * @param {ExtensionContext} _context The context of the extension
   * @memberof SarosSessionProvider
   */
  constructor(client: SarosClient, private _context: ExtensionContext) {
    this._users = [];
    this.onDidChangeTreeData = this._onDidChangeTreeData.event;

    client.onNotification(UserJoinedSessionNotification.type,
        (user: SessionUserDto) => {
          window.showInformationMessage(
              `'${user.nickname}' joined the session.`,
          );
          const userIndex = this._users.findIndex(
              (user) => user.id === user.id,
          );
          if (userIndex < 0) {
            this._users.push(user);
          }
          this.refresh();
        });

    client.onNotification(UserLeftSessionNotification.type,
        (user: SessionUserDto) => {
          const userIndex = this._users.findIndex(
              (user) => user.id === user.id,
          );

          if (userIndex < 0) {
            window.showErrorMessage(
                'Couldn\'t determine user that has left the session.',
            );
            return;
          }

          window.showInformationMessage(`'${user.nickname}' left the session.`);
          this._users.splice(userIndex, 1);
          this.refresh();
        });
  }

  /**
   * Refreshes the user list.
   *
   * @memberof SarosSessionProvider
   */
  refresh(): void {
    this._onDidChangeTreeData.fire(undefined);
  }

  /**
   * Clears the user list.
   *
   * @memberof SarosSessionProvider
   */
  clear(): void {
    this._users = [];
    this.refresh();
  }

  private _onDidChangeTreeData: EventEmitter<ContactDto | undefined> =
    new EventEmitter<ContactDto | undefined>();
  readonly onDidChangeTreeData: Event<ContactDto | undefined>;

  /**
   * Converts the user to a tree item.
   *
   * @param {SessionUserDto} element User to convert
   * @return {(TreeItem | Thenable<TreeItem>)} The converted user
   * @memberof SarosSessionProvider
   */
  getTreeItem(element: SessionUserDto): TreeItem | Thenable<TreeItem> {
    const contactItem = new TreeItem(element.nickname);

    contactItem.tooltip = element.id;
    contactItem.description = element.id;
    contactItem.contextValue = 'user';
    contactItem.iconPath =
      this._context.asAbsolutePath('/media/obj16/contact_saros_obj.png');

    return contactItem;
  }

  /**
   * Gets the children of a user.
   *
   * @param {(SessionUserDto | undefined)} [element]
   * @return {SessionUserDto[]} A sorted list of users on root
   *  level and empty otherwise
   * @memberof SarosSessionProvider
   */
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

/**
 * View that displays users that are in the session.
 *
 * @export
 * @class SarosSessionView
 * @implements {Disposable}
 */
export class SarosSessionView implements Disposable {
  private _provider!: SarosSessionProvider;
  private _viewMain!: TreeView<SessionUserDto>;
  private _viewSub!: TreeView<SessionUserDto>;

  private _isOnline: boolean = false;

  /**
   * Disposes all disposable resources.
   *
   * @memberof SarosSessionView
   */
  dispose(): void {
    this._viewMain.dispose();
    this._viewSub.dispose();
  }

  /**
   * Creates an instance of SarosSessionView.
   *
   * @param {SarosExtension} extension
   * @memberof SarosSessionView
   */
  constructor(extension: SarosExtension) {
    extension.client.onReady().then(() => {
      this._provider =
        new SarosSessionProvider(extension.client, extension.context);
      this._viewMain =
        window.createTreeView('saros-session-main',
            {treeDataProvider: this._provider});
      this._viewSub =
        window.createTreeView('saros-session-sub',
            {treeDataProvider: this._provider});

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

  /**
   * Sets the session state.
   *
   * @private
   * @param {boolean} inSession The session state
   * @memberof SarosSessionView
   */
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
