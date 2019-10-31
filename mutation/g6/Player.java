package mutation.g6;

import java.util.*;
import mutation.sim.Console;
import mutation.sim.Mutagen;

public class Player extends mutation.sim.Player {
    private Random random;
    private int[] beforeCounter;
    private int[] afterCounter;
    private Map<Character, Integer> hash;
    private Map<Integer, Character> antiHash;
    
    public Player() {
        random = new Random();
        hash = new HashMap<>();
        hash.put('a', 0); hash.put('c', 1); hash.put('g', 2); hash.put('t', 3);
        antiHash = new HashMap<>();
        antiHash.put(0, 'a'); antiHash.put(1, 'c'); antiHash.put(2, 'g'); antiHash.put(3, 't');
    }

    private String randomString() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++ i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        Mutagen result = new Mutagen();
        //result.add("a;c;c", "att");
        //result.add("g;c;c", "gtt");
        for (int i = 0; i < 100; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            char[] input = genome.toCharArray();
            char[] output = mutated.toCharArray();
            Element[] diff = checkDifference(input, output);
            result = getNaive(diff);
            console.Guess(result);
        }
        return result;
    }

    public Mutagen getNaive(Element[] diff) {
        Mutagen result = new Mutagen();
        int[][] set = new int[4][4];

        for(Element e : diff) {
            if(e.isMutated()) {
                set[hash.get(e.getOG())][hash.get(e.getAfter())]++;
            }
        }

        /*for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(set[i][j] > 0) {
                    result.add(Character.toString(antiHash.get(i)), Character.toString(antiHash.get(j)));
                }
            }
        }*/

        List<Window> winList = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
        	if(diff[i].isMutated()) {
        		Window temp = new Window(i, i+9, diff);
        		winList.add(temp);
        		i+=10;
        	}
        }

        Window temp = winList.get(0);
        Set<String> left = new HashSet<>();
        int length = getLength(winList);
        length = length*2 - 1;
        String output = "";
        for(Window w: winList) {
            String t = w.getOG();
            System.out.println(length + " " + t.length());
            if(t.length() == length) left.add(w.getOG());
        }

        if(left.size() == 1) {
            result.add(temp.getOG(), temp.getAfter());
            return result;
        }
        
        else {

            for(int i = 0; i < length; i += 2) {
                Set<Character> c = new HashSet<>();
                for(String s: left) {
                    System.out.println(s);
                    c.add(s.charAt(i));
                }
                for(char curr : c) {
                    System.out.println(curr+".");
                    output = output.concat(Character.toString(curr));
                }
                if(i != length - 1) output = output.concat(";");
            }
        }
        result.add(output, temp.getAfter());
        return result;
    }

    public int getLength(List<Window> winList) {
        int length = 11;
        for(Window w: winList) {
            if(w.getMutagenCount() < length) length = w.getMutagenCount();
        }
        return length;
    }

    public Element[] checkDifference(char[] input, char[] output) {
        Element[] diff = new Element[1000];
        beforeCounter = new int[4];
        afterCounter = new int[4];
        for(int i = 0; i < input.length; i++) {
            if(input[i] != output[i]) {
                diff[i] = new Element(true, input[i], output[i]);
                beforeCounter[hash.get(input[i])]++;
                afterCounter[hash.get(output[i])]++;
            }
            else diff[i] = new Element(input[i]);
        }
        return diff;
    }

    public class Element {
        private boolean mutated;
        private char og;
        private char after;

        public Element() {
            mutated = false;
        }

        public Element(char og) {
            mutated = false;
            this.og = og;
            this.after = og;
        }

        public Element(boolean mutated, char og, char after) {
            this.mutated = mutated;
            this.og = og;
            this.after = after;
        }

        public boolean isMutated() {
            return mutated;
        }

        public char getOG() {
            return og;
        }

        public char getAfter() {
            return after;
        }

        public void putOG(char og) {
            this.og = og;
        }

        public void putAfter(char after) {
            this.after = after;
        }
    }

    public class Window {
    	public int start;
    	public int end;
    	public int mutagenCount;
    	public Element[] window;

    	public Window() {

    	}

    	public Window(int left, int right, Element[] input) {
    		start = left;
    		end = right;
    		mutagenCount = 0;
    		window = new Element[10];
    		int index = 0;
    		for(int i = left; i <= right; i++) {
    			window[index++] = input[i];
    			if(input[i].isMutated()) {
    				mutagenCount++;
    			}
    		}
    	}

    	public Element[] getWindow() {
    		return window;
    	}

    	public int getMutagenCount() {
    		return mutagenCount;
    	}

    	public boolean isSameLoc(Window temp) {
    		for(int i = 0; i < 10; i++) {
    			if(temp.window[i].isMutated() && this.window[i].isMutated()) {
    				continue;
    				
    			} else return false;
    		}
    		return true;
    	}

        public String getAfter(){
            String temp = "";
            for(int i = 0; i < 10; i++) {
                if(window[i].isMutated()) {
                    temp = temp.concat(Character.toString(window[i].getAfter()));
                }
            }
            return temp;
        }

        public String getOG(){
            String temp = "";
            boolean first = true;
            for(int i = 0; i < 10; i++) {
                if(window[i].isMutated()) {
                    if(first) {
                        temp = temp.concat(Character.toString(window[i].getOG()));
                        first = false;
                    }
                    else {
                        temp = temp.concat(";").concat(Character.toString(window[i].getOG()));
                    }
                }
            }

            return temp;
        }
    }


}


