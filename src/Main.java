import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 * Starter for server
 * @author Stas Sușcov
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnknownHostException {
        String config_file = "";
        Logger logger = Logger.getLogger("Mesagerie");
        // Check for command line config file
        if(args.length != 0) {
            config_file = args[0]; // load command line config
        }

        Config Conf = new Config(config_file);

        String[] l = ((String) Conf.get("locale")).split("_");
        Locale.setDefault(new Locale(l[0], l[1]));
        
        try {
            FileHandler logfile = new FileHandler(Conf.log);
            logger.addHandler(logfile);
            logfile.setFormatter(new SimpleFormatter());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        logger.log(Level.INFO, i18n._("CONFIGURATION_FILE_LOADED"));
        try {
            new Mesagerie(Conf, logger);
        } catch (SqlJetException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }
}