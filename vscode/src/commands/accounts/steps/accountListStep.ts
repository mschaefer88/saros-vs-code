import {GetAllAccountRequest, AccountDto, SarosExtension} from '../../../lsp';
import {mapToQuickPickItems} from '../../../utils';
import {QuickPickItem, WizardStep, WizardContext} from '../../../types';
import {QuickInputButton} from 'vscode';
import {icons} from '../../../utils/icons';
import {addAccountWizard} from '../wizards';
import * as _ from 'lodash';

/**
 * Wizard step to select an account.
 *
 * @export
 * @class AccountListStep
 * @implements {WizardStep<AccountDto>}
 */
export class AccountListStep implements WizardStep<AccountDto> {
  private _addAccountButton: QuickInputButton;

  /**
   * Creates an instance of AccountListStep.
   *
   * @param {SarosExtension} _extension The instance of the extension
   * @memberof AccountListStep
   */
  public constructor(private _extension: SarosExtension) {
    this._addAccountButton = {
      iconPath: icons.getAddAccountIcon(this._extension.context),
      tooltip: 'Add New Account',
    } as QuickInputButton;
  }

  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof AccountListStep
   */
  canExecute(_context: WizardContext<AccountDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof AccountListStep
   */
  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const accounts =
      await this._extension.client.sendRequest(GetAllAccountRequest.type, null);
    const pick = await context.showQuickPick({
      items: mapToQuickPickItems(accounts.result,
          (c) => c.username,
          (c) => c.domain),
      activeItem: undefined,
      placeholder: 'Select account',
      buttons: [this._addAccountButton],
    });

    if (pick === this._addAccountButton) {
      const addedAccount = await addAccountWizard(this._extension);
      if (addedAccount) {
        context.target = addedAccount;
      }
    } else {
      context.target = (pick as QuickPickItem<AccountDto>).item;
    }
  }
}
