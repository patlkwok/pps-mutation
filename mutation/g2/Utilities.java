package mutation.g2;
import java.lang.*;
import java.util.*;

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
