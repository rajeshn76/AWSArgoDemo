package net.mls.modelserving.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

import static org.apache.commons.io.IOUtils.buffer;
import static org.apache.commons.io.IOUtils.toByteArray;

@Service("tf")
public class TensorFlowOperation implements ResourceLoaderAware, AutoCloseable, Function<Map<String, Object>, Tensor> {

    private static final Log logger = LogFactory.getLog(TensorFlowOperation.class);

    private Graph graph;

    private ResourceLoader resourceLoader;

    @Value("${tf.modelLocation}")
    private String modelLocation;

    @Value("${tf.outputName}")
    private String outputName;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void init() throws IOException {
        try (InputStream is = resourceLoader.getResource(modelLocation).getInputStream()) {
            graph = new Graph();
            logger.info("Loading TensorFlow graph model: " + modelLocation);
            graph.importGraphDef(toByteArray(buffer(is)));
            logger.info("TensorFlow Graph Model Ready To Serve!");
        }
    }

    @Override
    public Tensor apply(Map<String, Object> feeds) {
        try (Session session = new Session(graph)) {
            Session.Runner runner = session.runner();

            // Keep tensor references to release them in the finally block
            Tensor[] feedTensors = new Tensor[feeds.size()];
            try {
                int i = 0;
                for (Map.Entry<String, Object> e : feeds.entrySet()) {
                    String feedName = e.getKey();
                    feedTensors[i] = Tensor.create(e.getValue());
                    runner = runner.feed(feedName, feedTensors[i]);
                    i++;
                }
                return runner.fetch(outputName).run().get(0);
            }
            finally {
                // Release all feed tensors
                for (Tensor tensor : feedTensors) {
                    if (tensor != null) {
                        tensor.close();
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        logger.info("Close TensorFlow Graph!");
        if (graph != null) {
            graph.close();
        }
    }
}
