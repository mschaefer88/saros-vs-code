import { GetAllAccountRequest, AccountDto, SarosExtension } from "../../../lsp";
import { mapToQuickPickItems } from "../../../utils";
import { QuickPickItem, WizardStep, WizardContext } from "../../../types";
import * as _ from "lodash";
import { QuickInputButton } from "vscode";
import { icons } from "../../../utils/icons";
import { addAccountWizard } from "../wizards";

export class AccountListStep implements WizardStep<AccountDto> {

    private _addAccountButton: QuickInputButton;

    public constructor(private _extension: SarosExtension) {
        this._addAccountButton = {iconPath: icons.getAddAccountIcon(this._extension.context), tooltip: 'Add New Account'} as QuickInputButton;
    }

    async execute(context: WizardContext<AccountDto>): Promise<void> {
        const accounts = await this._extension.client.sendRequest(GetAllAccountRequest.type, null);
        const pick = await context.showQuickPick({
            items: mapToQuickPickItems(accounts.result, c => c.username, c => c.domain),
            activeItem: undefined,
            placeholder: 'Select account',
            buttons: [this._addAccountButton]
        });
        
        if(pick === this._addAccountButton) {
            const addedAccount = await addAccountWizard(this._extension);
            if (addedAccount) {
                context.target = addedAccount;
            }
        } else {            
            context.target = (pick as QuickPickItem<AccountDto>).item;
        }
    }

    canExecute(context: WizardContext<AccountDto>): boolean {
        return true;
    }
}