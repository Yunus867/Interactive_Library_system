package bcu.cmp5332.librarysystem.model;

import bcu.cmp5332.librarysystem.main.LibraryException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Patron {

    private int id;
    private String name;
    private String phone;
    private String email;

    // (70–79) Soft-delete flag
    private boolean deleted = false;

    // (70–79) Maximum books a patron can borrow
    private static final int MAX_LOANS = 3;

    private final List<Book> books = new ArrayList<>();

    // Backwards-compatible constructor
    public Patron(int id, String name, String phone) {
        this(id, name, phone, "");
    }

    // Constructor with email
    public Patron(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = (email == null) ? "" : email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = (email == null) ? "" : email; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public String getDetailsShort() {
        if (deleted) return null;
        return "Patron #" + id + " - " + name + " - " + phone;
    }

    public String getDetailsLong() {
        if (deleted) {
            return "Patron #" + id + "\n"
                    + "Name: " + name + "\n"
                    + "Status: Deleted/Hidden\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Patron #").append(id).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Phone: ").append(phone).append("\n");
        sb.append("Email: ").append(email == null || email.isBlank() ? "-" : email).append("\n");
        sb.append("Books on loan: ").append(books.size()).append("\n");
        return sb.toString();
    }

    public List<Book> getBooks() {
        return Collections.unmodifiableList(books);
    }

    // ✅ MAX-LOAN CHECK ENFORCED HERE
    public void borrowBook(Book book, LocalDate dueDate) throws LibraryException {

        if (deleted) {
            throw new LibraryException("This patron is deleted/hidden.");
        }

        if (books.size() >= MAX_LOANS) {
            throw new LibraryException("Patron has reached the maximum loan limit (" + MAX_LOANS + ").");
        }

        if (book.isDeleted()) {
            throw new LibraryException("This book is deleted/hidden.");
        }

        if (book.isOnLoan()) {
            throw new LibraryException("The book is currently on loan.");
        }

        Loan loan = new Loan(this, book, LocalDate.now(), dueDate);
        book.setLoan(loan);

        if (!books.contains(book)) {
            books.add(book);
        }
    }

    public void renewBook(Book book, LocalDate dueDate) throws LibraryException {
        if (!book.isOnLoan() || book.getLoan().getPatron() != this) {
            throw new LibraryException("The book is not on loan to this patron.");
        }
        book.setDueDate(dueDate);
    }

    public void returnBook(Book book) throws LibraryException {
        if (!book.isOnLoan() || book.getLoan().getPatron() != this) {
            throw new LibraryException("The book is not on loan to this patron.");
        }

        // ✅ IMPORTANT: remove from patron’s active books FIRST (so limit works)
        books.remove(book);

        // ✅ Then terminate the loan (keeps history)
        book.returnToLibrary(LocalDate.now());
    }

    // Used during loading: book should already have loan info set.
    public void addBook(Book book) {
        if (book != null && !books.contains(book)) {
            books.add(book);
        }
    }
}