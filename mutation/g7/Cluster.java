package mutation.g7;
import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

import java.util.*;

public class Cluster{

    private static LinkedList<Integer> mutated_bases; 
    private String mutated; 

    public Cluster(String genome, String mutated){
        mutated_bases = new LinkedList<>(); 
        for (int i = 0; i < genome.length(); i++){
            if (genome.charAt(i)!=mutated.charAt(i)) mutated_bases.add(i);
        }
        this.mutated = mutated; 
    }

    public Map<Integer, LinkedList<Integer>> findWindows(int k) {
        Map<Integer, LinkedList<Integer>> clusters = new HashMap<>();
        int[][] distances = new int[mutated_bases.size()][mutated_bases.size()];
        for (int i =0; i < mutated_bases.size(); i++){
            LinkedList<Integer> initial = new LinkedList<>();
            initial.add(i);
            clusters.put(i, initial);
            for (int j=0; j < mutated_bases.size(); j++){
                distances[i][j] = Math.abs(mutated_bases.get(i) - mutated_bases.get(j));
            }
        }
        while (clusters.size() > k){
            Pair<Integer, Integer> min_pair = locateMin(distances, clusters);
            mergeClusters(min_pair.getKey(), min_pair.getValue(), distances, clusters);
        }
       
        Map<Integer, LinkedList<Integer>> correct_indices = new HashMap<>();
        for(int index : clusters.keySet()){
            LinkedList<Integer> temp = new LinkedList<>(); 
            for (int item : clusters.get(index)){
                temp.add(mutated_bases.get(item));
            }
            correct_indices.put(mutated_bases.get(index), temp);
        }
        return correct_indices;
    }
    
    private static void mergeClusters(int i, int j, int[][] distances, Map<Integer, LinkedList<Integer>> clusters){
        LinkedList<Integer> i_clust = clusters.get(i);
        LinkedList<Integer> j_clust = clusters.get(j);
        for (int item : j_clust){
            i_clust.add(item);
        }
        for (int a = 0; a < distances[0].length; a++){
            int new_dist = distances[i][a] < distances[j][a] ? distances[i][a] : distances[j][a];
            if (a == i) continue; 
            distances[i][a] = new_dist;
            distances[a][i] = new_dist;
            distances[j][a] = 0; 
            distances[a][j] = 0;             
        } 
        clusters.remove(j);
        clusters.put(i, i_clust);
    }

    private static Pair<Integer, Integer> locateMin(int[][] distances, Map<Integer, LinkedList<Integer>> clusters){
        int min = 1000; 
        Pair<Integer, Integer> min_pair = new Pair<>(0,0);
        HashMap<Pair<Integer, Integer>, Integer> options = new HashMap<>();
        for (int i =0; i < mutated_bases.size(); i++){
            for (int j=0; j < mutated_bases.size(); j++){
                if (distances[i][j] <= min && distances[i][j] != 0){
                    min = distances[i][j];
                    min_pair = new Pair<>(i, j);
                    if (distances[i][j] == min)
                        options.put(min_pair, min);
                }
            }
        }
        // Return the min pair which merges the smallest clusters 
        // idea is that it'll merge windows where no overlap occured first (b/c shorter)
        int cluster_size = Integer.MAX_VALUE; 
        for (Pair<Integer, Integer> pair : options.keySet()){
            if (options.get(pair) == min){
                int size = clusters.get(pair.getKey()).size() + clusters.get(pair.getValue()).size();
                if (size < cluster_size){
                    cluster_size = size; 
                    min_pair = pair; 
                }
            }
        }
      
        return min_pair;
    }

}