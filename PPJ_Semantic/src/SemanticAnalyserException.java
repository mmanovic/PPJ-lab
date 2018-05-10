
public class SemanticAnalyserException extends Exception {
	private static final long serialVersionUID = 1L;
	private Node node = null;
	private String message = null;

	public SemanticAnalyserException(String message) {
		this.message = message;
	}

	public SemanticAnalyserException(Node node) {
		super("Semantic error");
		this.node = node;
	}

	public String getMessage() {
		if (node != null) {
			return node.getFullProduction();
		} else {
			return message;
		}
	}

	public String getProduction() {
		return node.getFullProduction();
	}
}
