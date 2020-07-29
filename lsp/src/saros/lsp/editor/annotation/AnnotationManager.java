package saros.lsp.editor.annotation;

import saros.activities.TextEditActivity;
import saros.lsp.extensions.client.ISarosLanguageClient;

public class AnnotationManager {

  private final ISarosLanguageClient languageClient;

  public AnnotationManager(ISarosLanguageClient languageClient) {
    this.languageClient = languageClient;
  }

  public void annotate(TextEditActivity activity) {

    // AnnotationParams annotation = new AnnotationParams();
    // this.languageClient.sendAnnotation(annotation);
  }
}
