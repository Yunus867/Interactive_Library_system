package bcu.cmp5332.librarysystem.data;

import bcu.cmp5332.librarysystem.main.LibraryException;
import bcu.cmp5332.librarysystem.model.Library;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryData {

    private static final List<DataManager> dataManagers = new ArrayList<>();

    // Runs once when the class is loaded
    static {
        dataManagers.add(new BookDataManager());
        dataManagers.add(new PatronDataManager());
        dataManagers.add(new LoanDataManager());
    }

    // Load all stored data into memory
    public static Library load() throws LibraryException, IOException {
        Library library = new Library();
        for (DataManager dm : dataManagers) {
            dm.loadData(library);
        }
        return library;
    }

    // Store all data back to files
    public static void store(Library library) throws IOException {
        for (DataManager dm : dataManagers) {
            dm.storeData(library);
        }
    }
}