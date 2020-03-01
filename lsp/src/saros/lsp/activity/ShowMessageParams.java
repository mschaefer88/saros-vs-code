package saros.lsp.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;

public class ShowMessageParams extends ShowMessageRequestParams{

    public ShowMessageParams(MessageType type, String title, String message, String... actions) {

        this.setType(type);
        this.setMessage(String.format("%s%s%s%s", title, System.lineSeparator(), System.lineSeparator(), message));

        if(actions.length > 0) {
            this.setActions(createActionItemList(actions));
        }
    }

    private static List<MessageActionItem> createActionItemList(String... actions) {
        List<MessageActionItem> actionItems = new ArrayList<>();

        for (String action : actions) {
            actionItems.add(new MessageActionItem(action));
        }

        return actionItems;
    }
}