import { commands } from "vscode";

export namespace variables {
    export function setInitialized(isInitialized: boolean) {
        commands.executeCommand('setContext', 'initialized', isInitialized);
    }

    export function setSessionActive(isActive: boolean) {
        commands.executeCommand('setContext', 'sessionActive', isActive);
    }

    export function setConnectionActive(isActive: boolean) {
        commands.executeCommand('setContext', 'connectionActive', isActive);
    }
}