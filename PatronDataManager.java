package bcu.cmp5332.librarysystem.data;

import bcu.cmp5332.librarysystem.main.LibraryException;
import bcu.cmp5332.librarysystem.model.Library;
import bcu.cmp5332.librarysystem.model.Patron;

import java.io.*;
import java.util.Scanner;

public class PatronDataManager implements DataManager {

    private static final String RESOURCE = "resources/data/patrons.txt";

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
                    int id = Integer.parseInt(p[0]);
                    String name = p.length > 1 ? p[1] : "";
                    String phone = p.length > 2 ? p[2] : "";
                    String email = p.length > 3 ? p[3] : "";

                    // deleted flag 
                    boolean deleted = false;
                    if (p.length > 4 && !p[4].isBlank()) {
                        deleted = Boolean.parseBoolean(p[4]);
                    }

                    Patron patron = new Patron(id, name, phone, email);
                    patron.setDeleted(deleted);

                    library.addPatron(patron);

                } catch (Exception ex) {
                    throw new LibraryException("Unable to parse patron on line " + lineNo + ": " + line);
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
            for (Patron patron : library.getPatrons()) {
                out.print(patron.getId() + SEPARATOR);
                out.print(patron.getName() + SEPARATOR);
                out.print(patron.getPhone() + SEPARATOR);
                out.print((patron.getEmail() == null ? "" : patron.getEmail()) + SEPARATOR);

                // deleted flag 
                out.print(patron.isDeleted());

                out.println();
            }
        }
    }
}