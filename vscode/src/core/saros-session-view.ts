import * as vscode from 'vscode';
import { Disposable } from 'vscode-languageclient';
import { SarosExtension } from './saros-extension';
import { SarosClient, ContactDto, IsOnlineRequest, GetAllContactRequest, UserJoinedSessionNotification, UserLeftSessionNotification, SessionUserDto } from './saros-client';
import { messages } from './messages';

export class SarosSessionProvider implements vscode.TreeDataProvider<SessionUserDto> {

	private client: SarosClient;
	private context: vscode.ExtensionContext;
	private users: SessionUserDto[];

	constructor(client: SarosClient, context: vscode.ExtensionContext) {
		this.client = client;
		this.context = context;
		this.users = [];

		this.client.onNotification(UserJoinedSessionNotification.type, user => {
			vscode.window.showInformationMessage(`'${user.nickname}' joined the session.`);
			this.users.push(user);
			this.refresh();
		});

		this.client.onNotification(UserLeftSessionNotification.type, id => {
			const user =  this.users.find(user => user.id === id.result);

			if(!user) {
				vscode.window.showErrorMessage('Couldn\'t determine user that has left the session.');
				return;
			}

			vscode.window.showInformationMessage(`'${user.nickname}' left the session.`);
			this.users.splice(this.users.indexOf(user), 1);
			this.refresh();
		});
	}

	refresh(): void {
		this._onDidChangeTreeData.fire();
	}

	clear(): void {
		this.users = [];
		this.refresh();
	}

    private _onDidChangeTreeData: vscode.EventEmitter<ContactDto | undefined> = new vscode.EventEmitter<ContactDto | undefined>();
	readonly onDidChangeTreeData: vscode.Event<ContactDto | undefined> = this._onDidChangeTreeData.event;   
    
    getTreeItem(element: SessionUserDto): vscode.TreeItem | Thenable<vscode.TreeItem> {
				
		let contactItem = new vscode.TreeItem(element.nickname);

		contactItem.tooltip = element.id;
		contactItem.description = element.id;
		contactItem.contextValue = "user";
		contactItem.iconPath = this.context.asAbsolutePath("/media/obj16/contact_saros_obj.png"); //TODO: host?

		return contactItem;
	}
	
    getChildren(element?: SessionUserDto | undefined): SessionUserDto[] {

		if(!element) {
			let contacts = this.users;

			let sorted = contacts.sort((a, b) => {
				return a.nickname > b.nickname ? -1 : 1;
			});

			return sorted;
		}

		return [];
    }


}

export class SarosSessionView implements Disposable{

    private provider!: SarosSessionProvider;
    private viewMain!: vscode.TreeView<SessionUserDto>;
    private viewSub!: vscode.TreeView<SessionUserDto>;
    
    private isOnline: boolean = false;

	dispose(): void {
		this.viewMain.dispose();
		this.viewSub.dispose();
	}

	constructor(extension: SarosExtension) {
        
		extension.client.onReady().then(() => {
			this.provider = new SarosSessionProvider(extension.client, extension.context);
            this.viewMain = vscode.window.createTreeView('saros-session-main', { treeDataProvider: this.provider });
            this.viewSub = vscode.window.createTreeView('saros-session-sub', { treeDataProvider: this.provider });
            
            this.setSession(false);

            extension.client.onConnectionChanged(isOnline => {
                this.isOnline = isOnline;
                this.setSession(false);
			});

			extension.client.onSessionChanged(inSession => {		
				this.setSession(inSession);
			});
		});
	}

	private setSession(inSession: boolean): void {
        if(!this.isOnline) {
            this.viewMain.message = messages.NOT_CONNECTED;
        } else if(!inSession) {
            this.viewMain.message = messages.NO_SESSION;
        } else {
            this.viewMain.message = undefined;
		}
		
		if(!this.isOnline || !inSession) {
			this.provider.clear();
		}

		vscode.commands.executeCommand('setContext', 'inSession', inSession);
	}
}