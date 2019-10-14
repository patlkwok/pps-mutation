package mutation.sim;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.awt.Desktop;

// This class wraps the Mutagen with limited usage
public class Console {

    public Console(int limit, int m, Mutagen mutagen, boolean gui, HTTPServer server) {
        this.mutagen = mutagen;
        this.limit = limit;
        this.m = m;
        this.gui = gui;
        this.server = server;
    }

    public String Mutate(String genome) {
        if (counter >= limit || genome.length() != 1000)
            return "";
        ++ counter;
        return mutagen.Mutate(genome, m);
    }

    public boolean Guess(Mutagen other) {
        correct = mutagen.equals(other);
        return correct;
    }

    public int getCounter() {
        return counter;
    }

    public boolean isCorrect() {
        return correct;
    }

    private int getNumberOfMutations() {
        return mutagen.getNumberOfMutations();
    }

    private int getNumberOfExperiments() {
        return counter;
    }


    private void sendGUI(HTTPServer server, String content) {
        if (server == null) return;
        String path = null;
        for (; ; ) {
            for (; ; ) {
                try {
                    path = server.request();
                    break;
                } catch (IOException e) {
                    Log.record("HTTP request error " + e.getMessage());
                }
            }
            if (path.equals("data.txt")) {
                try {
                    server.reply(content);
                } catch (IOException e) {
                    Log.record("HTTP dynamic reply error " + e.getMessage());
                }
                return;
            }
            if (path.equals("")) path = "webpage.html";
            else if (!Character.isLetter(path.charAt(0))) {
                Log.record("Potentially malicious HTTP request \"" + path + "\"");
                break;
            }

            File file = new File("statics" + File.separator + path);
            if (file == null) {
                Log.record("Unknown HTTP request \"" + path + "\"");
            } else {
                try {
                    server.reply(file);
                } catch (IOException e) {
                    Log.record("HTTP static reply error " + e.getMessage());
                }
            }
        }
    }

    private int counter = 0, limit, m = 1;
    private boolean correct = false, gui = false;
    private Mutagen mutagen;
    private HTTPServer server;
}