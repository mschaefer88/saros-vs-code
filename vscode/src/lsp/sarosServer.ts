import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as net from 'net';
import {StreamInfo} from 'vscode-languageclient';
import getPort = require('get-port');
import {config} from './sarosConfig';

class SarosState {
    port?: number;
}

const Used_Port_Key = 'SAROS_PORT';

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
    private _process?: cp.ChildProcess;

    private _output!: vscode.OutputChannel;

    /**
     * Creates an instance of SarosServer.
     *
     * @param {vscode.ExtensionContext} _context - The extension context
     * @memberof SarosServer
     */
    constructor(private _context: vscode.ExtensionContext) {

    }

    /**
     * Starts the server process.
     *
     * @param {number} port - The port the server listens on for connection
     * @memberof SarosServer
     */
    public async start(port: number): Promise<void> {
      this._startProcess(`-p=${port}`, `-l=${config.getLogServer()}`)
          ._withDebug(true);
    }

    /**
     * Provides access to the start function.
     *
     * @remarks A free port will be determined and used.
     * @returns {() => Thenable<StreamInfo>} Function that starts the server and retuns the io information
     * @memberof SarosServer
     */
    public getStartFunc(): () => Promise<StreamInfo> {
      const self = this;
      async function createServer(): Promise<StreamInfo> {
        const port = config.getServerPort() || await getPort();
        console.log(`Using port ${port} for server.`);

        if (!config.isServerStandalone()) {
          await self.start(port);
        }

        const connectionInfo: net.NetConnectOpts = {
          port: port,
        };
        const socket = net.connect(connectionInfo);

        const result: StreamInfo = {
          writer: socket,
          reader: socket,
        };

        return result;
      }

      return createServer;
    }

    /**
     * Starts the Saros server jar as process.
     *
     * @private
     * @param {...any[]} args - Additional command line arguments for the server
     * @return {SarosServer} Itself
     * @memberof SarosServer
     */
    private _startProcess(...args: any[]): SarosServer {
      const pathToJar = path.resolve(this._context.extensionPath, 'out', 'saros.lsp.jar'); // TODO: change on publish
      const jre = require('node-jre');

      if (this._process) {
        console.log('Killing old process.');
        this._process.kill();
      }


      // console.log('Installing jre.');
      // jre.install((error: Error) => console.log(error));

      console.log('Spawning jar process.');
      this._process = jre.spawn(
          [pathToJar],
          'saros.lsp.SarosLauncher',
          args,
          {encoding: 'utf8'},
      ) as cp.ChildProcess;

      try {

        // var pathToFork = path.resolve(this.context.extensionPath, 'out', 'core', 'process-starter.js');//TODO: put aside jar
        // console.log(pathToFork);
        // this.process = process.fork(pathToFork, [`java -jar "${pathToJar}" ${args[0]}`], {detached: true, stdio: 'ignore'});
        // this.process = process.fork(pathToFork, ['notepad'], {detached: true, stdio: 'ignore'});


        // let cp = require('child_process')
        // var child = cp.spawn('notepad', [], {
        //             detached: true,
        //             stdio: 'ignore'
        //             });

        //         console.log(`Server PID is ${child.pid}`);
        //             child.unref();
      } catch (err) {
        console.log(err);
      }

      console.log(`Server PID is ${this._process?.pid}`);

      return this;
    }

    /**
     * Attaches listeners for debug informations and prints
     * retrieved data to a newly created [output channel](#vscode.OutputChannel).
     *
     * @private
     * @param {boolean} isEnabled - Wether debug output is redirected or not
     * @return {SarosServer} Itself
     * @memberof SarosServer
     */
    private _withDebug(isEnabled: boolean): SarosServer {
      if (this._process === undefined) {
        throw new Error('Server process is undefined');
      }

      if (!isEnabled) {
        return this;
      }

      if (!this._output) {
        this._output = vscode.window.createOutputChannel('Saros (Server)');
      }

      this._output.clear();

      this._process.stdout.on('data', (data) => {
        this._output.appendLine(data);
      });

      this._process.stderr.on('data', (data) => {
        this._output.appendLine(data);
      });

      return this;
    }
}
