import { WizardStep, WizardContext } from "../../../types";
import { ContactDto, config } from "../../../lsp";

const regexJid = /^[^\u0000-\u001f\u0020\u0022\u0026\u0027\u002f\u003a\u003c\u003e\u0040\u007f\u0080-\u009f\u00a0]+@[a-z0-9.-]+\.[a-z]{2,10}$/;
const regexJidSuffix = /^[a-z0-9.-]+\.[a-z]{2,10}$/;

export class DomainStep implements WizardStep<ContactDto> {

	canExecute(context: WizardContext<ContactDto>): boolean {
		return !!context.target.id && !regexJid.test(context.target.id);
	}

	async execute(context: WizardContext<ContactDto>): Promise<void> {
		let domain = await context.showInputBox({
			value: config.getDefaultHost() || '',
			prompt: 'Enter domain',
            placeholder: undefined,
            password: false,
			validate: this.validateHost
        });
        
		context.target.id += `@${domain}`;
	}
    
    validateHost(input: string): Promise<string|undefined> {
        const isValid = regexJidSuffix.test(input);
        const result = isValid ? undefined : 'Not a valid host';
    
        return Promise.resolve(result);
    }
}