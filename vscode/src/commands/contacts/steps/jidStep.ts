import {WizardStep, WizardContext} from '../../../types';
import {ContactDto} from '../../../lsp';

const regexJid = /^[^\u0000-\u001f\u0020\u0022\u0026\u0027\u002f\u003a\u003c\u003e\u0040\u007f\u0080-\u009f\u00a0]+@[a-z0-9.-]+\.[a-z]{2,10}$/;
const regexJidPrefix = /^[^\u0000-\u001f\u0020\u0022\u0026\u0027\u002f\u003a\u003c\u003e\u0040\u007f\u0080-\u009f\u00a0]+$/;

export class JidStep implements WizardStep<ContactDto> {
  canExecute(context: WizardContext<ContactDto>): boolean {
    return true;
  }

  async execute(context: WizardContext<ContactDto>): Promise<void> {
    const id = await context.showInputBox({
      value: context.target.id || '',
      prompt: 'Enter name or JID',
      placeholder: undefined,
      password: false,
      validate: this.validateJid,
    });

    context.target.id = id;
  }

  validateJid(input: string): Promise<string|undefined> {
    const isValid = regexJid.test(input) || regexJidPrefix.test(input);
    const result = isValid ? undefined : 'Not a valid JID';

    return Promise.resolve(result);
  }
}
