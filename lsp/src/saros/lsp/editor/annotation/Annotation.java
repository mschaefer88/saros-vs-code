package saros.lsp.editor.annotation;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import saros.session.User;

public class Annotation {

  private Range range; // TODO: own class?

  private User source;

  private int version;

  public Range getRange() {
    return this.range;
  }

  public User getSource() {
    return this.source;
  }

  public int getVersion() {
    return this.version;
  }

  public Annotation(Range range, User source, int version) {
    this.range = range;
    this.source = source;
    this.version = version;
  }

  public boolean isAfter(Annotation other) {

    Range a = this.getRange();
    Range b = other.getRange();

    return b.getStart().getLine() > a.getEnd().getLine()
        || (b.getStart().getLine() == a.getEnd().getLine()
            && b.getStart().getCharacter() < a.getEnd().getCharacter());
  }

  private static final Logger LOG = Logger.getLogger(Annotation.class);

  public void move(int offset) {
    Position start = this.getRange().getStart();
    Position end = this.getRange().getEnd();

    start.setCharacter(start.getCharacter() + offset);
    end.setCharacter(end.getCharacter() + offset);
  }
}
