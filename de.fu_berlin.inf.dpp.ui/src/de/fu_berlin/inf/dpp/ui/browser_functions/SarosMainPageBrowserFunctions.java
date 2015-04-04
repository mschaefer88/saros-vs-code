package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_services.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.view_parts.AddAccountPage;
import de.fu_berlin.inf.dpp.ui.view_parts.AddContactPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import java.util.Arrays;
import java.util.List;

/**
 * This class implements the functions to be called by Javascript code for
 * the contact list. These are the callback functions to invoke Java code from
 * Javascript.
 */
public class SarosMainPageBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(SarosMainPageBrowserFunctions.class);

    private final ContactListCoreService contactListCoreService;

    private final IDialogManager dialogManager;

    private final AddContactPage addContactPage;

    private final AddAccountPage addAccountPage;

    public SarosMainPageBrowserFunctions(
        ContactListCoreService contactListCoreService,
        IDialogManager dialogManager, AddContactPage addContactPage,
        AddAccountPage addAccountPage) {
        this.contactListCoreService = contactListCoreService;
        this.dialogManager = dialogManager;
        this.addContactPage = addContactPage;
        this.addAccountPage = addAccountPage;
    }

    /**
     * Injects Javascript functions into the HTML page. These functions
     * call Java code below when invoked.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays.asList(new JavascriptFunction("__java_connect") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0 && arguments[0] != null) {
                    Gson gson = new Gson();
                    final Account account = gson
                        .fromJson((String) arguments[0], Account.class);
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            contactListCoreService.connect(account);
                        }
                    });
                } else {
                    LOG.error("Connect was called without an account.");
                    browser.run(
                        "alert('Cannot connect because no account was given.');");
                }
                return null;
            }
        }, new JavascriptFunction("__java_disconnect") {
            @Override
            public Object function(Object[] arguments) {
                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        contactListCoreService.disconnect();
                    }
                });
                return null;
            }
        }, new JavascriptFunction("__java_deleteContact") {
            @Override
            public Object function(final Object[] arguments) {
                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            contactListCoreService
                                .deleteContact(new JID((String) arguments[0]));
                        } catch (XMPPException e) {
                            LOG.error("Error deleting contact ", e);
                            browser.run("alert('Error deleting contact');");
                        }
                    }
                });
                return null;
            }
        }, new JavascriptFunction("__java_showAddContactWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.showDialogWindow(addContactPage);
                return null;
            }
        }, new JavascriptFunction("__java_showAddAccountWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.showDialogWindow(addAccountPage);
                return true;
            }
        });
    }
}
