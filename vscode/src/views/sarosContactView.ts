import {Disposable} from 'vscode-languageclient';
import {SarosClient, ContactDto, ContactStateNotification, IsOnlineRequest, GetAllContactRequest, SarosExtension} from '../lsp';
import {messages} from './labels';
import {TreeDataProvider, ExtensionContext, EventEmitter, Event, TreeItem, TreeView, window, commands} from 'vscode';
import {icons} from '../utils/icons';
import {variables} from './variables';

export class SarosContactProvider implements TreeDataProvider<ContactDto> {
	private _contacts: ContactDto[];

	constructor(client: SarosClient, private _context: ExtensionContext) {
	  this._contacts = [];

	  client.onNotification(ContactStateNotification.type, (contact: ContactDto) => {
	    this.remove(contact);
	    if (contact.subscribed) {
	      this._contacts.push(contact);
	    }

	    this.refresh();
	  });
	}

	private remove(contact: ContactDto): void {
	  const contactIndex = this._contacts.findIndex((c) => c.id === contact.id);
	  if (contactIndex >= 0) {
	    this._contacts.splice(contactIndex, 1);
	  }
	}

	refresh(): void {
	  this._onDidChangeTreeData.fire(undefined);
	}

	clear(): void {
	  this._contacts = [];
	  this.refresh();
	}

    private _onDidChangeTreeData: EventEmitter<ContactDto | undefined> = new EventEmitter<ContactDto | undefined>();
	readonly onDidChangeTreeData: Event<ContactDto | undefined> = this._onDidChangeTreeData.event;

	getTreeItem(element: ContactDto): TreeItem | Thenable<TreeItem> {
	  const contactItem = new TreeItem(element.nickname);

	  if (element.isOnline) {
	    if (element.hasSarosSupport) {
	      contactItem.iconPath = icons.getSarosSupportIcon(this._context);
	    } else {
	      contactItem.iconPath = icons.getIsOnlineIcon(this._context);
	    }
	  } else {
	    contactItem.iconPath = icons.getIsOfflinetIcon(this._context);
	  }

	  contactItem.tooltip = element.id;
	  contactItem.description = element.id;
	  contactItem.contextValue = 'contact';

	  return contactItem;
	}

	getChildren(element?: ContactDto | undefined): ContactDto[] {
	  if (!element) {
	    const sorted = this._contacts.sort((a: ContactDto, b: ContactDto) => {
	      const valA = +a.hasSarosSupport + +a.isOnline;
	      const valB = +b.hasSarosSupport + +b.isOnline;

	      if (valA === valB) {
	        return a.nickname > b.nickname ? -1 : 1;
	      }

	      return valA > valB ? -1 : 1;
	    });

	    return sorted;
	  }

	  return [];
	}
}

export class SarosContactView implements Disposable {
    private _provider!: SarosContactProvider;
    private _view!: TreeView<ContactDto>;

    dispose(): void {
      this._view.dispose();
    }

    constructor(extension: SarosExtension) {
      extension.client.onReady().then(() => {
        this._provider = new SarosContactProvider(extension.client, extension.context);
        this._view = window.createTreeView('saros-contacts', {treeDataProvider: this._provider});

        this._setOnline(false);

        extension.client.onConnectionChanged((isOnline: boolean) => {
          this._provider.refresh();
          this._setOnline(isOnline);
        });
      });
    }

    private _setOnline(isOnline: boolean): void {
      if (!isOnline) {
        this._view.message = messages.NOT_CONNECTED;
        this._provider.clear();
      } else {
        this._view.message = '';
      }

      variables.setConnectionActive(isOnline);
    }
}
