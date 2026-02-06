package bcu.cmp5332.librarysystem.gui;

import bcu.cmp5332.librarysystem.commands.*;
import bcu.cmp5332.librarysystem.main.LibraryException;
import bcu.cmp5332.librarysystem.model.Book;
import bcu.cmp5332.librarysystem.model.Library;
import bcu.cmp5332.librarysystem.model.Patron;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;

public class MainWindow extends JFrame implements ActionListener {

    private JMenuBar menuBar;
    private JMenu adminMenu;
    private JMenu booksMenu;
    private JMenu patronsMenu;

    private JMenuItem adminExit;

    private JMenuItem booksView;
    private JMenuItem booksAdd;
    private JMenuItem booksDel;
    private JMenuItem booksIssue;
    private JMenuItem booksReturn;
    private JMenuItem booksRenew;

    private JMenuItem patronsView;
    private JMenuItem patronsAdd;
    private JMenuItem patronsDel;

    private final Library library;

    // Track what is currently displayed so we can read selected row ID correctly
    private JTable currentTable = null;
    private boolean showingBooks = true; // true => books table, false => patrons table

    public MainWindow(Library library) {
        this.library = library;
        initialize();
        displayBooks(); // default view
    }

    public Library getLibrary() {
        return library;
    }

    private void initialize() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Library Management System");

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Admin
        adminMenu = new JMenu("Admin");
        menuBar.add(adminMenu);

        adminExit = new JMenuItem("Exit");
        adminMenu.add(adminExit);
        adminExit.addActionListener(this);

        // Books
        booksMenu = new JMenu("Books");
        menuBar.add(booksMenu);

        booksView = new JMenuItem("View");
        booksAdd = new JMenuItem("Add");
        booksDel = new JMenuItem("Delete");
        booksIssue = new JMenuItem("Issue");
        booksReturn = new JMenuItem("Return");
        booksRenew = new JMenuItem("Renew");

        booksMenu.add(booksView);
        booksMenu.add(booksAdd);
        booksMenu.add(booksDel);
        booksMenu.add(booksIssue);
        booksMenu.add(booksReturn);
        booksMenu.add(booksRenew);

        for (int i = 0; i < booksMenu.getItemCount(); i++) {
            booksMenu.getItem(i).addActionListener(this);
        }

        // Patrons
        patronsMenu = new JMenu("Patrons");
        menuBar.add(patronsMenu);

        patronsView = new JMenuItem("View");
        patronsAdd  = new JMenuItem("Add");
        patronsDel  = new JMenuItem("Delete");

        patronsMenu.add(patronsView);
        patronsMenu.add(patronsAdd);
        patronsMenu.add(patronsDel);

        patronsView.addActionListener(this);
        patronsAdd.addActionListener(this);
        patronsDel.addActionListener(this);

        setSize(800, 500);
        setVisible(true);
        setAutoRequestFocus(true);
        toFront();

        // DO NOT kill console app on GUI close
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        // Admin
        if (ae.getSource() == adminExit) {
            System.exit(0);
        }

        // Books menu
        if (ae.getSource() == booksView) {
            displayBooks();
            return;
        }
        if (ae.getSource() == booksAdd) {
            new AddBookWindow(this); // we will update AddBookWindow in Step A2
            return;
        }
        if (ae.getSource() == booksDel) {
            deleteSelectedBook();
            return;
        }
        if (ae.getSource() == booksIssue) {
            issueSelectedBook();
            return;
        }
        if (ae.getSource() == booksReturn) {
            returnSelectedBook();
            return;
        }
        if (ae.getSource() == booksRenew) {
            renewSelectedBook();
            return;
        }

        // Patrons menu
        if (ae.getSource() == patronsView) {
            displayPatrons();
            return;
        }
        if (ae.getSource() == patronsAdd) {
            addPatronPopup();
            return;
        }
        if (ae.getSource() == patronsDel) {
            deleteSelectedPatron();
        }
    }

    /* =========================
       TABLE RENDERING
       ========================= */

    public void displayBooks() {
        showingBooks = true;

        List<Book> booksList = library.getBooks();

        String[] columns = new String[]{"ID", "Title", "Author", "Year", "Status"};

        Object[][] data = new Object[booksList.size()][columns.length];
        int row = 0;
        for (Book b : booksList) {
            // Hide deleted books from table if your model returns null short details
            if (b.getDetailsShort() == null) continue;

            data[row][0] = b.getId();
            data[row][1] = b.getTitle();
            data[row][2] = b.getAuthor();
            data[row][3] = b.getPublicationYear();
            data[row][4] = b.isDeleted() ? "Deleted/Hidden" : b.getStatus();
            row++;
        }

        // If we skipped deleted rows, trim table
        Object[][] trimmed = new Object[row][columns.length];
        for (int i = 0; i < row; i++) trimmed[i] = data[i];

        JTable table = new JTable(trimmed, columns);
        currentTable = table;

        getContentPane().removeAll();
        getContentPane().add(new JScrollPane(table));
        revalidate();
        repaint();
    }

    public void displayPatrons() {
        showingBooks = false;

        List<Patron> patrons = library.getPatrons();
        String[] columns = new String[]{"ID", "Name", "Phone", "Email"};

        Object[][] data = new Object[patrons.size()][columns.length];
        int row = 0;
        for (Patron p : patrons) {
            if (p.getDetailsShort() == null) continue; // hide deleted
            data[row][0] = p.getId();
            data[row][1] = p.getName();
            data[row][2] = p.getPhone();
            data[row][3] = (p.getEmail() == null || p.getEmail().isBlank()) ? "-" : p.getEmail();
            row++;
        }

        Object[][] trimmed = new Object[row][columns.length];
        for (int i = 0; i < row; i++) trimmed[i] = data[i];

        JTable table = new JTable(trimmed, columns);
        currentTable = table;

        getContentPane().removeAll();
        getContentPane().add(new JScrollPane(table));
        revalidate();
        repaint();
    }

    /* =========================
       HELPERS
       ========================= */

    private Integer getSelectedIdOrNull() {
        if (currentTable == null) return null;
        int selectedRow = currentTable.getSelectedRow();
        if (selectedRow < 0) return null;

        Object idObj = currentTable.getValueAt(selectedRow, 0);
        if (idObj == null) return null;

        try {
            return Integer.parseInt(idObj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    /* =========================
       BOOK ACTIONS
       ========================= */

    private void deleteSelectedBook() {
        try {
            if (!showingBooks) displayBooks();

            Integer bookId = getSelectedIdOrNull();
            if (bookId == null) {
                JOptionPane.showMessageDialog(this, "Select a book row first.");
                return;
            }

            Command cmd = new DeleteBook(bookId);
            cmd.execute(library, LocalDate.now());

            displayBooks();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void issueSelectedBook() {
        try {
            if (!showingBooks) displayBooks();

            Integer bookId = getSelectedIdOrNull();
            if (bookId == null) {
                JOptionPane.showMessageDialog(this, "Select a book row first.");
                return;
            }

            String patronStr = JOptionPane.showInputDialog(this, "Enter Patron ID:");
            if (patronStr == null) return;
            int patronId = Integer.parseInt(patronStr.trim());

            Command cmd = new BorrowBook(patronId, bookId);
            cmd.execute(library, LocalDate.now());

            displayBooks();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void returnSelectedBook() {
        try {
            if (!showingBooks) displayBooks();

            Integer bookId = getSelectedIdOrNull();
            if (bookId == null) {
                JOptionPane.showMessageDialog(this, "Select a book row first.");
                return;
            }

            // Auto-detect patron from the current loan if possible (best UX)
            Book book = library.getBookByID(bookId);
            if (!book.isOnLoan()) {
                JOptionPane.showMessageDialog(this, "This book is not on loan.");
                return;
            }
            int patronId = book.getLoan().getPatron().getId();

            Command cmd = new ReturnBook(patronId, bookId);
            cmd.execute(library, LocalDate.now());

            displayBooks();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void renewSelectedBook() {
        try {
            if (!showingBooks) displayBooks();

            Integer bookId = getSelectedIdOrNull();
            if (bookId == null) {
                JOptionPane.showMessageDialog(this, "Select a book row first.");
                return;
            }

            Book book = library.getBookByID(bookId);
            if (!book.isOnLoan()) {
                JOptionPane.showMessageDialog(this, "This book is not on loan.");
                return;
            }

            int patronId = book.getLoan().getPatron().getId();

            Command cmd = new RenewBook(patronId, bookId);
            cmd.execute(library, LocalDate.now());

            displayBooks();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    /* =========================
       PATRON ACTIONS
       ========================= */

    private void addPatronPopup() {
        try {
            String name = JOptionPane.showInputDialog(this, "Name:");
            if (name == null) return;

            String phone = JOptionPane.showInputDialog(this, "Phone:");
            if (phone == null) return;

            String email = JOptionPane.showInputDialog(this, "Email:");
            if (email == null) email = "";

            Command cmd = new AddPatron(name, phone, email);
            cmd.execute(library, LocalDate.now());

            displayPatrons();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteSelectedPatron() {
        try {
            if (showingBooks) displayPatrons();

            Integer patronId = getSelectedIdOrNull();
            if (patronId == null) {
                JOptionPane.showMessageDialog(this, "Select a patron row first.");
                return;
            }

            Command cmd = new DeletePatron(patronId);
            cmd.execute(library, LocalDate.now());

            displayPatrons();
        } catch (Exception ex) {
            showError(ex);
        }
    }
}