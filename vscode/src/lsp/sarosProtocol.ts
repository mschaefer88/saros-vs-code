import {NotificationType, RequestType} from 'vscode-languageclient';
import {Range} from 'vscode';

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

export interface ContactDto {
  id: string;
  nickname: string;
  isOnline: boolean;
  hasSarosSupport: boolean;
  subscribed: boolean;
}

export interface InviteDto {
  id: string;
  description: string;
}

export interface SessionUserDto {
  id: string;
  nickname: string;
}

export interface AnnotationParams {
  uri: string;
  user: string;
  range: Range;
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
