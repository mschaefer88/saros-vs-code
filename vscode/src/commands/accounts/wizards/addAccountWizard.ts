import {Wizard} from '../../../types';
import {SarosExtension, AddAccountRequest, AccountDto} from '../../../lsp';
import {showMessage} from '../../../utils';
import {
  UsernameStep,
  DomainStep,
  PasswordStep,
  ServerStep,
  PortStep,
  TlsStep,
  SaslStep,
} from '../steps';

/**
 * Wizard to add an account.
 *
 * @export
 * @param {SarosExtension} extension The instance of the extension
 * @return {(Promise<AccountDto|undefined>)} An awaitable promise that
 *  returns the result if completed and undefined otherwise
 */
export async function addAccountWizard(extension: SarosExtension)
  : Promise<AccountDto|undefined> {
  const account: AccountDto = {port: 0} as any;
  const wizard = new Wizard(account, 'Add account', [
    new UsernameStep(),
    new DomainStep(),
    new PasswordStep(),
    new ServerStep(),
    new PortStep(),
    new TlsStep(),
    new SaslStep(),
  ]);
  await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await extension.client.sendRequest(AddAccountRequest.type, account);
    showMessage(result, 'Account created successfully!');

    return result.success ? account : undefined;
  }
}
