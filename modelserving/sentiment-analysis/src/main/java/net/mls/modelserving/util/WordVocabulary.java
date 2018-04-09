package net.mls.modelserving.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WordVocabulary implements ResourceLoaderAware, AutoCloseable {

    @Value("${tf.vocabLocation}")
    private String vocabLocation;

    private ResourceLoader resourceLoader;

    /**
     * This contains the word vocabulary used to train the TensorFlow model.
     */
    private final ConcurrentHashMap<String, Integer> vocabulary;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public WordVocabulary() {
        try (InputStream vocabularyInputStream = resourceLoader.getResource(vocabLocation).getInputStream()) {
            vocabulary = buildVocabulary(vocabularyInputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int[][] vectorizeSentence(String sentence) {
        int[][] vectorizedText = new int[1][128];
        String[] words = clearText(sentence).split(" ");
        for (int i = 0; i < words.length; i++) {
            Integer v = vocabulary.get(words[i]);
            vectorizedText[0][i] = (v != null) ? v : 0;
        }
        return vectorizedText;
    }

    private ConcurrentHashMap<String, Integer> buildVocabulary(InputStream input) throws IOException {
        ConcurrentHashMap<String, Integer> vocabulary = new ConcurrentHashMap<>();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            String l = buffer.readLine();
            while (l != null) {
                String p[] = l.split(",");
                if (p[1].length() > 1) {
                    vocabulary.put(p[0], Integer.valueOf(p[1]));
                }
                l = buffer.readLine();
            }
        }
        return vocabulary;
    }

    private String clearText(String sentence) {
        return sentence
                .trim()
                .replaceAll("[^A-Za-z0-9(),!?\\'\\`]", " ")
                .replaceAll("(.)\\1+", "\\1\\1")
                .replaceAll("\\'s", " \\'s")
                .replaceAll("\\'ve", " \\'ve")
                .replaceAll("n\\'t", " n\\'t")
                .replaceAll("\\'re", " \\'re")
                .replaceAll("\\'d", " \\'d")
                .replaceAll("\\'ll", " \\'ll")
                .replaceAll(",", " , ")
                .replaceAll("!", " ! ")
                .replaceAll("\\(", " \\( ")
                .replaceAll("\\)", " \\) ")
                .replaceAll("\\?", " \\? ")
                .replaceAll("\\s{2,}", " ")
                .toLowerCase();
    }

    @Override
    public void close() throws Exception {
        vocabulary.clear();
    }
}

