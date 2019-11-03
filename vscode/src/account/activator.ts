import * as vscode from 'vscode';
import { SarosExtension } from "../core/saros-extension";
import { commands } from "vscode";
import { SarosResponse, AccountDto, AddAccountRequest, GetAllAccountRequest, UpdateAccountRequest, RemoveAccountRequest, SetDefaultAccountRequest, CreateAccountRequest } from '../core/saros-client';
import { QuickPickItem } from '../core/types';

/**
 * Activation function of the account module.
 *
 * @export
 * @param {SarosExtension} extension - The instance of the extension
 */
export function activateAccounts(extension: SarosExtension) {
    commands.registerCommand('saros.account.add', () => {
        extension.onReady()
        .then(r => {
            let account: AccountDto = {port: 0} as any;
            return showEdit(account);
        })
        .then(a => {
            if(a) {
                extension.client.sendRequest(AddAccountRequest.type, a).then(r => {
                    showMessage(r, "Account created successfully!");
                });
            }
        });
    });
    commands.registerCommand('saros.account.update', () => {
        extension.onReady()
        .then(resolve => {
            return extension.client.sendRequest(GetAllAccountRequest.type, null);
        })
        .then(r => {
            const items: QuickPickItem<AccountDto>[] = r.result.map(account => {
                return {
                  label: account.username,
                  detail: account.domain,
                  item: account
                };
              });
            return vscode.window.showQuickPick(items);
        })
        .then(q => {
            if(q) {
                return showEdit(q.item);
            }
        })
        .then(a => {
            if(a) {
                extension.client.sendRequest(UpdateAccountRequest.type, a)
                .then(r => {
                    showMessage(r, "Account updated successfully!");
                });
            }
        });
    });
    commands.registerCommand('saros.account.delete', () => {
        extension.onReady()
        .then(resolve => {
            return extension.client.sendRequest(GetAllAccountRequest.type, null);
        })
        .then(r => {
            const items: QuickPickItem<AccountDto>[] = r.result.map(account => {
                return {
                  label: account.username,
                  detail: account.domain,
                  item: account
                };
              });
            return vscode.window.showQuickPick(items);
        })
        .then(r => {
            if(r) {
                extension.client.sendRequest(RemoveAccountRequest.type, r.item)
                .then(r => {
                    showMessage(r, "Account deleted successfully!");
                });
            }            
        });
    });
    commands.registerCommand('saros.account.setDefault', () => {
        extension.onReady()
        .then(resolve => {
            return extension.client.sendRequest(GetAllAccountRequest.type, null);
        })
        .then(r => {
            const items: QuickPickItem<AccountDto>[] = r.result.map(account => {
                return {
                  label: account.username,
                  detail: account.domain,                  
                  item: account,
                  description: account.isDefault ? "(default)" : undefined
                };
              });
            return vscode.window.showQuickPick(items);
        })
        .then(r => {
            if(r) {
                extension.client.sendRequest(SetDefaultAccountRequest.type, r.item)
                .then(r => {
                    showMessage(r, "Default account set successfully!");
                });
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

function showEdit(account: AccountDto) : Thenable<AccountDto|undefined> {
    
    return Promise.resolve() 
    .then(r => {
        if(!account.username) {
            return vscode.window.showInputBox({prompt: "Enter Username", value: account.username});
        }

        return Promise.resolve<string|undefined>("");
    }).then(r => {
        if(r) {
            account.username = r;
        }

        if(!account.domain) {
            return vscode.window.showInputBox({prompt: "Enter Domain", value: account.domain});
        }

        return Promise.resolve<string|undefined>("");
    })
    .then(r => {
        if(r) {
            account.domain = r;
        }

        return vscode.window.showInputBox({password: true, prompt: "Enter Password", value: account.password});
    })
    
                .then(r => {
                    if(r) {
                        account.password = r;

                        return vscode.window.showInputBox({prompt: "Enter Server", value: account.server, placeHolder: "optional"});
                    }
                })
                .then(r => {
                    if(r !== undefined) {
                        account.server = r;

                        return vscode.window.showInputBox({prompt: "Enter Port", placeHolder: "optional", value: account.port.toString(), validateInput: (input: string) => !isNaN(+input) ? '' : 'input must be a number'});
                    }                    
                })
                .then(r => 
                    {
                        if(r !== undefined) {
                            account.port = +r;

                            let items: QuickPickItem<boolean>[] = [{
                                label: "yes",
                                item: true
                            },{
                                label: "no",
                                item: false
                            }]; 
                            
                            //TODO: select current value
                            return vscode.window.showQuickPick(items, {placeHolder: "Use TLS?"});
                        }
                    }
                    ).then(r => {
                        if(r) {
                            account.useTLS = r.item;

                            let items: QuickPickItem<boolean>[] = [{
                                label: "yes",
                                item: true
                            },{
                                label: "no",
                                item: false
                            }];

                            //TODO: select current value
                            return vscode.window.showQuickPick(items, {placeHolder: "Use SASL?"}); 
                        }
                    }).then(r => {
                        if(r) {
                            account.useSASL = r.item;

                            return account;
                        }
                    }); 
}