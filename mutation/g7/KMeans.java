package mutation.g7;
import java.util.LinkedList;
import java.util.List;
import java.util.*;

/*
    Adapted from https://www.baeldung.com/java-k-means-clustering-algorithm
*/
public class KMeans{

    private static LinkedList<Integer> mutated_bases; 

    public KMeans(String genome, String mutated){
        mutated_bases = new LinkedList<>(); 
        for (int i = 0; i < genome.length(); i++){
            if (genome.charAt(i)!=mutated.charAt(i)) mutated_bases.add(i);
        }
    }

    public Map<Integer, LinkedList<Integer>> fit(int k,int maxIterations) {
    
        // generate random centroids (originally equidistant accross genome)
        Set<Integer> centroids = initializeCentroids(k);
        Map<Integer, LinkedList<Integer>> clusters = new HashMap<>();
        
        Map<Integer, LinkedList<Integer>> lastState = new HashMap<>();
    
        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;
    
            // in each iteration we should find the nearest centroid for each mutated base
            for (Integer index : mutated_bases) {
                int centroid = nearestCentroid(index, centroids);
                // Assign current index to closest cluster 
                LinkedList<Integer> current = clusters.containsKey(centroid) ? clusters.get(centroid) : new LinkedList<>(); 
                current.add(index);
                clusters.put(centroid, current);
            }
            
            
            // if the assignments do not change (and found k clusters), then the algorithm terminates
            boolean shouldTerminate = (isLastIteration || clusters.equals(lastState)) && clusters.size()==k;
            lastState = clusters;
            if (shouldTerminate) { 
                break; 
            }
    
            // at the end of each iteration we should relocate the centroids
            centroids = relocate(clusters, centroids);

            // if we have fewer centroids than k, need to add centroid to larger clusters 
            // we do this one cluster at a time 
            if(centroids.size()< k){
                centroids = splitCluster(clusters, centroids, k);
            }

            clusters = new HashMap<>();
        }
        return lastState;
    }

    public static Set<Integer> initializeCentroids(int k){
        // Centroids initially set to random distances accross the genome 
        Set<Integer> centroids = new HashSet<>(); 
        for (int i = 0; i < 1000 && centroids.size()<k; i+=1000/k)
            centroids.add(i);
        return centroids;
    }
   
    private static int nearestCentroid(Integer index, Set<Integer> centroids) {
        // Nearest centroid identified by distance from base on genome 
        double minimumDistance = Integer.MAX_VALUE;
        int nearest = -1;
     
        for (Integer centroid : centroids) {
            int currentDistance = Math.abs(centroid - index); 
            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }
     
        return nearest;
    }

    private static Set<Integer> relocate(Map<Integer, LinkedList<Integer>> clusters , Set<Integer> centroids) {
        // Move identified centroids to avg of base indices in their cluster 
        Set<Integer> updated_centroids = new HashSet<>(); 
        for (Integer centroid : centroids){
            if(clusters.containsKey(centroid)){  
                LinkedList<Integer> elements = clusters.get(centroid);
                int total = 0; 
                int count = 0; 
                for (int index: elements){
                    total += index; 
                    count ++; 
                }
                updated_centroids.add(total/count);
            }   
        }
        return updated_centroids;
    }


    private static Set<Integer> splitCluster(Map<Integer,LinkedList<Integer>> cluster, Set<Integer> centroids, int k){
        // Split centroid with largest distance between any two bases 
        int max_centroid = -1; 
        int new_c1 = -1; 
        int new_c2 = -1; 
        for (Integer centroid : cluster.keySet()){
            int min = 1000; 
            int max = 0; 
            for(Integer base: cluster.get(centroid)){
                if (base < min) min = base; 
                if (max < base) max = base; 
            }
            if (max-min > new_c2-new_c1){
                max_centroid = centroid;
                new_c1 = min; 
                new_c2 = max; 
            }
        }
        centroids.remove(max_centroid);
        centroids.add(new_c1);
        centroids.add(new_c2);
        return centroids; 
    }
}