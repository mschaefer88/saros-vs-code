import { AccountDto, UpdateAccountRequest, SarosExtension } from "../../../lsp";
import { Wizard } from "../../../types";
import { showMessage } from "../../../utils";
import { UsernameStep, DomainStep, PasswordStep, ServerStep, PortStep, TlsStep, SaslStep, AccountListStep } from "../steps";
import * as _ from "lodash";

export async function editAccountWizard(extension: SarosExtension): Promise<void> {

	const wizard = new Wizard<AccountDto>({} as any, 'Edit account', [
        new AccountListStep(extension),
		new UsernameStep(),
		new DomainStep(),
		new PasswordStep(),
		new ServerStep(),
		new PortStep(),
		new TlsStep(),
		new SaslStep()
	]);
	const account = await wizard.execute();

    if (!wizard.aborted) {
        const result = await extension.client.sendRequest(UpdateAccountRequest.type, account);
        showMessage(result, 'Account updated successfully!');//TODO: fire and forget
    }
}