package com.muyuanjin.feel.lang.type;

import com.muyuanjin.feel.parser.ParserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.beans.Introspector;

/**
 * @author muyuanjin
 */
public class RecordContextTest {
    @Test
    void test_2024_11_04_16_14_09() throws IntrospectionException {
        var descriptors = Introspector.getBeanInfo(MyPOJO.class, Object.class).getPropertyDescriptors();
        Assertions.assertEquals(1, descriptors.length);
        FContext fContext = FContext.of(MyPOJO.class);
        Assertions.assertTrue(fContext.getMembers().containsKey("name"));
        Assertions.assertTrue(fContext.getMembers().containsKey("age"));
        Assertions.assertTrue(fContext.getMembers().containsKey("email"));
        Assertions.assertTrue(fContext.getMembers().containsKey("named"));

        MyPOJO myPOJO = new MyPOJO("a", 22, "eee");

        Assertions.assertEquals(myPOJO.name(), ParserUtil.accessMember(myPOJO, "name"));
        Assertions.assertEquals(myPOJO.age(), ParserUtil.accessMember(myPOJO, "age"));
        Assertions.assertEquals(myPOJO.email(), ParserUtil.accessMember(myPOJO, "email"));
        Assertions.assertEquals(myPOJO.getNamed(), ParserUtil.accessMember(myPOJO, "named"));
    }

    public record MyPOJO(String name, Integer age, String email) {
        public Boolean getNamed() {
            return name != null;
        }
    }

}
