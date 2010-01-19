import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 * Main class for client hadling
 * @author stas
 */
class Client extends Thread {
    protected Socket socket;
    protected BufferedReader in;
    protected PrintStream out;
    protected Logger logger;
    protected String remote;
    protected Boolean exit = false;
    protected DB db;
    protected dbUser user = null;
    protected String responseKeyword = "/reply ";
    protected String okResponse = "OK";
    protected String failureResponse = "FAIL";

    public Client(Socket client, DB dbcon, Logger l) {
        socket = client;
        db = dbcon;
        logger = l;
        remote = socket.getRemoteSocketAddress().toString();
        logger.log(Level.INFO, i18n._("CONNECTED") + remote);
        
        try {
            // socket.readline() is deprecated as of JDK 1.1
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            out = new PrintStream(socket.getOutputStream());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
        this.start();
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run(){
        String line;
        String cmd;
        try {
            line = in.readLine();
            if(line != null) {
                if (this.getCmd(line.toUpperCase()).equals("LOGIN")) {
                    return;
                } else if (this.getCmd(line.toUpperCase()).equals("REGISTER")) {
                    String[] args = getArgs(line);
                    try {
                        db.registerUser(args[0].toLowerCase(), args[1].replace(".", " "), args[2].toLowerCase(), args[3]);
                        logger.log(Level.INFO, i18n._("NEW_REGISTRATION") + args[0]);
                    } catch (SqlJetException ex) {
                        logger.log(Level.SEVERE, i18n._("REGISTRATION_FAILED_FOR") + args[0], ex);
                    }
                    this.send(responseKeyword + okResponse);
                } else if (this.getCmd(line.toUpperCase()).equals("LOGOUT")) {
                    exit = true;
                }
            }

            if(exit != true && user != null && user.loggedin == true) {
                line = in.readLine();
                while(line != null) {//Otherwise you'll need horse powers to stop it
                    // TODO
                    line = in.readLine();
                    cmd = this.getCmd(line.toUpperCase());
                    switch(Command.valueOf(cmd)) {
                        case MSG : logger.log(Level.INFO, "msg"); break; //msg
                        case LOGOUT : exit = true; break; //logout
                        default: line = null; break;
                    }
                }
            }
        } catch (IOException e) {
            logger.log(
                        Level.WARNING,
                        "exception: " + socket.getRemoteSocketAddress() +
                        ": " + e
                    );
        }
        
        if(exit)
            this.send(responseKeyword + i18n._("DISCONNECTED_OR_NOT_LOGGEDIN"));

        logger.log(Level.INFO, i18n._("DISCONNECTED") + remote);
        
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }   
    }

    private void send(String s) {
        out.println(s);
    }

    private String getCmd(String line) {
        String[] c = line.split(" ");
        return new StringBuffer(c[0]).delete(0, 1).toString(); //remove the slash
    }

    private String[] getArgs(String line) {
        String[] args = null;
        String[] c = line.split(" ");
        line = new StringBuffer(line).delete(0, c[0].length()+1).toString(); // remove keyword
        String cmd = new StringBuffer(c[0]).delete(0, 1).toString();
        if(cmd.toUpperCase().equals("MSG")) {
            args[0] = line;
        }
        else if (cmd.toUpperCase().equals("REGISTER")) {
            args = line.split(" ");
        }
        return args;
    }
}
