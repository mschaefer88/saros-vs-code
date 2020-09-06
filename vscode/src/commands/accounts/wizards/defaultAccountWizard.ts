import {
  AccountDto,
  SetActiveAccountRequest,
  SarosExtension,
  events,
} from '../../../lsp';
import {Wizard} from '../../../types';
import {showMessage} from '../../../utils';
import {AccountListStep} from '../steps';
import * as _ from 'lodash';

/**
 * Wizard to select the default account.
 *
 * @export
 * @param {SarosExtension} extension The instance of the extension
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function defaultAccountWizard(extension: SarosExtension)
  : Promise<void> {
  const wizard =
    new Wizard<AccountDto|undefined>(undefined, 'Set active account', [
      new AccountListStep(extension),
    ]);
  const account = await wizard.execute();

  if (!wizard.aborted && account) {
    const result =
      await extension.client.sendRequest(SetActiveAccountRequest.type,
          account);
    showMessage(result, 'Active account set successfully!');

    if (result.success) {
      extension.publish(events.DefaultAccountChanged, account);
    }
  }
}
