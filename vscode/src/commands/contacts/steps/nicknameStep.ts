import { WizardContext, WizardStepBase } from "../../../types";
import { ContactDto } from "../../../lsp";

export class NicknameStep extends WizardStepBase<ContactDto> {

	canExecute(context: WizardContext<ContactDto>): boolean {
		return true;
	}

	async execute(context: WizardContext<ContactDto>): Promise<void> {
		let nickname = await context.showInputBox({
			value: context.target.nickname || '',
			prompt: 'Enter nickname',
            placeholder: undefined,
            password: false,
			validate: this.notEmpty
        });
        
		context.target.nickname = nickname;
	}
}