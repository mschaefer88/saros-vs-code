import {ContactDto, AddContactRequest, SarosExtension} from '../../../lsp';
import {Wizard} from '../../../types';
import {JidStep, DomainStep, NicknameStep} from '../steps';
import {showMessage} from '../../../utils';

export async function addContactWizard(extension: SarosExtension): Promise<void> {
  const contact: ContactDto = {
    id: '',
    nickname: '',
  } as any;
  const wizard = new Wizard(contact, 'Add contact', [
    new JidStep(),
    new DomainStep(),
    new NicknameStep(),
  ]);
  await wizard.execute();

  if (!wizard.aborted) {
    const result = await extension.client.sendRequest(AddContactRequest.type, contact);
    showMessage(result, 'Contact added successfully!');// TODO: fire and forget
  }
}
