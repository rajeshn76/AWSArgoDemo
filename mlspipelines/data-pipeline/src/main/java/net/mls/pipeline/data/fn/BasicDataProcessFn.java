package net.mls.pipeline.data.fn;

import net.mls.pipeline.common.avro.BasicData;

import java.util.function.Function;

public class BasicDataProcessFn implements Function<String[], BasicData> {
    @Override
    public BasicData apply(String[] strings) {
        return new BasicData(strings[2],strings[1],strings[3] );
    }
}
