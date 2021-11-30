package es.jdl.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class ExpandedPropertiesTest {

    @Test
    public void testSimpleReplace() throws IOException {
        Properties prop = new Properties();
        String jv = System.getProperty("java.version");
        prop.setProperty("version", "version-${java.version}");
        prop.setProperty("a", "a");
        prop.setProperty("b", "b+${a}");
        prop.setProperty("c", "${d}");
        ExpandedProperties p = new ExpandedProperties(prop);
        Assert.assertEquals("version-" + jv, p.getProperty("version"));
        Assert.assertEquals("b+a", p.getProperty("b"));
        Assert.assertEquals("${d}", p.getProperty("c"));
        Assert.assertNull(p.getProperty("d"));
    }

    @Test
    public void testReplaceAll() throws IOException {
        String jv = System.getProperty("java.version");
        String v = ExpandedProperties.replaceAll("version-${java.version}", System.getProperties());
        System.out.println("version-" + jv + "=>" + v);
        Assert.assertEquals("version-" + jv, v);
    }

}
