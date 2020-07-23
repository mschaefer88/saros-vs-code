import {AccountDto, RemoveAccountRequest, SarosExtension, events} from '../../../lsp';
import {Wizard} from '../../../types';
import {showMessage} from '../../../utils';
import {AccountListStep} from '../steps';
import * as _ from 'lodash';

export async function removeAccountWizard(extension: SarosExtension): Promise<void> {
  const wizard = new Wizard<AccountDto>({} as any, 'Remove account', [
    new AccountListStep(extension),
  ]);
  const account = await wizard.execute();

  if (!wizard.aborted) {
    const result = await extension.client.sendRequest(RemoveAccountRequest.type, account);
    showMessage(result, 'Account removed successfully!');// TODO: fire and forget

    extension.publish(events.AccountRemoved, account);
  }
}
