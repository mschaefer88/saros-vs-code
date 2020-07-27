import {commands} from 'vscode';

export namespace variables {
    /**
     * Sets the initialization state of the extension.
     *
     * @export
     * @param {boolean} isInitialized The initialization state
     */
    export const setInitialized = (isInitialized: boolean) => {
      commands.executeCommand('setContext', 'initialized', isInitialized);
    };

    /**
     * Sets the session state of the extension.
     *
     * @export
     * @param {boolean} isActive The session state
     */
    export const setSessionActive = (isActive: boolean) => {
      commands.executeCommand('setContext', 'sessionActive', isActive);
    };

    /**
     * Sets the connection state of the extension.
     *
     * @export
     * @param {boolean} isActive The connection state
     */
    export const setConnectionActive = (isActive: boolean) => {
      commands.executeCommand('setContext', 'connectionActive', isActive);
    };
}
