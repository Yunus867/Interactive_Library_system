package bcu.cmp5332.librarysystem.model;

import bcu.cmp5332.librarysystem.main.LibraryException;
import java.time.LocalDate;

public class Book {

    private int id;
    private String title;
    private String author;
    private String publicationYear;

    
    private String publisher;

    
    private boolean deleted = false;

    private Loan loan;

    // Backwards-compatible constructor 
    public Book(int id, String title, String author, String publicationYear) {
        this(id, title, author, publicationYear, "");
    }

    // Constructor with publisher
    public Book(int id, String title, String author, String publicationYear, String publisher) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.publisher = (publisher == null) ? "" : publisher;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    // Publisher getter/setter
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = (publisher == null) ? "" : publisher;
    }

    // (70–79) Deleted flag getter/setter
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /** One-line summary used by listbooks */
    public String getDetailsShort() {
        if (deleted) return null; // hidden from list
        return "Book #" + id + " - " + title;
    }

    /** Multi-line detailed view used by showbook */
    public String getDetailsLong() {
        StringBuilder sb = new StringBuilder();

        sb.append("Book #").append(id).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Author: ").append(author).append("\n");
        sb.append("Publication year: ").append(publicationYear).append("\n");
        sb.append("Publisher: ").append(publisher == null || publisher.isBlank() ? "-" : publisher).append("\n");

        if (deleted) {
            sb.append("Status: Deleted/Hidden\n");
            return sb.toString();
        }

        sb.append("Status: ").append(getStatus()).append("\n");

        if (isOnLoan()) {
            sb.append("Borrowed by: ")
              .append(loan.getPatron().getName())
              .append("\n");
            sb.append("Due date: ")
              .append(loan.getDueDate())
              .append("\n");

            // (80%+) If your Loan now tracks termination/returnDate, show it when relevant
            if (loan.isTerminated()) {
                sb.append("Returned on: ").append(loan.getReturnDate()).append("\n");
            }
        }

        return sb.toString();
    }

    public boolean isOnLoan() {
        
        return loan != null && !loan.isTerminated();
    }

    public String getStatus() {
        return isOnLoan() ? "On loan" : "Available";
    }

    public LocalDate getDueDate() {
        return isOnLoan() ? loan.getDueDate() : null;
    }

    public void setDueDate(LocalDate dueDate) throws LibraryException {
        if (!isOnLoan()) {
            throw new LibraryException("Cannot set due date because the book is not on loan.");
        }
        loan.setDueDate(dueDate);
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    
    public void returnToLibrary(LocalDate returnDate) {
        if (loan != null) {
            loan.terminate(returnDate);
        }
    }

    
    public void returnToLibrary() {
        returnToLibrary(LocalDate.now());
    }
}