import * as vscode from 'vscode';
import { Disposable } from 'vscode-jsonrpc';
import { sarosExtensionInstance } from './core/saros-extension';
import { activateAccounts } from './account/activator';
import { activateContacts } from './contact/activator';
import { activateConnection } from './session/activator';
import { SarosView } from './core/saros-view';

/**
 * Activation function of the extension.
 *
 * @export
 * @param {vscode.ExtensionContext} context - The extension context
 */
export function activate(context: vscode.ExtensionContext) {

	sarosExtensionInstance.setContext(context)
						.init()
						.then(() => {
							activateAccounts(sarosExtensionInstance);
							activateContacts(sarosExtensionInstance);
							activateConnection(sarosExtensionInstance);
							
							context.subscriptions.push(new SarosView(sarosExtensionInstance));

							console.log('Extension "Saros" is now active!');
						})
						.catch(reason => {
							vscode.window.showErrorMessage('Saros extension did not start propertly.'
														+ 'Reason: ' + reason); //TODO: restart feature
						});

	vscode.window.onDidChangeTextEditorSelection(l => {
		console.log("Kind: " + l.kind?.toString()); 
		l.selections.forEach(e => {
			console.log(`Line ${e.active.line} Char ${e.active.character}`);
		});
		console.log("Selections: " + l.selections.length.toString());
		console.log("Document: " + l.textEditor.document.fileName);
	});
}

class SarosTreeDataProvider implements vscode.TreeDataProvider<string>
{
	onDidChangeTreeData?: vscode.Event<string | null | undefined> | undefined;	
	
	getTreeItem(element: string): vscode.TreeItem | Thenable<vscode.TreeItem> {
		return new vscode.TreeItem(element);
	}

	getChildren(element?: string | undefined): vscode.ProviderResult<string[]> {
		return [""];
	}


}

/**
 * Deactivation function of the extension.
 *
 * @export
 */
export function deactivate() {
	sarosExtensionInstance.deactivate();
}
