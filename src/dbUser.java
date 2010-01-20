import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

/**
 * @author stas
 */
public class dbUser {
    public String username;
    public String name;
    public String email;
    public String password;
    public Boolean loggedin = false;

    public dbUser() {
    }

    public dbUser(ISqlJetCursor c) throws SqlJetException {
        this.username = c.getString("username");
        this.name = c.getString("name");
        this.email = c.getString("email");
        this.password = c.getString("password");
        this.loggedin = c.getBoolean("online");
    }

    public void create(String username, String name, String email, String password) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
