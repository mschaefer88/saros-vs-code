import * as vscode from 'vscode';
import { SarosExtension } from "../core/saros-extension";
import { commands } from "vscode";
import { SarosResponse, AccountDto, ContactDto, ConnectRequest, DisconnectRequest, IsOnlineRequest, StartSessionRequest, GetAllContactRequest, InviteContactRequest, StopSessionRequest } from '../core/saros-client';
import { QuickPickItem } from '../core/types';

/**
 * Activation function of the account module.
 *
 * @export
 * @param {SarosExtension} extension - The instance of the extension
 */
export function activateConnection(extension: SarosExtension) {
    commands.registerCommand('saros.session.connect', () => {
        let contact: ContactDto = {} as any;
        extension.onReady()
        .then(r => {
            return extension.client.sendRequest(ConnectRequest.type, null);
        })
        .then(a => {
            if(a) {
                showMessage(a, "Connected successfully!");
            }
        });
    }, extension.client);
    commands.registerCommand('saros.session.disconnect', () => {
        let contact: ContactDto = {} as any;
        extension.onReady()
        .then(r => {
            return extension.client.sendRequest(DisconnectRequest.type, null);
        })
        .then(a => {
            if(a) {
                showMessage(a, "Disconnected successfully!");
            }
        });
    });
    commands.registerCommand('saros.session.status', () => {
            let contact: ContactDto = {} as any;
            extension.onReady()
            .then(r => {
                return extension.client.sendRequest(IsOnlineRequest.type, null);
            })
            .then(a => {
                if(a) {
                    showMessage(a, a.result ? "Saros is connected!" : "Saros is disconnected!");
                }
            });
        });
        commands.registerCommand('saros.session.start', () => {
                extension.onReady()
                .then(r => {
                    return extension.client.sendRequest(StartSessionRequest.type, null);
                })
                .then(a => {
                    if(a) {
                        showMessage(a, "Session started!");
                    }
                });
            });
            commands.registerCommand('saros.session.stop', () => {
                    extension.onReady()
                    .then(r => {
                        return extension.client.sendRequest(StopSessionRequest.type, null);
                    })
                    .then(a => {
                        if(a) {
                            showMessage(a, "Session stopped!");
                        }
                    });
                });
        commands.registerCommand('saros.session.invite', (contact) => {//TODO: to contacts?
                extension.onReady()
                .then(resolve => {

                    if(contact) {
                        return contact;
                    }

                    return extension.client.sendRequest(GetAllContactRequest.type, null).then(async r => {
                        const items: QuickPickItem<ContactDto>[] = r.result.filter(contact => contact.isOnline).map(contact => {
                            return {
                              label: contact.nickname,
                              detail: contact.id,
                              item: contact
                            };
                          });
    
                        if(items.length > 0) {
                            let pick = await vscode.window.showQuickPick(items);

                            return pick?.item;
                        } else {
                            vscode.window.showWarningMessage("No contacts online!");
                        }
                    });
                })                
                .then(r => {
                    if(r) {
                       extension.client.sendRequest(InviteContactRequest.type, r)
                       .then(re => {
                            showMessage(re, `${r.item.nickname} has been invited!`);
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