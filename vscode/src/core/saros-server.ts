import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as net from 'net';
import { StreamInfo } from 'vscode-languageclient';
import * as port from 'get-port';
import getPort = require('get-port');
import { createWriteStream } from 'fs';

class SarosState {
    port?: number;
}

const Used_Port_Key = "SAROS_PORT";

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
    private process?: cp.ChildProcess;

    private output!: vscode.OutputChannel;

    private store: vscode.Memento;

    /**
     * Creates an instance of SarosServer.
     * 
     * @param {vscode.ExtensionContext} context - The extension context
     * @memberof SarosServer
     */
    constructor(private context: vscode.ExtensionContext) {

        this.store = context.globalState;   
    }

    /**
     * Starts the server process.
     *
     * @param {number} port - The port the server listens on for connection 
     * @memberof SarosServer
     */
    public async start(port: number): Promise<void> {

        this.startProcess(port)
            .withDebug(true);

        console.log("Wait.PRE")
        for(var i = 0; i < 5; i++) {
            console.log(`wait.${i}`);
            await this.delay(1000);
        }
        console.log("wait.POST");
    }

    private delay(ms: number) {
        return new Promise( resolve => setTimeout(resolve, ms) );
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
            
            let oldPort = self.store.get(Used_Port_Key);
            self.store.update(Used_Port_Key, undefined);
            if(oldPort) {
                try {
                    console.log(`Using old port ${oldPort} for server.`);
                    let connectionInfo = {
                        port: oldPort as number
                    };
                    let socket = net.connect(connectionInfo);
                    let result: StreamInfo = {
                        writer: socket,
                        reader: socket
                    };                

                    return Promise.resolve(result);
                } catch(error) {
                    console.log(error);
                }
            }

            return getPort().then(async port => {

                console.log(`Using port ${port} for server.`);
                // self.store.update(Used_Port_Key, port);

                await self.start(port);

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
        ) as cp.ChildProcess; 

        try {            

            //var pathToFork = path.resolve(this.context.extensionPath, 'out', 'core', 'process-starter.js');//TODO: put aside jar
            //console.log(pathToFork);
            //this.process = process.fork(pathToFork, [`java -jar "${pathToJar}" ${args[0]}`], {detached: true, stdio: 'ignore'});
            //this.process = process.fork(pathToFork, ['notepad'], {detached: true, stdio: 'ignore'});
           


            // let cp = require('child_process')
            // var child = cp.spawn('notepad', [], {
            //             detached: true,
            //             stdio: 'ignore'
            //             });
            
            //         console.log(`Server PID is ${child.pid}`);
            //             child.unref();
        }catch(err) {
            console.log(err);
        }
        
        console.log(`Server PID is ${this.process?.pid}`);
        
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