package saros.lsp.extensions.server.session.dto;

public class SessionUserDto {
  public String id;

  public String nickname;

  public int annotationColorId;

  public SessionUserDto(String id, String nickname, int annotationColorId) {
    this.id = id;
    this.nickname = nickname;
    this.annotationColorId = annotationColorId;
  }
}
