package saros.lsp.extensions.server;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
//import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;

import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.lsp.extensions.client.ISarosLanguageClient;

public abstract class SarosService {

    protected ConnectionHandler connectionHandler; //TODO: for testing

    protected XMPPAccountStore accountStore; //TODO: for testing

    protected ISarosLanguageClient client;

    private static final Logger LOG = Logger.getLogger(SarosService.class);

    protected SarosService(final ConnectionHandler connectionHandler, XMPPAccountStore accountStore, final ISarosLanguageClient client) {
        this.connectionHandler = connectionHandler;
        this.accountStore = accountStore;
        this.client = client;
    }

    protected <T> CompletableFuture<T> withConnection(Supplier<T> supplier) {
        if(this.connectionHandler.isConnected()) {
            this.connectionHandler.connect(this.accountStore.getDefaultAccount(), true);
        }

        CompletableFuture<T> c = new CompletableFuture<T>();

        //TODO: separate completable future?
        //TODO: try catch!
        Executors.newCachedThreadPool().submit(() -> {

            c.complete(supplier.get());

            return null;
        });

        return c;
    }

    protected <T> SarosResultResponse<T> withResultResponse(Supplier<T> supplier) {
        try {
            return new SarosResultResponse<T>(supplier.get());
        } catch(Exception e) {
            return new SarosResultResponse<T>(e);
        }
    }

    // protected SarosResponse withResponse(Procedure0 procedure) {
    //     try {
    //         procedure.apply();

    //         return new SarosResponse();
    //     } catch(Exception e) {
    //         return new SarosResponse(e);
    //     }
    // }

    protected <T> CompletableFuture<T> asFuture(Supplier<T> action) {

        CompletableFuture<T> c = new CompletableFuture<T>();
//TODO: Callable<>
        Executors.newCachedThreadPool().submit(() -> {

            c.complete(action.get());

            return null;
        });

        return c;
    }

    protected BiPredicate<String, String> fromUserInput() {
        return (title, message) -> this.getUserInput(title, message);
    }

    protected void requireConnection() {
        if(!this.connectionHandler.isConnected() && this.getUserInput("", "Not connected! Connect?")) {
            this.connectionHandler.connect(this.accountStore.getDefaultAccount(), true);
        }
    }

    protected boolean getUserInput(final String title, final String message) { //TODO: rename to yes no

        final MessageActionItem yes = new MessageActionItem("yes");
        final MessageActionItem no = new MessageActionItem("no");

        final ShowMessageRequestParams params = new ShowMessageRequestParams();
        params.setType(MessageType.Info);
        params.setMessage(String.format("%s\n\n%s", title, message));
        params.setActions(Arrays.asList(yes, no));

        MessageActionItem result;

        try {
            result = this.client.showMessageRequest(params).get();
            final boolean r = result.getTitle().equalsIgnoreCase("yes");

            return r;
        } catch (InterruptedException | ExecutionException e) { //TODO: LOG dialog failure
            final MessageParams p = new MessageParams();
            p.setType(MessageType.Error);
            p.setMessage(e.toString());

            this.client.showMessage(p);
            return false;
        }
    }
}