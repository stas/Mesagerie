import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 * Main class for client hadling
 * @author stas
 */
class Client extends Thread {
    protected Mesagerie server;
    protected Socket socket;
    protected BufferedReader in;
    protected PrintStream out;
    protected Logger logger;
    protected String remote;
    protected Boolean exit = false;
    protected Boolean allow_registration;
    protected DB db;
    protected dbUser user = null;
    protected String responseKeyword = "/reply ";
    protected String okResponse = "OK";
    protected String failureResponse = "FAIL";
    protected String serverName = "Mesagerie ";

    public Client(Mesagerie s, Socket client, DB dbcon, Boolean ar, Logger l) {
        server = s;
        socket = client;
        db = dbcon;
        allow_registration = ar;
        logger = l;
        remote = socket.getRemoteSocketAddress().toString();
        logger.log(Level.INFO, i18n._("CONNECTED") + " " + remote);
        
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
        String[] args;
        try {
            line = in.readLine();
            if(line != null) {
                if (this.getCmd(line.toUpperCase()).equals("LOGIN")) {
                    args = getArgs(line);
                    
                    try {
                        user = db.identifyUser(args[0].toLowerCase(), args[1]);
                    } catch (SqlJetException ex) {
                        logger.log(Level.SEVERE, i18n._("IDENTIFICATION_FAILED_FOR") + " " + args[0], ex);
                    }
                    
                    if(user.username.equals(args[0]) && user.password.equals(args[1])) {
                        user.loggedin = true;
                        this.send(okResponse);
                        logger.log(Level.INFO, i18n._("IDENTIFIED") + " " + args[0]);
                    } else {
                        this.send(failureResponse);
                        line = null;
                        logger.log(Level.SEVERE, i18n._("IDENTIFICATION_FAILED_FOR") + " " + args[0]);
                    }
                    line = "/msg " + user.name + " " + i18n._("LOGGEDIN"); //null means disconnection
                } else if (this.getCmd(line.toUpperCase()).equals("REGISTER") && this.allow_registration) {
                    args = getArgs(line);
                    try {
                        db.registerUser(args[0].toLowerCase(), args[1].replace(".", " "), args[2].toLowerCase(), args[3]);
                        logger.log(Level.INFO, i18n._("NEW_REGISTRATION") + " " + args[0]);
                    } catch (SqlJetException ex) {
                        logger.log(Level.SEVERE, i18n._("REGISTRATION_FAILED_FOR") + " " + args[0], ex);
                    }
                    this.send(okResponse);
                } else if (this.getCmd(line.toUpperCase()).equals("LOGOUT")) {
                    exit = true;
                } else if (this.getCmd(line.toUpperCase()).equals("HELP")) {
                    this.send(this.helpMsg());
                }
            }

            if(exit != true && user != null && user.loggedin == true) {
                while(line != null) {//Otherwise you'll need horse powers to stop it
                    // TODO
                    line = in.readLine();
                    cmd = this.getCmd(line.toUpperCase());
                    switch(Command.valueOf(cmd)) {
                        case MSG :
                            server.transmit(
                                    responseKeyword + serverName +
                                    user.name.replace(" ", ".") + ": " + this.getArgs(line)[0]
                            );
                            break; //msg
                        case LOGOUT : 
                            exit = true;
                            line = null;
                            break; //logout
                        default: this.send(i18n._("UNKNOWN_COMMAND")); line = null; break;
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
            this.send(i18n._("DISCONNECTED_OR_NOT_LOGGEDIN"));

        logger.log(Level.INFO, i18n._("DISCONNECTED") + " " + remote);
        
        try {
            in.close();
            out.close();
            server.removeClientStream(socket);
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }   
    }

    private void send(String s) {
        out.println(responseKeyword + serverName + s);
    }

    private String getCmd(String line) {
        String[] allowedCmds = {
            "MSG", "REGISTER", "LOGOUT", "HELP", "LOGIN"
        };
        String ourCmd;
        Boolean found = false;
        String[] c = line.split(" ");
        ourCmd = new StringBuffer(c[0]).delete(0, 1).toString(); //remove the slash
        // Check for unallowed commands
        for (int i = 0; i < allowedCmds.length && found != true; i++) {
            if(allowedCmds[i].equalsIgnoreCase(ourCmd) )
                found = true;
        }

        if(found)
            return ourCmd;
        else
            return "NILL";
    }

    private String[] getArgs(String line) {
        String[] args = {""};
        String[] c = line.split(" ");
        line = new StringBuffer(line).delete(0, c[0].length()+1).toString(); // remove keyword
        String cmd = new StringBuffer(c[0]).delete(0, 1).toString();
        if(cmd.toUpperCase().equals("MSG")) {
            args[0] = line;
        }
        else if (cmd.toUpperCase().equals("REGISTER")) {
            args = line.split(" ");
        }
        else if (cmd.toUpperCase().equals("LOGIN")) {
            args = line.split(" ");
        }
        return args;
    }

    private String helpMsg() {
        return i18n._("HELPMSG");
    }
}
