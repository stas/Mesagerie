import java.io.FileNotFoundException;

/**
 * Starter for server
 * @author Stas Sușcov
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        String config_file = "";

        // Check for command line config file
        if(args.length != 0) {
            config_file = args[0]; // load command line config
        }

        Config Conf = new Config(config_file);
        Mesagerie Server = new Mesagerie(Conf);
        //System.out.println(Conf.get("db"));
        
    }

}