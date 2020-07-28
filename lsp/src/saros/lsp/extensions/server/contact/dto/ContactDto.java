package saros.lsp.extensions.server.contact.dto;

public class ContactDto {
    public String id;

    public String nickname;

    public boolean isOnline;
    
    public boolean hasSarosSupport;

    public boolean subscribed;

    public ContactDto() {
        this.subscribed = true;
    }
}