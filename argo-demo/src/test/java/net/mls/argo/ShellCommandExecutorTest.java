package net.mls.argo;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShellCommandExecutorTest {
    @Test
    public void executeArgoCommand() throws Exception {
        String output = ShellCommandExecutor.execute("argo version");
        System.out.println(output);
    }

    @Test
    public void executeKubernetesCommand() throws Exception {
        String output = ShellCommandExecutor.execute("kubectl version");
        System.out.println(output);
        assertTrue(output.contains("Client Version:"));
    }
}
