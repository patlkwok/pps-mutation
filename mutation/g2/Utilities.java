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
    List<Change> consolidatedChanges = consolidateChanges(singleChanges);
    List<Change> withContext = generateContexts(consolidatedChanges, before, after);
    return withContext;
  }


  public static List<Change> generateContexts(List<Change> changes, String before, String after){
    List<Change> withContext = new ArrayList<Change>();
    for(Change change: changes){
      withContext.add(generateContext(change, before, after));
    }
    return withContext;
  }

  public static Change generateContext(Change c, String before, String after){
    int changeLength = c.before.length();
    int lookupLength = 10 - changeLength;
    int startingPosition = c.location - lookupLength;
    if(startingPosition < 0){
      startingPosition = 1000 - startingPosition; //wrap around
    }
    // System.out.println("lookupLength "+lookupLength);
    // System.out.println("startingPosition "+startingPosition);
    // System.out.println("changeLength "+changeLength);
    // System.out.println("Location "+c.location);
    for(int i = 0; i <= lookupLength; i++){
      int start = (startingPosition + i) % 1000;
      // System.out.println("Start "+start);
      String beforeContext = "";
      String afterContext = "";
      for(int j = 0; j < 10; j++){
        int current = (start+j)%1000;
        beforeContext += ""+before.charAt(current);
        afterContext += ""+after.charAt(current);
      }
      c.beforeContext.put((i-lookupLength), beforeContext);
      c.afterContext.put((i-lookupLength), afterContext);
    }
    return c;
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

  public static String collapseStrings(List<String> listStrings){
    String collapsed = "";
    int strLength = listStrings.get(0).length();
    for(int i = 0; i < strLength; i++){
      String chars = "";
      for(String c: listStrings){
        String currentChar = c.charAt(i)+"";
        if(!inString(chars,currentChar)){
          chars += currentChar;
        }
      }
      collapsed += ";"+chars;
    }
    return collapsed.substring(1);
  }

  public static boolean inString(String haystack, String needle){
    return (haystack.indexOf(needle) >= 0);
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


	// A list of before, after and location after each mutation
	public static List<Change> diff(String before, String after){
		List<Change> changeWindows = getChangeWindow(before, after);
		//    Collections.sort(changeWindows);
		return changeWindows;
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

	// find 10 change windows for each one base change
	public static List<Change> getChangeWindow(String before, String after){
		System.out.println("Start finding windows...");
		int len = before.length();
		Set<Integer> locations = new HashSet<>();
		List<Change> changes = new ArrayList<>();
		for(int ctr = 0; ctr < len; ctr++){
			if(before.charAt(ctr) != after.charAt(ctr)){
				for(int i = 0; i < 10; i++) {
					String b, a;
					int start = ctr-9+i;
					int end = ctr+i+1;
					if(start < 0) {
						b = before.substring(start+1000, 1000) + before.substring(0, end);
						a = after.substring(start+1000, 1000) + after.substring(0, end);
					}
					else if(end > 1000) {
						b = before.substring(start, 1000) + before.substring(0, end-1000);
						a = after.substring(start, 1000) + after.substring(0, end-1000);
					}
					else {
						b = before.substring(start, end);
						a = after.substring(start, end);
					}
					start = start < 0 ? start+1000 : start;
					if(!locations.contains(start)) {
						Change c = new Change(b, a, start);
						changes.add(c);
						locations.add(start);
//						System.out.println(c.before);
//						System.out.println(c.after);
//						System.out.println(c.location);
					}
				}
			}
		}
		System.out.println("Windows: " + changes.size());
		return changes;
	}

	// get maximum key in a counting hashmap
	public static <E> E argMax(HashMap<E, Integer> counts, Set<E> discard, int upperBound){
		if(counts.size() == 0){
			return null;
		}
		Map.Entry<E, Integer> maxEntry = null;
		for (Map.Entry<E, Integer> entry : counts.entrySet()) {
			if(entry.getValue() > upperBound) {
				discard.add(entry.getKey());
			}
			else if (!discard.contains(entry.getKey()) && (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)) {
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

	// find indexes of one base in a gene of length 10
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

	// find possible indexes of one base in the "after" string
	private static Set<Character> singlePossibleChar(Map<Character, Set<Character>> indexSets, char base) {
		Set<Character> indexes = new HashSet<Character>(indexSets.get(base));
		indexes.add(base);
		return indexes;
	}

	// convert "after" string to Mutation
	public static Mutation toSets(String before, String after, int location) {
		char[] pool = {'a', 'c', 'g', 't'};
		Map<Character, Set<Character>> indexSets = new HashMap<>();
		for(char c: pool) {
			indexSets.put(c, getIndexes(before, c));
		}
		Mutation results = new Mutation(location);
		for(int i = 0; i < 10; i++) {
			results.indexSets[i] = singlePossibleChar(indexSets, after.charAt(i));
		}
		return results;
	}

	// find intersection of two sets
	private static Set<Character> findSameInTwoSets(Set<Character> set1, Set<Character> set2) {
		Set<Character> res = new HashSet<Character>();
		for(char c: set1) {
			if(set2.contains(c))
				res.add(c);
		}
		return res;
	}

	// list all possible strings from the index sets
	private static void backtracking(List<List<Character>> possibleBases, List<String> results, StringBuilder temp, int minLen) {
		if(temp.length() == possibleBases.size()) {
			return;
		}
		for(char c: possibleBases.get(temp.length())) {
			temp.append(c);
			if(temp.length() > minLen)
				results.add(temp.toString());
			backtracking(possibleBases, results, temp, minLen);
			temp.deleteCharAt(temp.length()-1);
		}
	}

	// compare two mutations and output all possible combinations of these two
	public static List<String> compareTwoMutations(Mutation mutation1, Mutation mutation2, String before, String after) {
		int index = 0;
		while(before.charAt((mutation1.location+index)%1000) == after.charAt((mutation1.location+index)%1000) && before.charAt((mutation2.location+index)%1000) == after.charAt((mutation2.location+index)%1000))
			index++;
		List<List<Character>> possibleBases = new ArrayList<List<Character>>();
		for(int i = 0; i < 10; i++) {
			Set<Character> possibleBase = findSameInTwoSets(mutation1.indexSets[i], mutation2.indexSets[i]);
			if(possibleBase.size() == 0)
				break;
			possibleBases.add(new ArrayList<Character>(possibleBase));
		}
		List<String> results = new ArrayList<String>();
		if(possibleBases.size() == 0)
			return results;
		backtracking(possibleBases, results, new StringBuilder(), index);
		return results;
	}

	//  public static List<String> compareMultiMutations(List<Mutation> mutations) {
	//	  List<List<Character>> possibleBases = new ArrayList<List<Character>>();
	//
	//	  for(int i = 0; i < 10; i++) {
	//		  Set<Character> possibleBase = mutations.get(0).indexSets[i];
	//		  for(int j = 1; j < mutations.size(); j++) {
	//			  possibleBase.retainAll(mutations.get(j).indexSets[i]);
	//			  for(char c: possibleBase)
	//				  System.out.print(c);
	//			  System.out.println();
	//		  }
	//		  if(possibleBase.size() == 0)
	//			  break;
	//		  possibleBases.add(new ArrayList<Character>(possibleBase));
	//	  }
	//	  List<String> results = new ArrayList<String>();
	//	  if(possibleBases.size() == 0)
	//		  return results;
	//	  backtracking(possibleBases, results, new StringBuilder());
	//	  return results;
	//  }
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

class Mutation {
	public Set<Character>[] indexSets;
	public int location;
  public String before, after;
  public int location;
  public HashMap<Integer, String> beforeContext = new HashMap<Integer, String>();
  public HashMap<Integer, String> afterContext = new HashMap<Integer, String>();
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
    System.out.println("Before: "+beforeContext);
    System.out.println("After: "+afterContext);
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
	public Mutation(int loc) {
		indexSets = new Set[10];
		this.location = loc;
	}
}
