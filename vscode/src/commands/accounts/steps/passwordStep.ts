import {WizardStepBase, WizardContext} from '../../../types';
import {AccountDto} from '../../../lsp';

export class PasswordStep extends WizardStepBase<AccountDto> {
  canExecute(context: WizardContext<AccountDto>): boolean {
    return true;
  }

  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const password = await context.showInputBox({
      value: context.target.password || '',
      prompt: 'Enter password',
      placeholder: undefined,
      password: true,
      validate: this.notEmpty,
    });

    context.target.password = password;
  }
}
