import {WizardStep, WizardContext, QuickPickItem} from '../../../types';
import {AccountDto} from '../../../lsp';
import {mapToQuickPickItems} from '../../../utils';

export class SaslStep implements WizardStep<AccountDto> {
  canExecute(context: WizardContext<AccountDto>): boolean {
    return true;
  }

  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const items = [true, false];
    const pick = await context.showQuickPick({
      items: mapToQuickPickItems(items, (b) => b ? 'Yes' : 'No'),
      activeItem: undefined,
      placeholder: 'Use SASL?',
      buttons: undefined,
    }) as QuickPickItem<boolean>;

    context.target.useSASL = pick.item;
  }
}
