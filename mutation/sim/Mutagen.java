package mutation.sim;

import java.util.*;

public class Mutagen {
    public Mutagen() {
        this.random = new Random();
        this.patterns = new ArrayList<String>();
        this.actions = new ArrayList<String>();
    }
    public Mutagen(List<String> patterns, List<String> actions) {
        this.random = new Random();
        this.patterns = new ArrayList<String>(patterns);
        this.actions = new ArrayList<String>(actions);
    }

    private int match(String s, String pat) {
        String[] l = pat.split(";");
        boolean[][] chk = new boolean[l.length][4];
        for (int i = 0; i < l.length; ++ i) {
            Arrays.fill(chk[i], false);
            for (int j = 0; j < l[i].length(); ++ j)
                chk[i][translate(l[i].charAt(j))] = true;
        }
        for (int i = 0; i < s.length() - l.length + 1; ++ i) {
            boolean matched = true;
            for (int j = 0; j < l.length; ++ j)
                if (!chk[j][translate(s.charAt(i + j))]) {
                    matched = false;
                    break;
                }
            if (matched) return i;
        }
        return -1;
    }

    public String Mutate(String genome, int m) {
        int len = genome.length();
        char[] mutable = genome.toCharArray();
        for (numberOfMutations = 0; numberOfMutations < m; ++ numberOfMutations) {
            int offset = Math.abs(random.nextInt() % actions.size());
            int start = Math.abs(random.nextInt() % len);
            String str = String.valueOf(mutable);
            while (str.length() < start + len + 10)
                str += str;
            str = str.substring(start, start + len + 10);
            boolean matched = false;
            for (int i = 0; i < patterns.size(); ++ i) {
                int idx = (offset + i) % actions.size();
                int pos = match(str, patterns.get(idx));
                if (pos != -1) {
                    matched = true;
                    // Modification
                    String action = actions.get(idx);
                    for (int j = 0; j < action.length(); ++ j) {
                        char c = action.charAt(j);
                        if (c >= '0' && c <= '9')
                            c = str.charAt(pos + c - '0');
                        mutable[(start + pos + j) % len] = c;
                    }
                }
            }
            if (!matched) return String.valueOf(mutable);
        }
        return String.valueOf(mutable);
    }

    // Return the number of mutations performed during the previous Mutate(genome)
    public int getNumberOfMutations() {
        return numberOfMutations;
    }

    public void add(String pattern, String action) {
        patterns.add(pattern);
        actions.add(action);
    }

    // Remove the i-th mutation
    public void remove(int i) {
        if (i > 0 && i < patterns.size()) {
            patterns.remove(i);
            actions.remove(i);
        }
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public List<String> getActions() {
        return actions;
    }

    public Set<Long> jaccardSet(String s) {
        Set<Long> result = new HashSet<Long>();
        for (int k = 0; k < patterns.size(); ++ k) {
            String[] l = patterns.get(k).split(";");
            boolean[][] chk = new boolean[l.length][4];
            for (int i = 0; i < l.length; ++i) {
                Arrays.fill(chk[i], false);
                for (int j = 0; j < l[i].length(); ++j)
                    chk[i][translate(l[i].charAt(j))] = true;
            }
            for (int i = 0; i < s.length(); ++i) {
                boolean matched = true;
                for (int j = 0; j < l.length; ++j)
                    if (!chk[j][translate(s.charAt((i + j) % s.length()))]) {
                        matched = false;
                        break;
                    }
                if (matched) {
                    // Perform action & insert
                    Long entry = 0l;
                    String action = actions.get(k);
                    for (int j = 0; j < action.length(); ++ j) {
                        char c = action.charAt(j);
                        if (c >= '0' && c <= '9')
                            c = s.charAt(i + c - '0');
                        entry = entry * 4 + translate(c);
                    }
                    entry += i << 20;
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public boolean equals(Mutagen other) {
        if (patterns.size() != other.patterns.size())
            return false;
        Set<String> s1 = new HashSet<String>(), s2 = new HashSet<String>();
        for (int i = 0; i < patterns.size(); ++ i) {
            s1.add(patterns.get(i) + "$" + actions.get(i));
            s2.add(other.patterns.get(i) + "$" + other.actions.get(i));
        }
        return s1.equals(s2);
    }

    private int translate(char c) {
        if (c == 'a') return 0;
        if (c == 'c') return 1;
        if (c == 'g') return 2;
        if (c == 't') return 3;
        return -1;
    }

    private Random random;
    private List<String> patterns;
    private List<String> actions;
    private int numberOfMutations;

}