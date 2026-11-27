package io.github.albert200000.dlangdt.common.tooling;

import java.util.List;

/**
 * Executes a list of command strings and returns the exit code (0 for success).
 */
public interface IProcessExecutor {
    int execute(List<String> commands);
}
