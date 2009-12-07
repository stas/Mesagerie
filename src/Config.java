import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;
import org.ho.yaml.*;

/**
 * Class for yaml configuration manipulations
 * @author stas
 */
class Config extends Object{
    public InetAddress ip;
    public Integer port;
    public String log;
    public String db;
    private Map source;
    public Logger logger;

    Config(String config_file) throws FileNotFoundException, UnknownHostException {
        File f;
        
        if(!config_file.isEmpty()) {
            f = new File(config_file);
        }
        else {
            f = new File("/etc/mesagerie/mesagerie.yaml");
        }

        if(f.exists()) {
            source = (Map) Yaml.load(f);
            this.ip = InetAddress.getByName((String) source.get("ip"));
            this.port = (Integer) source.get("port");
            this.log = (String) source.get("log");
            this.db = (String) source.get("db");
        }
        else {
            System.out.println(i18n._("CONFIGURATION_FILE_DOESN'T_EXIST"));
            System.exit(-1);
        }
    }

    public Object get(String field) {
        return source.get(field);
    }

}
