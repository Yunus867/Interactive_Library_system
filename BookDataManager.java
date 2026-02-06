package bcu.cmp5332.librarysystem.data;

import bcu.cmp5332.librarysystem.model.Book;
import bcu.cmp5332.librarysystem.model.Library;
import bcu.cmp5332.librarysystem.main.LibraryException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class BookDataManager implements DataManager {

    private static final String RESOURCE = "resources/data/books.txt";

    @Override
    public void loadData(Library library) throws IOException, LibraryException {
        File file = new File(RESOURCE);
        if (!file.exists()) {
            return; // first run
        }

        try (Scanner sc = new Scanner(file)) {
            int lineNo = 1;

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) {
                    lineNo++;
                    continue;
                }

                String[] p = line.split(SEPARATOR, -1);

                try {
                    int id = Integer.parseInt(p[0]);
                    String title = p[1];
                    String author = p[2];
                    String publicationYear = p[3];

                    // publisher 
                    String publisher = (p.length > 4) ? p[4] : "";

                    // deleted flag 
                    boolean deleted = false;
                    if (p.length > 5 && !p[5].isBlank()) {
                        deleted = Boolean.parseBoolean(p[5]);
                    }

                    Book book = new Book(id, title, author, publicationYear, publisher);
                    book.setDeleted(deleted);

                    library.addBook(book);

                } catch (Exception ex) {
                    throw new LibraryException("Unable to parse book on line " + lineNo + ": " + line);
                }

                lineNo++;
            }
        }
    }

    @Override
    public void storeData(Library library) throws IOException {
        File file = new File(RESOURCE);
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            for (Book book : library.getBooks()) {
                out.print(book.getId() + SEPARATOR);
                out.print(book.getTitle() + SEPARATOR);
                out.print(book.getAuthor() + SEPARATOR);
                out.print(book.getPublicationYear() + SEPARATOR);

                // publisher
                out.print((book.getPublisher() == null ? "" : book.getPublisher()) + SEPARATOR);

                // deleted flag 
                out.print(book.isDeleted());

                out.println();
            }
        }
    }
}