import * as vscode from 'vscode';
import { SarosExtension } from "../core/saros-extension";
import { commands } from "vscode";
import { SarosResponse, AccountDto, ContactDto, AddContactRequest, GetAllContactRequest, RemoveContactRequest, RenameContactRequest } from '../core/saros-client';
import { QuickPickItem } from '../core/types';

/**
 * Activation function of the account module.
 *
 * @export
 * @param {SarosExtension} extension - The instance of the extension
 */
export function activateContacts(extension: SarosExtension) {
    commands.registerCommand('saros.contact.add', () => {
        let contact: ContactDto = {} as any;
        extension.onReady()
        .then(r => {
            return showEdit(contact);
        })
        .then(a => {
            if(a) {
                extension.client.sendRequest(AddContactRequest.type, a).then(r => {
                    showMessage(r, "Contact added successfully!");
                });
            }
        });
    });
    commands.registerCommand('saros.contact.getAll', () => {
        extension.onReady()
        .then(resolve => {
            return extension.client.sendRequest(GetAllContactRequest.type, null);
        })
        .then(r => {
            let i = 1;
            r.result.forEach(account => {
                showMessage(r, `Contact #${i++}: ${account.nickname} (${account.id})`);
            });
        });
    });
    commands.registerCommand('saros.contact.remove', (contact) => {
        extension.onReady()
        .then(resolve => {

            if(contact) {
                return contact;
            }

            return extension.client.sendRequest(GetAllContactRequest.type, null).then(r => {
                const items: QuickPickItem<ContactDto>[] = r.result.map(contact => {
                    return {
                      label: contact.nickname,
                      detail: contact.id,
                      item: contact
                    };
                  });
                return vscode.window.showQuickPick(items);
            }).then(r => {
                return r?.item;
            });
        })        
        .then(a => {
            if(a) {
                extension.client.sendRequest(RemoveContactRequest.type, a)
                .then(r => {
                    showMessage(r, "Contact removed successfully!");
                });
            }
        });
    });
    commands.registerCommand('saros.contact.rename', (contact) => {
        extension.onReady()
        .then(resolve => {

            if(contact) {
                return contact;
            }

            return extension.client.sendRequest(GetAllContactRequest.type, null)
            .then(r => {
                const items: QuickPickItem<ContactDto>[] = r.result.map(contact => {
                    return {
                      label: contact.nickname,
                      detail: contact.id,
                      item: contact
                    };
                  });
    
                if(items.length === 0) {
                    return;
                }
    
                return vscode.window.showQuickPick(items);
            }).then(r => {
                return r?.item;
            });
        })
        .then(r => {
            return showEdit(r);            
        })
        .then(async r => {
            if(r) {
                let c = await extension.client.sendRequest(RenameContactRequest.type, r);
                return c;          
            } 
        })
        .then(r => {
            if(r) {
                showMessage(r, "Contact renamed successfully!");
            }
        });
    });
}

function showMessage(response: SarosResponse, successMessage: string) { //TODO: use generic solution and return bare dto from client
    if(response.success) {
        vscode.window.showInformationMessage(successMessage);
    } else {
        vscode.window.showErrorMessage(response.error);
    }
}

function showEdit(contact: ContactDto) : Thenable<ContactDto|undefined> {
    //TODO: use await instead of promises?
    return Promise.resolve() 
    .then(r => {
        if(!contact.id) {
            return vscode.window.showInputBox({prompt: "Enter JID to add", /*placeHolder: "username@server.com", validateInput: (input: string) => new RegExp("^[^\u0000-\u001f\u0020\u0022\u0026\u0027\u002f\u003a\u003c\u003e\u0040\u007f\u0080-\u009f\u00a0]+@[a-z0-9.-]+\.[a-z]{2,10}$").test(input) ? "" : "Invalid JID"*/});
        }

        return Promise.resolve<string|undefined>("");
    })
    .then(r => {
        if(r || contact.id) {
            if(r) {
                contact.id = r;
            }

            return vscode.window.showInputBox({prompt: "Enter nickname", value: contact.nickname});
        }
    })
    .then(r => {

        if(r) {
            contact.nickname = r;
        
            return contact;
        }
    });
}