package saros.lsp.extensions.server;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.services.LanguageServer;

import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.session.ISessionService;
import saros.lsp.extensions.server.contact.IContactService;

/** 
 * Interface of the Saros language server. 
 * 
 * It defines which services are available
 * to be consumed.
 * 
 * All Saros related features that 
 * aren't covered by the lsp protocol 
 * have to be specified here.
 */
public interface ISarosLanguageServer extends LanguageServer {

  /** Provides access to the account services. */
  @JsonDelegate
  IAccountService getSarosAccountService();

  @JsonDelegate
  IContactService getSarosContactService();

  @JsonDelegate
  ISessionService getSarosConnectionService();

  void onExit(Runnable runnable);

  void onInitialize(Consumer<InitializeParams> consumer);
}
