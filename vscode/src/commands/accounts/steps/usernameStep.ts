import {WizardContext, WizardStepBase} from '../../../types';
import {AccountDto} from '../../../lsp';

export class UsernameStep extends WizardStepBase<AccountDto> {
  canExecute(context: WizardContext<AccountDto>): boolean {
    return true;
  }

  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const username = await context.showInputBox({
      value: context.target.username || '',
      prompt: 'Enter username',
      placeholder: undefined,
      password: false,
      validate: this.notEmpty,
    });

    context.target.username = username;
  }
}
