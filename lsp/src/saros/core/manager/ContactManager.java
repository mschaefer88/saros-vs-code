package saros.core.manager;

import saros.lsp.extensions.server.contact.dto.ContactDto;

public interface ContactManager { //TODO: for sake of simplicity use XMPP Dtos? -> later interfaces?
    //TODO: if now use Adapter with interfaces eg. Contact (I), XMPPContactAdapter extends XMPPContact implements Contact

    ContactDto add(ContactDto request);
  
    ContactDto remove(ContactDto request);
  
    ContactDto rename(ContactDto request);
  
    ContactDto[] getAll(ContactDto request);
}