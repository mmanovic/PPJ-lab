import java.util.HashMap;
import java.util.Map;

public class Scope {

	private Map<String, SymbolInfo> symbolTable = new HashMap<>();
	private Scope parent;

	public Scope(Scope parent) {
		this.parent = parent;
	}

	public Map<String, SymbolInfo> getSymbolTable() {
		return symbolTable;
	}

	public SymbolInfo addVariable(String symbolName) {
		SymbolInfo symbolInfo = new SymbolInfo();

		symbolTable.put(symbolName, symbolInfo);

		return symbolInfo;
	}

	public void addVariable(String name, SymbolInfo symbolInfo) {
		symbolTable.put(name, symbolInfo);
	}

	public boolean isDeclared(String name) {
		Scope current = this;

		while (current != null) {
			if (current.symbolExists(name)) {
				return true;
			}
			current = current.getParent();
		}

		return false;
	}

	public boolean isLocalScope() {
		return parent != null;
	}

	public boolean isFunctionDefined(String name) {
		Scope scope = this;

		while (this.parent != null) {
			scope = this.parent;
		}

		SymbolInfo symbolInfo = scope.getSymbolInfo(name);

		if (symbolInfo != null) {
			return symbolInfo.isDefined;
		}

		return false;
	}

	private boolean symbolExists(String name) {
		return symbolTable.containsKey(name);
	}

	public SymbolInfo getSymbolInfo(String name) {
		Scope scope = this;

		while (scope != null) {
			SymbolInfo symbolInfo = scope.getSymbolTable().get(name);
			if (symbolInfo != null) {
				return symbolInfo;
			}
			scope = scope.getParent();
		}

		return null;
	}

	public Scope getParent() {
		return parent;
	}

	public boolean isDeclaredLocally(String name) {
		return symbolExists(name);
	}

}
