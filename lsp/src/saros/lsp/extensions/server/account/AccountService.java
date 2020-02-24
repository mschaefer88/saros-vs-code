package saros.lsp.extensions.server.account;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;

import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.account.dto.AccountDto;
import saros.lsp.extensions.server.account.dto.AccountIdDto;
import saros.net.xmpp.JID;

/** Implementation of the account service. */
public class AccountService implements IAccountService {

  private final XMPPAccountStore accountStore;

  private static final Logger LOG = Logger.getLogger(AccountService.class);

  public AccountService(final XMPPAccountStore accountStore) {
    this.accountStore = accountStore;
  }

  public CompletableFuture<SarosResultResponse<AccountDto[]>> getAll() {
    
    final List<XMPPAccount> accounts = this.accountStore.getAllAccounts();
    final XMPPAccount defaultAccount = this.accountStore.getDefaultAccount();

    final AccountDto[] dtos = accounts.stream().map(account -> {
      AccountDto dto = new AccountDto();
      dto.domain = account.getDomain();
      dto.username = account.getUsername();
      dto.password = account.getPassword();
      dto.server = account.getServer();
      dto.port = account.getPort();
      dto.useTLS = account.useTLS();
      dto.useSASL = account.useSASL();
      dto.isDefault = account.equals(defaultAccount);

      return dto;
    }).toArray(size -> new AccountDto[size]);

    return CompletableFuture.completedFuture(new SarosResultResponse<AccountDto[]>(dtos));
  }

  @Override
  public CompletableFuture<SarosResponse> update(final AccountDto request) {
    
    try {
      //TODO: do better
      JID jid = new JID(request.username);
      //TODO: allow complete change? check Eclipse
      final XMPPAccount account = this.accountStore.getAccount(jid.getName(), jid.getDomain());
      this.accountStore.changeAccountData(account, request.username, request.password, request.domain,  //TODO: enable override with domain from request
        request.server, request.port, request.useTLS, request.useSASL);
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> remove(final AccountIdDto request) {
    
    try {
      final XMPPAccount account = this.accountStore.getAccount(request.username, request.domain);
      this.accountStore.deleteAccount(account);
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> setDefault(final AccountIdDto request) {
    
    try {
      final XMPPAccount account = this.accountStore.getAccount(request.username, request.domain);
      this.accountStore.setDefaultAccount(account);
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> add(AccountDto request) {
    try {
      //TODO: do better
      JID jid = new JID(request.username);
      this.accountStore.createAccount(jid.getName(), request.password, //TODO: enable override with domain from request
      jid.getDomain(), request.server, request.port, request.useTLS,
      request.useSASL);
    }catch(Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }
}
