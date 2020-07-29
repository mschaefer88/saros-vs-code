package saros.lsp.editor.adapter;

import org.eclipse.lsp4j.Position;

public class EditorString {

  private StringBuffer buffer;

  public EditorString(String text) {
    this.buffer = new StringBuffer(text);
  }

  private String[] getLines() {
    return this.buffer.toString().split("(?<=\n)");
  }

  public Position getPosition(int offset) {
    String[] lines = this.getLines();
    Position position = new Position(0, 0);

    for (String line : lines) {
      offset -= line.length();

      if (offset >= 0) {
        position.setLine(position.getLine() + 1);
      } else {
        position.setCharacter(offset + line.length());
        break;
      }
    }

    return position;
  }

  public int getOffset(Position position) {
    String[] lines = this.getLines();
    int offset = 0;

    for (int l = 0; l < lines.length && l < position.getLine(); l++) {

      String line = lines[l];

      offset += line.length();
    }

    return offset + position.getCharacter();
  }

  public String substring(int offset, int length) {
    return this.buffer.toString().substring(offset, offset + length);
  }

  public String substring(Position position, int length) {
    return this.substring(this.getOffset(position), length);
  }

  public int getLength(int line) {

    String[] lines = this.getLines();
    if (line < 0 || line >= lines.length) {
      throw new IllegalArgumentException("line cannot be negative or be larger than content");
    }

    return lines[line].length();
  }
}
