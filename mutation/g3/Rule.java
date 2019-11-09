package mutation.g3;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author group3
 */
public class Rule {

    public static final byte A = 0b1000;
    public static final byte C = 0b0100;
    public static final byte G = 0b0010;
    public static final byte T = 0b0001;

    private final byte[] pattern;
    private final String action;
    private final int scopeSize;

    public Rule(byte[] pattern, String action) {
        this.pattern = pattern;
        this.action = action;
        this.scopeSize = calculateScopeSize(pattern, action);
    }

    public byte[] getPattern() {
        return pattern;
    }

    public String getPatternString() {
        String patternStr = "";
        for (int i = 0; i < pattern.length; i++) {
            patternStr += (i != 0 ? ";" : "") + baseByteToString(pattern[i]);
        }
        return patternStr;
    }

    public String getAction() {
        return action;
    }

    public int getScopeSize() {
        return scopeSize;
    }

    public static int calculateScopeSize(byte[] pattern, String action) {
        int patternSize = pattern.length;
        char mai = '0' - 1;
        for (int i = 0; i < action.length(); i++) {
            char ai = action.charAt(i);
            if (ai >= '0' && ai <= '9' && ai > mai) {
                mai = ai;
            }
        }
        return Math.max(patternSize, Math.max(action.length(), (int) (mai - '0')));
    }

    public static int countActionDigits(Rule rule) {
        String pattern = rule.getAction();
        int digits = 0;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) >= '0' && pattern.charAt(i) <= '9') {
                digits++;
            }
        }
        return digits;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.hashCode(this.pattern);
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
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Arrays.equals(this.pattern, other.pattern)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getPatternString() + "@" + action;
    }

    public static Rule fromString(String ruleStr) {
        return fromString(
                ruleStr.substring(0, ruleStr.indexOf("@")),
                ruleStr.substring(ruleStr.indexOf("@") + 1)
        );
    }

    public static Rule fromString(String patternStr, String action) {
        String[] patternParts = patternStr.split(";");
        byte[] pattern = new byte[patternParts.length];
        for (int i = 0; i < patternParts.length; i++) {
            for (char c : patternParts[i].toCharArray()) {
                pattern[i] += baseCharToByte(c);
            }
        }
        return new Rule(pattern, action);
    }

    public static byte baseCharToByte(char b) {
        switch (b) {
            case 'a':
                return A;
            case 'c':
                return C;
            case 'g':
                return G;
            case 't':
                return T;
            default:
                throw new IllegalArgumentException(b + " is not a valid base");
        }
    }

    public static String baseByteToString(byte b) {
        String pattern = "";
        if ((b & A) > 0) {
            pattern += 'a';
        }
        if ((b & C) > 0) {
            pattern += 'c';
        }
        if ((b & G) > 0) {
            pattern += 'g';
        }
        if ((b & T) > 0) {
            pattern += 't';
        }
        return pattern;
    }

    public String apply(String original) {
        if (original.length() < pattern.length || original.length() < action.length()) return null;
        
        boolean match = true;
        int matchAt = -1;
        for (int k = 0; k <= original.length() - Math.max(pattern.length, action.length()); k++) {
            for (int i = 0; i < pattern.length; i++) {
                byte b = pattern[i];
                if ((b & baseCharToByte(original.charAt(i + k))) == 0){
                    match = false;
                    break;
                }
            }
            if (match) {
                matchAt = k;
                break;
            }
        }
        if (match && matchAt != -1) {
            char[] mutable = original.toCharArray();
            for (int j = 0; j < action.length(); ++ j) {
                char c = action.charAt(j);
                if (c >= '0' && c <= '9')
                    c = original.charAt(c - '0');
                mutable[matchAt + j] = c;
            }
            return String.valueOf(mutable);
        } else {
            return null;
        }
    }

}
