package net.mls.argo;

import org.junit.Test;

import static org.junit.Assert.*;

public class ShellCommandExecutorTest {
    @Test
    public void executePingCommand() throws Exception {
        String output = ShellCommandExecutor.execute("ping google.com");
        System.out.println(output);
        assertTrue(output.contains("Packets: Sent = 4, Received = 4, Lost = 0 (0% loss)"));
    }
}
