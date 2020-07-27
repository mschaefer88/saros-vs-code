import {ContactDto, RenameContactRequest, SarosExtension} from '../../../lsp';
import {Wizard} from '../../../types';
import {NicknameStep, ContactListStep} from '../steps';
import {showMessage} from '../../../utils';
import * as _ from 'lodash';

/**
 * Wizard to add edit a contact.
 *
 * @export
 * @param {ContactDto} contact The contact to edit or undefined
 * @param {SarosExtension} extension The instance of the extension
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function editContactWizard(contact: ContactDto,
    extension: SarosExtension) : Promise<void> {
  const contactClone: ContactDto = _.clone(contact);
  const wizard = new Wizard(contactClone, 'Rename contact', [
    new ContactListStep(extension),
    new NicknameStep(),
  ]);
  contact = await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await extension.client.sendRequest(RenameContactRequest.type, contact);
    showMessage(result, 'Contact renamed successfully!');
  }
}
