package net.mls.processor.service;

import net.mls.processor.operation.ProcessorOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
public class ProcessorService {

    @Autowired
    private ProcessorOperation op;

    private Logger LOG = LoggerFactory.getLogger(ProcessorService.class);

    @RequestMapping(value = "/predict", method = RequestMethod.POST)
    public String process(@RequestBody String content) {
        LOG.info("Received {}", content);


        return op.apply(content);
    }
}
