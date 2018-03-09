package net.mls.argo.template;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class YAMLGeneratorTest {

    @Test
    public void checkJsonConversion() throws Exception {
        User tester = createTestUser();

        // assert statements
        assertEquals("{\"name\":\"Ogra\",\"age\":25,\"messages\":[\"hello argo\",\"hello aws\",\"hello jackson\"]}", YAMLGenerator.asJson(tester));
    }

    @Test
    public void checkYamlConversion() throws Exception {
        User tester = createTestUser();

        String expected = "---\n" +
                "name: \"Ogra\"\n" +
                "age: 25\n" +
                "messages:\n" +
                "- \"hello argo\"\n" +
                "- \"hello aws\"\n" +
                "- \"hello jackson\"\n";
        // assert statements
        assertEquals(expected, YAMLGenerator.asYaml(tester));
    }

    private User createTestUser() {
        List<String> msgs = new ArrayList();
        msgs.add("hello argo");
        msgs.add("hello aws");
        msgs.add("hello jackson");

        User tester = new User("Ogra", 25, msgs);

        return tester;
    }

    public class User {
        public String name;
        public int age;
        public List<String> messages;

        public User() {
        }

        public User(String name, int age, List<String> messages) {
            this.name = name;
            this.age = age;
            this.messages = messages;
        }
    }
}
