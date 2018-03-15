package net.mls.argo;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShellCommandExecutorTest {
    @Test
    public void executePingCommand() throws Exception {
        String output = ShellCommandExecutor.execute("ping google.com");
        System.out.println(output);
        assertTrue(output.contains("google.com"));
        assertTrue(output.toLowerCase().contains("ttl="));
        assertTrue(output.toLowerCase().contains("packets"));
        assertTrue(output.toLowerCase().contains("received"));
    }
}
