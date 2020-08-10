package saros.lsp.preferences;

// import saros.annotations.Component;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;

// @Component(module = "server")
public class LspPreferences extends Preferences {

  /**
   * Initializes a ServerPrefrerences.
   *
   * @param store the preference store to use
   */
  public LspPreferences(IPreferenceStore store) {
    super(store);
  }
}
