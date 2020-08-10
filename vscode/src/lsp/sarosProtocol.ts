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

export namespace OpenProjectNotification {
  export const type =
    new NotificationType<SarosResultResponse<string>, void>(
        'saros/editor/open',
    );
}

export namespace SessionStateNotification {
  export const type =
    new NotificationType<SarosResultResponse<boolean>, void>(
        'saros/session/state',
    );
}

export namespace ConnectedStateNotification {
  export const type =
    new NotificationType<SarosResultResponse<boolean>, void>(
        'saros/session/connected',
    );
}

export namespace ContactStateNotification {
  export const type =
    new NotificationType<ContactDto, void>('saros/contact/state');
}

export namespace AddAccountRequest {
  export const type =
    new RequestType<AccountDto, SarosResponse, void, unknown>(
        'saros/account/add',
    );
}

export namespace CreateAccountRequest {
  export const type =
    new RequestType<void, SarosResultResponse<string>, void, unknown>(
        'saros/account/create',
    );
}

export namespace UpdateAccountRequest {
  export const type =
    new RequestType<AccountDto, SarosResponse, void, unknown>(
        'saros/account/update',
    );
}

export namespace RemoveAccountRequest {
  export const type =
    new RequestType<AccountIdDto, SarosResponse, void, unknown>(
        'saros/account/remove',
    );
}

export namespace SetDefaultAccountRequest {
  export const type =
    new RequestType<AccountIdDto, SarosResponse, void, unknown>(
        'saros/account/setDefault',
    );
}

export namespace GetAllAccountRequest {
  export const type =
    new RequestType<void, SarosResultResponse<AccountDto[]>, void, unknown>(
        'saros/account/getAll',
    );
}

export namespace AddContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResponse, void, unknown>(
        'saros/contact/add',
    );
}

export namespace RemoveContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResponse, void, unknown>(
        'saros/contact/remove',
    );
}

export namespace RenameContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResponse, void, unknown>(
        'saros/contact/rename',
    );
}

export namespace GetAllContactRequest {
  export const type =
    new RequestType<void, SarosResultResponse<ContactDto[]>, void, unknown>(
        'saros/contact/getAll',
    );
}

export namespace ConnectRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/connect',
    );
}

export namespace DisconnectRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/disconnect',
    );
}

export namespace IsOnlineRequest {
  export const type =
    new RequestType<void, SarosResultResponse<boolean>, void, unknown>(
        'saros/session/status',
    );
}

export namespace InviteContactRequest {
  export const type =
    new RequestType<ContactDto, SarosResultResponse<boolean>, void, unknown>(
        'saros/session/invite',
    );
}

export namespace StartSessionRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/start',
    );
}

export namespace StopSessionRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/session/stop',
    );
}

export namespace UserJoinedSessionNotification {
  export const type =
    new NotificationType<SessionUserDto, void>(
        'saros/session/user-joined',
    );
}

export namespace UserChangedSessionNotification {
  export const type =
    new NotificationType<SessionUserDto, void>(
        'saros/session/user-changed',
    );
}

export namespace UserLeftSessionNotification {
  export const type =
    new NotificationType<SessionUserDto, void>(
        'saros/session/user-left',
    );
}

export namespace AnnotationNotification {
  export const type =
    new NotificationType<SarosResultResponse<AnnotationParams[]>, void>(
        'saros/editor/annotate',
    );
}

export namespace GetAnnotationsRequest {
  export const type =
    new RequestType<TextDocumentIdentifier, SarosResultResponse<AnnotationParams[]>, void, unknown>(
        'textDocument/getAnnotations',
    );
}
