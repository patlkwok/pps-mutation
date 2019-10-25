package mutation.g3;

import java.util.Objects;

/**
 *
 * @author group3
 */
public class Rule {

    private final String pattern;
    private final String action;

    public Rule(String pattern, String action) {
        this.pattern = pattern;
        this.action = action;
    }

    public String getPattern() {
        return pattern;
    }

    public String getAction() {
        return action;
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

}
