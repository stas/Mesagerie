import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Locale;
import org.ho.yaml.*;

/**
 * Class for yaml configuration manipulations
 * @author stas
 */
class Config extends Object{
    String ip;
    Integer port;
    String log;
    String db;
    Map source;

    Config(String config_file) throws FileNotFoundException {
        File f;
        
        if(!config_file.isEmpty()) {
            f = new File(config_file);
        }
        else {
            f = new File("/etc/mesagerie/mesagerie.yaml");
        }
        
        if(f.exists()) {
            source = (Map) Yaml.load(f);
            this.ip = (String) (source.get("ip"));
            this.port = (Integer) source.get("port");
            this.log = (String) source.get("log");
            this.db = (String) source.get("db");
            
            String[] l = ((String) source.get("locale")).split("_");
            Locale.setDefault(new Locale(l[0], l[1]));
            System.out.println(i18n._("Configuration file loaded."));
        }
        else {
            System.out.println("Configuration file doesn't exist.");
            System.exit(1);
        }
            System.out.println(Locale.getDefault());
    }

    public Object get(String field) {
        return source.get(field);
    }

}
