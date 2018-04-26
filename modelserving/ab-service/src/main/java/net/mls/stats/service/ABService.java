package net.mls.stats.service;

import net.mls.stats.domain.Variation;
import net.mls.stats.operation.GetComparisonOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;




@RestController
@RequestMapping(value = "/ab")
public class ABService {

    @Autowired
    GetComparisonOperation getComparisonOp;


    @RequestMapping(method = RequestMethod.GET, value = "/compare/{model1}/{model2}")
    public ResponseEntity<List<Variation>> getComparison(@PathVariable String model1,@PathVariable String model2) {
        System.out.println(model1);

        return new ResponseEntity<List<Variation>>(this.getComparisonOp.apply(Arrays.asList(model1,model2)), HttpStatus.OK);
    }

}
