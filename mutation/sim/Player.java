package mutation.sim;

public abstract class Player {

    // Returns a mutagen
    // If you didn't make a correct guess in the console, we'll calculate the score of your provided mutagen
    public abstract Mutagen Play(Console console);
}