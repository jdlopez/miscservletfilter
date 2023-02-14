package es.jdl.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Properties;

public class BeanMapperTest {

    private String[] args = "sField=some_value iField=12345 dblField=1.0 dateField=01/12/2021 dField=2023-12-31".split(" ");

    @Before
    public void init() {
        //
    }

    @Test
    public void testArgs() {
        Properties conf = BeanMapper.parseArgs(args);
        Assert.assertEquals(conf.getProperty("sField"), "some_value");
        Assert.assertEquals(conf.getProperty("iField"), "12345");
        Assert.assertEquals(conf.getProperty("dblField"), "1.0");
    }

    @Test
    public void testProp2Bean() throws Exception {
        Properties conf = BeanMapper.parseArgs(args);
        DomainTest tst = BeanMapper.mapToObject(conf, DomainTest.class);
        Assert.assertEquals(tst.getsField(), "some_value");

        System.out.println(tst);
    }

    @Test
    public void testFiledCheck() throws Exception {
        DomainTest obj = new DomainTest();
        Field f = DomainTest.class.getDeclaredField("iField");
        System.out.println(f.getType().getName() + " isPrimitive:" + f.getType().isPrimitive());
        BeanMapper.setFieldParsedValue(f, obj, "123");
        f = DomainTest.class.getDeclaredField("dField");
        System.out.println(f.getType().getName() + " isPrimitive:" + f.getType().isPrimitive());
        BeanMapper.setFieldParsedValue(f, obj, "2022-12-31");

        System.out.println(obj);
    }

    @Test
    public void extPropMerge() throws Exception {
        System.setProperty("exampleone", "supervalue");
        ExpandedProperties exProp = new ExpandedProperties(this.getClass().getResource("/sample.properties")
                .toURI().toURL().openStream());
        System.out.println(exProp);
        Properties conf = BeanMapper.parseArgs(args);
        conf.remove("sField");
        System.out.println(conf);
        BeanMapper.mergeProperties(exProp, conf);

        DomainTest tst = BeanMapper.mapToObject(exProp, DomainTest.class);

        System.out.println(tst);

        Assert.assertEquals(exProp.getProperty("iField"), conf.getProperty("iField"));
        Assert.assertEquals(exProp.getProperty("dblField"), conf.getProperty("dblField"));
        Assert.assertNotEquals(exProp.getProperty("sField"), conf.getProperty("sField"));

        Assert.assertEquals(exProp.getProperty("sField"), tst.getsField());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Assert.assertEquals(df.parse(exProp.getProperty("dateField")), tst.getDateField());
    }

    @Test
    public void testCopyObject() {
        DomainTest tst1 = new DomainTest();
        DomainTest tst2 = new DomainTest();
        tst2.setiField(123);
        tst2.setsField("example");
        tst2.setdField(LocalDate.now());
        tst2.setBdField(BigDecimal.valueOf(2.0));
        tst2.setDateField(new Date());

        BeanMapper.copyBean(tst1, tst2);

        Assert.assertEquals(tst1, tst2);
        tst1.setDblField(1.0);
        Assert.assertNotEquals(tst1, tst2);

    }

}
