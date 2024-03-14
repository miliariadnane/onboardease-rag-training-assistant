package dev.nano.tptragbot;

import lombok.Setter;

public class Progress {
    private int current;
    
    @Setter
    private int total;

    public void increment() {
        current++;
    }

    public int getPercentage() {
        return (int) ((double) current / total * 100);
    }
}
