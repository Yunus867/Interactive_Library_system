package bcu.cmp5332.librarysystem.main;

import bcu.cmp5332.librarysystem.data.LibraryData;
import bcu.cmp5332.librarysystem.commands.Command;
import bcu.cmp5332.librarysystem.model.Library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) throws IOException, LibraryException {

        // Load from file storage
        Library library = LibraryData.load();

        // Helpful for debugging: shows where files will be read/written relative to
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        // If the user stops the run, this hook will try to save anyway
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LibraryData.store(library);
                System.out.println("\n[Auto-saved on shutdown]");
            } catch (Exception e) {
                System.out.println("\n[Auto-save failed: " + e.getMessage() + "]");
            }
        }));

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Library system");
        System.out.println("Enter 'help' to see a list of available commands.");

        try {
            while (true) {
                System.out.print("> ");
                String line = br.readLine();

                // If input stream closed
                if (line == null) {
                    break;
                }

                line = line.trim();

                // Ignore blank lines
                if (line.isEmpty()) {
                    continue;
                }

                // Exit command
                if (line.equalsIgnoreCase("exit")) {
                    break;
                }

                try {
                    Command command = CommandParser.parse(line);
                    command.execute(library, LocalDate.now());
                } catch (LibraryException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } finally {
            // Always store when leaving main loop normally
            LibraryData.store(library);
            System.out.println("Saved. Goodbye.");
        }

        System.exit(0);
    }
}