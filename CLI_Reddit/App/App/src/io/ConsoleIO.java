package io;

import java.util.Scanner;

public class ConsoleIO implements StringReader, IntReader, OutputWriter {
    private final Scanner scanner;
    private OutputWriter outputWriter;
    public ConsoleIO() {
        this.scanner = new Scanner(System.in);
        this.outputWriter = this;
    }

            @Override
            public String readString(String prompt) {
                outputWriter.write(prompt);
                return scanner.nextLine();
            }

            @Override
            public int readInt(String prompt) {
                System.out.println(prompt);
                while (!scanner.hasNextInt()) {
                    outputWriter.write("Error: Must be a number. Try again:");
                    scanner.nextLine();
                }
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            }

            @Override
            public void write(String message) {
                System.out.println(message);
            }
}
