package mutation.g7;
import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

import java.util.*;

public class Cluster{

    private static LinkedList<Integer> mutated_bases; 

    public Cluster(String genome, String mutated){
        mutated_bases = new LinkedList<>(); 
        for (int i = 0; i < genome.length(); i++){
            if (genome.charAt(i)!=mutated.charAt(i)) mutated_bases.add(i);
        }
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
            Pair<Integer, Integer> min_pair = locateMin(distances);
            mergeClusters(min_pair.getKey(), min_pair.getValue(), distances, clusters);
        }
        Map<Integer, LinkedList<Integer>> correct_indices = new HashMap<>();
        for(int index : clusters.keySet()){
            LinkedList<Integer> temp = new LinkedList<>(); 
            for (int item : clusters.get(index)){
                temp.add(mutated_bases.get(item));
            }
            correct_indices.put(mutated_bases.get(index), temp);
            System.out.println(mutated_bases.get(index));
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

    private static Pair<Integer, Integer> locateMin(int[][] distances){
        int min = 1000; 
        Pair<Integer, Integer> min_pair = new Pair<>(0,0);
        for (int i =0; i < mutated_bases.size(); i++){
            for (int j=0; j < mutated_bases.size(); j++){
                if (distances[i][j] < min && distances[i][j] != 0){
                    min = distances[i][j];
                    min_pair = new Pair<Integer,Integer>(i, j);
                }
            }
        }
        return min_pair;
    }

}