package saros.lsp.annotation;

import org.eclipse.lsp4j.Range;

import saros.activities.TextEditActivity;
import saros.lsp.adapter.EditorString;
import saros.session.User;

public class Annotation {
    
    private Range range; //TODO: own class?

    private User source;

    public Range getRange() {
        return this.range;
    }

    public User getSource() {
        return this.source;
    }

    public Annotation(Range range, User source) {
        this.range = range;
        this.source = source;
    }

    public boolean isPredecessor(Annotation other, EditorString content) {
        if(!this.getSource().equals(other.getSource())) {
            return false;
        }

        return this.isPredecessor(this.range, other.range, content);
    }

    private boolean isPredecessor(Range a, Range b, EditorString content) {        
        return (a.getEnd().getLine() == b.getStart().getLine() && a.getEnd().getCharacter() == b.getStart().getCharacter())
            || (a.getEnd().getLine() == b.getStart().getLine() - 1 && b.getStart().getCharacter() == 0 && a.getEnd().getCharacter() == content.getLength(b.getStart().getLine())); //TODO: get length of line
    }

    private boolean isIntercepting(Annotation other) {

        return false;
    }

    public void merge(Annotation successor) {//TODO: check? at least warning!
        this.range = new Range(this.range.getStart(), successor.range.getEnd());
    }
}