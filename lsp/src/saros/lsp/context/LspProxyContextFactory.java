package saros.lsp.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import saros.communication.connection.IProxyResolver;
import saros.context.AbstractContextFactory;
import saros.editor.IEditorManager;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IPathFactory;
import saros.lsp.SarosLanguageServer;
import saros.lsp.extensions.ISarosLanguageServer;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.path.PathFactory;
import saros.lsp.extensions.server.account.AccountService;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.session.ISessionService;
import saros.lsp.extensions.server.session.SessionService;
import saros.lsp.extensions.server.ui.UISynchronizerImpl;
import saros.lsp.extensions.server.contact.ContactService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.editor.EditorManager;
import saros.lsp.extensions.server.eventhandler.NegotiationHandler;
import saros.lsp.extensions.server.net.SubscriptionAuthorizer;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;

public class LspProxyContextFactory extends AbstractContextFactory {

    private Supplier<ISarosLanguageClient> supplier;

    public LspProxyContextFactory(Supplier<ISarosLanguageClient> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void createComponents(MutablePicoContainer container) { 

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                
                ISarosLanguageClient c = supplier.get();
                if(c != null) {
                    try {
                        return method.invoke(c, args);
                    } catch(Exception e) {

                    }
                }

                return null;
            }
        };
            
        ISarosLanguageClient pc = (ISarosLanguageClient) Proxy.newProxyInstance(ISarosLanguageClient.class.getClassLoader(), new Class[] { ISarosLanguageClient.class }, handler);
        
        container.addComponent(ISarosLanguageClient.class, pc);
    }
}
