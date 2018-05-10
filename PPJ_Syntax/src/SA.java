
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class SA {

    private List<String> tokens;
    private List<String[]> LRTable = new ArrayList<>();
    private List<String> syncTokens = new ArrayList<>();

    private SyntaxTree syntaxTree = new SyntaxTree();

    private Stack<SyntaxTree.Node> nodes;
    private Stack<Integer> states;

    public SA(String[] args) {
        input(args);
    }

    private SyntaxTree parse() {
        nodes = new Stack<SyntaxTree.Node>();
        states = new Stack<Integer>();

        // pocetno stanje
        states.push(0);

        for (int i = 0; i < tokens.size() + 1; i++) {
            String token = "#";
            String tokenData = "";

            if (i < tokens.size()) {
                tokenData = tokens.get(i);
                token = tokenData.split(" ")[0];
            }

            int state = states.peek();

            String action = getAction(token, state);

            if (action.equals("Prihvati")) {
                // korijen sintaksnog stabla
                SyntaxTree.Node root = nodes.pop();

                syntaxTree = new SyntaxTree(root);
            } else if (action.startsWith("p")) {
                // pomakni

                // napravi novi cvor (list)
                SyntaxTree.Node tokenNode = new SyntaxTree.Node(tokenData, state);

                nodes.push(tokenNode);
                states.push(Integer.parseInt(action.substring(1)));
            } else if (action.startsWith("<")) {
                // reduciraj

                String[] splitAction = action.split("->", 2);

                String[] productionTokens = splitProduction(splitAction[1]);

                // novi unutarnji cvor (nezavrsni znak)

                SyntaxTree.Node productionNode = new SyntaxTree.Node(splitAction[0], state);
                List<SyntaxTree.Node> nodeList = new ArrayList<SyntaxTree.Node>();

                if (productionTokens.length == 1 && productionTokens[0].equals("$")) {
                    // u slucaju epsilon produkcije nista ne skidamo sa stoga
                    productionNode.addChild(new SyntaxTree.Node("$", state));
                } else {
                    for (int j = 0; j < productionTokens.length; j++) {
                        states.pop();
                        nodeList.add(0, nodes.pop()); // u obrnutom poretku
                    }
                }

                nodeList.forEach(node -> productionNode.addChild(node));

                // nadji akciju stavi u LR tablici
                int row = states.peek() + 1; // +1 jer je prvi red zaglavlje
                int col = LRColumn(splitAction[0]);

                String putAction = LRTable.get(row)[col];

                // pushni nezavrsni znak i stanje iz akcije sBroj
                nodes.push(productionNode);
                states.push(Integer.parseInt(putAction.substring(1)));

                // nismo obradili trenutni ulazni znak
                i--;

            } else {
                // odbaci, nadji sinkronizacijski znak

                String line = tokenData.split(" ")[1];
                System.err.println("Error on line " + line + "!");

                System.err.println("Got '" + tokenData.split(" ")[2] + "', but was expecting some of:");

                findExpectedTokens(state).forEach(System.err::println);

                while (i < tokens.size()) {
                    String nextToken = tokens.get(i).split(" ", 2)[0].trim();
                    if (syncTokens.contains(nextToken)) {
                        // pop stack until you find defined action
                        // i++ in the next for iteration

                        findLastValidToken(states, nodes, nextToken);

                        i--;

                        break;
                    }
                    i++;
                }
            }
        }

        return syntaxTree;
    }

    private List<String> findExpectedTokens(int state) {
        List<String> expectedTokens = new ArrayList<>();

        String[] LRheader = LRTable.get(0);
        String[] LRrow = LRTable.get(state + 1);

        for (int i = 0; i < LRrow.length; i++) {
            if (!LRrow[i].equals("-") && !LRheader[i].startsWith("<")) {
                expectedTokens.add(LRheader[i]);
            }
        }

        return expectedTokens;
    }

    /**
     * Searches through syntax to find last token for which valid action is
     * defined.
     * 
     * @param states
     *            state stack.
     * @param nodes
     *            node stack.
     */
    private void findLastValidToken(Stack<Integer> states, Stack<SyntaxTree.Node> nodes, String syncToken) {
        SyntaxTree.Node node = nodes.peek();
        int state = states.peek();

        while (states.size() > 1 && !checkNode(node, state, syncToken)) {
            nodes.pop();
            states.pop();
            if (!nodes.isEmpty() && !states.isEmpty()) {
                node = nodes.peek();
                state = states.peek();
            }
        }
    }

    private boolean checkNode(SyntaxTree.Node node, int state, String syncToken) {
        String action = getAction(syncToken, state);

        return !action.equals("-");

    }

    private String getAction(String token, int state) {
        String[] LRRow = LRTable.get(state + 1);
        int LRCol = LRColumn(token);

        return LRRow[LRCol];
    }

    private int LRColumn(String token) {
        String[] characters = LRTable.get(0);

        for (int col = 0; col < characters.length; col++) {
            if (characters[col].equals(token.trim())) {
                return col;
            }
        }

        return -1;
    }

    private String[] splitProduction(String production) {
        return production.trim().split(" ");
    }

    private void input(String[] args) {

        // tokens
        try (BufferedReader scanner = new BufferedReader(
                new InputStreamReader(args.length > 0 ? new FileInputStream(args[0]) : System.in))) {

            tokens = scanner.lines().filter(line -> !line.isEmpty()).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // LR(1) table

        try (BufferedReader reader = new BufferedReader(new FileReader("sa.data"))) {
            reader.lines().forEachOrdered(line -> LRTable.add(line.split("\t")));

            int size = LRTable.size();

            for (String syncToken : LRTable.get(size - 1)) {
                syncTokens.add(syncToken);
            }

            LRTable.remove(size - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class SyntaxTree {
        private SyntaxTree.Node root;

        public SyntaxTree() {
        }

        public SyntaxTree(SyntaxTree.Node root) {
            this.root = root;
        }

        public void print() {
            root.print(0);
        }

        public static class Node {
            private String data;
            private int state;
            private List<SyntaxTree.Node> children = new ArrayList<SyntaxTree.Node>();

            public Node(String data, int state) {
                this.data = data.trim();
                this.state = state;
            }

            /**
             * Prints tree preorder.
             * 
             * @param level
             *            current level in the tree.
             */
            public void print(int level) {
                System.out.println(whitespace(level) + data);

                for (SyntaxTree.Node node : children) {
                    node.print(level + 1);
                }
            }

            public void addChild(SyntaxTree.Node child) {
                children.add(child);
            }

            private String whitespace(int length) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < length; i++) {
                    sb.append(' ');
                }

                return sb.toString();
            }
        }
    }

    public static void main(String[] args) {
        SA sa = new SA(args);

        SyntaxTree syntaxTree = sa.parse();

        syntaxTree.print();
    }

}
