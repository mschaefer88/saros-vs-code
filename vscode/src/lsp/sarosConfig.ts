import { workspace, WorkspaceConfiguration } from 'vscode';

export namespace config {
    export enum ServerTrace {
        Off = 'off',
		Messages = 'messages',
		Verbose = 'verbose'
    }
    
    export enum ServerLog {
        All ='all',
		Debug =	'debug',
		Error =	'error',
		Fatal =	'fatal',
		Info = 'info',
		Off = 'off',
		Trace =	'trace',
		Warn = 'warn'
    }

    export const appName = 'saros';

    export function getTraceServer(): ServerTrace {
        const trace = getConfiguration().get('trace.server') as ServerTrace;
        
        return trace;
    }

    export function getLogServer(): ServerLog {
        const log = getConfiguration().get('log.server') as ServerLog;
        
        return log;
    }

    export function getDefaultHost(): string {
        const host = getConfiguration().get('defaultHost.client') as string;
        
        return host;
    }

    export function getServerPort(): number | null {
        const port = getConfiguration().get('port.server') as number;
        
        return port;
    }

    export function isServerStandalone(): boolean {
        const standalone = getConfiguration().get('standalone.server') as boolean;

        return standalone;
    }

    function getConfiguration(): WorkspaceConfiguration {
        return workspace.getConfiguration(appName);
    }
}