package es.jdl.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ExpandedProperties extends Properties {

    public ExpandedProperties(String path) throws IOException {
        super();
        load(new FileReader(path));
        expand(System.getProperties());
    }

    public ExpandedProperties(Properties p) {
        super();
        putAll(p);
        expand(System.getProperties());
    }

    public ExpandedProperties(InputStream inputStream) throws IOException {
        super();
        load(inputStream);
        expand(System.getProperties());
    }

    public void expand(Properties properties) {
        Properties propCopy = new Properties();
        propCopy.putAll(this);
        propCopy.putAll(properties);
        for (String k: this.stringPropertyNames()) {
            String v = this.getProperty(k);
            if (v != null)
                this.setProperty(k, replaceAll(v, propCopy));
            else
                this.remove(k);
        }
    }

    // Usefull statics for properties and configuration

    public static String replaceAll(String value, Properties properties) {
        for (String k: properties.stringPropertyNames())
            value = value.replace("${" + k + "}", properties.getProperty(k));
        return value;
    }

    public static boolean isStringNotEmpty(String param) {
        return param != null && !"".equals(param.trim());
    }

    /**
     * Very simple but method name helps meaning in code
     */
    public static String getDefault(String value, String defaultValue) {
        return value == null?defaultValue:value;
    }


}
