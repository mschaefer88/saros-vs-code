package saros.lsp.annotation;

import saros.activities.TextEditActivity;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.client.dto.AnnotationParams;

public class AnnotationManager {

    private final ISarosLanguageClient languageClient;

    public AnnotationManager(ISarosLanguageClient languageClient) {
        this.languageClient = languageClient;
    }

    public void annotate(TextEditActivity activity) {

        AnnotationParams annotation = new AnnotationParams();
        this.languageClient.sendAnnotation(annotation);
    }
}