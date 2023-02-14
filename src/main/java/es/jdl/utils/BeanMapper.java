package es.jdl.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Properties to Bean mapper utilities.
 * <br/>
 * Useful to parse main args and holds to configuration class:
 * <pre>
 *         ExpandedProperties exProp = new ExpandedProperties(this.getClass().getResource("/appconfig.properties")
 *                 .toURI().toURL().openStream());
 *         BeanMapper.mergeProperties(exProp, BeanMapper.parseArgs(args));
 * </pre>
 */
public class BeanMapper {

    /**
     * Parse main arguments like 'someProperty=someValue' to java.map
     * @param args main arguments
     * @return parsed key-value pair
     */
    public static Properties parseArgs(String[] args) {
        Properties ret = new Properties();
        if (args != null) {
            for (String p: args) {
                int idx = p.indexOf("=");
                if (idx > -1)
                    ret.setProperty(p.substring(0, idx),
                            p.substring(idx + 1));
            } // for
        } // args <> null
        return ret;
    }

    public static <T> T mapToObject(Properties p, Class<T> clazz) {
        T obj = null;
        try {
            obj = clazz.getConstructor().newInstance();
            mapToObject(p, obj);
            return obj;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mapToObject(Properties p, Object obj) {
        for (String fieldName: p.stringPropertyNames()) {
            try {
                Field f = obj.getClass().getDeclaredField(fieldName);
                setFieldParsedValue(f, obj, p.getProperty(fieldName));
            } catch (NoSuchFieldException e) {
                // avoid error on additional non-mapped config entries
                //e.printStackTrace();
            }
        } // for
    }

    public static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static void setFieldParsedValue(Field f, Object obj, String value) {
        f.setAccessible(true);
        try {
            switch (f.getType().getName()) {
                case "int":
                    f.setInt(obj, Integer.parseInt(value));
                    break;
                case "java.lang.Integer":
                    f.set(obj, Integer.valueOf(value));
                    break;
                case "long":
                    f.setLong(obj, Long.parseLong(value));
                    break;
                case "java.lang.Long":
                    f.set(obj, Long.valueOf(value));
                    break;
                case "double":
                    f.setDouble(obj, Double.valueOf(value));
                    break;
                case "java.lang.Double":
                    f.set(obj, Double.valueOf(value));
                    break;
                case "java.time.LocalDate": // no config then DateTimeFormatter.ISO_LOCAL_DATE
                    f.set(obj, LocalDate.parse(value));
                    break;
                case "java.time.LocalDateTime": // no config then DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    f.set(obj, LocalDateTime.parse(value));
                    break;
                case "java.util.Date":
                    f.set(obj, defaultDateFormat.parse(value));
                    break;
                case "java.math.BigDecimal":
                    f.set(obj, new BigDecimal(value));
                    break;
                default:
                    f.set(obj, value);
            }
        } catch (IllegalAccessException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Override dest properties with source's
     */
    public static void mergeProperties(Properties dest, Properties source) {
        source.stringPropertyNames().forEach(x -> dest.setProperty(x, source.getProperty(x)));
    }

    /**
     * Copies all orig properties into dest. Using reflection non-recursive
     */
    public static void copyBean(Object dest, Object orig) {
        for (Field f: dest.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                f.set(dest, f.get(orig));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
