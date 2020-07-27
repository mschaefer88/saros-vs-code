import {ContactDto, InviteContactRequest, SarosExtension} from '../../../lsp';
import {Wizard} from '../../../types';
import {ContactListStep} from '../steps';
import {showMessage} from '../../../utils';

/**
 * Wizard to invite a contact to a session.
 *
 * @export
 * @param {ContactDto} contact The contact to invite or undefined
 * @param {SarosExtension} extension The instance of the extension
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function inviteContactWizard(contact: ContactDto,
    extension: SarosExtension): Promise<void> {
  const wizard = new Wizard(contact, 'Invite contact', [
    new ContactListStep(extension),
  ]);
  contact = await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await extension.client.sendRequest(InviteContactRequest.type, contact);
    showMessage(result, `${contact.nickname} has been invited!`);
  }
}
