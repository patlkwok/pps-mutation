package mutation.g2;
import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;

public class Utilities {

  // Wrapper around SOPln to improve readability
  public static <E> void alert(E ... content){
    System.out.println("\n\n====== G2 ======");
    for(E c: content){
      System.out.print(" "+c+" ");
    }
    System.out.println("\n================\n\n");
  }


  // A list of before, after and location after each mutation
  public static List<Change> diff(String before, String after){
    List<Change> singleChanges = getSingleChanges(before, after);
    Collections.sort(singleChanges);
    return consolidateChanges(singleChanges);
  }

  // consolidate a bunch of single letter changes by location
  public static List<Change> consolidateChanges (List<Change> changes) {
    if(changes.size() == 0){
      return changes;
    }
    List<Change> consolidated = new ArrayList<>();
    int ctr = 1;
    Change ongoing = changes.get(0);
    for(Change current : changes.subList(1,changes.size())){
      if(current.location == ongoing.location+ongoing.before.length()){
        ongoing = new Change(ongoing.before + current.before, ongoing.after + current.after, ongoing.location);
      } else {
        consolidated.add(ongoing);
        ongoing = current;
      }
    }
    consolidated.add(ongoing);
    return consolidated;
  }

  public static List<Integer> getChangeLocations(List<Change> changes) {
    return changes.stream().map(c -> c.location).collect(Collectors.toList());
  }

  // get single character changes and locations
  public static List<Change> getSingleChanges(String before, String after){
    int len = before.length();
    ArrayList<Change> changes = new ArrayList<>();
    for(int ctr = 0; ctr < len; ctr++){
      if(before.charAt(ctr) != after.charAt(ctr)){
        Change c = new Change(before.charAt(ctr)+"", after.charAt(ctr)+"", ctr);
        changes.add(c);
      }
    }
    return changes;
  }

  // get maximum key in a counting hashmap
  public static <E> E argMax(HashMap<E, Integer> counts){
    if(counts.size() == 0){
      return null;
    }
    Map.Entry<E, Integer> maxEntry = null;
    for (Map.Entry<E, Integer> entry : counts.entrySet()) {
        if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
            maxEntry = entry;
        }
    }
    return maxEntry.getKey();
  }

  // format pattern
  public static String formatPattern(String pattern){
    String f = "";
    for(int i = 0; i<pattern.length()-1; i++){
      f+=""+pattern.charAt(i);
      f+=";";
    }
    f+=pattern.charAt(pattern.length()-1);
    return f;
  }

  public static List<Rule> generateRules(List<Change> changes) {
    List<Rule> rules = new ArrayList<Rule>();
    for(Change change:changes) {
      boolean matchesRule = false;
      for(Rule rule: rules) {
        if(change.after.equals(rule.after)) {
          matchesRule = true;
          for(int i = 0; i<rule.before.length; i++) {
            if(rule.before[i].indexOf(change.before.charAt(i)) < 0) {
              //rule missing this possibility -- i.e. given gat -> ccc, if the rule was previously ac;a;t -> ccc make it gac;a;t -> ccc
              rule.before[i] += change.before.charAt(i);
            }
          }
          break;
        }
      }
      if(!matchesRule) {
        //Create new rule
        String[] arr = new String[change.before.length()];
        for(int i = 0; i<change.before.length(); i++) {
          arr[i] = "" + change.before.charAt(i);
        }
        rules.add(new Rule(arr, change.after));
      }
    }
    return rules;
  }
  
  private static Set<Character> getIndexes(String genome, char base) {
	Set<Character> results = new HashSet<Character>();
	char[] bases = genome.toCharArray();
	for(int i = 0; i < bases.length; i++) {
		if(bases[i] == base) {
			results.add((char)(i + '0'));
		}
	}
	return results;
  }
  
  private static Set<Character> singlePossibleChar(Map<Character, Set<Character>> indexSets, char base) {
	  Set<Character> indexes = new HashSet<Character>(indexSets.get(base));
	  indexes.add(base);
	  return indexes;
  }

  public static Set<Character>[] toSets(String before, String after) {
	char[] pool = {'a', 'c', 'g', 't'};
	Map<Character, Set<Character>> indexSets = new HashMap<>();
	for(char c: pool) {
		indexSets.put(c, getIndexes(before, c));
	}
	Set<Character>[] results = new Set[10];
	for(int i = 0; i < 10; i++) {
		results[i] = singlePossibleChar(indexSets, after.charAt(i));
	}
	return results;
  }
  
  private static List<Character> findSameInTwoSets(Set<Character> set1, Set<Character> set2) {
	  List<Character> res = new ArrayList<Character>();
	  for(char c: set1) {
		  if(set2.contains(c))
			  res.add(c);
	  }
	  return res;
  }
  
  private static void backtracking(List<List<Character>> possibleBases, List<String> results, StringBuilder temp) {
	  if(temp.length() == possibleBases.size()) {
		  results.add(temp.toString());
		  return;
	  }
	  for(char c: possibleBases.get(temp.length())) {
		  temp.append(c);
		  backtracking(possibleBases, results, temp);
		  temp.deleteCharAt(temp.length()-1);
	  }
  }
  
  public static List<String> compareTwoMutations(Set<Character>[] mutation1, Set<Character>[] mutation2) {
	  List<List<Character>> possibleBases = new ArrayList<List<Character>>();
	  for(int i = 0; i < 10; i++) {
		  List<Character> possibleBase = findSameInTwoSets(mutation1[i], mutation2[i]);
		  if(possibleBase.size() == 0)
			  break;
		  possibleBases.add(possibleBase);
	  }
	  List<String> results = new ArrayList<String>();
	  backtracking(possibleBases, results, new StringBuilder());
	  return results;
  }
}

class Rule {
  public String[] before;
  public String after;

  public Rule(String[] b, String a) {
    this.before = b;
    this.after = a;
  }
  public String formatBefore() {
    String rule = "";
    for (String position : before) {
      rule+=position;
      rule += ";";
    }
    return rule.substring(0, rule.length()-1);
  }
  public String toString() {
    String rule = "";
    for (String position : before) {
      rule+=position;
      rule += ";";
    }
    rule += "@" + after;
    return rule;
  }
  
}
// ADT for a change type
class Change implements Comparable<Change>{
  public String before, after;
  public int location;
  static String delimiter = " => ";

  public Change(String before, String after, int location){
    this.before = before;
    this.after = after;
    this.location = location;
  }

  public String getChange(){
    return this.before + delimiter + this.after;
  }

  @Override
  public String toString(){
    return getChange() + " @ " + this.location;
  }

  @Override
  public int compareTo(Change other){
    return this.location - other.location;
  }

  public static Change fromChangeString(String cs){
    String[] components = cs.split(delimiter);
    return new Change(components[0], components[1], -1);
  }

}
