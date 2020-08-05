package saros.lsp.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import saros.filesystem.IFile;
import saros.activities.TextEditActivity;
import saros.editor.text.TextPosition;
import saros.lsp.editor.annotation.Annotation;
import saros.session.User;

public class Editor extends TextDocumentItem { // TODO: base class necessary?

  private static final Logger LOG = Logger.getLogger(Editor.class);

  private final Object lock = new Object();

  static final int MAX_HISTORY_LENGTH = 5; // 20;

  public Editor(TextDocumentItem documentItem) {
    this(documentItem.getText(), documentItem.getUri());
    super.setVersion(documentItem.getVersion());
    super.setLanguageId(documentItem.getLanguageId());
  }

  public Editor(String text, String uri) {
    super.setUri(uri);
    super.setText(text);
  }

  public Editor(IFile file) throws IOException {
    super.setUri(
        "file:///" + file.getReferencePointRelativePath().toString()); // TODO: MIGRATION

    try (InputStream stream = file.getContents()) {
      super.setText(IOUtils.toString(stream));
    }
  }

  private String[] getLines() {
    return this.getText().split("(?<=\n)");
  }

  public String getAt(int offset, int length) {
    return this.getText().substring(offset, offset + length);
  }

  public String getAt(Position position, int length) {
    return this.getAt(this.offsetAt(position), length);
  }

  public Position positionAt(int offset) {
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

  public int offsetAt(Position position) {
    String[] lines = this.getLines();
    int offset = 0;

    for (int l = 0; l < lines.length && l < position.getLine(); l++) {

      String line = lines[l];

      offset += line.length();
    }

    return offset + position.getCharacter();
  }

  private int offsetAt(TextPosition textPosition) {
    return this.offsetAt(new Position(textPosition.getLineNumber(), textPosition.getInLineOffset()));
  }

  public TextEditActivity convert(
      TextDocumentContentChangeEvent changeEvent,
      User source,
      IFile path) { // TODO: set path in ctor
    
    Range range = changeEvent.getRange();
    int length = 0;
    if(range != null){
      length = this.getLength(range); //TODO: MIGRATION
    } else {
      length = this.getText().length();
    }
    String text = changeEvent.getText();
    String replacedText = this.getAt(range.getStart(), length); 

     return TextEditActivity.buildTextEditActivity(source, 
     new TextPosition(changeEvent.getRange().getStart().getLine(), changeEvent.getRange().getStart().getCharacter()), 
     text, 
     replacedText, 
     path);
  }

  private int getLength(Range range) {
    return this.offsetAt(range.getEnd()) - this.offsetAt(range.getStart());
  }

  public TextDocumentContentChangeEvent convert(TextEditActivity editActivity) {
    TextPosition startPos = editActivity.getStartPosition();
    TextPosition endPos = editActivity.getNewEndPosition();
    Position start = new Position(startPos.getLineNumber(), startPos.getInLineOffset());
    Position end = new Position(endPos.getLineNumber(), endPos.getInLineOffset());
    Range range = new Range(start, end);
    return new TextDocumentContentChangeEvent(
        range, editActivity.getReplacedText().length(), editActivity.getNewText()); //TODO: MIGRATION
  }

  public void apply(List<TextDocumentContentChangeEvent> changes, int version) {
    synchronized (lock) {
      StringBuilder buffer = new StringBuilder(this.getText());

      for (TextDocumentContentChangeEvent changeEvent : changes) {
        Range range = changeEvent.getRange();
        int length = 0;

        if (range != null) {
          length = this.getLength(range);
        } else {
          length = buffer.length();
          range = new Range(positionAt(0), positionAt(length));
        }
        String text = changeEvent.getText();
        int startOffset = offsetAt(range.getStart());

        buffer.replace(startOffset, startOffset + length, text);
      }

      this.setText(buffer.toString());
      this.setVersion(version);
    }
  }

  private Set<Annotation> annotations = new HashSet<>();

  public void apply(TextEditActivity activity) {
    synchronized (lock) {
      StringBuilder buffer = new StringBuilder(this.getText());
      int startOffset = this.offsetAt(activity.getStartPosition());
      int length = activity.getReplacedText().length();
      String text = activity.getNewText();

      buffer.replace(startOffset, startOffset + length, text);

      this.setText(buffer.toString());

      this.annotate(activity);
    }
  }

  private void annotate(TextEditActivity activity) {

    Range range =
        new Range(
            new Position(activity.getStartPosition().getLineNumber(), activity.getStartPosition().getInLineOffset()), //TODO: converter
            new Position(activity.getNewEndPosition().getLineNumber(), activity.getNewEndPosition().getInLineOffset())); //TODO: converter
    Annotation recent = new Annotation(range, activity.getSource(), this.getVersion());
    Annotation[] after =
        this.annotations
            .stream()
            .filter(a -> a.isAfter(recent))
            .toArray(size -> new Annotation[size]);
    for (Annotation annotation : after) {
      annotation.move(1);
    }
    this.annotations.add(recent);

    Map<User, List<Annotation>> history =
        this.annotations.stream().collect(Collectors.groupingBy(Annotation::getSource));

    for (Entry<User, List<Annotation>> group : history.entrySet()) {
      List<Annotation> annotationsForUser = group.getValue();
      if (annotationsForUser.size() > MAX_HISTORY_LENGTH) {

        int removeCount = group.getValue().size() - MAX_HISTORY_LENGTH;
        annotationsForUser.sort(
            new Comparator<Annotation>() {

              @Override
              public int compare(Annotation o1, Annotation o2) {
                return o1.getVersion() - o2.getVersion();
              }
            });
        for (int i = 0; i < removeCount; i++) {
          this.annotations.remove(annotationsForUser.get(i));
        }
      }
    }
  }

  public Annotation[] getAnnotations() {
    return this.annotations.toArray(new Annotation[0]);
  }

  public VersionedTextDocumentIdentifier toVersionedIdentifier() {
    return new VersionedTextDocumentIdentifier(this.getUri(), this.getVersion());
  }

  public void save() {}
}
