import { activateAccounts, activateContacts, activateSessions } from './commands';
import { SarosContactView, SarosSessionView, SarosAccountView } from './views';
import { sarosExtensionInstance } from './lsp';
import { window, ExtensionContext } from 'vscode';
import { variables } from './views/variables';

/**
 * Activation function of the extension.
 *
 * @export
 * @param {ExtensionContext} context - The extension context
 */
export function activate(context: ExtensionContext) {

	sarosExtensionInstance.setContext(context)
						.init()
						.then(() => {
							activateAccounts(sarosExtensionInstance);
							activateContacts(sarosExtensionInstance);
							activateSessions(sarosExtensionInstance);
							
							context.subscriptions.push(new SarosAccountView(sarosExtensionInstance));
							context.subscriptions.push(new SarosContactView(sarosExtensionInstance));
							context.subscriptions.push(new SarosSessionView(sarosExtensionInstance));

							variables.setInitialized(true);
						})
						.catch((reason?: string) => {
							window.showErrorMessage('Saros extension did not start properly. '
														+ 'Reason: ' + reason);
						});

	window.onDidChangeTextEditorSelection(l => {
		console.log("Kind: " + l.kind?.toString()); 
		l.selections.forEach(e => {
			console.log(`Line ${e.active.line} Char ${e.active.character}`);
		});
		console.log("Selections: " + l.selections.length.toString());
		console.log("Document: " + l.textEditor.document.fileName);
	});
}

/**
 * Deactivation function of the extension.
 *
 * @export
 */
export function deactivate() {
	sarosExtensionInstance.deactivate();
	variables.setInitialized(false);
}
