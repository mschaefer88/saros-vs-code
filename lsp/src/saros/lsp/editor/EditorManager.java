package saros.lsp.editor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

import saros.activities.TextEditActivity;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;

public class EditorManager implements IEditorManager {

  private static final Logger LOG = Logger.getLogger(EditorManager.class);
  private Map<IFile, Editor> openEditors =
      Collections.synchronizedMap(new HashMap<IFile, Editor>());

  private final ISarosLanguageClient languageClient;

  private List<ISharedEditorListener> listeners = new CopyOnWriteArrayList<>(); // TODO: used?!

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(final ISarosSession session) {
          initialize(session);
        }

        @Override
        public void sessionEnded(final ISarosSession session, SessionEndReason reason) {
          uninitialize();
        }
      };

  public EditorManager(ISarosSessionManager sessionManager, ISarosLanguageClient languageClient) {
    sessionManager.addSessionLifecycleListener(this.sessionLifecycleListener);

    this.languageClient = languageClient;

    LOG.info(String.format("Init %d", this.hashCode()));
  }

  protected void uninitialize() {
    this.openEditors.clear();
  }

  protected void initialize(ISarosSession session) {
    assert this.openEditors.size() == 0 : "Open editors were not correctly reset!";
  }

  @Override
  public void openEditor(IFile path, boolean activate) {

    LOG.debug(String.format("openEditor(%s, %b)", path.toString(), activate));

    try {
      Editor editor =
          this.openEditors.containsKey(path)
              ? this.openEditors.get(path)
              : this.openEditors.put(path, new Editor(path));

      if (activate) {
        this.languageClient.openEditor(new SarosResultResponse<String>(editor.getUri()));
      }
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public Set<IFile> getOpenEditors() {
    return this.openEditors.keySet();
  }

  @Override
  public String getContent(IFile path) {

    LOG.debug(String.format("getContent(%s)", path.toString()));

    if (this.openEditors.containsKey(path)) {
      return this.openEditors.get(path).getText();//TODO: ask editor?!
    } else {
      try {
        return IOUtils.toString(path.getContents());
      } catch (IOException e) {
        LOG.error(e);
        return null;
      }
    }
  }

  @Override
  public void saveEditors(IReferencePoint project) {

    LOG.debug(String.format("saveEditors(%s)", project.toString()));

    this.openEditors.keySet().forEach(path -> this.saveEditor(path));
  }

  @Override
  public void closeEditor(IFile path) {

    LOG.info(String.format("closeEditor(%s)", path.toString()));

    this.openEditors.remove(path);
    // TODO: close in vscode
  }

  @Override
  public void adjustViewport(IFile path, LineRange range, TextSelection selection) {
    // TODO Auto-generated method stub

    LOG.info(
        String.format(
            "adjustViewport(%s, %s, %s)", path.toString(), range.toString(), selection.toString()));
  }

  @Override
  public void jumpToUser(User target) {
    // TODO Auto-generated method stub

    LOG.info(String.format("jumpToUser(%s)", target.toString()));
  }

  @Override
  public void addSharedEditorListener(ISharedEditorListener listener) {
    // TODO Auto-generated method stub

    LOG.info(String.format("addSharedEditorListener(%s)", listener.toString()));
    this.listeners.add(listener);
  }

  @Override
  public void removeSharedEditorListener(ISharedEditorListener listener) {
    // TODO Auto-generated method stub

    LOG.info(String.format("removeSharedEditorListener(%s)", listener.toString()));
    this.listeners.remove(listener);
  }

  public void applyTextEdit(TextEditActivity activity) {
    Editor editor = this.openEditors.get(activity.getResource());

    editor.apply(activity);

    this.listeners.forEach(listener -> listener.textEdited(activity));
  }

  public void setVersion(IFile path, int version) { // TODO: experimental!

    LOG.info(String.format("setVersion('%s', %d)", path, version));
    for(IFile editor : this.openEditors.keySet()) {      
      LOG.info(String.format("openEditors = '%s'", editor));
    }
    Editor e = this.openEditors.get(path);
    LOG.info(String.format("setVersion.editor opened = '%s'", e));

    e.setVersion(version);
  }

  public int getVersion(IFile path) {
    if (this.openEditors.containsKey(path)) {
      return this.openEditors.get(path).getVersion();
    } else {
      return 0;
    }
  }

  public Editor getEditor(IFile path) {
    return this.openEditors.get(path); // TODO: check
  }

  public void saveEditor(IFile path) {
    try {
      path
          .setContents(IOUtils.toInputStream(this.openEditors.get(path).getText()));
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  
  /** The default Windows line separator. */
  public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
  /** The default Unix line separator. */
  public static final String UNIX_LINE_SEPARATOR = "\n";

  /**
   * The line separator used in normalized content.
   *
   * <p>The default Unix line separator.
   */
  public static final String NORMALIZED_LINE_SEPARATOR = "\n";
  /**
   * Tries to figure out the line separator used in the text by whether it contains the Windows or
   * Unix line separator.
   *
   * @param text the text to check
   * @return the used line separator or an empty string if no line separator could be found
   * @see #WINDOWS_LINE_SEPARATOR
   * @see #UNIX_LINE_SEPARATOR
   */
  public static String guessLineSeparator(String text) {

    // Windows line ending must be tested first as the Unix line ending is a substring of it
    if (text.contains(WINDOWS_LINE_SEPARATOR)) {
      return WINDOWS_LINE_SEPARATOR;
    }

    if (text.contains(UNIX_LINE_SEPARATOR)) {
      return UNIX_LINE_SEPARATOR;
    }

    return SystemUtils.IS_OS_WINDOWS ? WINDOWS_LINE_SEPARATOR : UNIX_LINE_SEPARATOR;
  }

  /**
   * Normalizes the line endings in the given text by replacing all occurrences of the passed line
   * separator with Unix line separator.
   *
   * <p>Does nothing if the given line separator is the Unix line separator or is not contained in
   * the given text.
   *
   * @param text the text to normalize
   * @param usedLineSeparator the line separator used in the given text
   * @return the normalized text containing only Unix line endings
   * @throws NullPointerException if the given text or line separator to use is <code>null</code>
   * @throws IllegalArgumentException if the given line separator to use is empty
   * @see #NORMALIZED_LINE_SEPARATOR
   */
  public static String normalize(String text, String usedLineSeparator) {
    Objects.requireNonNull(text, "The given text must not be null");
    Objects.requireNonNull(usedLineSeparator, "The given line separator must not be null");

    if (usedLineSeparator.isEmpty()) {
      throw new IllegalArgumentException("The given line separator must not be empty.");
    }

    if (text.isEmpty() || usedLineSeparator.equals(NORMALIZED_LINE_SEPARATOR)) {
      return text;
    }

    return text.replace(usedLineSeparator, NORMALIZED_LINE_SEPARATOR);
  }

  @Override
  public String getNormalizedContent(IFile file) {
    String content = this.getContent(file);
    String normContent = "";
    if(content != null && !content.isEmpty()) {
      normContent = normalize(content, guessLineSeparator(content));
    }

    LOG.info(String.format("Normalized Content for '%s' is '%s'", file.getName(), content));

    return normContent;
  }
}
