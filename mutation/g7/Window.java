package mutation.g7;

import java.util.*;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import javafx.util.Pair;

import java.util.Map;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;


public class Window {
    private static Integer maxMutagenLength = 10;
    private static Integer overlap = 9;

    private static void removeOverlap(Map<Integer, LinkedList<Integer>> mutations){
        // Get rid of overlap -- if two mutations are right next to eachother, ignore
        Map<Integer, Integer> start_indices = new HashMap<>();
        Map<Integer, Integer> end_indices = new HashMap<>();
        LinkedList<Integer> keys = new LinkedList<>(mutations.keySet());
        for(int centroid: keys){
            int cluster_size = mutations.get(centroid).size();
            Collections.sort(mutations.get(centroid));
            int start = mutations.get(centroid).get(0);
            int end = mutations.get(centroid).get(cluster_size-1);
            start_indices.put(start, centroid);
            end_indices.put(end, centroid);
        }
        for(int j =0; j < keys.size(); j++){
            int centroid = keys.get(j);
            if (!mutations.keySet().contains(centroid)) continue ;
            int start = mutations.get(centroid).get(0);
            int end = mutations.get(centroid).get(mutations.get(centroid).size()-1);
            if (end_indices.keySet().contains(start-1)){ //||
                mutations.remove(centroid);
                mutations.remove(start-1);
            }
            else if (start_indices.keySet().contains(end+1)){
                mutations.remove(centroid);
                mutations.remove(end+1);
            }
        }
    }


    public static Vector<Pair<Integer, Integer>> getWindows(String genome, String mutated, Integer numberOfMutations) {
        Cluster cluster = new Cluster(genome, mutated);
        Map<Integer, LinkedList<Integer>> mutations = cluster.findWindows(numberOfMutations);
        removeOverlap(mutations);

        // Collect the change windows
        Vector<Pair<Integer, Integer>> changeWindows = new Vector<Pair<Integer, Integer>>();

        Map<Integer, LinkedList<Integer>> sorted = mutations.entrySet().stream().sorted(comparingByKey()).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
        for (Integer centroid : sorted.keySet()) {
            int start = 1000;
            int finish = 0;
            for (Integer j : sorted.get(centroid)) {
                if (j < start) {
                    start = j;
                }
                if (j > finish) {
                    finish = j;
                }
            }
            changeWindows.add(new Pair<Integer, Integer>(overlap + start, overlap + finish));
        }

        return changeWindows;
    }

    public static Vector<Pair<Integer, Integer>> getPossibleWindows(Vector<Pair<Integer, Integer>> changeWindows, String genome, String mutated) {
        // Get the window sizes distribution and generate all possible windows
        int[] windowSizesCounts = new int[maxMutagenLength];
        Vector<Pair<Integer, Integer>> possibleWindows = new Vector<Pair<Integer, Integer>>();
        for (Pair<Integer, Integer> window : changeWindows) {
            int start = window.getKey();
            int finish = window.getValue();
            String before = genome.substring(start, finish + 1);
            String after = mutated.substring(start, finish + 1);
            if (before.length() - 1 >= maxMutagenLength) continue;
            int windowLength = finish - start + 1;
            windowSizesCounts[windowLength - 1]++;
            if (windowLength == maxMutagenLength) {
                possibleWindows.add(window);
            } else {
                for (int proposedWindowLength = windowLength; proposedWindowLength <= maxMutagenLength; proposedWindowLength++) {
                    int diff = proposedWindowLength - windowLength;
                    for (int offset = -diff; offset <= 0; offset++) {
                        int newStart = start + offset;
                        int newFinish = newStart + proposedWindowLength - 1;
                        possibleWindows.add(new Pair<Integer, Integer>(newStart, newFinish));
                    }
                }
            }
        }
        return possibleWindows;
    }
}