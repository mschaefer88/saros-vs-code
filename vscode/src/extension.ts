import {activateAccounts, activateContacts, activateSessions} from './commands';
import {SarosContactView, SarosSessionView, SarosAccountView} from './views';
import {sarosExtensionInstance} from './lsp';
import {window, ExtensionContext} from 'vscode';
import {variables} from './views/variables';

/**
 * Activates the extension.
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

        context.subscriptions
            .push(new SarosAccountView(sarosExtensionInstance));
        context.subscriptions
            .push(new SarosContactView(sarosExtensionInstance));
        context.subscriptions
            .push(new SarosSessionView(sarosExtensionInstance));

        variables.setInitialized(true);
      })
      .catch((reason?: string) => {
        window.showErrorMessage('Saros extension did not start properly. ' +
          'Reason: ' + reason);
      });
}

/**
 * Deactivates the extension.
 *
 * @export
 */
export function deactivate() {
  sarosExtensionInstance.deactivate();
  variables.setInitialized(false);
}
