package net.mls.argo;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShellCommandExecutorTest {
    @Test
    public void executeKubernetesCommand() throws Exception {
        String output = ShellCommandExecutor.execute("kubectl version");
        System.out.println(output);
        assertTrue(output.contains("Client Version:"));
    }
}
