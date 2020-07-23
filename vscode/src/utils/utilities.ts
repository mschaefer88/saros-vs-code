import {QuickPickItem} from '../types';
import {window} from 'vscode';
import {SarosResponse} from '../lsp';

export function pick<T>(items: T[], labelFunc: ((item: T) => string), detailFunc: ((item: T) => string)) {
  const pickItems = mapToQuickPickItems(items, labelFunc, detailFunc);
  return window.showQuickPick(pickItems);
}

export function mapToQuickPickItems<T>(items: T[], labelFunc: ((item: T) => string), detailFunc?: ((item: T) => string)): QuickPickItem<T>[] {
  return items.map((item) => {
    return {
      label: labelFunc(item),
      detail: detailFunc ? detailFunc(item) : undefined,
      item: item,
    };
  });
}

export function showMessage(response: SarosResponse, successMessage: string, errorMessage?: string) {
  if (response.success) {
    window.showInformationMessage(successMessage);
  } else {
    window.showErrorMessage(response.error || errorMessage || 'Unknown Error');
  }
}
