import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by adam on 3/26/15
 */
public class PropertiesUtility {

    private Properties properties;

    public PropertiesUtility(String fileName) {

        InputStream is = getClass().getResourceAsStream(fileName);
        this.properties = new Properties();

        try {
            this.properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Properties getProperties() {
        return this.properties;
    }
}
