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
    protected String db = "mesagerie.sqlite3";
    protected Integer maxclients = 5;
    protected Logger logger;

    Mesagerie(Config Conf, Logger l) {
        logger = l;

        if(!Conf.ip.toString().isEmpty())
            ip = Conf.ip;

        if(Conf.port != 0)
            port = Conf.port;

        if(Conf.db.length() > 0)
            db = Conf.db;

        if((Integer) Conf.get("maxclients") > 5)
            maxclients = (Integer) Conf.get("maxclients");
        
        try {
            if(!ip.toString().isEmpty())
                socket = new ServerSocket(port, -1, ip);
            else {
                socket = new ServerSocket(port);
            }
            logger.log(Level.INFO, i18n._("MESAGERIE_WILL_START_ON") + socket.getLocalSocketAddress());
            this.start();
        } catch (IOException ex) {
            logger.log(Level.ALL, ex.getMessage());
        }

    }

    @Override
    public void run() {
        Integer clients = 0;
        while(clients < maxclients) {
            try {
                Socket client = socket.accept();
                clients++;
                Client c = new Client(client, logger);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
        logger.log(Level.WARNING, "MAXIMUM_NUMBER_OF_CLIENTS_WAS_REACHED");
        
        try {
            if(socket != null)
                socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }
}
