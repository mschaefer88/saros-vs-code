import { ContactDto, InviteContactRequest, SarosExtension } from "../../../lsp";
import { Wizard } from "../../../types";
import { ContactListStep } from "../steps";
import { showMessage } from "../../../utils";

export async function inviteContactWizard(contact: ContactDto, extension: SarosExtension): Promise<void> {

	const wizard = new Wizard(contact, 'Invite contact', [
        new ContactListStep(extension)
	]);
	contact = await wizard.execute();

    if (!wizard.aborted) {
        const result = await extension.client.sendRequest(InviteContactRequest.type, contact);
        showMessage(result, `${contact.nickname} has been invited!`);
    }
}