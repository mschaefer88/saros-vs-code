import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as net from 'net';
import {StreamInfo} from 'vscode-languageclient';
import getPort = require('get-port');
import {config} from './sarosConfig';

export type LanguageServerOptions = (() => Promise<StreamInfo>);

const sarosLspJarName = 'saros.lsp.jar';
const sarosLspJarFolder = 'out';

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
     * @return {LanguageServerOptions} Function that starts
     *  the server and retuns the io information
     * @memberof SarosServer
     */
    public getStartFunc(): LanguageServerOptions {
      const self = this;
      /**
       * Reference to creation function of the server
       * for usage in the language server infrastructure.
       *
       * @return {Promise<StreamInfo>} Awaitable promise to the
       *  connection informations
       */
      function createServerFunc(): Promise<StreamInfo> {
        return self.createServer(self);
      }

      return createServerFunc;
    }

    /**
     * Starts the LSP server.
     *
     * @private
     * @param {SarosServer} self Reference to itself
     * @return {Promise<StreamInfo>} Awaitable promise to the
     *  connection informations
     * @memberof SarosServer
     */
    private async createServer(self: SarosServer): Promise<StreamInfo> {
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

    /**
     * Starts the Saros server jar as process.
     *
     * @private
     * @param {...any[]} args - Additional command line arguments for the server
     * @return {SarosServer} Itself
     * @memberof SarosServer
     */
    private _startProcess(...args: any[]): SarosServer {
      const pathToJar = path.resolve(
          this._context.extensionPath,
          sarosLspJarFolder,
          sarosLspJarName,
      );
      const jre = require('node-jre');

      if (this._process) {
        console.log('Killing old process.');
        this._process.kill();
      }

      console.log('Spawning jar process.');
      this._process = jre.spawn(
          [pathToJar],
          'saros.lsp.SarosLauncher',
          args,
          {encoding: 'utf8'},
      ) as cp.ChildProcess;

      return this;
    }

    /**
     * Attaches listeners for debug informations and prints
     * retrieved data to a newly created
     * [output channel](#vscode.OutputChannel).
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
