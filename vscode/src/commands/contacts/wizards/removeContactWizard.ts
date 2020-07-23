import {ContactDto, RemoveContactRequest, SarosExtension} from '../../../lsp';
import {Wizard} from '../../../types';
import {ContactListStep} from '../steps';
import {showMessage} from '../../../utils';

export async function removeContactWizard(contact: ContactDto, extension: SarosExtension): Promise<void> {
  const wizard = new Wizard(contact, 'Remove contact', [
    new ContactListStep(extension),
  ]);
  contact = await wizard.execute();

  if (!wizard.aborted) {
    const result = await extension.client.sendRequest(RemoveContactRequest.type, contact);
    showMessage(result, 'Contact removed successfully!');
  }
}
