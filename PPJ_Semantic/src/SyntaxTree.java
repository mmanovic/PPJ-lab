import java.util.List;

public class SyntaxTree {

    private Node root;

    public Node build(List<String> lines) {
        Node node = null;

        for (String line : lines) {
            int nodeDepth = node != null ? countPrefixSpace(node.getLabel()) : -1;
            int currentDepth = countPrefixSpace(line);

            if (node == null) {
                node = new Node(line, null);

            } else if (currentDepth > nodeDepth) {
                Node child = new Node(line, node);
                node.addChild(child);
                node = child;

            } else {
                for (int i = 0; i < nodeDepth - currentDepth; i++) {
                    node = node.getParent();
                }

                Node child = new Node(line, node.getParent());
                node.getParent().addChild(child);
                node = child;
            }
        }

        while (node.getParent() != null) {
            node = node.getParent();
        }

        return this.root = cleanSyntaxTree(node);
    }

    private int countPrefixSpace(String s) {
        int res = 0;

        while (res < s.length() && s.charAt(res) == ' ') {
            res++;
        }

        return res;
    }

    private Node cleanSyntaxTree(Node root) {
        root.setLabel(root.getLabel().trim());

        for (Node node : root.getChildren()) {
            cleanSyntaxTree(node);
        }

        return root;
    }

    public Node getRoot() {
        return root;
    }
}