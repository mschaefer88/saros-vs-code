import {NotificationType, RequestType, TextDocumentIdentifier} from 'vscode-languageclient';
import {Range} from 'vscode';

/**
 * Generic response that indicates success or failure.
 *
 * @export
 * @interface SarosResponse
 */
export interface SarosResponse {
  success: boolean;
  error: string;
}

/**
 * Response that indicates success or failure and
 * contains a payload.
 *
 * @export
 * @interface SarosResultResponse
 * @extends {SarosResponse}
 * @template T
 */
export interface SarosResultResponse<T> extends SarosResponse {
  result: T;
}

/**
 * Contains data about an account.
 *
 * @export
 * @interface AccountDto
 * @extends {AccountIdDto}
 */
export interface AccountDto extends AccountIdDto {
  password: string;
  server: string;
  port: number;
  useTLS: boolean;
  useSASL: boolean;
  isDefault: boolean;
}

/**
 * Identifies an account.
 *
 * @export
 * @interface AccountIdDto
 */
export interface AccountIdDto {
  username: string;
  domain: string;
}

/**
 * Contains data about a contact.
 *
 * @export
 * @interface ContactDto
 */
export interface ContactDto {
  id: string;
  nickname: string;
  isOnline: boolean;
  hasSarosSupport: boolean;
  subscribed: boolean;
}

/**
 * Contains data about the user to invite.
 *
 * @export
 * @interface InviteDto
 */
export interface InviteDto {
  id: string;
  description: string;
}

/**
 * Contains data about an user in the session.
 *
 * @export
 * @interface SessionUserDto
 */
export interface SessionUserDto {
  id: string;
  nickname: string;
  annotationColorId: number;
}

/**
 * Contains data about annotations.
 *
 * @export
 * @interface AnnotationParams
 */
export interface AnnotationParams {
  uri: string;
  user: string;
  range: Range;
  annotationColorId: number;
}

/**
 * Notification that issues the client to open
 * an editor with the file contents.
 * 
 * @export
 */
export namespace OpenEditorNotification {
  export const type =
    new NotificationType<SarosResultResponse<string>, void>(
        'saros/editor/open',
    );
}

/**
 * Notification that informs the client about a
 * state change of the session, ie. if it's
 * active or not.
 * 
 * @export
 */
export namespace SessionStateNotification {
  export const type =
    new NotificationType<SarosResultResponse<boolean>, void>(
        'saros/session/state',
    );
}

/**
 * Notification that informs the client about a
 * state change of the XMPP connection, ie. if
 * it's active or not.
 * 
 * @export
 */
export namespace ConnectedStateNotification {
  export const type =
    new NotificationType<SarosResultResponse<boolean>, void>(
        'saros/session/connected',
    );
}

/**
 * Notification that informs the client about a
 * state change of a contact, eg. online status
 * or saros support.
 * 
 * @export
 */
export namespace ContactStateNotification {
  export const type =
    new NotificationType<ContactDto, void>('saros/contact/state');
}

/**
 * Request to the server to add a new account for
 * connections to the XMPP server.
 * 
 * @export
 */
export namespace AddAccountRequest {
  export const type =
    new RequestType<AccountDto, SarosResponse, void, unknown>(
        'saros/account/add',
    );
}

/**
 * Request to the server to update an existing account.
 * 
 * @export
 */
export namespace UpdateAccountRequest {
  export const type =
    new RequestType<AccountDto, SarosResponse, void, unknown>(
        'saros/account/update',
    );
}

/**
 * Request to the server to remove an existing account.
 * 
 * @export
 */
export namespace RemoveAccountRequest {
  export const type =
    new RequestType<AccountIdDto, SarosResponse, void, unknown>(
        'saros/account/remove',
    );
}

/**
 * Request to the server to set the currently active account.
 * 
 * @export
 */
export namespace SetActiveAccountRequest {
  export const type =
    new RequestType<AccountIdDto, SarosResponse, void, unknown>(
        'saros/account/setActive',
    );
}

/**
 * Request to the server to get all saved accounts.
 * 
 * @export
 */
export namespace GetAllAccountRequest {
  export const type =
    new RequestType<void, SarosResultResponse<AccountDto[]>, void, unknown>(
        'saros/account/getAll',
    );
}

/**
 * Request to the server to add a new contact.
 * 
 * @export
 */
export namespace AddContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResponse, void, unknown>(
        'saros/contact/add',
    );
}

/**
 * Request to the server to remove an existing contact.
 * 
 * @export
 */
export namespace RemoveContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResponse, void, unknown>(
        'saros/contact/remove',
    );
}

/**
 * Request to the server to change the nickname of an
 * existing contact.
 * 
 * @export
 */
export namespace RenameContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResponse, void, unknown>(
        'saros/contact/rename',
    );
}

/**
 * Request to the server to get all contacts
 * of the contact list.
 * 
 * @export
 */
export namespace GetAllContactRequest {
  export const type =
    new RequestType<void, SarosResultResponse<ContactDto[]>, void, unknown>(
        'saros/contact/getAll',
    );
}

/**
 * Request to the server to connect to the XMPP
 * server with the currently active account.
 * 
 * @export
 */
export namespace ConnectRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/connect',
    );
}

/**
 * Request to the server to disconnect from the XMPP
 * server.
 * 
 * @export
 */
export namespace DisconnectRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/disconnect',
    );
}

/**
 * Request to the server to get the current state
 * of the session, ie. active or not.
 * 
 * @export
 */
export namespace IsOnlineRequest {
  export const type =
    new RequestType<void, SarosResultResponse<boolean>, void, unknown>(
        'saros/session/status',
    );
}

/**
 * Request to the server to invite a contact from
 * the contact list to a new or active session.
 * 
 * @export
 */
export namespace InviteContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResultResponse<boolean>, void, unknown>(
        'saros/session/invite',
    );
}

/**
 * Request to the server to start a new empty session.
 * 
 * @export
 */
export namespace StartSessionRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/start',
    );
}

/**
 * Request to the server to stop the currently
 * active session.
 * 
 * @export
 */
export namespace StopSessionRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/stop',
    );
}

/**
 * Notification to the client that a user has
 * joined the currently active session.
 * 
 * @export
 */
export namespace UserJoinedSessionNotification {
  export const type =
    new NotificationType<SessionUserDto, void>(
        'saros/session/user-joined',
    );
}

/**
 * Notification to the client that the state
 * of a user that is part of the session has
 * changed.
 * 
 * @export
 */
export namespace UserChangedSessionNotification {
  export const type =
    new NotificationType<SessionUserDto, void>(
        'saros/session/user-changed',
    );
}

/**
 * Notification to the client that a user
 * that is part of the session has left.
 * 
 * @export
 */
export namespace UserLeftSessionNotification {
  export const type =
    new NotificationType<SessionUserDto, void>(
        'saros/session/user-left',
    );
}

/**
 * Notification to the client that annotations
 * have changed.
 * 
 * @export
 */
export namespace AnnotationNotification {
  export const type =
    new NotificationType<SarosResultResponse<AnnotationParams[]>, void>(
        'saros/editor/annotate',
    );
}

/**
 * Request to the server to get all annotations
 * of an editor.
 * 
 * @export
 */
export namespace GetAnnotationsRequest {
  export const type =
    new RequestType<TextDocumentIdentifier, SarosResultResponse<AnnotationParams[]>, void, unknown>(
        'textDocument/getAnnotations',
    );
}
