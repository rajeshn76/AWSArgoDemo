package net.mls.argo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES;

public class YAMLGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> String asJson(T object) throws IOException {
        //Object to JSON in String
        String jsonInString = mapper.writeValueAsString(object);

        return jsonInString;
    }

    public static <T> String asYaml(T object) throws IOException {
        //Object to JSON in String
        String jsonInString = asJson(object);
        //JSON to YAML in String
        return asYaml(jsonInString);
    }

    public static String asYaml(String jsonString) throws IOException {
        // parse JSON
        JsonNode jsonNodeTree = mapper.readTree(jsonString);
        // save it as YAML

        YAMLFactory f = new YAMLFactory();
        f.enable(MINIMIZE_QUOTES);
        f.disable(SPLIT_LINES);

        String jsonAsYaml = new YAMLMapper(f).writeValueAsString(jsonNodeTree);

        return jsonAsYaml;
    }
}
