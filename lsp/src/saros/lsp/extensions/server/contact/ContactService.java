package saros.lsp.extensions.server.contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressParams;

import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.RequireConnection;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.SarosService;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.extensions.server.contact.dto.Test;
import saros.monitoring.IProgressMonitor;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

public class ContactService extends SarosService implements IContactService, IContactsUpdate { //TODO: get added etc.

    private XMPPContactsService contactsService;    

    private IProgressMonitor progressMonitor;

    private static final Logger LOG = Logger.getLogger(ContactService.class);

    public ContactService(XMPPContactsService contactsService,
    ConnectionHandler connectionHandler, XMPPAccountStore accountStore, 
    ISarosLanguageClient client, IProgressMonitor progressMonitor) {
        super(connectionHandler, accountStore, client);
        this.contactsService = contactsService;
        this.progressMonitor = progressMonitor;

        this.contactsService.addListener(this); //TODO: do different!
    }

    private void tryConnect() {
        if(!this.connectionHandler.isConnected()) {
            if(this.getUserInput("Connection needed", "Connect?")) {
                this.connectionHandler.connect(this.accountStore.getDefaultAccount(), true);
            }
        }
    }

    @RequireConnection
    @Override
    public CompletableFuture<SarosResponse> add(ContactDto request) {

        CompletableFuture<SarosResponse> c = new CompletableFuture<SarosResponse>();

        Executors.newCachedThreadPool().submit(() -> {

            try {
                this.contactsService.addContact(new JID(request.id), request.nickname,
                        this.fromUserInput());
                        
                c.complete(new SarosResponse());
            } catch (Exception e) {
                c.complete(new SarosResponse(e));
            }

            return null;
        });

        return c;
    }

    @Override
    public CompletableFuture<SarosResponse> remove(ContactDto request) {
        
        try {
            final Optional<XMPPContact> contact = this.contactsService.getContact(request.id);
            
            if(contact.isPresent()) {                
                this.contactsService.removeContact(contact.get());
            }
        } catch(Exception e) {
            return CompletableFuture.completedFuture(new SarosResponse(e));
        }

        return CompletableFuture.completedFuture(new SarosResponse());
    }

    @Override
    public CompletableFuture<SarosResponse> rename(ContactDto request) {
        
        try {
            LOG.info("1/7");
            final Optional<XMPPContact> contact = this.contactsService.getContact(request.id);
            
            LOG.info("2/7");
            if(contact.isPresent()) {
                LOG.info("3/7");
                this.contactsService.renameContact(contact.get(), request.nickname);
                LOG.info("4/7");
                this.client.sendStateContact(request);
                LOG.info("5/7");
            }
        } catch(Exception e) {
            return CompletableFuture.completedFuture(new SarosResponse(e));
        }

        return CompletableFuture.completedFuture(new SarosResponse());
    }

    @Override
    public CompletableFuture<SarosResultResponse<ContactDto[]>> getAll(ContactDto request) {
        
        final List<XMPPContact> contacts = this.contactsService.getAllContacts();

        LOG.info(String.format("Retrieved %d contacts", contacts.size()));

        final ContactDto[] dtos = contacts.stream().map(contact -> {
            ContactDto dto = new ContactDto();
            dto.id = contact.getBareJid().toString(); //TODO: deprecated
            dto.nickname = contact.getDisplayableName();
            dto.isOnline = contact.getStatus().isOnline();
            dto.hasSarosSupport = contact.hasSarosSupport();

            return dto;
        }).toArray(size -> new ContactDto[size]);

        return CompletableFuture.completedFuture(new SarosResultResponse<ContactDto[]>(dtos));
    }

    @Override
    public void update(Optional<XMPPContact> contact, UpdateType updateType) {
        
        LOG.info("6/7");
        if(contact.isPresent()) {

            LOG.info("7/7");
            XMPPContact con = contact.get();
            ContactDto dto = new ContactDto();
            dto.id = con.getBareJid().toString(); //TODO: deprecated
            dto.hasSarosSupport = con.hasSarosSupport();
            dto.isOnline = con.getStatus().isOnline();
            dto.nickname = con.getNickname().orElse("");

            this.client.sendStateContact(dto);
        }
    }

    @Override
    public CompletableFuture<Void> test(Test t) {
        
        CompletableFuture<Void> c = new CompletableFuture<Void>();

        Executors.newCachedThreadPool().submit(() -> {

            String token = t.workDoneToken; //UUID.randomUUID().toString();

            this.progressMonitor.subTask("Sub");
            this.progressMonitor.beginTask("Test", 10);
            
            TimeUnit.SECONDS.sleep(3);
            this.progressMonitor.worked(2);
            
            TimeUnit.SECONDS.sleep(3);
            this.progressMonitor.worked(4);
            
            TimeUnit.SECONDS.sleep(3);
            this.progressMonitor.worked(6);
            
            TimeUnit.SECONDS.sleep(3);
            this.progressMonitor.worked(8);
            
            TimeUnit.SECONDS.sleep(3);
            this.progressMonitor.worked(10);
            this.progressMonitor.done();
            
            c.complete(null);
            return null;
        });

        return c;        
    }
}