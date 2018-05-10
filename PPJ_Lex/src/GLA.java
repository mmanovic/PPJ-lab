import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generator for Lexical Analyzer.
 *
 * @author nikola
 */
public class GLA {

    /**
     * Regular definitions (name, regular expression)
     */
    private static Map<String, String> regularneDefinicije = new HashMap<>();

    /**
     * States of Lexical Analyzer.
     */
    private static List<String> stanjaLA = new ArrayList<>();

    /**
     * Names of lexical classes.
     */
    private static List<String> leksickeJedinke = new ArrayList<>();

    /**
     * List of lexer rules.
     */
    private static List<LexerRule> lexerRules = new ArrayList<>();

    /**
     * Main method of the program.
     *
     * @param args Not used here.
     */
    public static void main(String[] args) {
        input(args);
        writeLexerData();
    }

    private static void writeLexerData() {

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("states.txt")))) {
            for (String stanje : stanjaLA) {
                writer.println(stanje);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // FILE LAYOUT:
        // {
        // state
        // priority
        // action1,action2,action3...
        // start_state
        // acceptable_state
        // s1,c1->s1',s2',s3'...
        // s2,c2->s1'',s2'',s3''...
        // }
        // {
        // ...
        // }
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("lexerRules.txt")))) {
            for (LexerRule lexerRule : lexerRules) {
                writer.println("{");
                writer.write(lexerRule.toString());
                writer.println();
                writer.println("}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("lexerClasses.txt")))) {
            writer.println(leksickeJedinke.stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("DONE!");

    }

    /**
     * Reads Lexical Analyzer definition.
     */
    private static void input(String[] args) {
        try (BufferedReader scanner = new BufferedReader(
                new InputStreamReader(args.length > 0 ? new FileInputStream(args[0]) : System.in))) {

            String linija;

            // regularne definicije
            while ((linija = scanner.readLine()) != null && linija.startsWith("{")) {
                String tmp[] = linija.split(" ");

                tmp[0] = tmp[0].substring(1, tmp[0].length() - 1);
                String naziv = tmp[0];
                String izraz = expandRegularDefinition(tmp[1]);

                regularneDefinicije.put(naziv, izraz);

                // System.out.println(naziv + ", " + izraz);
            }

            // stanja
            while (!linija.startsWith("%X")) {
                linija = scanner.readLine().trim();
            }

            skipSplitAdd(linija, stanjaLA);

            // leksicke jedinke
            while (!linija.startsWith("%L")) {
                linija = scanner.readLine().trim();
            }

            skipSplitAdd(linija, leksickeJedinke);

            // pravila leksickog analizatora

            while ((linija = scanner.readLine()) != null) {
                while (!linija.startsWith("<")) {
                    linija = scanner.readLine();
                }

                String tmp[] = linija.split(">", 2);

                String stateName = tmp[0].substring(1, tmp[0].length());
                String regDef = tmp[1];

                regDef = expandRegularDefinition(regDef);

                // System.out.println(stateName + "<> " + regDef);
                LexerRule lexerRule = new LexerRule(regDef, stateName, 1, "<" + stateName + ">" + regDef);
                lexerRules.add(lexerRule);

                scanner.readLine(); // preskoci {

                linija = scanner.readLine().trim();
                while (linija != null && scanner.ready() && !linija.equals("}")) {
                    // radi nesto s naredbom
                    lexerRule.addAction(linija);
                    linija = scanner.readLine().trim();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Expands all references in regular definitions.
     * <p>
     * <p>
     * For example: {reg1} 1|2|3 {reg2} {reg1}|4|5
     * <p>
     * {reg2} becomes (1|2|3)|4|5
     *
     * @param regDef regular definition to expand.
     * @return Returns regular definition with all references expanded.
     */
    private static String expandRegularDefinition(String regDef) {
        // nadji reference na regularne definicije
        int start = regDef.indexOf('{');

        while (start >= 0) {
            // provjeri je li { escapean
            if (!isEscaped(regDef, start)) {
                int end = regDef.indexOf('}', start);

                // provjeri je li } escapean
                while (isEscaped(regDef, end)) {
                    end = regDef.indexOf('}', end + 1);
                }

                String regRef = regDef.substring(start + 1, end);

                regDef = regDef.substring(0, start) + "(" + regularneDefinicije.get(regRef) + ")"
                        + regDef.substring(end + 1, regDef.length());
            }

            start = regDef.indexOf('{', start + 1);
        }

        return regDef;
    }

    /**
     * Checks if character at given position is escaped with \.
     *
     * @param s   String to check.
     * @param pos Position of character in the string.
     * @return True if character at provided position is escaped, false otherwise.
     */
    private static boolean isEscaped(String s, int pos) {
        if (pos < 0 || pos >= s.length()) {
            throw new IllegalArgumentException();
        }

        if (pos == 0) {
            return false;
        }

        int count = 0;
        pos--;

        while (pos >= 0 && s.charAt(pos) == '\\') {
            pos--;
            count++;
        }

        return count % 2 != 0;
    }

    /**
     * Splits given string by empty spaces and adds all but first to given list.
     *
     * @param s    String to split.
     * @param list List to add split strings to.
     */
    private static void skipSplitAdd(String s, List<String> list) {
        String tmp[] = s.split(" ");

        for (int i = 1; i < tmp.length; i++) {
            list.add(tmp[i]);

            // System.out.println(tmp[i]);
        }
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
         * "trenutnostanje prijelazniznak" a vrijednost "novostanje". Mada ce ova
         * stanje biti integeri ovdje ce biti kao String.
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
         * @param izraz   Regularni izraz.
         * @param automat Objekt automat.
         */
        private void pretvori(String izraz) {
            // rastavljaje regularnog izraza na podizraze
            List<String> izbori = rastaviizraz(izraz);

            int lijevo_stanje = brojStanja++;
            int desno_stanje = brojStanja++;

            // ako ima vise izbora obradimo podizbore inace obradimo cijeli izraz
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
         * Pomocna metoda koja provjerava da li je znak na zadanom indeksu operator
         * tj. ako nije onda je prefiksiran. Valjda ju je Nidjo dobro napisao :)
         *
         * @param regex Regularni izraz
         * @param index Indeks znaka
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
         * Pomocna metoda koja rastavlja pocetni regularni izraz na podizraze. Nidjo
         * ju je takodjer pisao valjda je dobro napisana.
         *
         * @param regex Izraz kojeg rastavljamo.
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
         * Pomocna metoda koja dodaje u automat prijelaz.Ovdje je epsilon prijelaz
         * pa je zu prijelazni znak jednak znaku $$.
         *
         * @param automat  Automat
         * @param pocetno  Trenutno stanje.
         * @param sljedece Sljedece stanje.
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
         * @param automat  Automat
         * @param pocetno  Trenutno stanje.
         * @param sljedece Sljedece stanje.
         * @param znak     Prijelazni znak.
         */
        public void dodajPrijelaz(int pocetno, int sljedece, char znak) {
            dodajPrijelaz(pocetno, sljedece, Character.toString(znak));
        }

        /**
         * Metoda koja dodaje u automat prijelaz.
         *
         * @param automat  Automat
         * @param pocetno  Trenutno stanje.
         * @param sljedece Sljedece stanje.
         * @param znak     Prijelazni znak.
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


    // Mislim da je nepotrebno escapat to se radi u pretvorbi regularnog izraza
    // u automat
    /**
     * Escapes all occurences of '\' before a character.
     * <p>
     * For example '\|' is '|' after escape, also '\\' == '\'.
     *
     * @param s
     *            String to escape.
     * @return Returns escaped string.
     */
    // private static String escape(String s) {
    // StringBuilder sb = new StringBuilder();
    // char ss[] = s.toCharArray();
    //
    // for (int i = 0; i < ss.length; i++) {
    // if (ss[i] == '\\') {
    // if (ss[i + 1] == 'n') {
    // sb.append('\n');
    // } else if (ss[i + 1] == 't') {
    // sb.append('\t');
    // } else if (ss[i + 1] == '_') {
    // sb.append(' ');
    // } else {
    // sb.append(ss[i + 1]);
    // }
    //
    // i++;
    // } else {
    // sb.append(ss[i]);
    // }
    // }
    //
    // return sb.toString();
    // }

}
