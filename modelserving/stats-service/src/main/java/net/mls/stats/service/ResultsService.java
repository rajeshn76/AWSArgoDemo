package net.mls.stats.service;

import net.mls.stats.domain.PerformanceResults;
import net.mls.stats.operation.GetResultsOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/performance")
public class ResultsService {

    @Autowired
    GetResultsOperation getResultsOp;


    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<PerformanceResults> getModelInfo(@PathVariable String id) {
        return new ResponseEntity<PerformanceResults>(this.getResultsOp.apply(id), HttpStatus.OK);
    }


}
