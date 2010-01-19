import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final int version = 3;
    private static final String users_table = "users";
    private static final String users_index = "users_index";
    private static final String username_field = "username";
    private static final String name_field = "name";
    private static final String email_field = "email";
    private static final String password_field = "password";
    private static final String loggedin_field = "online";
    private static Boolean new_db_flag = false;
    private static SqlJetDb db;
    private Logger logger;

    DB(String file, Logger l) throws SqlJetException, IOException {
        File db_file = new File(file);
        logger = l;

        if(!db_file.exists()) {
            //db_file.createNewFile();
            new_db_flag = true;
        }
        // Start SQLite driver
        db = new SqlJetDb(db_file, true);
        db.open();
        // Creare schema bd-ului
        if(new_db_flag == true) {
            logger.log(Level.WARNING, i18n._("DATABASE_WILL_BE_CREATED"));
            db.beginTransaction(SqlJetTransactionMode.WRITE);
            db.runWriteTransaction(new ISqlJetTransaction() {
                public Object run(SqlJetDb db) throws SqlJetException {
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
                    db.getOptions().setUserVersion(version);
                    db.commit();
                    return null;
                }
            });
        }
    }

    public void disconnect() throws SqlJetException {
        db.close();
    }

    public void registerUser(final String u, final String n, final String e, final String p) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        db.runWriteTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                return db.getTable(users_table).insert(null, u, n, e, p);
            }
	});
        db.commit();
    }

    public void changeUserPassword(final String u, String p) throws SqlJetException {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put(password_field, p);
        db.beginTransaction(SqlJetTransactionMode.WRITE);
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
        db.commit();
    }

    public void offlineUser(final String u) throws SqlJetException {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put(loggedin_field, 0); // marcheaza utilizatorul ca offline
        db.beginTransaction(SqlJetTransactionMode.WRITE);
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
        db.commit();
    }

    public void onlineUser(final String u) throws SqlJetException {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put(loggedin_field, 1); // marcheaza utilizatorul ca online
        db.beginTransaction(SqlJetTransactionMode.WRITE);
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
        db.commit();
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
