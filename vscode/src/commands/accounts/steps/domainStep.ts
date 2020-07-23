import {WizardStep, WizardContext} from '../../../types';
import {AccountDto, config} from '../../../lsp';

const regexDomain = /^[a-z0-9.-]+\.[a-z]{2,10}$/;

export class DomainStep implements WizardStep<AccountDto> {
  canExecute(context: WizardContext<AccountDto>): boolean {
    return true;
  }

  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const domain = await context.showInputBox({
      value: context.target.domain || config.getDefaultHost() || '',
      prompt: 'Enter domain',
      placeholder: undefined,
      password: false,
      validate: this.validateHost,
    });

    context.target.domain = domain;
  }

  validateHost(input: string): Promise<string|undefined> {
    const isValid = regexDomain.test(input);
    const result = isValid ? undefined : 'Not a valid domain';

    return Promise.resolve(result);
  }
}
