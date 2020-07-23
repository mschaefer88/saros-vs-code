import { ExtensionContext } from "vscode";

export namespace icons {
    export function getSarosSupportIcon(context: ExtensionContext) {
        return context.asAbsolutePath("/media/obj16/contact_saros_obj.png");
    }
    
    export function getIsOnlineIcon(context: ExtensionContext) {
        return context.asAbsolutePath("/media/obj16/contact_obj.png");
    }
    
    export function getIsOfflinetIcon(context: ExtensionContext) {
        return context.asAbsolutePath("/media/obj16/contact_offline_obj.png");
    }
    
    export function getAddAccountIcon(context: ExtensionContext) {
        return context.asAbsolutePath("/media/btn/addaccount.png");
    }
}