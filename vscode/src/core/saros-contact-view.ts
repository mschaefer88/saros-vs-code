import * as vscode from 'vscode';
import { Disposable } from 'vscode-languageclient';
import { SarosExtension } from './saros-extension';
import { SarosClient, ContactDto, SarosResultResponse, SessionStateNotification, ContactStateNotification, IsOnlineRequest, GetAllContactRequest, ConnectedStateNotification } from './saros-client';
import { messages } from './messages';

export class SarosProvider implements vscode.TreeDataProvider<ContactDto> {

	private client: SarosClient;
	private context: vscode.ExtensionContext;

	constructor(client: SarosClient, context: vscode.ExtensionContext) {
		this.client = client;
		this.context = context;
	}

	refresh(): void {
		this._onDidChangeTreeData.fire();
	}

    private _onDidChangeTreeData: vscode.EventEmitter<ContactDto | undefined> = new vscode.EventEmitter<ContactDto | undefined>();
	readonly onDidChangeTreeData: vscode.Event<ContactDto | undefined> = this._onDidChangeTreeData.event;   
    
    getTreeItem(element: ContactDto): vscode.TreeItem | Thenable<vscode.TreeItem> {
				
		let contactItem = new vscode.TreeItem(element.nickname);
		
		if(element.isOnline) {
			if(element.hasSarosSupport) {
				console.log("SAROS");
				contactItem.iconPath = this.context.asAbsolutePath("/media/obj16/contact_saros_obj.png");
			} else {
				console.log("ONLINE");
				contactItem.iconPath = this.context.asAbsolutePath("/media/obj16/contact_obj.png");
			}
		} else {
			console.log("OFFLINE");
			contactItem.iconPath = this.context.asAbsolutePath("/media/obj16/contact_offline_obj.png");
		}

		contactItem.tooltip = element.id;
		contactItem.description = element.id;
		contactItem.contextValue = "contact";

		return contactItem;
	}
	
    async getChildren(element?: ContactDto | undefined): Promise<ContactDto[]> {

		//TODO: use sent value instead?
		if((await this.client.sendRequest(IsOnlineRequest.type, null)).result) {
			if(!element) {
				let contacts = await this.client.sendRequest(GetAllContactRequest.type, null);

				let sorted = contacts.result.sort((a, b) => {
					let valA = +a.hasSarosSupport + +a.isOnline;
					let valB = +b.hasSarosSupport + +b.isOnline;

					if(valA === valB) {
						return a.nickname > b.nickname ? -1 : 1;
					}

					return valA > valB ? -1 : 1;
				});

				return sorted;
			}
		}
		
		return [];
    }


}

export class SarosContactView implements Disposable{

    private provider!: SarosProvider;
    private view!: vscode.TreeView<ContactDto>;

	dispose(): void {
		this.view.dispose();
	}

	constructor(extension: SarosExtension) {
		
		extension.client.onReady().then(() => {
			this.provider = new SarosProvider(extension.client, extension.context);
            this.view = vscode.window.createTreeView('saros-contacts', { treeDataProvider: this.provider });

			this.setOnline(false);

			extension.client.onConnectionChanged(isOnline => {
				this.provider.refresh();						
				this.setOnline(isOnline);
			});

			//TODO: do better!
			extension.client.onNotification(ContactStateNotification.type, (contact: ContactDto) => {
				this.provider.refresh();
			});
		});
	}

	private setOnline(isOnline: boolean): void { //TODO: use Boolean or boolean consistently or as required, here it cant be null -> boolean!
		if(!isOnline) {
			this.view.message = messages.NOT_CONNECTED;
		} else {
			this.view.message = '';
		}
		vscode.commands.executeCommand('setContext', 'connected', isOnline);		
	}
}