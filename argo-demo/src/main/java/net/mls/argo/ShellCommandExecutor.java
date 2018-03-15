package net.mls.argo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class ShellCommandExecutor {

    public static String execute(String command) throws Exception {
        StringBuffer output = new StringBuffer();

        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }

        return output.toString();
    }
}
