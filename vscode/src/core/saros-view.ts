import * as vscode from 'vscode';
import { Disposable } from 'vscode-languageclient';
import { SarosExtension } from './saros-extension';
import { SarosClient, ContactDto, SarosResultResponse, SessionStateNotification, ContactStateNotification, IsOnlineRequest, GetAllContactRequest, ConnectedStateNotification } from './saros-client';

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

export class SarosView implements Disposable{

	private disposables: Disposable[] = [];

	dispose(): void {
		this.disposables.forEach(disposable => {
			disposable.dispose();
		});
	}

	constructor(extension: SarosExtension) {
		
		extension.client.onReady().then(() => {
			const treeDataProvider = new SarosProvider(extension.client, extension.context);
			const contactsView = vscode.window.createTreeView('saros-contacts', { treeDataProvider });
			const sessionView = vscode.window.createTreeView('saros-session', { treeDataProvider });

			extension.client.onNotification(ConnectedStateNotification.type, isOnline => {
				treeDataProvider.refresh();				
				this.setOnline(isOnline.result);
			});

			extension.client.onNotification(SessionStateNotification.type, inSession => {
							
				this.setSession(inSession.result);
			});

			//TODO: do better!
			extension.client.onNotification(ContactStateNotification.type, (contact: ContactDto) => {
				treeDataProvider.refresh();
			});

			this.disposables.push(contactsView);
		});
	}

	private setOnline(isOnline: boolean): void { //TODO: use Boolean or boolean consistently or as required, here it cant be null -> boolean!
		vscode.commands.executeCommand('setContext', 'connected', isOnline);
	}

	private setSession(inSession: boolean): void {
		vscode.commands.executeCommand('setContext', 'inSession', inSession);
	}
}