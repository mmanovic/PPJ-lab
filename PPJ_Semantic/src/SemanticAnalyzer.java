import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Mato Manovic
 * @version 1.0
 */
public class SemanticAnalyzer {

	private static final int MAX_ARRAY_SIZE = 1024;
	private static final char[] escapableChars = { 't', 'n', '0', '\'', '"', '\\' };

	private SyntaxTree syntaxTree;
	private Scope scope;

	private List<Scope> scopes = new ArrayList<>();

	/**
	 * Nasljedno svojstvo se stavlja na stog prije poziva provjere, a skida
	 * nakon provjere.
	 */
	private Stack<DataType> inheritableType = new Stack<>();
	private int numParams;
	private boolean isFunction;

	public SemanticAnalyzer(SyntaxTree syntaxTree) {
		this.syntaxTree = syntaxTree;
	}

	public void analyze() {
		this.scope = new Scope(null); // global context, no parent
		scopes.add(scope);

		try {
			check(syntaxTree.getRoot());

			postAnalysis();
		} catch (SemanticAnalyserException e) {
			System.out.println(e.getMessage());
		}
	}

	private void postAnalysis() throws SemanticAnalyserException {
		// je li main deklariran?

		scope = getGlobalScope();

		if (!scope.isFunctionDefined("main-0")) {
			throw new SemanticAnalyserException("main");
		}

		List<DataType> mainSignature = scope.getSymbolInfo("main-0").dataType;

		if (mainSignature.size() != 2 || !mainSignature.get(0).equals(DataType.INT)
				|| !mainSignature.get(1).equals(DataType.VOID)) {
			throw new SemanticAnalyserException("main");
		}

		// jesu li sve deklarirane funkcije definirane?

		// System.out.println(scopes.size());
		for (Scope sc : scopes) {
			if (sc.getSymbolTable().entrySet().stream().anyMatch(
					entry -> entry.getValue().symbolType.equals(SymbolType.FUNCTION) && !entry.getValue().isDefined)) {
				throw new SemanticAnalyserException("funkcija");
			}
		}
	}

	private boolean check(Node node) throws SemanticAnalyserException {

		if (!node.isLeaf()) {
			completeActions(node);
		}

		return true;
	}

	/**
	 * Returns true if new context got assigned to the given Node.
	 */
	private boolean checkScope(Node root) { // TODO: ovo treba nadopuniti
		String rootLabel = root.getLabel();

		if (rootLabel.equals("<slozena_naredba>") || rootLabel.equals("<vanjska_deklaracija>")
				|| rootLabel.equals("<definicija_funkcije>")) {
			this.scope = new Scope(this.scope); // change current context to a
			scopes.add(scope); // new one
			return true;
		}

		return false;
	}

	private int countArguments(Node node) {
		String production = node.getProduction();
		int res = 0;

		for (Node child : node.getChildren()) {
			if (child.getLabel().equals("<izraz_pridruzivanja>")) {
				res++;
			}

			res += countArguments(child);
		}

		return res;
	}

	private void completeActions(Node node) throws SemanticAnalyserException {
		String nodeName = node.getLabel();

		switch (nodeName) {
		case "<primarni_izraz>": {
			primarni_izraz(node);
			break;
		}
		case "<postfiks_izraz>": {
			postfiks_izraz(node);
			break;
		}
		case "<lista_argumenata>": {
			lista_argumenata(node);
			break;
		}
		case "<unarni_izraz>": {
			unarni_izraz(node);
			break;
		}
		case "<unarni_operator>": {
			unarni_operator(node);
			break;
		}
		case "<cast_izraz>": {
			cast_izraz(node);
			break;
		}
		case "<ime_tipa>": {
			ime_tipa(node);
			break;
		}
		case "<specifikator_tipa>": {
			specifikator_tipa(node);
			break;
		}
		case "<multiplikativni_izraz>": {
			multiplikativni_izraz(node);
			break;
		}
		case "<aditivni_izraz>": {
			aditivni_izraz(node);
			break;
		}
		case "<odnosni_izraz>": {
			odnosni_izraz(node);
			break;
		}
		case "<jednakosni_izraz>": {
			jednakosni_izraz(node);
			break;
		}
		case "<bin_i_izraz>": {
			bin_i_izraz(node);
			break;
		}
		case "<bin_xili_izraz>": {
			bin_xili_izraz(node);
			break;
		}
		case "<bin_ili_izraz>": {
			bin_ili_izraz(node);
			break;
		}
		case "<log_i_izraz>": {
			log_i_izraz(node);
			break;
		}
		case "<log_ili_izraz>": {
			log_ili_izraz(node);
			break;
		}
		case "<izraz_pridruzivanja>": {
			izraz_pridruzivanja(node);
			break;
		}
		case "<izraz>": {
			izraz(node);
			break;
		}
		case "<slozena_naredba>": {
			slozena_naredba(node);
			break;
		}
		case "<lista_naredbi>": {
			lista_naredbi(node);
			break;
		}
		case "<naredba>": {
			naredba(node);
			break;
		}
		case "<izraz_naredba>": {
			izraz_naredba(node);
			break;
		}
		case "<naredba_grananja>": {
			naredba_grananja(node);
			break;
		}
		case "<naredba_petlje>": {
			naredba_petlje(node);
			break;
		}
		case "<naredba_skoka>": {
			naredba_skoka(node);
			break;
		}
		case "<prijevodna_jedinica>": {
			prijevodna_jedinica(node);
			break;
		}
		case "<vanjska_deklaracija>": {
			vanjska_deklaracija(node);
			break;
		}
		case "<definicija_funkcije>": {
			definicija_funkcije(node);
			break;
		}
		case "<lista_parametara>": {
			lista_parametara(node);
			break;
		}
		case "<deklaracija_parametra>": {
			deklaracija_parametra(node);
			break;
		}
		case "<lista_deklaracija>": {
			lista_deklaracija(node);
			break;
		}
		case "<deklaracija>": {
			deklaracija(node);
			break;
		}
		case "<lista_init_deklaratora>": {
			lista_init_deklaratora(node);
			break;
		}
		case "<init_deklarator>": {
			init_deklarator(node);
			break;
		}
		case "<izravni_deklarator>": {
			izravni_deklarator(node);
			break;
		}
		case "<inicijalizator>": {
			inicijalizator(node);
			break;
		}
		case "<lista_izraza_pridruzivanja>": {
			lista_izraza_pridruzivanja(node);
			break;
		}

		default:
			break;
		}
	}

	private void lista_deklaracija(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<lista_deklaracija> ::= <deklaracija>")) {
			check(context.firstChild);
		} else if (context.isProduction("<lista_deklaracija> ::= <lista_deklaracija> <deklaracija>")) {
			check(context.firstChild);
			check(node.getChild(1));
		}
	}

	private void deklaracija_parametra(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<deklaracija_parametra> ::= <ime_tipa> IDN")) {
			check(context.firstChild);

			if (context.firstChild.getSymbolInfo().getType().equals(DataType.VOID)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.argumentNames.add(node.getChild(1).getTokenName());
		} else if (context.isProduction("<deklaracija_parametra> ::= <ime_tipa> IDN L_UGL_ZAGRADA D_UGL_ZAGRADA")) {
			check(context.firstChild);

			if (context.firstChild.getSymbolInfo().getType().equals(DataType.VOID)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(context.firstChild.getSymbolInfo().getType().toArray());
			context.symbolInfo.argumentNames.add(node.getChild(1).getTokenName());
		}
	}

	private void deklaracija(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<deklaracija> ::= <ime_tipa> <lista_init_deklaratora> TOCKAZAREZ")) {
			check(context.firstChild);

			// uz nasljedno svojstvo <lista_init_deklaratora>.ntip ←
			// <ime_tipa>.tip

			inheritableType.push(context.firstChild.getSymbolInfo().getType());
			check(node.getChild(1));
			inheritableType.pop();

		}
	}

	private void lista_init_deklaratora(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<lista_init_deklaratora> ::= <init_deklarator>")) {
			// nasljedno svojstvo na stogu
			check(context.firstChild);
		} else if (context
				.isProduction("<lista_init_deklaratora> ::= <lista_init_deklaratora> ZAREZ <init_deklarator>")) {
			// nasljedno svojstvo na stogu
			check(context.firstChild);

			check(node.getChild(2));
		}
	}

	private void init_deklarator(Node node) throws SemanticAnalyserException {
		// <init_deklarator> ::= <izravni_deklarator>
		// | <izravni_deklarator> OP_PRIDRUZI <inicijalizator>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<init_deklarator> ::= <izravni_deklarator>")) {

			// nasljedno svojstvo na stogu
			check(context.firstChild);

			DataType dataType = context.firstChild.getSymbolInfo().getType();

			if (dataType.isConst()) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<init_deklarator> ::= <izravni_deklarator> OP_PRIDRUZI <inicijalizator>")) {

			check(context.firstChild);

			check(node.getChild(2));

			// if (node.getChild(2).getSymbolInfo().dataType.size() != 1) {
			// throw new SemanticAnalyserException(node);
			// }

			DataType dataType = context.firstChild.getSymbolInfo().getType();

			if (dataType.isPlain() || (dataType.isConst() && !dataType.isArray())) {
				if (!node.getChild(2).getSymbolInfo().getType().implicit(dataType)) {
					throw new SemanticAnalyserException(node);
				}
			} else if (dataType.isArray()) {
				if (node.getChild(2).getSymbolInfo().elemCount > node.getChild(0).getSymbolInfo().elemCount) {
					throw new SemanticAnalyserException(node);
				}

				dataType = dataType.removeArray();

				List<DataType> initializerTypes = node.getChild(2).getSymbolInfo().dataType;

				for (DataType initDataType : initializerTypes) {
					if (!initDataType.implicit(dataType)) {
						throw new SemanticAnalyserException(node);
					}
				}
			} else {
				// iz uputa
				throw new SemanticAnalyserException(node);
			}
		}
	}

	private void izravni_deklarator(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		String name = context.firstChild.getTokenName();
		// Scope globalScope = getGlobalScope();

		context.symbolInfo.l_expr = true;

		if (context.isProduction("<izravni_deklarator> ::= IDN")) {

			// name = context.firstChild.getTokenName();

			if (inheritableType.peek().equals(DataType.VOID) || isDeclaredLocally(name)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.symbolType = SymbolType.VARIABLE;
			context.symbolInfo.argumentNames.add(name);
			context.symbolInfo.dataType.add(inheritableType.peek());
			context.symbolInfo.l_expr = !inheritableType.peek().isConst();

			scope.addVariable(name, context.symbolInfo);
		} else if (context.isProduction("<izravni_deklarator> ::= IDN L_UGL_ZAGRADA BROJ D_UGL_ZAGRADA")) {

			if (inheritableType.peek().equals(DataType.VOID) || isDeclaredLocally(name)) {
				throw new SemanticAnalyserException(node);
			}

			String number = node.getChild(2).getTokenName();

			try {
				int elemCount = Integer.parseInt(number);

				if (elemCount <= 0 || elemCount > MAX_ARRAY_SIZE) {
					throw new SemanticAnalyserException(node);
				}

				context.symbolInfo.symbolType = SymbolType.ARRAY;
				context.symbolInfo.dataType.add(inheritableType.peek().toArray());
				context.symbolInfo.elemCount = elemCount;
				context.symbolInfo.l_expr = !inheritableType.peek().isConst();

				scope.addVariable(name, context.symbolInfo);
			} catch (NumberFormatException e) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<izravni_deklarator> ::= IDN L_ZAGRADA KR_VOID D_ZAGRADA")) {
			/*
			 * Moze postojati vise funkcija istog imena, ali sve moraju imati
			 * razlicit broj parametara. Npr. mozemo imati na pocetku bloka
			 * deklariranu int f(void), a zatim int f(int x).
			 * 
			 */

			if (scope.isDeclaredLocally(name)) {
				throw new SemanticAnalyserException(node);
			}

			// zero arguments
			name += "-0";

			if (scope.isDeclaredLocally(name)) {
				SymbolInfo symbolInfo = scope.getSymbolInfo(name);

				if (symbolInfo.dataType.size() != 2 || !symbolInfo.getType().equals(inheritableType.peek())
						|| !symbolInfo.dataType.get(1).equals(DataType.VOID)) {
					throw new SemanticAnalyserException(node);
				}
			} else {
				context.symbolInfo.symbolType = SymbolType.FUNCTION;
				context.symbolInfo.dataType.add(inheritableType.peek());
				context.symbolInfo.dataType.add(DataType.VOID);

				scope.addVariable(name, context.symbolInfo);
				// declareGlobally(name, context.symbolInfo);
			}
		} else if (context.isProduction("<izravni_deklarator> ::= IDN L_ZAGRADA <lista_parametara> D_ZAGRADA")) {
			check(node.getChild(2));

			if (scope.isDeclaredLocally(name)) {
				throw new SemanticAnalyserException(node);
			}

			// n arguments
			name += "-" + node.getChild(2).getSymbolInfo().dataType.size();

			if (scope.isDeclaredLocally(name)) {
				SymbolInfo symbolInfo = scope.getSymbolInfo(name);
				List<DataType> declared = symbolInfo.dataType;
				List<DataType> newlyDeclared = node.getChild(2).getSymbolInfo().dataType;

				if (!symbolInfo.symbolType.equals(SymbolType.FUNCTION)) {
					/*
					 * trebalo bi biti moguce imati varijablu istog naziva i
					 * deklaraciju fje istog naziva u istom bloku..
					 */
					throw new SemanticAnalyserException(node);
				}

				/*
				 * dodaj provjeru tipova argumenata, f(int,int), f(char,char) su
				 * 2 razl stvari
				 */

				if (newlyDeclared.size() + 1 != declared.size()) { // ako
																	// postoji
																	// f(int), a
																	// mi
																	// dodajemo
																	// npr
																	// f(int,int)
					context.symbolInfo.symbolType = SymbolType.FUNCTION;
					context.symbolInfo.dataType.add(inheritableType.peek());
					context.symbolInfo.dataType.addAll(node.getChild(2).getSymbolInfo().dataType);
					scope.addVariable(name, context.symbolInfo);

				} else {
					context.symbolInfo.dataType.add(inheritableType.peek());
					context.symbolInfo.dataType.addAll(newlyDeclared);

					for (int i = 0; i < declared.size(); i++) {
						if (!declared.get(i).equals(context.symbolInfo.dataType.get(i))) {
							throw new SemanticAnalyserException(node);
						}
					}
				}
			} else {
				context.symbolInfo.symbolType = SymbolType.FUNCTION;
				context.symbolInfo.dataType.add(inheritableType.peek());
				context.symbolInfo.dataType.addAll(node.getChild(2).getSymbolInfo().dataType);

				scope.addVariable(name, context.symbolInfo);
				// declareGlobally(name, context.symbolInfo);
			}
		}
	}

	private void inicijalizator(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<inicijalizator> ::= <izraz_pridruzivanja>")) {
			check(context.firstChild);

			Node charArrayNode = goesToCharArray(node);

			if (charArrayNode != null) {
				// there can be spaces in string?
				String charArray = charArrayNode.getLabel().split(" ", 3)[2];

				// don't count "", but count \0
				context.symbolInfo.elemCount = charArray.length() - 2 + 1;

				for (int i = 0; i < context.symbolInfo.elemCount; i++) {
					context.symbolInfo.dataType.add(DataType.CHAR);
				}
			} else {
				context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
				context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
			}
		} else if (context
				.isProduction("<inicijalizator> ::= L_VIT_ZAGRADA <lista_izraza_pridruzivanja> D_VIT_ZAGRADA")) {
			check(node.getChild(1));

			context.symbolInfo.elemCount = node.getChild(1).getSymbolInfo().elemCount;
			context.symbolInfo.dataType.addAll(node.getChild(1).getSymbolInfo().dataType);
		}

	}

	private void lista_izraza_pridruzivanja(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<lista_izraza_pridruzivanja> ::= <izraz_pridruzivanja>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.elemCount = 1;
		} else if (context.isProduction(
				"<lista_izraza_pridruzivanja> ::= <lista_izraza_pridruzivanja> ZAREZ <izraz_pridruzivanja>")) {
			check(context.firstChild);
			check(node.getChild(2));

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.dataType.add(node.getChild(2).getSymbolInfo().getType());

			context.symbolInfo.elemCount = context.firstChild.getSymbolInfo().elemCount + 1;
		}
	}

	private void lista_parametara(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<lista_parametara> ::= <deklaracija_parametra>")) {
			check(context.firstChild);

			context.symbolInfo.argumentNames.addAll(context.firstChild.getSymbolInfo().argumentNames);
			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
		} else if (context.isProduction("<lista_parametara> ::= <lista_parametara> ZAREZ <deklaracija_parametra>")) {
			check(context.firstChild);
			check(node.getChild(2));

			SymbolInfo paramListSymbolInfo = context.firstChild.getSymbolInfo();
			SymbolInfo declParamSymbolInfo = node.getChild(2).getSymbolInfo();

			if (paramListSymbolInfo.argumentNames.contains(declParamSymbolInfo.argumentNames.get(0))) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(paramListSymbolInfo.dataType);
			// only one data type here
			context.symbolInfo.dataType.addAll(declParamSymbolInfo.dataType);

			context.symbolInfo.argumentNames.addAll(paramListSymbolInfo.argumentNames);
			// only one name here
			context.symbolInfo.argumentNames.addAll(declParamSymbolInfo.argumentNames);
		}
	}

	private void definicija_funkcije(Node node) throws SemanticAnalyserException {
		// <definicija_funkcije> ::= <ime_tipa> IDN L_ZAGRADA KR_VOID D_ZAGRADA
		// <slozena_naredba>
		// | <ime_tipa> IDN L_ZAGRADA <lista_parametara> D_ZAGRADA
		// <slozena_naredba>
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction(
				"<definicija_funkcije> ::= <ime_tipa> IDN L_ZAGRADA KR_VOID D_ZAGRADA <slozena_naredba>")) {
			check(context.firstChild);
			
			if (context.firstChild.getSymbolInfo().dataType.get(0).isConst()) {
				throw new SemanticAnalyserException(node);
			}

			// void nema argumenata
			String functionName = node.getChild(1).getTokenName();

			if (scope.isDeclared(functionName)) {
				throw new SemanticAnalyserException(node);
			}

			functionName += "-0";

			if (scope.isFunctionDefined(functionName)) {
				throw new SemanticAnalyserException(node);
			}

			if (scope.isDeclared(functionName)) {
				List<DataType> functionSignature = scope.getSymbolInfo(functionName).dataType;

				if (functionSignature.size() != 2
						|| !functionSignature.get(0).equals(context.firstChild.getSymbolInfo().getType())
						|| !functionSignature.get(1).equals(DataType.VOID)) {
					throw new SemanticAnalyserException(node);
				}

				scope.getSymbolInfo(functionName).isDefined = true;
				
				context.symbolInfo = scope.getSymbolInfo(functionName);
			} else {
				SymbolInfo symbolInfo = scope.addVariable(functionName);
				symbolInfo.symbolType = SymbolType.FUNCTION;
				symbolInfo.isDefined = true;
				symbolInfo.dataType.add(node.getChild(0).getSymbolInfo().getType());
				symbolInfo.dataType.add(DataType.VOID);
				
				context.symbolInfo.dataType.addAll(symbolInfo.dataType);

				scope = new Scope(scope);
				scopes.add(scope);
				
				check(node.getChild(5));

				scope = scope.getParent();
			}
		} else if (context.isProduction(
				"<definicija_funkcije> ::= <ime_tipa> IDN L_ZAGRADA <lista_parametara> D_ZAGRADA <slozena_naredba>")) {
			check(context.firstChild);

			String functionName = node.getChild(1).getTokenName();

			// ako je povratni tip s const-modifikatorom ili je vec deklarirana
			// varijabla s tim imenom
			if (context.firstChild.getSymbolInfo().getType().isConst() || scope.isDeclared(functionName)) {
				throw new SemanticAnalyserException(node);
			}

			check(node.getChild(3));

			functionName += "-" + node.getChild(3).getSymbolInfo().dataType.size();

			if (scope.isFunctionDefined(functionName)) {
				throw new SemanticAnalyserException(node);
			}

			SymbolInfo symbolInfo;

			if (scope.isLocalScope()) {
				throw new SemanticAnalyserException(node);
			}

			if (scope.isDeclared(functionName)) {
				symbolInfo = scope.getSymbolInfo(functionName);
			} else {
				symbolInfo = scope.addVariable(functionName);
			}

			symbolInfo.symbolType = SymbolType.FUNCTION;
			symbolInfo.dataType.add(node.getChild(0).getSymbolInfo().getType());
			symbolInfo.dataType.addAll(node.getChild(3).getSymbolInfo().dataType);

			symbolInfo.isDefined = true;
			
			context.symbolInfo.dataType.addAll(symbolInfo.dataType);

			scope = new Scope(scope);
			scopes.add(scope);
			// dodaj parametre funkcije u novostvoreni scope

			List<DataType> types = node.getChild(3).getSymbolInfo().dataType;
			List<String> arguments = node.getChild(3).getSymbolInfo().argumentNames;

			for (int i = 0; i < arguments.size(); i++) {
				symbolInfo = scope.addVariable(arguments.get(i));
				symbolInfo.dataType.add(types.get(i));
				symbolInfo.symbolType = SymbolType.VARIABLE;
			}

			check(node.getChild(5));

			scope = scope.getParent();
		}
	}

	/**
	 * Nezavrsni znak <naredba_skoka> generira continue, break i return naredbe.
	 */
	private void naredba_skoka(Node node) throws SemanticAnalyserException {
		// <naredba_skoka> ::= KR_CONTINUE TOCKAZAREZ
		// | KR_BREAK TOCKAZAREZ
		// | KR_RETURN TOCKAZAREZ
		// | KR_RETURN <izraz> TOCKAZAREZ

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<naredba_skoka> ::= KR_CONTINUE TOCKAZAREZ")
				|| context.isProduction("<naredba_skoka> ::= KR_BREAK TOCKAZAREZ")) {
			if (!isInsideLoop(node)) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<naredba_skoka> ::= KR_RETURN TOCKAZAREZ")) {
			List<DataType> functionSignature = getFunctionSignature(node);

			if (functionSignature.size() == 0 || !functionSignature.get(0).equals(DataType.VOID)) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<naredba_skoka> ::= KR_RETURN <izraz> TOCKAZAREZ")) {
			check(node.getChild(1));

			List<DataType> functionSignature = getFunctionSignature(node);

			if ((node.getChild(1).getSymbolInfo().dataType.size() != 1 && !isFunctionCall(node.getChild(1)))
					|| functionSignature.size() == 0
					|| !node.getChild(1).getSymbolInfo().getType().implicit(functionSignature.get(0))) {
				throw new SemanticAnalyserException(node);
			}
		}
	}

	private void naredba_petlje(Node node) throws SemanticAnalyserException {
		// <naredba_petlje> ::= KR_WHILE L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		// | KR_FOR L_ZAGRADA <izraz_naredba> <izraz_naredba> D_ZAGRADA
		// <naredba>
		// | KR_FOR L_ZAGRADA <izraz_naredba> <izraz_naredba> <izraz> D_ZAGRADA
		// <naredba>
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<naredba_petlje> ::= KR_WHILE L_ZAGRADA <izraz> D_ZAGRADA <naredba>")) {
			check(node.getChild(2));
			if (!node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}
			check(node.getChild(4));
		} else if (context.isProduction(
				"<naredba_petlje> ::= KR_FOR L_ZAGRADA <izraz_naredba> <izraz_naredba> D_ZAGRADA <naredba>")) {
			check(node.getChild(2));
			check(node.getChild(3));
			if (!node.getChild(3).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}
			check(node.getChild(5));
		} else {

			check(node.getChild(2));
			check(node.getChild(3));

			if (!node.getChild(3).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			if (node.getChild(3).getSymbolInfo().dataType.size() != 1) { // ako
				// je
				// funkcija
				if (node.getChild(3).getSymbolInfo().symbolType != SymbolType.FUNCTION) { // a
					// nije
					// dobro
					// deklarirana
					throw new SemanticAnalyserException(node);
				}
			}

			check(node.getChild(4));

			check(node.getChild(6));
		}

	}

	private void vanjska_deklaracija(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<vanjska_deklaracija> ::= <definicija_funkcije>")) {
			check(context.firstChild);
		} else if (context.isProduction("<vanjska_deklaracija> ::= <deklaracija>")) {
			check(context.firstChild);
		}
	}

	private void prijevodna_jedinica(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<prijevodna_jedinica> ::= <vanjska_deklaracija>")) {
			check(context.firstChild);
		} else if (context.isProduction("<prijevodna_jedinica> ::= <prijevodna_jedinica> <vanjska_deklaracija>")) {
			check(context.firstChild);
			check(node.getChild(1));
		}
	}

	private void naredba_grananja(Node node) throws SemanticAnalyserException {
		// <naredba_grananja> ::= KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		// | KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba> KR_ELSE <naredba>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<naredba_grananja> ::= KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba>")) {
			check(node.getChild(2));

			if (node.getChild(2).getSymbolInfo().dataType.size() != 1
					|| !node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			check(node.getChild(4));
		} else {
			check(node.getChild(2));

			if (!node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			check(node.getChild(4));

			check(node.getChild(6));
		}

	}

	private void izraz_naredba(Node node) throws SemanticAnalyserException {

		// <izraz_naredba> ::= TOCKAZAREZ
		// | <izraz> TOCKAZAREZ

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<izraz_naredba> ::= TOCKAZAREZ")) {
			context.symbolInfo.dataType.add(DataType.INT);
		} else if (context.isProduction("<izraz_naredba> ::= <izraz> TOCKAZAREZ")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
		}
	}

	private void naredba(Node node) throws SemanticAnalyserException {
		// <naredba> ::= <slozena_naredba>
		// | <izraz_naredba>
		// | <naredba_grananja>
		// | <naredba_petlje>
		// | <naredba_skoka>

		InternalNodeContext context = new InternalNodeContext(node);

		// sve jedinicne produkcije, samo se provjeravaju

		check(context.firstChild);
	}

	private void lista_naredbi(Node node) throws SemanticAnalyserException {
		// <lista_naredbi> ::= <naredba>
		// | <lista_naredbi> <naredba>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<lista_naredbi> ::= <naredba>")) {
			if (!check(context.firstChild)) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<lista_naredbi> ::= <lista_naredbi> <naredba>")) {
			if (!check(context.firstChild) || !check(node.getChild(1))) {
				throw new SemanticAnalyserException(node);
			}
		}
	}

	/**
	 * Nezavrsni znak <slozena_naredba> predstavlja blok naredbi koji opcionalno
	 * pocinje lis- tom deklaracija. Svaki blok je odvojeni djelokrug, a
	 * nelokalnim imenima se pristupa u ugnijezdujucem bloku (i potencijalno
	 * tako dalje sve do globalnog djelokruga)
	 */
	private void slozena_naredba(Node node) throws SemanticAnalyserException {
		// <slozena_naredba> ::= L_VIT_ZAGRADA <lista_naredbi> D_VIT_ZAGRADA
		// | L_VIT_ZAGRADA <lista_deklaracija> <lista_naredbi> D_VIT_ZAGRADA

		InternalNodeContext context = new InternalNodeContext(node);

		// stvori novi scope

		if (context.isProduction("<slozena_naredba> ::= L_VIT_ZAGRADA <lista_naredbi> D_VIT_ZAGRADA")) {

			if (scope.getParent().isLocalScope()) {
				scope = new Scope(scope);
				scopes.add(scope);
			}

			check(node.getChild(1));

			if (scope.getParent().isLocalScope()) {
				scope = scope.getParent();
			}
		} else if (context.isProduction(
				"<slozena_naredba> ::= L_VIT_ZAGRADA <lista_deklaracija> <lista_naredbi> D_VIT_ZAGRADA")) {

			if (scope.getParent().isLocalScope()) {
				scope = new Scope(scope);
				scopes.add(scope);
			}

			check(node.getChild(1));
			check(node.getChild(2));

			if (scope.getParent().isLocalScope()) {
				scope = scope.getParent();
			}
		}
	}

	private void log_ili_izraz(Node node) throws SemanticAnalyserException {
		// <log_ili_izraz> ::= <log_i_izraz>
		// | <log_ili_izraz> OP_ILI <log_i_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<log_ili_izraz> ::= <log_i_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<log_ili_izraz> ::= <log_ili_izraz> OP_ILI <log_i_izraz>")) {
			check(context.firstChild);

			if (!context.firstChild.getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			check(node.getChild(2));

			if (!node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void log_i_izraz(Node node) throws SemanticAnalyserException {
		// <log_i_izraz> ::= <bin_ili_izraz>
		// | <log_i_izraz> OP_I <bin_ili_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<log_i_izraz> ::= <bin_ili_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<log_i_izraz> ::= <log_i_izraz> OP_I <bin_ili_izraz>")) {
			check(context.firstChild);

			if (!context.firstChild.getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			check(node.getChild(2));

			if (!node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void bin_ili_izraz(Node node) throws SemanticAnalyserException {
		// <bin_ili_izraz> ::= <bin_xili_izraz>
		// | <bin_ili_izraz> OP_BIN_ILI <bin_xili_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<bin_ili_izraz> ::= <bin_xili_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<bin_ili_izraz> ::= <bin_ili_izraz> OP_BIN_ILI <bin_xili_izraz>")) {
			if (!check(context.firstChild) || !context.firstChild.getSymbolInfo().getType().implicit(DataType.INT)
					|| !check(node.getChild(2)) || !node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void bin_xili_izraz(Node node) throws SemanticAnalyserException {
		// <bin_xili_izraz> ::= <bin_i_izraz>
		// | <bin_xili_izraz> OP_BIN_XILI <bin_i_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<bin_xili_izraz> ::= <bin_i_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<bin_xili_izraz> ::= <bin_xili_izraz> OP_BIN_XILI <bin_i_izraz>")) {
			if (!check(context.firstChild) || !context.firstChild.getSymbolInfo().getType().implicit(DataType.INT)
					|| !check(node.getChild(2)) || !node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void bin_i_izraz(Node node) throws SemanticAnalyserException {
		// <bin_i_izraz> ::= <jednakosni_izraz>
		// | <bin_i_izraz> OP_BIN_I <jednakosni_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<bin_i_izraz> ::= <jednakosni_izraz>")) {
			if (!check(context.firstChild)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<bin_i_izraz> ::= <bin_i_izraz> OP_BIN_I <jednakosni_izraz>")) {
			check(context.firstChild);

			if (!context.firstChild.getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			check(node.getChild(2));

			if (!node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void izraz_pridruzivanja(Node node) throws SemanticAnalyserException {
		// <izraz_pridruzivanja> ::= <log_ili_izraz>
		// | <postfiks_izraz> OP_PRIDRUZI <izraz_pridruzivanja>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<izraz_pridruzivanja> ::= <log_ili_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context
				.isProduction("<izraz_pridruzivanja> ::= <postfiks_izraz> OP_PRIDRUZI <izraz_pridruzivanja>")) {
			check(context.firstChild);

			if (!context.firstChild.getSymbolInfo().l_expr) {
				throw new SemanticAnalyserException(node);
			}
			check(node.getChild(2));

			if (!node.getChild(2).getSymbolInfo().getType().implicit(context.firstChild.getSymbolInfo().getType())) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = false;
		}
	}

	private void jednakosni_izraz(Node node) throws SemanticAnalyserException {
		// <jednakosni_izraz> ::= <odnosni_izraz>
		// | <jednakosni_izraz> OP_EQ <odnosni_izraz>
		// | <jednakosni_izraz> OP_NEQ <odnosni_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<jednakosni_izraz> ::= <odnosni_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else {
			check(context.firstChild);

			if (context.firstChild.getSymbolInfo().dataType.size() != 1
					|| !context.firstChild.getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			check(node.getChild(2));

			if (node.getChild(2).getSymbolInfo().dataType.size() != 1
					|| !node.getChild(2).getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void odnosni_izraz(Node node) throws SemanticAnalyserException {
		// <odnosni_izraz> ::= <aditivni_izraz>
		// | <odnosni_izraz> OP_LT <aditivni_izraz>
		// | <odnosni_izraz> OP_GT <aditivni_izraz>
		// | <odnosni_izraz> OP_LTE <aditivni_izraz>
		// | <odnosni_izraz> OP_GTE <aditivni_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<odnosni_izraz> ::= <aditivni_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else {
			check(context.firstChild);

			if (!context.firstChild.getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}
			check(node.getChild(2));

			if (!node.getChild(2).getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void multiplikativni_izraz(Node node) throws SemanticAnalyserException {
		// <multiplikativni_izraz> ::= <cast_izraz>
		// | <multiplikativni_izraz> OP_PUTA <cast_izraz>
		// | <multiplikativni_izraz> OP_DIJELI <cast_izraz>
		// | <multiplikativni_izraz> OP_MOD <cast_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<multiplikativni_izraz> ::= <cast_izraz>")) {
			if (!check(context.firstChild)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<multiplikativni_izraz> ::= <multiplikativni_izraz> OP_PUTA <cast_izraz>")
				|| context.isProduction("<multiplikativni_izraz> ::= <multiplikativni_izraz> OP_DIJELI <cast_izraz>")
				|| context.isProduction("<multiplikativni_izraz> ::= <multiplikativni_izraz> OP_MOD <cast_izraz>")) {
			if (!check(context.firstChild) || !context.firstChild.getSymbolInfo().dataType.get(0).implicit(DataType.INT)
					|| !check(node.getChild(2))
					|| !node.getChild(2).getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void specifikator_tipa(Node node) {
		InternalNodeContext context = new InternalNodeContext(node);
		if (context.isProduction("<specifikator_tipa> ::= KR_VOID")) {
			context.symbolInfo.dataType.add(DataType.VOID);
		} else if (context.isProduction("<specifikator_tipa> ::= KR_CHAR")) {
			context.symbolInfo.dataType.add(DataType.CHAR);
		} else if (context.isProduction("<specifikator_tipa> ::= KR_INT")) {
			context.symbolInfo.dataType.add(DataType.INT);
		}

	}

	/**
	 * Nezavrsni znak <ime_tipa> generira imena opcionalno const-kvalificiranih
	 * brojevnih ti- pova i kljucnu rijec void. U ovim produkcijama ce se
	 * izracunati izvedeno svojstvo tip koje se koristi u produkcijama gdje se
	 * <ime_tipa> pojavljuje s desne strane i dodatno ce se onemoguciti tip
	 * const void (koji je sintaksno ispravan, ali nema smisla).
	 *
	 * @throws SemanticAnalyserException
	 */
	private void ime_tipa(Node node) throws SemanticAnalyserException {
		// <ime_tipa> ::= <specifikator_tipa>
		// | KR_CONST <specifikator_tipa>
		InternalNodeContext context = new InternalNodeContext(node);
		if (context.isProduction("<ime_tipa> ::= <specifikator_tipa>")) {
			check(context.firstChild);
			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
		} else {
			check(node.getChild(1));
			if (node.getChild(1).getSymbolInfo().dataType.contains(DataType.VOID)) {
				throw new SemanticAnalyserException(node);
			}
			context.symbolInfo.l_expr = false;

			SymbolInfo symbolInfo = node.getChild(1).getSymbolInfo();
			if (symbolInfo.dataType.contains(DataType.INT)) {
				context.symbolInfo.dataType.add(DataType.CONST_INT);
			} else if (symbolInfo.dataType.contains(DataType.CHAR)) {
				context.symbolInfo.dataType.add(DataType.CONST_CHAR);
			} else if (symbolInfo.dataType.contains(DataType.INT_ARRAY)) {
				context.symbolInfo.dataType.add(DataType.CONST_INT_ARRAY);
			} else if (symbolInfo.dataType.contains(DataType.CHAR_ARRAY)) {
				context.symbolInfo.dataType.add(DataType.CONST_CHAR_ARRAY);
			}
		}

	}

	/**
	 * Nezavrsni znak <cast_izraz> generira izraze s opcionalnim cast
	 * operatorom.
	 *
	 * @throws SemanticAnalyserException
	 */
	private void cast_izraz(Node node) throws SemanticAnalyserException {
		// <cast_izraz> ::= <unarni_izraz>
		// | L_ZAGRADA <ime_tipa> D_ZAGRADA <cast_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<cast_izraz> ::= <unarni_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<cast_izraz> ::= L_ZAGRADA <ime_tipa> D_ZAGRADA <cast_izraz>")) {

			check(node.getChild(1));

			check(node.getChild(3));

			if (node.getChild(3).getSymbolInfo().dataType.size() != 1 || !node.getChild(3).getSymbolInfo().dataType
					.get(0).explicit(node.getChild(1).getSymbolInfo().dataType.get(0))) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(node.getChild(1).getSymbolInfo().getType());
			context.symbolInfo.l_expr = false;
		}
	}

	/**
	 * Nezavrsni znak <unarni_operator> generira aritmeticke (PLUS i MINUS),
	 * bitovne (OP_TILDA) i logicke (OP_NEG) prefiks unarne operatore. Kako u
	 * ovim produkcijama u semantickoj analizi ne treba nista provjeriti,
	 * produkcije ovdje nisu navedene.
	 */
	private void unarni_operator(Node node) {
		// <unarni_operator> ::= PLUS
		// | MINUS
		// | OP_TILDA
		// | OP_NEG

		// nista za provjeru, jeeej :D
	}

	/**
	 * Nezavrsni znak <unarni_izraz> generira izraze s opcionalnim prefiks
	 * unarnim operatorima.
	 *
	 * @throws SemanticAnalyserException
	 */
	private void unarni_izraz(Node node) throws SemanticAnalyserException {
		// <unarni_izraz> ::= <postfiks_izraz>
		// | OP_INC <unarni_izraz>
		// | OP_DEC <unarni_izraz>
		// | <unarni_operator> <cast_izraz>
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<unarni_izraz> ::= <postfiks_izraz>")) {
			if (!check(context.firstChild)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<unarni_izraz> ::= OP_INC <unarni_izraz>")
				|| context.isProduction("<unarni_izraz> ::= OP_DEC <unarni_izraz>")) {
			Node child = node.getChild(1);
			if (!check(child) || !child.getSymbolInfo().l_expr
					|| !child.getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		} else if (context.isProduction("<unarni_izraz> ::= <unarni_operator> <cast_izraz>")) {
			if (!check(node.getChild(1)) || !node.getChild(1).getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	/**
	 * Nezavrsni znak <lista_argumenata> generira listu argumenata za poziv
	 * funkcije, a za razliku od nezavrsnih znakova koji generiraju izraze, imat
	 * ce svojsto tipovi koje predstavlja listu tipova argumenata, s lijeva na
	 * desno.
	 *
	 * @throws SemanticAnalyserException
	 */
	private void lista_argumenata(Node node) throws SemanticAnalyserException {
		// <lista_argumenata> ::= <izraz_pridruzivanja>
		// | <lista_argumenata> ZAREZ <izraz_pridruzivanja>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<lista_argumenata> ::= <izraz_pridruzivanja>")) {
			if (!check(context.firstChild)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
		} else if (context.isProduction("<lista_argumenata> ::= <lista_argumenata> ZAREZ <izraz_pridruzivanja>")) {
			if (!check(context.firstChild) || !check(node.getChild(2))) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.dataType.add(node.getChildren().get(2).getSymbolInfo().getType());
		}

	}

	/**
	 * Nezavrsni znak <postfiks_izraz> generira neki primarni izraz s
	 * opcionalnim postfiks- operatorima.
	 *
	 * @throws SemanticAnalyserException
	 */
	private void postfiks_izraz(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<postfiks_izraz> ::= <primarni_izraz>")) {
			if (check(context.firstChild)) {
				context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
				context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
				context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
			} else {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<postfiks_izraz> ::= <postfiks_izraz> L_UGL_ZAGRADA <izraz> D_UGL_ZAGRADA")) {
			Node child = context.firstChild;
			check(child);
			if (!child.getSymbolInfo().getType().isArray()) {
				throw new SemanticAnalyserException(node);
			}
			Node child2 = node.getChild(2);
			check(child2);
			if (context.firstChild.getSymbolInfo().getType().implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(child.getSymbolInfo().getType().removeArray());
			context.symbolInfo.l_expr = !child.getSymbolInfo().getType().isConst();

		} else if (context.isProduction("<postfiks_izraz> ::= <postfiks_izraz> L_ZAGRADA D_ZAGRADA")) {
			Node child = context.firstChild;

			numParams = 0;
			isFunction = true;

			check(child);

			isFunction = false;

			if (child.getSymbolInfo().dataType.size() != 2
					|| !child.getSymbolInfo().dataType.get(1).equals(DataType.VOID)) {
				throw new SemanticAnalyserException(node);
			}
			context.symbolInfo.validFunctionCall = true;
			context.symbolInfo.dataType.add(child.getSymbolInfo().dataType.get(0));
			context.symbolInfo.l_expr = false;

		} else if (context
				.isProduction("<postfiks_izraz> ::= <postfiks_izraz> L_ZAGRADA <lista_argumenata> D_ZAGRADA")) {

			numParams = countArguments(node.getChild(2));
			isFunction = true;

			check(context.firstChild);

			isFunction = false;

			check(node.getChild(2));

			// ako nije u pitanju funkcija
			if (context.firstChild.getSymbolInfo().dataType.size() < 2) {
				throw new SemanticAnalyserException(node);
			}

			Node child = node.getChild(2);
			int argNum = child.getSymbolInfo().dataType.size();

			// ako nije u pitanju funkcija
			if (context.firstChild.getSymbolInfo().dataType.size() - 1 != argNum) {
				throw new SemanticAnalyserException(node);
			}

			List<DataType> paramTypes = context.firstChild.getSymbolInfo().dataTypeTail();
			List<DataType> argTypes = child.getSymbolInfo().dataType;

			for (int i = 0; i < argNum; i++) {
				if (!argTypes.get(i).implicit(paramTypes.get(i))) {
					throw new SemanticAnalyserException(node);
				}
			}

			context.symbolInfo.validFunctionCall = true;
			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = false;
		} else if (context.isProduction("<postfiks_izraz> ::= <postfiks_izraz> OP_INC")
				|| context.isProduction("<postfiks_izraz> ::= <postfiks_izraz> OP_DEC")) {
			if (!check(context.firstChild) || !context.firstChild.getSymbolInfo().l_expr
					|| !context.firstChild.getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	/**
	 * Nezavrsni znak <primarni_izraz> generira najjednostavnije izraze koji se
	 * sastoje od jednog identifikatora, neke vrste konstante ili izraza u
	 * zagradi.
	 *
	 * @throws SemanticAnalyserException
	 */
	private void primarni_izraz(Node node) throws SemanticAnalyserException {
		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<primarni_izraz> ::= IDN")) {
			String name = context.firstChild.getTokenName();

			if (isFunction) {
				name += "-" + numParams;
			}

			if (scope.isDeclared(name)) {
				context.symbolInfo.dataType.addAll(scope.getSymbolInfo(name).dataType);
				context.symbolInfo.l_expr = scope.getSymbolInfo(name).l_expr;
				context.symbolInfo.symbolType = scope.getSymbolInfo(name).symbolType;
				context.symbolInfo.name = context.firstChild.getTokenName();
			} else {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<primarni_izraz> ::= BROJ")) {
			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;

			if (!validIntRange(context.firstChild.getTokenName())) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<primarni_izraz> ::= ZNAK")) {
			context.symbolInfo.dataType.add(DataType.CHAR);
			context.symbolInfo.l_expr = false;

			if (!validChar(context.firstChild.getTokenName())) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<primarni_izraz> ::= NIZ_ZNAKOVA")) {
			context.symbolInfo.dataType.add(DataType.CONST_CHAR_ARRAY);
			context.symbolInfo.l_expr = false;

			if (!validCharArray(context.firstChild.getTokenName())) {
				throw new SemanticAnalyserException(node);
			}
		} else if (context.isProduction("<primarni_izraz> ::= L_ZAGRADA <izraz> D_ZAGRADA")) {
			Node child = node.getChild(1);

			check(child);
			context.symbolInfo.dataType.add(child.getSymbolInfo().getType());
			context.symbolInfo.l_expr = child.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = child.getSymbolInfo().symbolType;

		}
	}

	private void aditivni_izraz(Node node) throws SemanticAnalyserException {
		// <aditivni_izraz> ::= <multiplikativni_izraz>
		// | <aditivni_izraz> PLUS <multiplikativni_izraz>
		// | <aditivni_izraz> MINUS <multiplikativni_izraz>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<aditivni_izraz> ::= <multiplikativni_izraz>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
			context.symbolInfo.symbolType = context.firstChild.getSymbolInfo().symbolType;
		} else if (context.isProduction("<aditivni_izraz> ::= <aditivni_izraz> PLUS <multiplikativni_izraz>")
				|| context.isProduction("<aditivni_izraz> ::= <aditivni_izraz> MINUS <multiplikativni_izraz>")) {
			check(context.firstChild);

			if (!context.firstChild.getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}
			check(node.getChild(2));

			if (!node.getChild(2).getSymbolInfo().dataType.get(0).implicit(DataType.INT)) {
				throw new SemanticAnalyserException(node);
			}

			context.symbolInfo.dataType.add(DataType.INT);
			context.symbolInfo.l_expr = false;
		}
	}

	private void izraz(Node node) throws SemanticAnalyserException {
		// <izraz> ::= <izraz_pridruzivanja>
		// | <izraz> ZAREZ <izraz_pridruzivanja>

		InternalNodeContext context = new InternalNodeContext(node);

		if (context.isProduction("<izraz> ::= <izraz_pridruzivanja>")) {
			check(context.firstChild);

			context.symbolInfo.dataType.addAll(context.firstChild.getSymbolInfo().dataType);
			context.symbolInfo.l_expr = context.firstChild.getSymbolInfo().l_expr;
		} else if (context.isProduction("<izraz> ::= <izraz> ZAREZ <izraz_pridruzivanja>")) {
			check(context.firstChild);

			check(node.getChild(2));

			context.symbolInfo.dataType.add(node.getChild(2).getSymbolInfo().getType());
			context.symbolInfo.l_expr = false;
		}
	}

	private boolean isInsideFunction(Node node) {
		while (node.getParent() != null) {
			node = node.getParent();

			if (node.getLabel().equals("<definicija_funkcije>")) {
				return true;
			}
		}

		return false;
	}

	private boolean isFunctionCall(Node node) {
		Node current = node;
		while (current != null) {
			if (current.getLabel().equals("<postfiks_izraz>")) {
				return current.getSymbolInfo().validFunctionCall;
			}

			if (current.getChildren() != null && current.getChildren().size() > 0) {
				current = current.getChild(0);
			} else {
				return false;
			}
		}
		return false;
	}

	private List<DataType> getFunctionSignature(Node node) {
		List<DataType> signature = new ArrayList<>();
		while (node.getParent() != null) {
			node = node.getParent();

			if (node.getLabel().equals("<definicija_funkcije>")) {
				signature.addAll(node.getSymbolInfo().dataType);
				break;
			}
		}

		return signature;
	}

	private boolean isInsideLoop(Node node) {
		while (node.getParent() != null) {
			node = node.getParent();

			if (node.getLabel().equals("<naredba_petlje>")) {
				return true;
			}
		}

		return false;
	}

	private static final String searchString = "NIZ_ZNAKOVA";

	private Node goesToCharArray(Node node) {
		if (node.getLabel().contains(searchString)) {
			return node;
		}

		Node res = null;

		for (Node child : node.getChildren()) {
			res = goesToCharArray(child);

			if (res != null) {
				return res;
			}
		}

		return res;
	}

	private void declareGlobally(String name, SymbolInfo symbolInfo) {
		Scope global = scope;

		while (global.getParent() != null) {
			global = global.getParent();
		}

		global.addVariable(name, symbolInfo);
	}

	private Scope getGlobalScope() {
		Scope global = scope;

		while (global.isLocalScope()) {
			global = global.getParent();
		}

		return global;
	}

	private boolean isDeclaredLocally(String name) {
		return scope.isDeclared(name);
	}

	private static boolean implicitInt(DataType dataType) {
		return dataType == DataType.CHAR || dataType == DataType.INT;
	}

	private static boolean validIntRange(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean arrayContains(char[] array, char c) {
		boolean contains = false;

		for (int i = 0; i < array.length && !contains; i++) {
			if (array[i] == c)
				contains = true;
		}

		return contains;
	}

	private boolean validChar(String value) {
		int len = value.length();

		if (len < 3 || len > 4)
			return false;

		if (value.length() == 4) {
			if (value.charAt(1) != '\\')
				return false;

			return arrayContains(escapableChars, value.charAt(2));
		}

		return (value.length() == 3 && value.charAt(1) != '\\' && value.charAt(1) != '\'');
	}

	public boolean isEscaped(String s, int index) {
		int count = 0;

		index--;

		while (index >= 0 && s.charAt(index) == '\\') {
			count++;
			index--;
		}

		return count % 2 != 0;
	}

	boolean shouldBeEscaped(char c) {
		boolean valid = false;
		for (int j = 0; j < escapableChars.length && !valid; j++) {
			if (c == escapableChars[j]) {
				valid = true;
			}
		}

		return valid;
	}

	private boolean validCharArray(String value) {
		for (int i = 1; i < value.length() - 1; i++) {
			if (value.charAt(i) == '"') {
				// not escaped
				return false;
			}

			// count # of \\
			int len = 0;
			while (value.charAt(i) == '\\') {
				i++;
				len++;
			}

			if (len == 0)
				continue;

			if (i == value.length() - 1 && len % 2 != 0) {
				// escaped end of string
				return false;
			} else if (i < value.length() - 1) {
				if (len % 2 != 0 && !arrayContains(escapableChars, value.charAt(i))) {
					// we escaped something that should not be escaped
					return false;
				}
			}
		}

		return true;
	}
}
