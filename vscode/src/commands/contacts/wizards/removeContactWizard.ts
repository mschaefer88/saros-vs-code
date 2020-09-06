import {ContactDto, RemoveContactRequest, SarosExtension} from '../../../lsp';
import {Wizard} from '../../../types';
import {ContactListStep} from '../steps';
import {showMessage} from '../../../utils';

/**
 * Wizard to remove a contact.
 *
 * @export
 * @param {ContactDto} contact The contact to remove or undefined
 * @param {SarosExtension} extension The instance of the extension
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function removeContactWizard(contact: ContactDto,
    extension: SarosExtension): Promise<void> {
  const wizard = new Wizard(contact, 'Remove contact', [
    new ContactListStep(extension),
  ]);
  contact = await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await extension.client.sendRequest(RemoveContactRequest.type, contact);
    showMessage(result, 'Contact removed successfully!');
  }
}