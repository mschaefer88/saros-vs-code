import { WizardStep, WizardContext } from "../../../types";
import { AccountDto } from "../../../lsp";

const regexServer = /^[a-z0-9.-]+\.[a-z]{2,10}$/;

export class ServerStep implements WizardStep<AccountDto> {

	canExecute(context: WizardContext<AccountDto>): boolean {
		return true;
	}

	async execute(context: WizardContext<AccountDto>): Promise<void> {
		let server = await context.showInputBox({
			value: context.target.server || '',
            prompt: 'Enter server',
            placeholder: 'optional',
            password: false,
			validate: this.validateServer
        });
        
		context.target.server = server;
	}
    
    validateServer(input: string): Promise<string|undefined> {        
        const isValid = !input || regexServer.test(input);
        const result = isValid ? undefined : 'Not a valid address';
    
        return Promise.resolve(result);
    }
}