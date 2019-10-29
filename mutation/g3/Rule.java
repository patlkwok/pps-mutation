package mutation.g3;

import java.util.Objects;

/**
 *
 * @author group3
 */
public class Rule {

    private final String pattern;
    private final String action;
    private final int scopeSize;

    public Rule(String pattern, String action) {
        this.pattern = pattern;
        this.action = action;
        this.scopeSize = calculateScopeSize(pattern, action);
    }

    public String getPattern() {
        return pattern;
    }

    public String getAction() {
        return action;
    }

    public int getScopeSize() {
        return scopeSize;
    }

    public static int calculateScopeSize(String pattern, String action) {
        int patternSize = pattern.split(";").length;
        char mai = '0' - 1;
        for (int i = 0; i < action.length(); i++) {
            char ai = action.charAt(i);
            if (ai >= '0' && ai <= '9' && ai > mai) {
                mai = ai;
            }
        }
        return Math.max(patternSize, (int) (mai - '0'));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.pattern);
        hash = 53 * hash + Objects.hashCode(this.action);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rule other = (Rule) obj;
        if (!Objects.equals(this.pattern, other.pattern)) {
            return false;
        }
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return pattern + "@" + action;
    }

}
