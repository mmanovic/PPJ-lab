import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class GSA {
    /**
     * types of characters
     */
    private static List<String> nonTerminal = new ArrayList<>();
    private static List<String> terminal = new ArrayList<>();
    private static List<String> synchronization = new ArrayList<>();
    /**
     * productions of grammar, for one nonTerminal character as key(left side of
     * production) is associated with several groups of characters(right side of
     * production) separated by character '|'.
     */
    private static Map<String, String> productions = new LinkedHashMap<>();
    private static List<String> allProductions = new ArrayList<>();
    private static List<String> emptyNonTerminal = new ArrayList<>();
    /**
     * Za kljuc je ovdje stavljeno LR stavka dok je vrijednost pridruzen nekakav
     * skup znakova za koje ne razumijem cemu sluze to ima veze s nekim zivim
     * prefiksima i cim sve ne.
     */
    private static List<String> LRItems = new ArrayList<>();

    private static Map<String, String> transitions = new HashMap<>();

    private static Map<String, String> dkaTransitions = new LinkedHashMap<>();

    /**
     * Each state of dka with his LRitems.
     */
    private static List<TreeSet<String>> dkaState = new ArrayList<>();

    /**
     * zapocinje skup za svaki znak
     */
    private static Map<String, List<String>> startGroup = new HashMap<>();

    /**
     * mapa LR stavki sa pripadajucom listom znakova koji pripadaju pojedinoj
     * stavci
     */
    private static Map<String, List<String>> LRItemsWithStart = new HashMap<>();

    private static List<String> processed = new ArrayList<>();

    public static void main(String[] args) {
        input(args);
//		for (Entry<String, String> production : productions.entrySet()) {
//			System.out.println(production);
//		}

        computeEmptyNonTerminal();
//		System.out.println("prazni nezavrsni znakovi");
//        for (String entry : emptyNonTerminal) {
//            System.out.println(entry);
//        }
        computeStart();
        generateLRitems();
        generateENKA();
//		System.out.println();
//		for (Entry<String, String> entry : transitions.entrySet()) {
//			System.out.println(entry.getKey() + " => " + entry.getValue());
//		}
        // System.out.println(processed.size());
        transformENKaToDKA();
//		System.out.println();
        createLRtable();
        // System.out.println(dkaState.size());

        // System.out.println(processed);

    }

    private static void createLRtable() {

        TreeSet<String> allTerminal = new TreeSet<>();
        allTerminal.addAll(terminal);
        allTerminal.add("#");
        TreeSet<String> allNonTerminal = new TreeSet<>();
        allNonTerminal.addAll(nonTerminal);
        allNonTerminal.add("<%>");
        List<String> allChars = new ArrayList<>();
        allChars.addAll(allTerminal);
        allChars.addAll(allNonTerminal);
        int n = dkaState.size();
        int m = allChars.size();
        String[][] matrix = new String[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = "-";
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sa.data"))) {

            StringBuilder builder = new StringBuilder();
            for (String entry : allChars) {
                builder.append(entry + "\t");
            }

            writer.write(builder.toString());
            writer.newLine();

            for (int i = 0; i < n; i++) {
                // find complete LRitems
                int min = Integer.MAX_VALUE;
                TreeSet<String> zapocinje = new TreeSet<>();
                String[] zapocinje2 = null;
                for (String item : dkaState.get(i)) {
                    int tmpMin;
                    String[] tmp = item.split(",");
                    if (tmp[0].trim().endsWith("-> *")) {
                        tmp[0] = tmp[0].substring(0, tmp[0].length() - 1);
                        tmp[0] += "$";
                        tmpMin = allProductions.indexOf(tmp[0]);
                        zapocinje2 = tmp[1].trim().substring(2, tmp[1].length() - 2).trim().split(" ");
                        if (tmpMin < min) {
                            min = tmpMin;
                            zapocinje.clear();
                            for (String entry : zapocinje2) {
                                zapocinje.add(entry);
                            }
                        } else if (tmpMin == min) {
                            for (String entry : zapocinje2) {
                                zapocinje.add(entry);
                            }
                        }
                    } else if (tmp[0].trim().endsWith("*")) {
                        tmpMin = allProductions.indexOf(tmp[0].substring(0, tmp[0].length() - 2));
                        zapocinje2 = tmp[1].trim().substring(2, tmp[1].length() - 2).trim().split(" ");
                        if (tmpMin < min) {
                            min = tmpMin;
                            zapocinje.clear();
                            for (String entry : zapocinje2) {
                                zapocinje.add(entry);

                            }
                        } else if (tmpMin == min) {
                            for (String entry : zapocinje2) {
                                zapocinje.add(entry);
                            }
                        }
                    }

                }
                if (min != Integer.MAX_VALUE) {
                    String tmp = allProductions.get(min);
                    if (tmp.endsWith("$")) {
                        tmp = tmp.substring(0, tmp.length() - 2);
                    }
                    tmp += " *";
                    for (String sign : zapocinje) {
                        matrix[i][allChars.indexOf(sign)] = allProductions.get(min);
                        if (min == 0) {
                            matrix[i][allChars.indexOf(sign)] = "Prihvati";
                        }
                    }

                }

                for (String entry : allChars) {
                    String newState = dkaTransitions.get(i + "," + entry);
                    if (newState == null) {
                        // matrix[i][allChars.indexOf(entry)]="-";
                    } else {
                        if (allTerminal.contains(entry)) {
                            matrix[i][allChars.indexOf(entry)] = "p" + newState;
                        } else {
                            matrix[i][allChars.indexOf(entry)] = "s" + newState;
                        }
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    writer.write(matrix[i][j] + "\t");
                }
                writer.newLine();
            }

            for (String s : synchronization) {
                writer.write(s + "\t");
            }

            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void transformENKaToDKA() {
        TreeSet<String> allChars = new TreeSet<>();
        allChars.addAll(terminal);
        allChars.addAll(nonTerminal);
        String firstLRItem = LRItems.get(0);
        LRItem item = new LRItem(firstLRItem);
        item.start.add("#");
        TreeSet<String> poc = new TreeSet<>();
        poc.add(item.toString());
        int povratak = 1;
        while (true) {
            poc = epsilonOkolina(poc);
            if (poc.size() == povratak) {
                break;
            }
            povratak = poc.size();
        }

        dkaState.add(poc);
        int i = 0;
        while (i < dkaState.size()) {
            TreeSet<String> tmpState = dkaState.get(i);
            TreeSet<String> tmp;
            for (String entry : allChars) {
                tmp = napraviPrijelaz(tmpState, entry);
                if (tmp.size() != 0) {
                    povratak = tmp.size();
                    while (true) {
                        tmp = epsilonOkolina(tmp);
                        if (tmp.size() == povratak) {
                            break;
                        }
                        povratak = tmp.size();
                    }
                    if (!dkaState.contains(tmp)) {
                        dkaState.add(tmp);
                    }
                    dkaTransitions.put(i + "," + entry, dkaState.indexOf(tmp) + "");
                }
            }

            i++;
        }
    }

    /**
     * Copy-pejstano iz labosa UTR-a
     */
    private static TreeSet<String> napraviPrijelaz(TreeSet<String> poc, String znak) {
        TreeSet<String> povratni = new TreeSet<>();
        for (String entry : poc) {
            String prijelaz = entry.concat(":" + znak);

            String novaStanja = transitions.get(prijelaz);

            if (novaStanja != null) {
                String[] fieldNova = novaStanja.split(";");
                povratni.addAll(Arrays.asList(fieldNova));
            }
        }
        return povratni;
    }

    /**
     * copy pejstano iz labosa UTR-a
     */
    private static TreeSet<String> epsilonOkolina(TreeSet<String> pocetno) {
        TreeSet<String> okolina = new TreeSet<>();
        for (String entry : pocetno) {
            okolina.add(entry);
            String prijelaz = entry.concat(":$");
            String novaStanja = transitions.get(prijelaz);

            if (novaStanja != null) {
                String[] fieldNova = novaStanja.split(";");
                okolina.addAll(Arrays.asList(fieldNova));
            }
        }
        return okolina;
    }

    private static void generateENKA() {
        String firstLRItem = LRItems.get(0);
        LRItem item = new LRItem(firstLRItem);
        item.start.add("#");
        List<LRItem> list = new LinkedList<>();
        list.add(item);
        LRItemsWithStart.put(item.item, item.start);

        while (list.size() != 0) {
            LRItem LRitem = list.remove(0);
            if (processed.contains(LRitem.toString())) {
                continue;
            }
            String[] tmp = LRitem.item.split("\\*");
            if (tmp.length == 1) {
                processed.add(LRitem.toString());
                continue;
            }

            String[] tmp1 = tmp[1].trim().split(" ", 2);
            String newItem = tmp[0].trim() + " " + tmp1[0] + " *";
            if (tmp1.length == 2) {
                newItem += " " + tmp1[1].trim();
            }
            LRItem newLRItem = new LRItem(newItem);
            newLRItem.start.addAll(LRitem.start);
            list.add(newLRItem);
            LRItemsWithStart.put(newLRItem.item, newLRItem.start);
            transitions.put(LRitem.toString() + ":" + tmp1[0], newLRItem.toString());

            if (nonTerminal.contains(tmp1[0])) {
                String start = tmp1[0] + " -> *";
                for (String entry : LRItems) {
                    if (entry.startsWith(start)) {
                        newLRItem = new LRItem(entry);
                        if (tmp1.length == 1) {
                            newLRItem.start.addAll(LRitem.start);
                        } else {
                            String[] tmp2 = tmp1[1].trim().split(" ");
                            newLRItem.start.addAll(startGroup.get(tmp2[0]));
                            boolean flag = true;
                            for (String entry2 : tmp2) {
                                if (!emptyNonTerminal.contains(entry2)) {
                                    flag = false;
                                }
                            }
                            if (flag) {
                                newLRItem.start.addAll(LRitem.start);
                            }
                        }
                        list.add(newLRItem);
                        LRItemsWithStart.put(newLRItem.item, newLRItem.start);
                        if (transitions.containsKey(LRitem.toString() + ":$")) {
                            String value = transitions.get(LRitem.toString() + ":$") + ";" + newLRItem.toString();
                            transitions.put(LRitem.toString() + ":$", value);
                        } else {
                            transitions.put(LRitem.toString() + ":$", newLRItem.toString());
                        }
                    }
                }
            }
            processed.add(LRitem.toString());
        }

    }

    private static void generateLRitems() {
        for (String nonTerminal : productions.keySet()) {
            if (nonTerminal.equals("<%>")) {
                LRItems.add(0, nonTerminal + " -> " + productions.get(nonTerminal).trim() + " *");
                LRItems.add(0, nonTerminal + " -> * " + productions.get(nonTerminal).trim());
                continue;
            }
            String tmp[] = productions.get(nonTerminal).split("\\|");
            for (String entry : tmp) {
                if (entry.equals("$")) {
                    LRItems.add(nonTerminal + " -> *");
                } else {
                    String[] tmp1 = entry.split(" ");
                    List<String> list = new LinkedList<>(Arrays.asList(tmp1));
                    int n = list.size();
                    for (int i = 0; i <= n; i++) {
                        list.add(i, "*");
                        StringBuilder str = new StringBuilder();
                        for (String entry2 : list) {
                            str.append(" " + entry2);
                        }
                        LRItems.add(nonTerminal + " ->" + str);
                        list.remove(i);
                    }
                }
            }
        }
//        for (String entry : LRItems) {
//            System.out.println(entry);
//        }
    }

    /**
     * Racuna skupove ZAPOCINJE.
     */
    private static void computeStart() {
        List<String> allChars = new ArrayList<>();
        allChars.addAll(nonTerminal);
        allChars.addAll(terminal);
        int n = allChars.size();
        boolean[][] matrix = new boolean[n][n];

        for (String entry : allChars) {
//            System.out.print(entry + " ");
        }
//        System.out.println();
        // reflexivity
        for (int i = 0; i < n; i++) {
            matrix[i][i] = true;
        }
        // start directly with character
        for (String nonTerminal : productions.keySet()) {
            String tmp[] = productions.get(nonTerminal).split("\\|");
            for (String entry : tmp) {
                if (entry.equals("$")) {
                    continue;
                } else {
                    matrix[allChars.indexOf(nonTerminal)][allChars.indexOf(entry.split(" ")[0])] = true;
                    String[] tmp2 = entry.split(" ");

                    for (int i = 0; i < tmp2.length - 1; i++) {
                        if (emptyNonTerminal.contains(tmp2[i])) {
                            matrix[allChars.indexOf(nonTerminal)][allChars.indexOf(tmp2[i + 1])] = true;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j]) {
//                    System.out.print(1 + " ");
                } else {
//                    System.out.print(0 + " ");
                }
            }
//            System.out.println();
        }
        int x = nonTerminal.size();
        while (true) {
            int numberOfChanges = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < x; j++) {
                    if (matrix[i][j]) {
                        for (int k = 0; k < n; k++) {
                            if (matrix[j][k]) {
                                if (matrix[i][k] == false) {
                                    matrix[i][k] = true;
                                    numberOfChanges++;
                                }
                            }
                        }
                    }
                }
            }
            if (numberOfChanges == 0) {
                break;
            }
        }

//        System.out.println("\nZAPOCINJE za znakove");
        for (int i = 0; i < n; i++) {
//            System.out.print(allChars.get(i) + ":");
            List<String> tmp = new ArrayList<>();
            for (int j = x; j < n; j++) {
                if (matrix[i][j]) {
//                    System.out.print(" " + allChars.get(j));
                    tmp.add(allChars.get(j));
                }
            }
//            System.out.println();
            startGroup.put(allChars.get(i), tmp);
        }

    }

    /**
     * Method which compute empty non terminal characters.
     */
    private static void computeEmptyNonTerminal() {
        // as first we add left sides of epsilon productions
        for (String nonTerminal : productions.keySet()) {
            // Attention! .split("|") does not work.
            String tmp[] = productions.get(nonTerminal).split("\\|");
            for (String entry : tmp) {
                if (entry.equals("$")) {
                    emptyNonTerminal.add(nonTerminal);
                }
            }
        }
        /*
         * then we extend list of empty non terminal characters with non
		 * terminal characters which right side of productions already contains
		 * all empty non terminal characters
		 */

        while (true) {
            int size = emptyNonTerminal.size();
            for (String nonTerminal : productions.keySet()) {
                if (emptyNonTerminal.contains(nonTerminal)) {
                    continue;
                }
                boolean shouldBeAdded = false;
                // right sides
                // Attention! .split("|") does not work.
                String tmp[] = productions.get(nonTerminal).split("\\|");
                for (String entry : tmp) {
                    String[] tmp1 = entry.split(" ");
                    boolean add = true;
                    for (String entry1 : tmp1) {
                        if (!emptyNonTerminal.contains(entry1)) {
                            add = false;
                        }
                    }
                    if (add == true) {
                        shouldBeAdded = true;
                    }

                }
                if (shouldBeAdded) {
                    emptyNonTerminal.add(nonTerminal);
                }
            }
            if (size == emptyNonTerminal.size()) {
                break;
            }
        }

    }

    /**
     * Reads Syntax Analyzer definition.
     */
    private static void input(String[] args) {
        try (BufferedReader scanner = new BufferedReader(
                new InputStreamReader(args.length > 0 ? new FileInputStream(args[0]) : System.in))) {
            String line;

            // nonTerminal characters
            line = scanner.readLine().trim();
            line = line.substring(3);
            skipSplitAdd(line, nonTerminal);
            // it says in the instructions that should be added new start
            // nonTerminal
            // character, in this case it will be <%>.
            nonTerminal.add("<%>");

            // terminal characters
            line = scanner.readLine().trim();
            line = line.substring(3);
            skipSplitAdd(line, terminal);

            // synchronization characters
            line = scanner.readLine().trim();
            line = line.substring(5);
            skipSplitAdd(line, synchronization);

            line = scanner.readLine();
            // add start production as stated in the instruction
            productions.put("<%>", nonTerminal.get(0));
            allProductions.add("<%> -> " + nonTerminal.get(0).trim());
            while (line != null) {
                if (!line.startsWith(" ")) {
                    String key = line;
                    String value = "";
                    while (true) {
                        line = scanner.readLine();
                        if (line != null && line.startsWith(" ")) {
                            line = line.substring(1);
                            allProductions.add(key + " -> " + line);
                            value += line + "|";
                        } else {
                            if (productions.containsKey(key)) {
                                productions.put(key, productions.get(key) + "|" + value);
                            } else {
                                productions.put(key, value.substring(0, value.length() - 1));
                            }
                            break;
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits given string by empty spaces and adds all to given list.
     *
     * @param s    String to split.
     * @param list List to add split strings to.
     */
    private static void skipSplitAdd(String s, List<String> list) {
        String tmp[] = s.split(" ");

        for (int i = 0; i < tmp.length; i++) {
            list.add(tmp[i]);

            // System.out.println(tmp[i]);
        }
    }

    private static class LRItem {
        public String item;
        /**
         * tu pohranjujem skup tih zavrsnih znakova za svaku LR stavku.
         */
        public List<String> start = new ArrayList<>();

        public LRItem(String item) {
            super();
            this.item = item;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(item + ", {");
            for (String entry : start) {
                builder.append(" " + entry);
            }
            builder.append(" }");
            return builder.toString();
        }

    }
}
