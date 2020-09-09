import {
  activateAccounts,
  activateContacts,
  activateSessions,
} from './commands';
import {SarosContactView, SarosSessionView, SarosAccountView} from './views';
import {sarosExtensionInstance} from './lsp';
import {ExtensionContext, workspace, window, commands} from 'vscode';
import {variables} from './views/variables';

/**
 * Activates the extension.
 *
 * @export
 * @param {ExtensionContext} context - The extension context
 */
export function activate(context: ExtensionContext) {
  const activationConditionError = getActivationConditionError();
  if (activationConditionError) {
    window.showErrorMessage(activationConditionError);
    deactivate();
    return;
  }
  commands.registerCommand('fff.fff', () => {
    console.log('HI');
  });
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
      });
}

/**
 * Checks if extension is supported within the opened workspace.
 *
 * @return {(string|undefined)} undefined if extension can be activated
 *  and a reason if extension doesn't support the opened workspace.
 */
function getActivationConditionError(): string|undefined {
  if (workspace.workspaceFolders === undefined) {
    return 'Workspace is empty - Saros deactivated';
  } else if (workspace.workspaceFolders.length > 1) {
    return 'Multiple workspaces aren\'t currently supported' +
           ' - Saros deactivated';
  }
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
