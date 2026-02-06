package bcu.cmp5332.librarysystem.main;

import bcu.cmp5332.librarysystem.commands.AddBook;
import bcu.cmp5332.librarysystem.commands.AddPatron;
import bcu.cmp5332.librarysystem.commands.BorrowBook;
import bcu.cmp5332.librarysystem.commands.Command;
import bcu.cmp5332.librarysystem.commands.DeleteBook;
import bcu.cmp5332.librarysystem.commands.DeletePatron;
import bcu.cmp5332.librarysystem.commands.Help;
import bcu.cmp5332.librarysystem.commands.ListBooks;
import bcu.cmp5332.librarysystem.commands.ListPatrons;
import bcu.cmp5332.librarysystem.commands.LoadGUI;
import bcu.cmp5332.librarysystem.commands.RenewBook;
import bcu.cmp5332.librarysystem.commands.ReturnBook;
import bcu.cmp5332.librarysystem.commands.ShowBook;
import bcu.cmp5332.librarysystem.commands.ShowPatron;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandParser {

    public static Command parse(String line) throws IOException, LibraryException {

        
        line = (line == null) ? "" : line.trim();
        if (line.isEmpty()) {
            throw new LibraryException("Invalid command.");
        }

        try {
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            /* ===============================
               Commands with NO arguments
               =============================== */
            if (parts.length == 1) {
                switch (cmd) {
                    case "listbooks":
                        return new ListBooks();

                    case "listpatrons":
                        return new ListPatrons();

                    case "addbook": {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                        System.out.print("Title: ");
                        String title = br.readLine();

                        System.out.print("Author: ");
                        String author = br.readLine();

                        System.out.print("Publication Year: ");
                        String publicationYear = br.readLine();

                        System.out.print("Publisher: ");
                        String publisher = br.readLine();

                        return new AddBook(title, author, publicationYear, publisher);
                    }

                    case "addpatron": {
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                        System.out.print("Name: ");
                        String name = br.readLine();

                        System.out.print("Phone: ");
                        String phone = br.readLine();

                        System.out.print("Email: ");
                        String email = br.readLine();

                        return new AddPatron(name, phone, email);
                    }

                    case "loadgui":
                        return new LoadGUI();

                    case "help":
                        return new Help();
                }
            }

            /* ===============================
               Commands with ONE argument
               =============================== */
            if (parts.length == 2) {
                int id = Integer.parseInt(parts[1]);

                switch (cmd) {
                    case "showbook":
                        return new ShowBook(id);

                    case "showpatron":
                        return new ShowPatron(id);

                    case "deletebook":
                        return new DeleteBook(id);

                    case "deletepatron":
                        return new DeletePatron(id);
                }
            }

            /* ===============================
               Commands with TWO arguments
               =============================== */
            if (parts.length == 3) {
                int patronId = Integer.parseInt(parts[1]);
                int bookId = Integer.parseInt(parts[2]);

                switch (cmd) {
                    case "borrow":
                        return new BorrowBook(patronId, bookId);

                    case "return":
                        return new ReturnBook(patronId, bookId);

                    case "renew":
                        return new RenewBook(patronId, bookId);
                }
            }

        } catch (NumberFormatException ex) {
            throw new LibraryException(
                "Invalid number in command.\n" +
                "Usage:\n" +
                "  showbook <bookId>\n" +
                "  showpatron <patronId>\n" +
                "  deletebook <bookId>\n" +
                "  deletepatron <patronId>\n" +
                "  borrow <patronId> <bookId>\n" +
                "  renew <patronId> <bookId>\n" +
                "  return <patronId> <bookId>"
            );
        }

        throw new LibraryException("Invalid command.");
    }
}