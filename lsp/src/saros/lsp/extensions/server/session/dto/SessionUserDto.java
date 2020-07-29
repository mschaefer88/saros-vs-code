package saros.lsp.extensions.server.session.dto;

public class SessionUserDto {
  public String id;

  public String nickname;

  public SessionUserDto(String id, String nickname) {
    this.id = id;
    this.nickname = nickname;
  }
}
