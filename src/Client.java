import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.logging.Level;

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

    public Client(Socket client, Logger l) {
        socket = client;
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
        String line = " ";
        try {
            while(line != null) {//Otherwise you'll need horse powers to stop it
                line = in.readLine();
                if(line != null)
                    logger.log( //Something to show up the results... tmp
                        Level.INFO,
                        socket.getRemoteSocketAddress() + ": " + line
                    );
            }
        } catch (IOException e) {
            ;
        }
        
        logger.log(Level.INFO, i18n._("DISCONNECTED") + remote);

        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }   
    }
}
