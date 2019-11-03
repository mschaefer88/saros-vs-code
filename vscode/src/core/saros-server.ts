import * as vscode from 'vscode';
import * as path from 'path';
import * as process from 'child_process';
import * as net from 'net';
import { StreamInfo } from 'vscode-languageclient';
import * as port from 'get-port';
import getPort = require('get-port');

/**
 * Encapsulation of the Saros server.
 *
 * @export
 * @class SarosServer
 */
export class SarosServer {
    
    /**
     * Started process of the server.
     *
     * @private
     * @type {process.ChildProcess}
     * @memberof SarosServer
     */
    private process?: process.ChildProcess;

    private output!: vscode.OutputChannel;

    /**
     * Creates an instance of SarosServer.
     * 
     * @param {vscode.ExtensionContext} context - The extension context
     * @memberof SarosServer
     */
    constructor(private context: vscode.ExtensionContext) {
                
    }

    /**
     * Starts the server process.
     *
     * @param {number} port - The port the server listens on for connection 
     * @memberof SarosServer
     */
    public start(port: number): void {

        this.startProcess(port)
            .withDebug(true);
    }

    /**
     * Provides access to the start function.
     *
     * @remarks A free port will be determined and used.
     * @returns {() => Thenable<StreamInfo>} Function that starts the server and retuns the io information
     * @memberof SarosServer
     */
    public getStartFunc(): () => Promise<StreamInfo> {
        
        let self = this;
        function createServer(): Promise<StreamInfo> {
            
            return getPort().then(async port => {
                console.log(`Using port ${port} for server.`);

                await self.start(port);

                console.log("RETURN");

                let connectionInfo = {
                    port: port
                };
                let socket = net.connect(connectionInfo);
                let result: StreamInfo = {
                    writer: socket,
                    reader: socket
                };                

                return result;
            });
        }

        return createServer;
    }

    /**
     * Starts the Saros server jar as process.
     *
     * @private
     * @param {...any[]} args - Additional command line arguments for the server
     * @returns {SarosServer} Itself 
     * @memberof SarosServer
     */
    private startProcess(...args: any[]): SarosServer {
        
        var pathToJar = path.resolve(this.context.extensionPath, 'out', 'saros.lsp.jar'); //TODO: change on publish
        var jre = require('node-jre');

        if(this.process) {
            console.log('Killing old process.');
            this.process.kill();
        }


        // console.log('Installing jre.');
        // jre.install((error: Error) => console.log(error));

        console.log('Spawning jar process.');
        this.process = jre.spawn(
            [pathToJar],
            'saros.lsp.SarosLauncher',
            args,
            { encoding: 'utf8' }
        ) as process.ChildProcess; 
        
        return this;
    }

    /**
     * Attaches listeners for debug informations and prints
     * retrieved data to a newly created [output channel](#vscode.OutputChannel).
     * 
     * @private
     * @param {boolean} isEnabled - Wether debug output is redirected or not
     * @returns {SarosServer} Itself
     * @memberof SarosServer
     */
    private withDebug(isEnabled: boolean): SarosServer {

        if(this.process === undefined) {
            throw new Error('Server process is undefined');
        } 

        if(!isEnabled) {
            return this;
        }

        if(!this.output) {
            this.output = vscode.window.createOutputChannel('Saros Log');
        }

        this.output.clear();

        this.process.stdout.on("data", (data) => {
            this.output.appendLine(data);
        });

        this.process.stderr.on("data", (data) => {
            this.output.appendLine(data);
        });   

        return this;
    }
}