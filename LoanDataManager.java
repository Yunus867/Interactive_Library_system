package bcu.cmp5332.librarysystem.data;

import bcu.cmp5332.librarysystem.main.LibraryException;
import bcu.cmp5332.librarysystem.model.Book;
import bcu.cmp5332.librarysystem.model.Library;
import bcu.cmp5332.librarysystem.model.Loan;
import bcu.cmp5332.librarysystem.model.Patron;

import java.io.*;
import java.time.LocalDate;
import java.util.Scanner;

public class LoanDataManager implements DataManager {

    private static final String RESOURCE = "resources/data/loans.txt";

    /*
     * Format (each line):
     * patronId | bookId | startDate | dueDate | terminated | returnDate
     *
     * - terminated: true/false
     * - returnDate: blank if not terminated
     */
    @Override
    public void loadData(Library library) throws IOException, LibraryException {
        File file = new File(RESOURCE);
        if (!file.exists()) return;

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
                    int patronId = Integer.parseInt(p[0]);
                    int bookId = Integer.parseInt(p[1]);

                    LocalDate startDate = LocalDate.parse(p[2]);
                    LocalDate dueDate = LocalDate.parse(p[3]);

                    boolean terminated = false;
                    if (p.length > 4 && !p[4].isBlank()) {
                        terminated = Boolean.parseBoolean(p[4]);
                    }

                    LocalDate returnDate = null;
                    if (p.length > 5 && !p[5].isBlank()) {
                        returnDate = LocalDate.parse(p[5]);
                    }

                    Patron patron = library.getPatronByID(patronId);
                    Book book = library.getBookByID(bookId);

                    Loan loan = new Loan(patron, book, startDate, dueDate);
                    loan.setTerminated(terminated);
                    loan.setReturnDate(returnDate);

                    // Add to library history (ALL loans)
                    library.addLoan(loan);

                    // If this loan is the current active loan, link it to the book
                    if (!terminated) {
                        book.setLoan(loan);

                        // ensure patron has the book in their list (current relationship)
                        patron.addBook(book);
                    }

                } catch (Exception ex) {
                    throw new LibraryException("Unable to parse loan on line " + lineNo + ": " + line);
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
            for (Loan loan : library.getLoans()) {
                out.print(loan.getPatron().getId() + SEPARATOR);
                out.print(loan.getBook().getId() + SEPARATOR);
                out.print(loan.getStartDate() + SEPARATOR);
                out.print(loan.getDueDate() + SEPARATOR);
                out.print(loan.isTerminated() + SEPARATOR);

                // returnDate blank if not terminated
                out.print(loan.getReturnDate() == null ? "" : loan.getReturnDate().toString());

                out.println();
            }
        }
    }
}