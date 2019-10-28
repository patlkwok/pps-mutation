public class Simplify{
    
    public static void main(String[] args){
        String[] testMutagens = new String[]{"a;c;c@att", "g;c;c@gtt","c@t", "cg;at;gta@cat", "a;atcg;atcg;cg@2a31", "c@c","a;g@01","cg@c0" };
        for (String test : testMutagens){
            if(!simplifyMutagens(test)) System.out.println(test);

        }
    }

    /**
     * Helper method to identify redundant rules 
     * Could be extended to simplify redundant indices to their corresponding char or 
     * redundant chars to their corresponding indices 
     * @param mutagen
     * @return
     */
    private static Boolean simplifyMutagens(String mutagen){
        String[] arr = mutagen.split("@");
        String[] action = arr[0].split(";");
        String pattern = arr[1];
    
        // Identify if rule does nothing 
        Boolean redundant = true; 
        for(int i =0; i < pattern.length(); i++){
            Character curr_action = pattern.charAt(i);
            if(!Character.isDigit(curr_action)){
                String curr_match = action[i];
                if (curr_match.length()!=1 || curr_match.charAt(0) != curr_action){
                    redundant = false; 
                }
                else{
                    // Could extend here to replace value with index if remains same 
                }
            }
            else if ( Character.getNumericValue(curr_action) != i){
                redundant = false; 
            }
            else{
                // Could extend here to replace index with value originally there if desired
            }
        }
        return redundant;
    }
}
