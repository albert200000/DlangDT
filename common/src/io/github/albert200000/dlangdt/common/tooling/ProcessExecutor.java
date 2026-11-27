package io.github.albert200000.dlangdt.common.tooling;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessExecutor implements IProcessExecutor {
    @Override
    public int execute(List<String> commands) {
        try {
            var pb = new ProcessBuilder(commands);
            var proc = pb.start();
            try {
                proc.waitFor(2000, TimeUnit.SECONDS);
                return proc.exitValue();
            } catch (InterruptedException e) {
                proc.destroyForcibly();
                return -1;
            }
        } catch (IOException e) {
            return -1;
        }
    }
}
