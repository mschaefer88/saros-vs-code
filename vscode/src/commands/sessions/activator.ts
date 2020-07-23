import { commands } from "vscode";
import { ConnectRequest, DisconnectRequest, IsOnlineRequest, StartSessionRequest, StopSessionRequest, SarosExtension } from '../../lsp';
import { showMessage } from '../../utils';
import { inviteContactWizard } from './wizards';

/**
 * Activation function of the account module.
 *
 * @export
 * @param {SarosExtension} extension - The instance of the extension
 */
export function activateSessions(extension: SarosExtension) {

    commands.registerCommand('saros.session.connect', async () => {
        await extension.onReady();
        const result = await extension.client.sendRequest(ConnectRequest.type, null);
        showMessage(result, "Connected successfully!", "Couldn't connect.");
    });

    commands.registerCommand('saros.session.disconnect', async () => {
        await extension.onReady();
        const result = await extension.client.sendRequest(DisconnectRequest.type, null);
        showMessage(result, "Disconnected successfully!");
    });

    commands.registerCommand('saros.session.status', async () => {
        await extension.onReady();
        const result = await extension.client.sendRequest(IsOnlineRequest.type, null);
        showMessage(result, result.result ? "Saros is connected!" : "Saros is disconnected!");
    });

    commands.registerCommand('saros.session.start', async () => {
        await extension.onReady();
        const result = await extension.client.sendRequest(StartSessionRequest.type, null);
        showMessage(result, "Session started!");
    });

    commands.registerCommand('saros.session.stop', async () => {
        await extension.onReady();
        const result = await extension.client.sendRequest(StopSessionRequest.type, null);
        showMessage(result, "Session stopped!");
    });  

    commands.registerCommand('saros.session.invite', async (contact) => {
        await extension.onReady();
        return inviteContactWizard(contact, extension);
    });
}