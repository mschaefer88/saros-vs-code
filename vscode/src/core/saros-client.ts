import * as vscode from 'vscode';
import { LanguageClient, Message, ServerOptions, LanguageClientOptions, StreamInfo, VersionedTextDocumentIdentifier, NotificationHandler, NotificationType, RequestType, RPCMessageType } from "vscode-languageclient";

/**
 * Response for adding new accounts.
 *
 * @export
 * @interface AddAccountResponse
 */
export interface SarosResponse {
    success: boolean;
    error: string;
}

export interface SarosResultResponse<T> extends SarosResponse {
    result: T;
}

export interface AccountDto extends AccountIdDto {
    password: string;
    server: string; 
    port: number;
    useTLS: boolean; 
    useSASL: boolean;
    isDefault: boolean;
}

export interface AccountIdDto {
    username: string;
    domain: string;
}

export interface ContactDto { //TODO: differenciate between request and response! (clnt&svr)
    id: string;
    nickname: string;
    isOnline: boolean;
    hasSarosSupport: boolean;
}

export interface InviteDto {
    id: string;
    description: string;
}

//TODO: use namespaces for grouping
export namespace OpenProjectNotification {
    export const type = new NotificationType<SarosResultResponse<string>, void>('saros/editor/open'); //TODO: naming
}

export namespace SessionStateNotification {
	export const type = new NotificationType<SarosResultResponse<boolean>, void>('saros/session/state'); //TODO: naming
}

export namespace ConnectedStateNotification {
	export const type = new NotificationType<SarosResultResponse<boolean>, void>('saros/session/connected'); //TODO: naming
}

export namespace ContactStateNotification {
    export const type = new NotificationType<ContactDto, void>('saros/contact/state'); //TODO: naming
}

export namespace AddAccountRequest {
    export const type = new RequestType<AccountDto, SarosResponse, void>('saros/account/add');
}

export namespace CreateAccountRequest {
    export const type = new RequestType<void, SarosResultResponse<string>, void>('saros/account/create');
}

export namespace UpdateAccountRequest {
    export const type = new RequestType<AccountDto, SarosResponse, void>('saros/account/update');
}

export namespace RemoveAccountRequest {
    export const type = new RequestType<AccountIdDto, SarosResponse, void>('saros/account/remove');
}

export namespace SetDefaultAccountRequest {
    export const type = new RequestType<AccountIdDto, SarosResponse, void>('saros/account/setDefault');
}

export namespace GetAllAccountRequest {
    export const type = new RequestType<void, SarosResultResponse<AccountDto[]>, void>('saros/account/getAll');
}

export namespace AddContactRequest {
    export const type = new RequestType<ContactDto, SarosResponse, void>('saros/contact/add');
}

export namespace RemoveContactRequest {
    export const type = new RequestType<ContactDto, SarosResponse, void>('saros/contact/remove');
}

export namespace RenameContactRequest {
    export const type = new RequestType<ContactDto, SarosResponse, void>('saros/contact/rename');
}

export namespace GetAllContactRequest {
    export const type = new RequestType<void, SarosResultResponse<ContactDto[]>, void>('saros/contact/getAll');
}

export namespace ConnectRequest {
    export const type = new RequestType<void, SarosResponse, void>('saros/session/connect');
}

export namespace DisconnectRequest {
    export const type = new RequestType<void, SarosResponse, void>('saros/session/disconnect');
}

export namespace IsOnlineRequest {
    export const type = new RequestType<void, SarosResultResponse<boolean>, void>('saros/session/status'); //TODO: naming
}

export namespace InviteContactRequest {
    export const type = new RequestType<ContactDto, SarosResultResponse<boolean>, void>('saros/session/invite'); //TODO: naming
}

export namespace StartSessionRequest {
    export const type = new RequestType<void, SarosResponse, void>('saros/session/start');
}

export namespace StopSessionRequest {
    export const type = new RequestType<void, SarosResponse, void>('saros/session/stop');
}

export interface AnnotationParams {
    uri: string;

    user: string;

    range: vscode.Range;
}

export namespace AnnotationNotification {
    export const type = new NotificationType<SarosResultResponse<AnnotationParams[]>, void>('saros/editor/annotate');
}

/**
 * Custom language client for Saros protocol.
 *
 * @export
 * @class SarosClient
 * @extends {LanguageClient}
 */
export class SarosClient extends LanguageClient {

    constructor(serverOptions: (() => Promise<StreamInfo>), clientOptions: LanguageClientOptions) {
        super('saros', 'Saros Server', serverOptions, clientOptions, true);//TODO: get from config
    }
}