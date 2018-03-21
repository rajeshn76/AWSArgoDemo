package net.mls.argo.util;

import net.mls.argo.util.ShellCommandExecutor;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShellCommandExecutorTest {
    @Test
    public void executeArgoCommand() throws Exception {
        ShellCommandExecutor.execute("argo submit ../pipeline-docker.yaml");
        String output = ShellCommandExecutor.execute("argo list --running");
        System.out.println(output);
        assertTrue(output.contains("mls-pipelines-direct"));
    }

    @Test
    public void executeKubernetesCommand() throws Exception {
        String output = ShellCommandExecutor.execute("kubectl version");
        System.out.println(output);
        assertTrue(output.contains("Client Version:"));
    }
}
