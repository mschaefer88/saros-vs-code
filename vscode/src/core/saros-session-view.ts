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

export class SarosSessionView implements Disposable{

    private provider!: SarosProvider;
    private view!: vscode.TreeView<ContactDto>;
    
    private isOnline: boolean = false;

	dispose(): void {
		this.view.dispose();
	}

	constructor(extension: SarosExtension) {
        
		extension.client.onReady().then(() => {
			this.provider = new SarosProvider(extension.client, extension.context);
            this.view = vscode.window.createTreeView('saros-session', { treeDataProvider: this.provider });
            
            this.setSession(false);

            extension.client.onConnectionChanged(isOnline => {
                this.isOnline = isOnline;
                this.setSession(false);
			});

			extension.client.onSessionChanged(inSession => {
				//this.provider.refresh();				
				this.setSession(inSession);
			});
		});
	}

	private setSession(inSession: boolean): void {
        if(!this.isOnline) {
            this.view.message = messages.NOT_CONNECTED;
        } else if(!inSession) {
            this.view.message = messages.NO_SESSION;
        } else {
            this.view.message = undefined;
        }

		vscode.commands.executeCommand('setContext', 'inSession', inSession);
	}
}