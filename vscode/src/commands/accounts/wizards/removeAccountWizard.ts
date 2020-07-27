import {
  AccountDto,
  RemoveAccountRequest,
  SarosExtension,
  events,
} from '../../../lsp';
import {Wizard} from '../../../types';
import {showMessage} from '../../../utils';
import {AccountListStep} from '../steps';
import * as _ from 'lodash';

/**
 * Wizard to remove an account.
 *
 * @export
 * @param {SarosExtension} extension The instance of the extension
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function removeAccountWizard(extension: SarosExtension)
  : Promise<void> {
  const wizard = new Wizard<AccountDto>({} as any, 'Remove account', [
    new AccountListStep(extension),
  ]);
  const account = await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await extension.client.sendRequest(RemoveAccountRequest.type, account);
    showMessage(result, 'Account removed successfully!');

    extension.publish(events.AccountRemoved, account);
  }
}
