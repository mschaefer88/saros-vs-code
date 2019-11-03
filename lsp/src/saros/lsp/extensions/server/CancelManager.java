package saros.lsp.extensions.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CancelManager {

    private List<CancelObserver> observerList = new LinkedList<CancelObserver>();

    private Map<String, CompletableFuture> map = new HashMap<>();

    public void add(CancelObserver observer) {
        this.observerList.add(observer);
    }

    public void register(CompletableFuture future, String token) {
        this.map.put(token, future);
        future.thenAccept(result -> {
            this.map.remove(token);
        });
    }

    public void cancel(String token) {
        for (CancelObserver observer : observerList) {
            if(observer.tryCancel(token)) {
                break;
            }            
        }

        CompletableFuture cf = this.map.get(token);
        if(cf != null) {
            cf.cancel(true);
        }
    }
}