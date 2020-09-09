import {
  ContactDto,
  InviteContactRequest,
  SarosExtension,
  InviteInput,
} from '../../../lsp';
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
    const inviteInput = {
      id: contact.id,
      description: 'VS Code Invitation',
    } as InviteInput;
    const result =
      await extension.client.sendRequest(InviteContactRequest.type,
          inviteInput);
    showMessage(result, `${contact.nickname} has been invited!`);
  }
}
