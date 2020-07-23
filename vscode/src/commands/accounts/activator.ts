
import {SarosExtension} from '../../lsp';
import {commands} from 'vscode';
import {addAccountWizard, editAccountWizard, removeAccountWizard, defaultAccountWizard} from './wizards';

/**
 * Activation function of the account module.
 *
 * @export
 * @param {SarosExtension} extension - The instance of the extension
 */
export function activateAccounts(extension: SarosExtension) {
  commands.registerCommand('saros.account.add', async () => {
    await extension.onReady();
    return addAccountWizard(extension);
  });

  commands.registerCommand('saros.account.update', async () => {
    await extension.onReady();
    return editAccountWizard(extension);
  });

  commands.registerCommand('saros.account.remove', async () => {
    await extension.onReady();
    return removeAccountWizard(extension);
  });

  commands.registerCommand('saros.account.setDefault', async () => {
    await extension.onReady();
    return defaultAccountWizard(extension);
  });
}
