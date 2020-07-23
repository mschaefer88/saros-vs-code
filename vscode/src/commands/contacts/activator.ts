import {SarosExtension} from '../../lsp';
import {commands} from 'vscode';
import {addContactWizard, editContactWizard, removeContactWizard} from './wizards';

/**
 * Activation function of the account module.
 *
 * @export
 * @param {SarosExtension} extension - The instance of the extension
 */
export function activateContacts(extension: SarosExtension) {
  commands.registerCommand('saros.contact.add', async () => {
    await extension.onReady();
    return addContactWizard(extension);
  });

  commands.registerCommand('saros.contact.remove', async (contact) => {
    await extension.onReady();
    return removeContactWizard(contact, extension);
  });

  commands.registerCommand('saros.contact.rename', async (contact) => {
    await extension.onReady();
    return editContactWizard(contact, extension);
  });
}
