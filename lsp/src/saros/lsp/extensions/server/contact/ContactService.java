package saros.lsp.extensions.server.contact;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiPredicate;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.ui.UIInteractionManager;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

public class ContactService implements IContactService, IContactsUpdate {

  private XMPPContactsService contactsService;
  private ISarosLanguageClient client;
  private UIInteractionManager interactionManager;

  public ContactService(
      XMPPContactsService contactsService,
      ISarosLanguageClient client,
      UIInteractionManager interactionManager) {
    this.contactsService = contactsService;
    this.client = client;
    this.interactionManager = interactionManager;

    this.contactsService.addListener(this);
  }

  @Override
  public CompletableFuture<SarosResponse> add(ContactDto request) {

    CompletableFuture<SarosResponse> c = new CompletableFuture<SarosResponse>();

    Executors.newCachedThreadPool()
        .submit(
            () -> {
              try {
                this.contactsService.addContact(
                    new JID(request.id), request.nickname, this.fromUserInput());

                c.complete(new SarosResponse());
              } catch (Exception e) {
                c.complete(new SarosResponse(e));
              }

              return null;
            });

    return c;
  }

  private BiPredicate<String, String> fromUserInput() {
    return (title, message) -> this.interactionManager.getUserInputYesNo(title, message);
  }

  @Override
  public CompletableFuture<SarosResponse> remove(ContactDto request) {

    try {
      final Optional<XMPPContact> contact = this.contactsService.getContact(request.id);

      if (contact.isPresent()) {
        this.contactsService.removeContact(contact.get());
      }
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> rename(ContactDto request) {

    try {
      final Optional<XMPPContact> contact = this.contactsService.getContact(request.id);
      if (contact.isPresent()) {
        this.contactsService.renameContact(contact.get(), request.nickname);
        this.client.sendStateContact(request);
      }
    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResultResponse<ContactDto[]>> getAll() {

    final List<XMPPContact> contacts = this.contactsService.getAllContacts();

    final ContactDto[] dtos =
        contacts
            .stream()
            .map(
                contact -> {
                  ContactDto dto = new ContactDto();
                  dto.id = contact.getBareJid().toString();
                  dto.nickname = contact.getDisplayableName();
                  dto.isOnline = contact.getStatus().isOnline();
                  dto.hasSarosSupport = contact.hasSarosSupport();

                  return dto;
                })
            .toArray(size -> new ContactDto[size]);

    return CompletableFuture.completedFuture(new SarosResultResponse<ContactDto[]>(dtos));
  }

  @Override
  public void update(Optional<XMPPContact> contact, UpdateType updateType) {
    if (contact.isPresent()) {
      XMPPContact con = contact.get();
      ContactDto dto = new ContactDto();
      dto.id = con.getBareJid().toString();
      dto.hasSarosSupport = con.hasSarosSupport();
      dto.isOnline = con.getStatus().isOnline();
      dto.nickname = con.getDisplayableName();
      dto.subscribed = updateType != UpdateType.REMOVED;

      this.client.sendStateContact(dto);
    }
  }
}
