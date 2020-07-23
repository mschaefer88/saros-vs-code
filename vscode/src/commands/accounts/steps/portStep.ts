import {WizardStep, WizardContext} from '../../../types';
import {AccountDto} from '../../../lsp';

export class PortStep implements WizardStep<AccountDto> {
  canExecute(context: WizardContext<AccountDto>): boolean {
    return !!context.target.server;
  }

  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const port = await context.showInputBox({
      value: context.target.port.toString(),
      prompt: 'Enter port',
      placeholder: 'optional',
      password: false,
      validate: this._isNumber,
    });

    context.target.port = +port;
  }

  private _isNumber(input: string): Promise<string|undefined> {
    return Promise.resolve(+input > 0 ? '' : 'port has to be a number greater than 0');
  }
}
