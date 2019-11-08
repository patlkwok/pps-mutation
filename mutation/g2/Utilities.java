package mutation.g2;
import java.lang.*;
import java.util.*;
import java.util.stream.Collectors;

public class Utilities {
  static String noSignal = "acgt";

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
      startingPosition = 1000 + startingPosition; //wrap around
    }

    for(int i = 0; i <= lookupLength; i++){
      int start = (startingPosition + i) % 1000;
      String beforeContext = "";
      String afterContext = "";
      for(int j = 0; j < 10; j++){
        int current = (start+j)%1000;
        beforeContext += ""+before.charAt(current);
        afterContext += ""+after.charAt(current);
      }

      // weird bug bypass for now
      if(afterContext.indexOf(c.after) == -1 || afterContext == null){
        // System.out.println(c +","+ before +","+ after +","+ i +","+ start);
        System.out.println("lookupLength "+lookupLength);
        System.out.println("startingPosition "+startingPosition);
        System.out.println("changeLength "+changeLength);
        System.out.println("Location "+c.location);
        System.out.println("Start "+start);
        System.out.println("Before "+before);
        System.out.println("After, "+after);
        System.out.println("AfterContext "+afterContext);
        System.exit(9);
        continue;
      }
      // weird bug bypass for now
      if(beforeContext.equals(null)){
        System.exit(0);
      }
      if(beforeContext.indexOf(c.before) == -1 || beforeContext == null){
        System.out.println("lookupLength "+lookupLength);
        System.out.println("startingPosition "+startingPosition);
        System.out.println("changeLength "+changeLength);
        System.out.println("Location "+c.location);
        System.out.println("Start "+start);
        System.out.println("Before "+before);
        System.out.println("After, "+after);
        System.out.println("AfterContext "+afterContext);
        System.exit(9);
        continue;
      }
      c.beforeContext.put((i-lookupLength), beforeContext);
      c.afterContext.put((i-lookupLength), afterContext);
    }
    return c;
  }


  // best window is the one with the earliest nondisjunction and ties broken by least ACGTs;
  public static int bestWindow(HashMap<Integer, String> windows){

    Integer best = -1;
    int bestND = 10;
    int bestTrailingACGT = 10;
    for(Integer loc: new ArrayList<Integer>(windows.keySet())){
      String[] window = windows.get(loc).split(";");

      //find earliest nondisjunctive element
      int earliestNondisjunction = 10;
      for(int i = 0; i < window.length; i++){
        if(window[i].length() < 4){
          earliestNondisjunction = i;
          break;
        }
      }

      //find trailing acgt's
      int trailingACGTs = 0;
      for(int i = 9; i >= 0; i--){
        if(!window[i].equals(noSignal)){
          break;
        }
        trailingACGTs+=1;
      }

      if(earliestNondisjunction < bestND){
        best = loc;
        bestND = earliestNondisjunction;
        bestTrailingACGT = trailingACGTs;
      }

      if(earliestNondisjunction == bestND && trailingACGTs < bestTrailingACGT){
        best = loc;
        bestND = earliestNondisjunction;
        bestTrailingACGT = trailingACGTs;
      }
    }
    return best;
  }

  // get last non nondisjunctive element idx
  public static int getLastNondisjunctive(String swindow){
    int lastNd = 0;
    String[] window = swindow.split(";");
    for(int i = 0; i < window.length; i++){
      if(window[i].length() < 4){
        lastNd = i;
      }
    }
    return lastNd;
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
        String beforeGenome = ongoing.beforeGenome;
        String afterGenome = ongoing.afterGenome;
        ongoing = new Change(ongoing.before + current.before, ongoing.after + current.after, ongoing.location);
        ongoing.beforeGenome = beforeGenome;
        ongoing.afterGenome = afterGenome;

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
        c.beforeGenome = before;
        c.afterGenome = after;
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

  // pivot data by after artifact
  public static HashMap<String, List<Change>> sortByAfterArtifact(List<Change> changes){
    HashMap<String, List<Change>> actionInstances = new HashMap<>();
    //sort experimental data by after artifact
    for(Change c: changes){
      String after = c.after;
      if(!actionInstances.containsKey(after)){
        actionInstances.put(after, new ArrayList<Change>());
      }
      actionInstances.get(after).add(c);
    }
    return actionInstances;
  }

  // get hash of after contexts by reference point
  public static HashMap<Integer, List<String>> getAfterContextByReferencePoint(List<Change> relatedChanges){
    HashMap<Integer, List<String>> contextsByReferencePoint = new HashMap<>();
    List<Integer> contextPoints = new ArrayList<Integer>(relatedChanges.get(0).afterContext.keySet());
    for(Integer index: contextPoints){
      List<String> contextsAtIndex = new ArrayList<String>();
      for(Change c: relatedChanges){
        contextsAtIndex.add(c.afterContext.get(index));
      }
      contextsByReferencePoint.put(index, contextsAtIndex);
      // System.out.println(Utilities.collapseStrings(contextsByReferencePoint.get(index)));
    }
    return contextsByReferencePoint;
  }

  // get hash of before contexts by reference point
  public static HashMap<Integer, List<String>> getBeforeContextByReferencePoint(List<Change> relatedChanges){
    HashMap<Integer, List<String>> contextsByReferencePoint = new HashMap<>();
    List<Integer> contextPoints = new ArrayList<Integer>(relatedChanges.get(0).beforeContext.keySet());
    for(Integer index: contextPoints){
      List<String> contextsAtIndex = new ArrayList<String>();
      for(Change c: relatedChanges){
        contextsAtIndex.add(c.beforeContext.get(index));
      }
      contextsByReferencePoint.put(index, contextsAtIndex);
      // System.out.println(Utilities.collapseStrings(contextsByReferencePoint.get(index)));
    }
    return contextsByReferencePoint;
  }

  // get hash of collapsed contexts by reference point
  public static HashMap<Integer, String> collapseContexts(HashMap<Integer, List<String>> contexts){

    List<Integer> contextPoints = new ArrayList<Integer>(contexts.keySet());
    HashMap<Integer, String> collapsedContexts = new HashMap<>();
    for(Integer i: contextPoints){
      collapsedContexts.put(i, collapseStrings(contexts.get(i)));
    }
    return collapsedContexts;
  }

  // collapse a string to its barebones representation
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
      collapsed += ";"+sortString(chars);
    }
    return collapsed.substring(1);
  }

  public static boolean inString(String haystack, String needle){
    return (haystack.indexOf(needle) >= 0);
  }

  public static String sortString(String inputString) {
    char tempArray[] = inputString.toCharArray();
    Arrays.sort(tempArray);
    return new String(tempArray);
  }

  //
  public static List<Change> deduplicateListChanges(List<Change> changes){
    HashSet<String> seenBefores = new HashSet<String>();
    List<Change> dedup = new ArrayList<Change>();
    for(Change c: changes){
      if(seenBefores.contains(c.beforeGenome)){
        continue;
      }
      dedup.add(c);
      seenBefores.add(c.beforeGenome);
    }
    return dedup;
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

  // scoring function
  public static int getScore(HashMap<Integer, List<String>> patternContext, int bestWindowReference){
    return patternContext.get(bestWindowReference).size();
  }

// function to sort hashmap by values
public static HashMap<List<String>, Integer> sortByValue(HashMap<List<String>, Integer> hm)
{
    // Create a list from elements of HashMap
    List<Map.Entry<List<String>, Integer> > list =
           new LinkedList<Map.Entry<List<String>, Integer> >(hm.entrySet());

    // Sort the list
    Collections.sort(list, new Comparator<Map.Entry<List<String>, Integer> >() {
        public int compare(Map.Entry<List<String>, Integer> o1,
                           Map.Entry<List<String>, Integer> o2)
        {
            return -1*(o1.getValue()).compareTo(o2.getValue());
        }
    });

    // put data from sorted list to hashmap
    HashMap<List<String>, Integer> temp = new LinkedHashMap<List<String>, Integer>();
    for (Map.Entry<List<String>, Integer> aa : list) {
        temp.put(aa.getKey(), aa.getValue());
    }
    return temp;
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

class ActionComposite {
  HashMap<String, List<Change>> actionChanges;
  HashMap<String, Integer> actionCount;
}

class ActionComposite {
  HashMap<String, List<Change>> actionChanges;
  HashMap<String, Integer> actionCount;
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
  public String beforeGenome, afterGenome;
  public int location;
  public int pointOfInterest;
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
