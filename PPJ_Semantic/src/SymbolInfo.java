import java.util.ArrayList;
import java.util.List;

public class SymbolInfo {
	SymbolType symbolType = SymbolType.UNDEFINED;
	/**
	 * In case of a function, first element is return type, other elements are
	 * argument types. In case of variable, just one element is needed -- type
	 * of variable.
	 */
	List<DataType> dataType = new ArrayList<>();
	boolean l_expr;
	boolean isDefined;
    boolean validFunctionCall;

	int elemCount;

	List<String> argumentNames = new ArrayList<>();
	
	String name;

	public SymbolInfo() {
	}

	public SymbolInfo(SymbolType symbolType, DataType dataType, boolean l_expr) {
		this.symbolType = symbolType;
		this.l_expr = l_expr;

		this.dataType.add(dataType);
	}

	public SymbolInfo(SymbolType symbolType, List<DataType> dataType, boolean l_expr) {
		this.symbolType = symbolType;
		this.l_expr = l_expr;

		this.dataType.addAll(dataType);
	}

	/**
	 * Returns first data type in data type list. Should be used only with
	 * variables!
	 * 
	 * @return First element from dataType list.
	 */
	public DataType getType() {
		if (dataType.size() == 0) {
			throw new IllegalArgumentException("This symbol has no type yet or is a function!");
		}

		return dataType.get(0);
	}

	public List<DataType> dataTypeTail() {
		List<DataType> tail = new ArrayList<>();
		
		for (int i = 1; i < dataType.size(); i++) {
			tail.add(dataType.get(i));
		}
		
		return tail;
	}

}
