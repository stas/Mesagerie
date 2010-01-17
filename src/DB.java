import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

/**
 * Main DB controller class
 * @author stas
 */
public class DB {
    private static final String users_table = "users";
    private static final String users_index = "users_index";
    private static final String username_field = "username";
    private static final String name_field = "name";
    private static final String email_field = "email";
    private static final String password_field = "password";
    private static final String loggedin_field = "online";
    private static Boolean new_db_flag = false;
    private static SqlJetDb db;

    private static void main(String file) throws SqlJetException {
        File db_file = new File(file);

        if(!db_file.exists())
            new_db_flag = true;

        // Start SQLite driver
        db = new SqlJetDb(db_file, true);
        db.getOptions().setAutovacuum(true);
        db.runTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                db.getOptions().setUserVersion(3);
                return true;
            }
        }, SqlJetTransactionMode.WRITE);
        // Creare schema bd-ului
        if(new_db_flag) {
            db.beginTransaction(SqlJetTransactionMode.WRITE);
            try {
                db.createTable(
                        "CREATE TABLE " + users_table
                        + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + username_field + " TEXT NOT NULL UNIQUE, "
                        + name_field + " TEXT NOT NULL, "
                        + email_field + " TEXT NOT NULL UNIQUE, "
                        + loggedin_field + " BOOLEAN DEFAULT 0 ), "
                        + password_field + " TEXT NOT NULL ) ;"
                        );
                db.createIndex(
                        "CREATE INDEX " + users_index + " ON "
                        + users_table + " (" + username_field + ");"
                        );
            } finally {
                db.commit();
            }
        }
    }

    public void registerUser(final String u, final String n, final String e, final String p) throws SqlJetException {
        db.runWriteTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                return db.getTable(users_table).insert(u, n, e, p);
            }
	});
    }

    public void changeUserPassword(final String u, String p) throws SqlJetException {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put(password_field, p);
        db.runWriteTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                ISqlJetTable t = db.getTable(users_table);
                ISqlJetCursor c = t.lookup(t.getPrimaryKeyIndexName(), u);
                try {
                    if(!c.eof()) {
                        c.updateByFieldNames(values);
                    }
                } finally {
                    c.close();
                }
                return null;
            }
        });
    }

    public void offlineUser(final String u) throws SqlJetException {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put(loggedin_field, 0); // marcheaza utilizatorul ca offline
        db.runWriteTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                ISqlJetTable t = db.getTable(users_table);
                ISqlJetCursor c = t.lookup(t.getPrimaryKeyIndexName(), u);
                try {
                    if(!c.eof()) {
                        c.updateByFieldNames(values);
                    }
                } finally {
                    c.close();
                }
                return null;
            }
        });
    }

    public void onlineUser(final String u) throws SqlJetException {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put(loggedin_field, 1); // marcheaza utilizatorul ca online
        db.runWriteTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                ISqlJetTable t = db.getTable(users_table);
                ISqlJetCursor c = t.lookup(t.getPrimaryKeyIndexName(), u);
                try {
                    if(!c.eof()) {
                        c.updateByFieldNames(values);
                    }
                } finally {
                    c.close();
                }
                return null;
            }
        });
    }

    public void changeEmail(final String u, String e) throws SqlJetException {
        /**
         * TODO: delete entry from db, examples available at
         * http://svn.sqljet.com/repos/sqljet/trunk/sqljet-examples/inventory/src/org/tmatesoft/sqljet/examples/inventory/InventoryDB.java
         */
    }

    public void changeName(final String u, String n) throws SqlJetException {
        /**
         * TODO: delete entry from db, examples available at
         * http://svn.sqljet.com/repos/sqljet/trunk/sqljet-examples/inventory/src/org/tmatesoft/sqljet/examples/inventory/InventoryDB.java
         */
    }

    public void removeUser(final String u) throws SqlJetException {
        /**
         * TODO: delete entry from db, examples available at
         * http://svn.sqljet.com/repos/sqljet/trunk/sqljet-examples/inventory/src/org/tmatesoft/sqljet/examples/inventory/InventoryDB.java
         */
    }

    private ISqlJetCursor loadUsers() throws SqlJetException {
	return db.getTable(users_table).open();
    }

    public Map<String, dbUser> getAllUsers() throws SqlJetException {
        ISqlJetCursor c = this.loadUsers();
        Map<String, dbUser> users = new HashMap<String, dbUser>();
        while(!c.eof()) {
            users.put(c.getString(username_field), new dbUser(c));
            c.next();
        }
        
        return users;
    }

}
