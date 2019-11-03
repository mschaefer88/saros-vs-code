package saros.core.manager.xmpp;

import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.core.manager.ContactManager;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.net.xmpp.contact.XMPPContactsService;

public class XmppContactManager implements ContactManager {

    private XMPPContactsService contactsService;
    
    private ConnectionHandler connectionHandler;
    
    private XMPPAccountStore accountStore;

    public XmppContactManager(XMPPContactsService contactsService,
    ConnectionHandler connectionHandler, XMPPAccountStore accountStore) {
        this.contactsService = contactsService;
        this.connectionHandler = connectionHandler;
        this.accountStore = accountStore;
    }
    
    @Override
    public ContactDto add(ContactDto request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContactDto remove(ContactDto request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContactDto rename(ContactDto request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContactDto[] getAll(ContactDto request) {
        // TODO Auto-generated method stub
        return null;
    }

}