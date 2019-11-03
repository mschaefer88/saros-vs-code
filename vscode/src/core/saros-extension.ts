import { ExtensionContext, workspace, window, ProgressLocation, commands } from "vscode"; //TODO: überall so machen!
import { SarosServer } from "./saros-server";
import { SarosClient } from "./saros-client";
import { LanguageClientOptions, RevealOutputChannelOn, ErrorHandler, Message, ErrorAction, CloseAction, InitializationFailedHandler, WorkDoneProgressParams } from "vscode-languageclient";

/**
 * The Saros extension.
 *
 * @export
 * @class SarosExtension
 */
export class SarosExtension {
    private context!: ExtensionContext;
    public client!: SarosClient;

    /**
     * Creates an instance of SarosExtension.
     *
     * @memberof SarosExtension
     */
    constructor() {
        
    }

    /**
     * Sets the context the extension runs on.
     *
     * @param {ExtensionContext} context - The extension context
     * @returns {SarosExtension} Itself
     * @memberof SarosExtension
     */
    setContext(context: ExtensionContext): SarosExtension {
        this.context = context;

        return this;
    }

    /**
     * Initializes the extension.
     *
     * @returns
     * @memberof SarosExtension
     */
    async init() {
        if(!this.context) {
            return Promise.reject('Context not set');
        }

        try {   //TODO: try within promise?                   
            let self = this;
            
            window.withProgress({location: ProgressLocation.Window, cancellable: false, title: "Starting Saros"}, 
            () => {
                return new Promise(async resolve => {
                    const server = new SarosServer(self.context);
                    self.client = new SarosClient(server.getStartFunc(), this.createClientOptions());                                     
                    self.context.subscriptions.push(self.client.start());
                
                    self.client.onReady().then(() => {

                        //TODO: just for testing!!!                      
						commands.registerCommand("saros.test", () => {
                            let t: WorkDoneProgressParams ={workDoneToken: "TESTEST"};
							this.client.sendRequest("saros/contact/test", t);
						});
                        
                        resolve();
                    });                    
                });
            });            
        } catch(ex) {
            const msg = "Error while activating plugin. " + (ex.message ? ex.message : ex);
            return Promise.reject(msg);
        }
    }

    /**
     * Callback when extension is ready.
     *
     * @returns
     * @memberof SarosExtension
     */
    async onReady() {
        if(!this.client) {
            return Promise.reject('SarosExtension is not initialized');
        }

        return this.client.onReady();
    }

    /**
     * Creates the client options.
     *
     * @private
     * @returns {LanguageClientOptions} The client options
     * @memberof SarosExtension
     */
    private createClientOptions(): LanguageClientOptions {
        let clientOptions: LanguageClientOptions = {
            // Register the server for plain text documents
            documentSelector: ['plaintext'],
            synchronize: {
                // Synchronize the setting section 'languageServerExample' to the server
                //configurationSection: 'sarosServer',
                // Notify the server about file changes to '.clientrc files contain in the workspace
                fileEvents: workspace.createFileSystemWatcher('**/.clientrc')
            },
            outputChannel: window.createOutputChannel('Saros'),
            revealOutputChannelOn: RevealOutputChannelOn.Error, //TODO: set with config file
            errorHandler: new MyErrorHandler(),
            progressOnInitialization: true,
            traceOutputChannel: window.createOutputChannel('Saros Trace') //TODO: set with config        
        };

        return clientOptions;
    }

    public deactivate(): void {
        this.client.stop();
    }
}

class MyErrorHandler implements ErrorHandler { //TODO: move to own file?! + better name

    error(error: Error, message: Message, count: number): ErrorAction {
        console.log(`<ERROR>`);
        console.log(`error = ${error.message}`);
        //console.log(`message = ${message.jsonrpc}`);
        //console.log(`count = ${count}`);
        console.log(`</ERROR>`);
       
        let t = typeof(error);

        if(error.code === "ECONNREFUSED") {
            return ErrorAction.Shutdown;
        }

        //ECONNREFUSED
        return ErrorAction.Continue;
    }    
    
    closed(): CloseAction {
        console.log(`CLOSED!`);

        return CloseAction.DoNotRestart;
    }


}

export const sarosExtensionInstance = new SarosExtension();