import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    protected Logger logger;

    Mesagerie(Config Conf, Logger l) {
        logger = l;

        if(!Conf.ip.toString().isEmpty())
            ip = Conf.ip;

        if(Conf.port != 0)
            port = Conf.port;

        if(Conf.db.length() > 0)
            db = Conf.db;

        if((Integer) Conf.get("maxclients") > 1)
            maxclients = (Integer) Conf.get("maxclients");

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
                    Client c = new Client(client, logger);
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
            try {
                if(socket != null)
                    socket.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }
}
