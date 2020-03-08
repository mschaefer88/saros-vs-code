import { ExtensionContext, workspace, window, ProgressLocation, commands, Uri, DocumentSelector, DocumentFilter, OverviewRulerLane, Range, ThemeColor, Color, TextEditorDecorationType } from "vscode"; //TODO: überall so machen!
import { SarosServer } from "./saros-server";
import { SarosClient, OpenProjectNotification, AnnotationNotification, AnnotationParams } from "./saros-client";
import { LanguageClientOptions, RevealOutputChannelOn, ErrorHandler, Message, ErrorAction, CloseAction, InitializationFailedHandler, WorkDoneProgressParams } from "vscode-languageclient";
import * as _ from "lodash";

/**
 * The Saros extension.
 *
 * @export
 * @class SarosExtension
 */
export class SarosExtension {
    public context!: ExtensionContext;
    public client!: SarosClient;

    /**
     * Creates an instance of SarosExtension.
     *
     * @memberof SarosExtension
     */
    constructor() {
        this.annotationType = window.createTextEditorDecorationType({
            overviewRulerColor: '#00ffff',
            overviewRulerLane: OverviewRulerLane.Left,
            backgroundColor: '#00ffff'
        });
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

    private annotationType: TextEditorDecorationType;

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

                        self.client.onNotification(OpenProjectNotification.type, async project => {
                            let uri = Uri.file(project.result);
                            let newWindow = false;
                            //let result = await commands.executeCommand('vscode.openFolder', uri, newWindow);
                            //console.log(`Open Project Result: ${result}`);
                        });

                        self.client.onNotification(AnnotationNotification.type, params => {
                            let user = _.groupBy(params.result, a => a.user);

                            _.forEach(user, (as, u) => {
                                
                                if(u !== 'mschaefer88_u') {                                
                                    let ranges = _.map(as, a => a.range);

                                    window.activeTextEditor?.setDecorations(this.annotationType, ranges);
                                }
                            });
                        });
                        
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
            documentSelector: [{scheme: 'file'}],
            synchronize: {
                // Synchronize the setting section 'languageServerExample' to the server
                //configurationSection: 'sarosServer',
                // Notify the server about file changes to '.clientrc files contain in the workspace
                fileEvents: workspace.createFileSystemWatcher('**/*')
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
        //this.client.stop();
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

        return ErrorAction.Continue;
        // if(error.code === "ECONNREFUSED") {
        //     return ErrorAction.Continue;
        // }

        //ECONNREFUSED
        return ErrorAction.Shutdown;
    }    
    
    closed(): CloseAction {
        console.log(`CLOSED!`);

        return CloseAction.Restart;
    }


}

export const sarosExtensionInstance = new SarosExtension();