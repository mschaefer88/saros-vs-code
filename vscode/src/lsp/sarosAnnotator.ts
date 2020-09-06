import { AnnotationParams, AnnotationNotification, GetAnnotationsRequest, SarosResultResponse } from "./sarosProtocol";
import { window, OverviewRulerLane, TextEditorDecorationType, Uri } from "vscode";
import * as _ from 'lodash';
import { config } from "./sarosConfig";
import { SarosClient } from "./sarosClient";
import { TextDocumentIdentifier } from "vscode-languageclient";

export class SarosAnnotator {

  private _annotationTypes = new Map<number, TextEditorDecorationType>();

  /**
   * Creates an instance of SarosAnnotator.
   *
   * @memberof SarosAnnotator
   */
  constructor(client: SarosClient) {
    client.onReady().then(() => {
      client.onNotification(AnnotationNotification.type,
          (params) => {
            this.processAnnotations(params.result);
          },
      );
      window.onDidChangeActiveTextEditor(async activeEditor =>
        {
          if(!activeEditor) {
            return;
          }

          const result = await client.sendRequest(GetAnnotationsRequest.type,
            TextDocumentIdentifier.create(activeEditor.document.uri.toString()));
          this.processAnnotations(result.result);
        });
    });
  }

  private createDecorationType(color: string): TextEditorDecorationType {
    return window.createTextEditorDecorationType({
      overviewRulerColor: color,
      overviewRulerLane: OverviewRulerLane.Left,
      backgroundColor: color,
    });
  }

  private getOrCreateDecorationType(annotationColorId: number)
    : TextEditorDecorationType {
    let decorationType = this._annotationTypes.get(annotationColorId) ||
      this.createDecorationType(config.getAnnotationColor(annotationColorId));
    if(!this._annotationTypes.has(annotationColorId)) {
      this._annotationTypes.set(annotationColorId, decorationType);
    }

    return decorationType;
  }

  /**
   * Processes incoming annotations.
   *
   * @private
   * @param {AnnotationParams[]} annotations Current annotations
   * @memberof SarosAnnotator
   */
  public processAnnotations(annotations: AnnotationParams[]) {
    const user = _.groupBy(annotations, (a) => a.user);
    _.forEach(user, (as, u) => {
      const ranges = _.map(as, (a) => a.range);
      const annotationColorId = as[0].annotationColorId;
      const targetUri = decodeURIComponent(as[0].uri);

      const editor = window.visibleTextEditors.find(editor => 
        decodeURIComponent(editor.document.uri.toString()) === targetUri);
      editor?.setDecorations(this.getOrCreateDecorationType(annotationColorId),
        ranges);
    });
  }
}