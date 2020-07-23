import { WizardStep, WizardContext, QuickPickItem } from "../../../types";
import { ContactDto, GetAllContactRequest, SarosExtension } from "../../../lsp";
import { mapToQuickPickItems } from "../../../utils";
import * as _ from "lodash";

export class ContactListStep implements WizardStep<ContactDto> {

    public constructor(private _extension: SarosExtension) {}

    async execute(context: WizardContext<ContactDto>): Promise<void> {
        const contacts = await this._extension.client.sendRequest(GetAllContactRequest.type, null); //TODO: what if error?
        const pick = await context.showQuickPick({
            items: mapToQuickPickItems(contacts.result, c => c.nickname, c => c.id),
            activeItem: undefined,
            placeholder: 'Select contact',
            buttons: undefined
        }) as QuickPickItem<ContactDto>;

        context.target = pick.item;
    }

    canExecute(context: WizardContext<ContactDto>): boolean {
        return !context.target || !context.target.id;
    }
}