import * as vscode from 'vscode';

export class QuickPickItem<T> implements vscode.QuickPickItem {
    label: string;    
    description?: string | undefined;
    detail?: string | undefined;
    picked?: boolean | undefined;
    alwaysShow?: boolean | undefined;
    item: T;

    constructor(label: string, item: T) {
        this.label = label;
        this.item = item;
    }
}