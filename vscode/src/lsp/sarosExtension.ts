import { ExtensionContext, workspace, window, ProgressLocation, Uri, OverviewRulerLane, TextEditorDecorationType, OutputChannel } from "vscode"; //TODO: überall so machen!
import { SarosServer } from "./sarosServer";
import { OpenProjectNotification, AnnotationNotification } from "./sarosProtocol";
import { LanguageClientOptions, RevealOutputChannelOn, ErrorHandler, Message, ErrorAction, CloseAction } from "vscode-languageclient";
import { config } from "./sarosConfig";
import { SarosClient } from "./sarosClient";
import * as _ from "lodash";
import { SarosEvent } from "./sarosEvents";
import { IEventAggregator, EventAggregator } from "../types/eventAggregator";

type ErrorCallback = (reason?: any) => void;

/**
 * The Saros extension.
 *
 * @export
 * @class SarosExtension
 */
export class SarosExtension implements IEventAggregator {
    public context!: ExtensionContext;
    public client!: SarosClient;
    public channel: OutputChannel;

    private _eventAggregator = new EventAggregator();

    public subscribe<TArgs>(event: string, callback: (args: TArgs) => void) {
        this._eventAggregator.subscribe(event, callback);
    }

    public publish<TArgs>(event: String, args: TArgs) {
        this._eventAggregator.publish(event, args);
    }

    /**
     * Creates an instance of SarosExtension.
     *
     * @memberof SarosExtension
     */
    constructor() {
        this._annotationType = window.createTextEditorDecorationType({
            overviewRulerColor: '#00ffff',
            overviewRulerLane: OverviewRulerLane.Left,
            backgroundColor: '#00ffff'
        });
        this.channel = window.createOutputChannel('Saros');
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

    private _annotationType: TextEditorDecorationType;

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
                
        let self = this;
            
        return window.withProgress({location: ProgressLocation.Window, cancellable: false, title: "Starting Saros"}, 
        () => {
            return new Promise((resolve, reject) => {
                const server = new SarosServer(self.context);
                self.client = new SarosClient(server.getStartFunc(), this._createClientOptions(reject));           
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

                                window.activeTextEditor?.setDecorations(this._annotationType, ranges);
                            }
                        });
                    });
                        
                    resolve();
                });                    
            });
        });
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
    private _createClientOptions(callback: ErrorCallback): LanguageClientOptions { //TODO: param in docs
        let clientOptions: LanguageClientOptions = {
            // Register the server for plain text documents
            documentSelector: [{scheme: 'file'}],
            synchronize: {
                configurationSection: config.appName,
                fileEvents: workspace.createFileSystemWatcher('**/*')
            },
            revealOutputChannelOn: RevealOutputChannelOn.Error, //TODO: set with config file
            errorHandler: new MyErrorHandler(callback),
            progressOnInitialization: true,  
            outputChannel: this.channel
        };

        return clientOptions;
    }

    public deactivate(): void {
        //this.client.stop();
    }
}

class MyErrorHandler implements ErrorHandler { //TODO: move to own file?! + better name

    constructor(private _callback: ErrorCallback) {
        console.log('ctor', _callback, this._callback);
    }

    error(error: Error, message: Message, count: number): ErrorAction {
        console.log(`<ERROR>`);
        console.log(`error = ${error.message}`);
        //console.log(`message = ${message.jsonrpc}`);
        //console.log(`count = ${count}`);
        console.log(`</ERROR>`);
       
        let t = typeof(error);

        console.log('reject');
        console.log(this._callback);
        this._callback(error.message);
        return ErrorAction.Continue;
        // if(error.code === "ECONNREFUSED") {
        //     return ErrorAction.Continue;
        // }

        //ECONNREFUSED
        this._callback(error.message);
        return ErrorAction.Shutdown;
    }    
    
    closed(): CloseAction {
        console.log(`CLOSED!`);
        return CloseAction.DoNotRestart;
        return CloseAction.Restart; //TODO: abort after count
    }


}

export const sarosExtensionInstance = new SarosExtension();