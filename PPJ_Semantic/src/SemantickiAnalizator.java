import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SemantickiAnalizator {

	public static void main(String[] args) {

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader((args.length > 0 ? new FileInputStream(args[0]) : System.in)))) {

			SyntaxTree syntaxTree = new SyntaxTree();
			syntaxTree.build(br.lines().collect(Collectors.toList()));

			// syntaxTree.getRoot().print(0);

			SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(syntaxTree);
			semanticAnalyzer.analyze();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
