import java.util.ResourceBundle;

/**
 * Localization support class
 * @author stas
 */
public class i18n {
    private static ResourceBundle catalog = ResourceBundle.getBundle("Mesagerie");

    public static String _(String s) {
        if(catalog.getString(s) != null)
            return catalog.getString(s);
        else return s;
    }
}