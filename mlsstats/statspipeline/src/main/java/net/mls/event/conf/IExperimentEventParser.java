package net.mls.event.conf;

import net.mls.event.ExperimentEvent;

public interface IExperimentEventParser {
    ExperimentEvent parse(String payload);
}
