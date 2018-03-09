package net.mls.pipeline.feature.fn;

import net.mls.pipeline.feature.avro.DataModel;

import java.util.function.Function;

/**
 * Created by char on 1/31/18.
 */
public class DataModelStringifyFn implements Function<DataModel, String> {
    @Override
    public String apply(DataModel dataModel) {
        return dataModel.getText()+","+dataModel.getAfterRelease()+","+dataModel.getVersion()+","+dataModel.getLabel();
    }
}
