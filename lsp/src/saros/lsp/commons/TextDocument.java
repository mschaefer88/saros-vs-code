package saros.lsp.commons;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.session.User;

public class TextDocument extends TextDocumentItem {//TODO: use generic for SPath or IFile

    public final static String[] DELIMITERS = { "\r", "\n", "\r\n" };

    private static final Logger LOG = Logger.getLogger(TextDocument.class);

    private final Object lock = new Object();

    public TextDocument(TextDocumentItem documentItem) {
        this(documentItem.getText(), documentItem.getUri());
		super.setVersion(documentItem.getVersion());
		super.setLanguageId(documentItem.getLanguageId());
    }

	public TextDocument(String text, String uri) {
		super.setUri(uri);
		super.setText(text);
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
        Position position = new Position(1, 0);

        for (String line : lines) {
            offset -= line.length();
            
            if(offset >= 0) {
                position.setLine(position.getLine()+1);
            } else {
                position.setCharacter(offset+line.length());
                break;
            }
        }

        return position;
	}

	public int offsetAt(Position position) {
        assert position.getLine() > 0;

        String[] lines = this.getLines();
        int offset = 0;

        for (int l = 0; l < lines.length && l+1 < position.getLine(); l++) {

            String line = lines[l];

            offset += line.length();
        }

        return offset + position.getCharacter();
	}

    public List<TextEditActivity> apply(List<TextDocumentContentChangeEvent> changes, User user, SPath path) {//TODO: set path in ctor
        synchronized (lock) {
            StringBuilder buffer = new StringBuilder(this.getText());
            List<TextEditActivity> activities = Collections.emptyList();

            for (TextDocumentContentChangeEvent changeEvent : changes) {
                Range range = changeEvent.getRange();
                int length = 0;

                if (range != null) {
                    length = changeEvent.getRangeLength().intValue();
                } else {
                    length = buffer.length();
                    range = new Range(positionAt(0), positionAt(length));
                }
                String text = changeEvent.getText();
                int startOffset = offsetAt(range.getStart());

                if(user != null) { //TODO: do better!
                    activities.add(new TextEditActivity(user, startOffset, text, this.getAt(startOffset, length), path));
                }
                buffer.replace(startOffset, startOffset + length, text);
            }
                
            setText(buffer.toString());

            return activities;
        }
    }
}