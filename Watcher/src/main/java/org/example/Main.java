package org.example;

public class Main {
    public static void main(String[] args) {
        try {
            FileWatch fileWatch = new FileWatch();
            fileWatch.create();
            fileWatch.run();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

}
