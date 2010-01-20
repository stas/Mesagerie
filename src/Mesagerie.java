import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 * Main server class
 * @author stas
 */
class Mesagerie extends Thread {
    protected InetAddress ip;
    protected int port = 7777;
    protected ServerSocket socket;
    protected String db = "/tmp/mesagerie.sqlite3";
    protected Integer maxclients = 1;
    protected Boolean allow_registration = false;
    protected Logger logger;
    protected DB dbcon;
    protected final Hashtable client_streams = new Hashtable();

    Mesagerie(Config Conf, Logger l) throws SqlJetException, IOException {
        dbcon = new DB(Conf.db, l);
        logger = l;

        if(!Conf.ip.toString().isEmpty())
            ip = Conf.ip;

        if(Conf.port != 0)
            port = Conf.port;

        if(Conf.db.length() > 0)
            db = Conf.db;

        if((Integer) Conf.get("maxclients") > 1)
            maxclients = (Integer) Conf.get("maxclients");

        if(Conf.get("registration") != null)
            allow_registration = (Boolean) Conf.get("registration");

        try {
            if(!ip.toString().isEmpty())
                socket = new ServerSocket(port, -1, ip);
            else {
                socket = new ServerSocket(port);
            }
            logger.log(
                    Level.INFO,
                    i18n._("MESAGERIE_WILL_START_ON")
                    + socket.getLocalSocketAddress()
            );
            this.start();
        } catch (IOException ex) {
            logger.log(Level.ALL, ex.getLocalizedMessage());
        }

    }

    @Override
    public void run() {
        Integer clients;
        Integer started = Client.activeCount();
        try {
            while(true) {
                clients = Client.activeCount() + 1; // dont count from 0
                Socket client = socket.accept();
                if(clients - started <= maxclients) {
                    Client c = new Client(this, client, dbcon, allow_registration, logger);
                    DataOutputStream client_os = new DataOutputStream(client.getOutputStream());
                    client_streams.put(client, client_os);
                }
                else {
                    logger.log(
                            Level.WARNING,
                            i18n._("MAXIMUM_NUMBER_OF_CLIENTS_WAS_REACHED")
                             + (clients - started) + "/" + maxclients
                    );
                }
            }
        }
        catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getLocalizedMessage());
        }
        finally {
            // Close socket
            try {
                if(socket != null)
                    socket.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
            // Close database
            try {
                dbcon.disconnect();
            } catch (SqlJetException ex) {
                logger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    private Enumeration getClientStreams() {
        return client_streams.elements();
    }

    public void transmit(String msg) throws IOException {
        synchronized(client_streams) {
            for (Enumeration e = getClientStreams(); e.hasMoreElements();) {
                DataOutputStream dos = (DataOutputStream) e.nextElement();
                dos.writeUTF(msg + "\n");
            }
        }
    }

    void removeClientStream(Socket s) {
        synchronized(client_streams) {
            logger.log(
                    Level.INFO,
                    i18n._("REMOVING_STREAM") + " " + s.getRemoteSocketAddress().toString()
            );
            client_streams.remove(s);
        }
    }
}
