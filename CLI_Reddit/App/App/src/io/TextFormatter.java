package io;

public class TextFormatter {

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    public static String color(String text, String color) {
        return color + text + RESET;
    }

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String error(String text) {
        return RED + text + RESET;
    }

    public static String success(String text) {
        return GREEN + text + RESET;
    }

    public static String warning(String text) {
        return YELLOW + text + RESET;
    }

    public static String header(String text) {
        return BOLD + BLUE + text + RESET;
    }

    public static String separator(int length) {
        return "─".repeat(Math.max(0, length));
    }

}
