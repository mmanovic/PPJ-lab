package analizator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LexerRule implements Serializable {

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
