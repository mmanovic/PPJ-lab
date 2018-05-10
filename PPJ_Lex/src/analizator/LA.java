package analizator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LA {

    private List<String> lexerStates = new ArrayList<>();
    private List<String> lexerVariables = new ArrayList<>();
    private List<LexerRule> lexerRules = new ArrayList<>();

    private String source;

    public LA(final String sourceCode) {
        this.source = sourceCode;
        loadDefinitions();
    }

    private void loadDefinitions() {

        try (BufferedReader reader = new BufferedReader(new FileReader("states.txt"))) {
            while (reader.ready()) {
                lexerStates.add(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("lexerClasses.txt"))) {
            while (reader.ready()) {
                lexerVariables.add(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("lexerRules.txt"))) {
            while (reader.ready()) {
                reader.readLine(); // {

                // LexerRule
                String startStateLA = reader.readLine();
                String priority = reader.readLine();
                String[] actions = reader.readLine().split(",");

                LexerRule lexerRule = new LexerRule(startStateLA, Integer.parseInt(priority));

                for (String action : actions) {
                    lexerRule.addAction(action);
                }

                // Automat
                String automatStart = reader.readLine();
                String automatAccept = reader.readLine();

                // prijelazi automata
                Automat automat = lexerRule.getRegexAutomat();

                automat.setStartState(Integer.parseInt(automatStart));
                automat.setAcceptableState(Integer.parseInt(automatAccept));

                String line = reader.readLine();

                while (!line.equals("}")) {

                    if (line.endsWith(",")) { // \n
                        line += "\n" + reader.readLine();
                    }

                    String[] tmp = line.split("->");

                    String[] left = tmp[0].split(",", 2);
                    String from = left[0];
                    String forChar = left[1];
                    String to = tmp[1];

                    String[] toStates = to.split(",");

                    for (String toState : toStates) {
                        automat.dodajPrijelaz(Integer.parseInt(from), Integer.parseInt(toState), forChar);
                    }

                    line = reader.readLine();
                }

                lexerRules.add(lexerRule);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String lexerState;
    private Integer currentLine = 1;
    private String bufferedString;

    public void generateTokens() {
        lexerState = lexerStates.get(0);

        bufferedString = "";

        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            bufferedString += current;

            boolean atLeastOneStateValid = hasAtLeastOneState(lexerState, bufferedString);

            if (atLeastOneStateValid) {
                continue; // hope to find even better solution so just continue

            } else if (bufferedString.length() <= 1 && !atLeastOneStateValid) {
                continue; // no valid states but too few characters to recover,
                          // read further

            } else { // no more matches exist, extract the best one and recover

                /* temporarily remove last char and return it later */
                bufferedString = bufferedString.substring(0, bufferedString.length() - 1);

                LexerRule rule = findBestRule(lexerState, bufferedString);

                /* error recovery */
                if (rule == null) {
                    // System.out.println("Error while processing ->" +
                    // bufferedString + "<-");

                    LexerRule prefixRule = bestPrefixRule(lexerState, bufferedString);

                    if (prefixRule == null) {
                        // System.err.println("Error char: ->" +
                        // bufferedString.charAt(0) + "<-");
                        bufferedString = bufferedString.substring(1);
                    } else {
                        completeActions(prefixRule);
                    }

                    i--; // start again
                    continue;

                } else {
                    completeActions(rule);
                }

                bufferedString += current; // removed character is added back
            }
        }
    }

    private void completeActions(LexerRule rule) {
        // lexerState, bufferedString, currentLine

        // System.out.println("Considering string: ->" + bufferedString + "<-");

        LexerRule.MatchState matchState = rule.getMatchState(bufferedString);

        // System.out.println("Match length: " + matchState.getMatchLength());
        // System.out.println("Match: ->" + bufferedString.substring(0,
        // matchState.getMatchLength()) + "<-");
        // System.out.println("Rule name: " + rule.getName());
        // System.out.println();

        boolean vratiSeActionUsed = false;
        String match = bufferedString.substring(0, matchState.getMatchLength());

        List<String> actions = rule.getActions();

        if (actions.contains("NOVI_REDAK")) {
            currentLine++;
        }

        for (String action : actions) {
            if (action.startsWith("VRATI_SE ")) {
                action = action.substring("VRATI_SE ".length());
                vratiSeActionUsed = true;
                match = bufferedString.substring(0, Integer.parseInt(action));
                bufferedString = bufferedString.substring(Integer.parseInt(action));

            }
        }

        for (String action : actions) {
            if (action.startsWith("UDJI_U_STANJE ")) {
                action = action.substring("UDJI_U_STANJE ".length());
                lexerState = action;
            }
        }

        for (String action : actions) {
            if (lexerVariables.contains(action)) {
                String output = action + " " + currentLine + " " + match;
                // if (output.equals("OP_PRIDRUZI 27 =")) {
                // System.out.print("");
                // }
                System.out.println(output);
            }
        }

        // for (String action : rule.getActions()) {
        // action = action.trim();
        // if (action.equals("NOVI_REDAK")) {
        // currentLine++;
        //
        // } else if (action.startsWith("UDJI_U_STANJE ")) {
        // action = action.substring("UDJI_U_STANJE ".length());
        // lexerState = action;
        //
        // } else if (action.startsWith("VRATI_SE ")) {
        // action = action.substring("VRATI_SE ".length());
        // vratiSeActionUsed = true;
        // match = bufferedString.substring(0, Integer.parseInt(action));
        // bufferedString = bufferedString.substring(Integer.parseInt(action));
        //
        // } else if (lexerVariables.contains(action)) {
        // String output = action + " " + currentLine + " " + match;
        // if (output.equals("OPERAND 2 3")) {
        // System.out.println("iduci");
        // }
        // System.out.println(output);
        //
        // }
        // }

        if (!vratiSeActionUsed) {
            bufferedString = bufferedString.substring(matchState.getMatchLength());
        }

    }

    private LexerRule bestPrefixRule(String currentState, String input) {
        int bestLen = 0;
        LexerRule bestRule = null;

        for (int i = 0; i < input.length(); i++) {
            for (LexerRule lexerRule : lexerRules) {
                if (lexerRule.getState().equals(currentState)) {
                    LexerRule.MatchState matchState = lexerRule.getMatchState(input.substring(0, i + 1));
                    int curLen = matchState.getMatchLength();
                    if (curLen > bestLen && matchState.isFullyMatched()) {
                        bestLen = curLen;
                        bestRule = lexerRule;
                    }
                }
            }
        }
        return bestRule;
    }

    private LexerRule findBestRule(String currentState, String bufferedString) {
        if (bufferedString.isEmpty()) {
            return null;
        }

        LexerRule bestRule = null;
        int bestLength = -1, bestPriority = -1;

        for (LexerRule lexerRule : lexerRules) {
            if (lexerRule.getState().equals(currentState)) {
                LexerRule.MatchState matchState = lexerRule.getMatchState(bufferedString);
                if (matchState.isFullyMatched()) {
                    int length = matchState.getMatchLength();
                    int priority = lexerRule.getPriority();

                    if (length > bestLength || (length == bestLength && priority > bestPriority)) {
                        bestLength = length;
                        bestPriority = priority;
                        bestRule = lexerRule;
                    }
                }
            }
        }

        return bestRule;
    }

    private boolean hasAtLeastOneState(String currentState, String bufferedString) {
        for (LexerRule lexerRule : lexerRules) {
            if (lexerRule.getState().equals(currentState)) {
                LexerRule.MatchState matchState = lexerRule.getMatchState(bufferedString);
                if (matchState.getMatchLength() == bufferedString.length()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Razred koji definira automat.
     *
     * @author Mato Manovic
     * @version 1.0
     */
    public static class Automat implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public int startState;// pocetno stanje automata
        public int acceptableState;// krajnje prihvatljivo stanje automata
        /**
         * Kao na utr-u pohranjujem prijlaze na nacin da je kljuc oblika
         * "trenutnostanje prijelazniznak" a vrijednost "novostanje". Mada ce
         * ova stanje biti integeri ovdje ce biti kao String.
         */
        public Map<String, String> prijelazi = new LinkedHashMap<>();

        private Set<String> stanja = new HashSet<>();

        /**
         * koliko je trenutno pohranjenih stanja
         */
        public int brojStanja;

        public Automat(String regex) {
            pretvori(regex);
        }

        public Automat() {
        }

        public void reset() {
            stanja.clear();
            stanja.add(Integer.toString(startState));
        }

        public LexerRule.MatchState isValidInput(String input) {
            return izvrsiAutomat(input);
        }

        private LexerRule.MatchState izvrsiAutomat(String ulazniNiz) {
            TreeSet<String> pocetno = new TreeSet<>();
            pocetno.add(String.valueOf(startState));
            int povratak = 1;
            while (true) {
                pocetno = epsilonOkolina(pocetno);
                if (pocetno.size() == povratak) {
                    break;
                }
                povratak = pocetno.size();
            }

            if (ulazniNiz.length() == 0) {
                return new LexerRule.MatchState(ulazniNiz.length(), pocetno.contains(String.valueOf(acceptableState)));
            }

            String[] znakovi = ulazniNiz.split("");
            int cnt = 0;
            for (String entry : znakovi) {
                pocetno = napraviPrijelaz(pocetno, entry);

                int povratak2 = pocetno.size();
                while (true) {
                    pocetno = epsilonOkolina(pocetno);
                    if (pocetno.size() == povratak2) {
                        break;
                    }
                    povratak2 = pocetno.size();
                }

                if (pocetno.size() == 0) {
                    return new LexerRule.MatchState(cnt, false);
                }
                cnt++;

            }

            return new LexerRule.MatchState(ulazniNiz.length(), pocetno.contains(String.valueOf(acceptableState)));
        }

        private TreeSet<String> napraviPrijelaz(Set<String> poc, String znak) {
            TreeSet<String> povratni = new TreeSet<>();
            for (String entry : poc) {
                String prijelaz = entry.concat("," + znak);

                String novaStanja = prijelazi.get(prijelaz);

                if (novaStanja != null) {
                    String[] fieldNova = novaStanja.split(",");
                    povratni.addAll(Arrays.asList(fieldNova));
                }
            }
            return povratni;
        }

        private TreeSet<String> epsilonOkolina(Set<String> pocetno) {
            TreeSet<String> okolina = new TreeSet<>();
            for (String entry : pocetno) {
                okolina.add(entry);
                String prijelaz = entry.concat(",$$");
                String novaStanja = prijelazi.get(prijelaz);

                if (novaStanja != null) {
                    String[] fieldNova = novaStanja.split(",");
                    okolina.addAll(Arrays.asList(fieldNova));
                }
            }
            return okolina;
        }

        // private static void ispis(TreeSet<String> stanja) {
        // int length = stanja.size();
        // if (stanja.size() == 0) {
        // System.out.print("#");
        //
        // }
        // for (String entry : stanja) {
        //
        // System.out.print(entry);
        // if (length > 1) {
        // System.out.print(",");
        //
        // }
        // length--;
        // }
        // }

        /**
         * Metoda koja pretvara regularni izraz u automat
         *
         * @param izraz
         *            Regularni izraz.
         * @param automat
         *            Objekt automat.
         */
        private void pretvori(String izraz) {
            // rastavljaje regularnog izraza na podizraze
            List<String> izbori = rastaviizraz(izraz);

            int lijevo_stanje = brojStanja++;
            int desno_stanje = brojStanja++;

            // ako ima vise izbora obradimo podizbore inace obradimo cijeli
            // izraz
            if (izbori.size() != 0) {
                for (String izbor : izbori) {
                    pretvori(izbor);
                    dodajEpsilonPrijelaz(lijevo_stanje, this.startState);
                    dodajEpsilonPrijelaz(this.acceptableState, desno_stanje);
                }
            } else {
                boolean prefiksirano = false;
                int zadnje_stanje = lijevo_stanje;
                int duljinaIzraza = izraz.length();
                // izraz u polje znakova
                char[] tmpIzraz = izraz.toCharArray();

                for (int i = 0; i < duljinaIzraza; i++) {
                    int a, b;
                    // slucaj 1
                    if (prefiksirano == true) {
                        prefiksirano = false;
                        char prijelazniZnak;
                        if (tmpIzraz[i] == 't') {
                            prijelazniZnak = '\t';
                        } else if (tmpIzraz[i] == 'n') {
                            prijelazniZnak = '\n';
                        } else if (tmpIzraz[i] == '_') {
                            prijelazniZnak = ' ';
                        } else {
                            prijelazniZnak = tmpIzraz[i];
                        }

                        a = brojStanja++;
                        b = brojStanja++;
                        dodajPrijelaz(a, b, prijelazniZnak);
                    } else {
                        // slucaj 2
                        if (tmpIzraz[i] == '\\') {
                            prefiksirano = true;
                            continue;
                        }
                        if (tmpIzraz[i] != '(') {
                            // slucaj 2a
                            a = brojStanja++;
                            b = brojStanja++;
                            if (tmpIzraz[i] == '$') {
                                dodajEpsilonPrijelaz(a, b);
                            } else {
                                dodajPrijelaz(a, b, tmpIzraz[i]);
                            }
                        } else {
                            int j = i;
                            int brojOtvorenihZagrada = 0;
                            // trazim odgovarajucu zatvorenu zagradu
                            do {
                                j++;
                                if (tmpIzraz[j] == ')' && brojOtvorenihZagrada == 0) {
                                    break;
                                }
                                if (tmpIzraz[j] == '(') {
                                    brojOtvorenihZagrada++;
                                } else if (tmpIzraz[j] == ')') {
                                    brojOtvorenihZagrada--;
                                }
                            } while (true);
                            pretvori(new String(tmpIzraz, i + 1, j - i - 1));
                            a = this.startState;
                            b = this.acceptableState;
                            i = j;
                        }
                    }

                    // provjera ponavljanja(Kleenov operator)
                    if (i + 1 < duljinaIzraza && tmpIzraz[i + 1] == '*') {
                        int x = a;
                        int y = b;
                        a = brojStanja++;
                        b = brojStanja++;
                        dodajEpsilonPrijelaz(a, x);
                        dodajEpsilonPrijelaz(y, b);
                        dodajEpsilonPrijelaz(a, b);
                        dodajEpsilonPrijelaz(y, x);
                        i++;
                    }

                    // povezivanje s ostatkom automata
                    dodajEpsilonPrijelaz(zadnje_stanje, a);
                    zadnje_stanje = b;
                }
                dodajEpsilonPrijelaz(zadnje_stanje, desno_stanje);
            }
            this.startState = lijevo_stanje;
            this.acceptableState = desno_stanje;

        }

        /**
         * Pomocna metoda koja provjerava da li je znak na zadanom indeksu
         * operator tj. ako nije onda je prefiksiran. Valjda ju je Nidjo dobro
         * napisao :)
         *
         * @param regex
         *            Regularni izraz
         * @param index
         *            Indeks znaka
         * @return Je li znak operator.
         */
        private static boolean je_operator(String regex, int index) {
            int count = 0;

            while (index - 1 >= 0 && regex.charAt(index - 1) == '\\') {
                count++;
                index--;
            }

            return count % 2 == 0;
        }

        /**
         * Pomocna metoda koja rastavlja pocetni regularni izraz na podizraze.
         * Nidjo ju je takodjer pisao valjda je dobro napisana.
         *
         * @param regex
         *            Izraz kojeg rastavljamo.
         * @return Listu podizraza.
         */
        private static List<String> rastaviizraz(String regex) {
            List<String> izbori = new ArrayList<>();
            int br_zagrada = 0;
            int start = 0;
            for (int i = 0; i < regex.length(); i++) {
                if (regex.charAt(i) == '(' && je_operator(regex, i)) {
                    br_zagrada++;
                } else if (regex.charAt(i) == ')' && je_operator(regex, i)) {
                    br_zagrada--;
                } else if (br_zagrada == 0 && regex.charAt(i) == '|' && je_operator(regex, i)) {
                    izbori.add(regex.substring(start, i));
                    start = i + 1;
                }
            }
            if (start > 0) {
                izbori.add(regex.substring(start, regex.length()));
            }

            // for (String s : izbori) {
            // System.out.println(s);
            // }
            return izbori;
        }

        /**
         * Pomocna metoda koja dodaje u automat prijelaz.Ovdje je epsilon
         * prijelaz pa je zu prijelazni znak jednak znaku $$.
         *
         * @param automat
         *            Automat
         * @param pocetno
         *            Trenutno stanje.
         * @param sljedece
         *            Sljedece stanje.
         */
        private void dodajEpsilonPrijelaz(int pocetno, int sljedece) {
            String key = pocetno + ",$$";
            if (!prijelazi.containsKey(key)) {
                prijelazi.put(key, new Integer(sljedece).toString());
            } else {
                String value = prijelazi.get(key) + "," + new Integer(sljedece).toString();
                prijelazi.put(key, value);
            }
        }

        /**
         * Metoda koja dodaje u automat prijelaz.
         *
         * @param automat
         *            Automat
         * @param pocetno
         *            Trenutno stanje.
         * @param sljedece
         *            Sljedece stanje.
         * @param znak
         *            Prijelazni znak.
         */
        public void dodajPrijelaz(int pocetno, int sljedece, char znak) {
            dodajPrijelaz(pocetno, sljedece, Character.toString(znak));
        }

        /**
         * Metoda koja dodaje u automat prijelaz.
         *
         * @param automat
         *            Automat
         * @param pocetno
         *            Trenutno stanje.
         * @param sljedece
         *            Sljedece stanje.
         * @param znak
         *            Prijelazni znak.
         */
        public void dodajPrijelaz(int pocetno, int sljedece, String s) {
            String key = pocetno + "," + s;
            if (!prijelazi.containsKey(key)) {
                prijelazi.put(key, new Integer(sljedece).toString());
            } else {
                String value = prijelazi.get(key) + "," + Integer.toString(sljedece);
                prijelazi.put(key, value);
            }
        }

        public void setStartState(int startState) {
            this.startState = startState;
        }

        public void setAcceptableState(int acceptableState) {
            this.acceptableState = acceptableState;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(startState).append('\n');
            sb.append(acceptableState).append('\n');

            sb.append(prijelazi.entrySet().stream().map(entry -> entry.getKey() + "->" + entry.getValue())
                    .collect(Collectors.joining("\n")));

            return sb.toString();
        }
    }

    public static class LexerRule implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private String state; // probably not required
        private int priority;
        private String name; // rule name
        private String regexDefinition;

        private Automat regexAutomat;

        private List<String> actions = new ArrayList<>();

        public LexerRule(String regex, String state, int priority, String name) {
            this.regexDefinition = regex;
            this.priority = priority;
            this.state = state;
            this.name = name;

            this.regexAutomat = new Automat(regex);
        }

        public LexerRule(String state, int priority) {
            this.state = state;
            this.priority = priority;
            this.regexAutomat = new Automat();
        }

        public void addAction(String action) {
            actions.add(action);
        }

        public Automat getRegexAutomat() {
            return regexAutomat;
        }

        public List<String> getActions() {
            return actions;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

        public String getRegexDefinition() {
            return regexDefinition;
        }

        public MatchState getMatchState(String input) {
            return regexAutomat.isValidInput(input);
        }

        public String getState() {
            return state;
        }

        public static class MatchState {

            int matchLength;
            boolean isFullyMatched;

            public MatchState(int matchLength, boolean isFullyMatched) {

                this.matchLength = matchLength;
                this.isFullyMatched = isFullyMatched;
            }

            public int getMatchLength() {
                return matchLength;
            }

            public boolean isFullyMatched() {
                return isFullyMatched;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(state).append('\n');
            sb.append(priority).append('\n');

            sb.append(actions.stream().collect(Collectors.joining(",")));

            sb.append('\n');

            sb.append(regexAutomat);

            return sb.toString();
        }
    }

    public static void main(String[] args) {

        StringBuilder sourceCode = new StringBuilder();
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(args.length > 0 ? new FileInputStream(args[0]) : System.in));

            String line;
            while ((line = br.readLine()) != null) {
                sourceCode.append(line).append("\n");
            }

            LA lexicalAnalyzer = new LA(sourceCode.toString());

            lexicalAnalyzer.generateTokens();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
