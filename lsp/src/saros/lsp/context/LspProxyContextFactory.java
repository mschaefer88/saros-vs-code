package saros.lsp.context;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

public class LspProxyContextFactory<T> extends AbstractContextFactory {

    private final Supplier<T> supplier;
    private final Class<T> clazz;

    public LspProxyContextFactory(Class<T> clazz, Supplier<T> supplier) {
        this.clazz = clazz;
        this.supplier = supplier;
    }

    @Override
    public void createComponents(MutablePicoContainer container) {              
        container.addComponent(this.clazz, this.createProxy());
    }

    public Object createProxy() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                
                T c = supplier.get();
                if(c != null) {
                    try {
                        return method.invoke(c, args);
                    } catch(Exception e) {

                    }
                }

                return null;
            }
        };
            
        return newProxyInstance(this.clazz.getClassLoader(), new Class[] { this.clazz }, handler);
    }
}
