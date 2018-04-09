package net.mls.modelserving.service;

import net.mls.modelserving.MovieView;
import net.mls.modelserving.operation.MatrixFactorizationOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class RecommenderEngineService {

    @Autowired
    private MatrixFactorizationOperation recOp;

    @RequestMapping(value = "rec/{userId}", method = RequestMethod.GET)
    public List<MovieView> getRecs(@PathVariable String userId) {
        List<MovieView> ratings = this.recOp.apply(userId);


        return  ratings;
    }
}
