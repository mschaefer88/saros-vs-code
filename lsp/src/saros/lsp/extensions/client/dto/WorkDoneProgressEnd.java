package saros.lsp.extensions.client.dto;

public class WorkDoneProgressEnd {
    String kind = "end";

	/**
	 * Optional, a final message indicating to for example indicate the outcome
	 * of the operation.
	 */
    String message;
    
    public WorkDoneProgressEnd(String message) {
        this.message = message;
    }
}