package mutation.g7;

import java.util.*;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import javafx.util.Pair;

import java.util.Map;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;


public class Utils {
    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> findGreatest(Map<K, V> map, int n, boolean isMinHeap) {
        Comparator<? super Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1) {
                V v0 = e0.getValue();
                V v1 = e1.getValue();
                if (isMinHeap) return v0.compareTo(v1);
                return v1.compareTo(v0);
            }
        };
        PriorityQueue<Map.Entry<K, V>> highest = new PriorityQueue<>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n) {
                highest.poll();
            }
        }
        List<Map.Entry<K, V>> result = new ArrayList<>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }

    public static String randomString(int n) {
        Random random = new Random();
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < n; ++i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }

    public static String testString() {
        String result = "";
        while (result.length() < 1000) {
            result += "acgtg";
        }
        return result.substring(0, 1000);
    }

    public static void printWindows(Map<Integer, LinkedList<Integer>> mutations, String genome, String mutant) {
        Map<Integer, LinkedList<Integer>> sorted = mutations.entrySet().stream().sorted(comparingByKey()).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
        for (Integer i : sorted.keySet()) {
            // I is current centroid
            System.out.println("Mutation " + i);
            for (Integer j : sorted.get(i))
                System.out.println(genome.charAt(j) + " ->" + mutant.charAt(j));
        }
    }
}