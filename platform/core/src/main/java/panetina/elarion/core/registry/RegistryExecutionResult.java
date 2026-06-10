package panetina.elarion.core.registry;

import java.util.List;

public record RegistryExecutionResult(
        boolean success,
        String message,
        List<Runnable> serverTasks
) {
    public RegistryExecutionResult {
        message = message == null ? "" : message;
        serverTasks = serverTasks == null ? List.of() : List.copyOf(serverTasks);
    }

    public static RegistryExecutionResult ok() {
        return new RegistryExecutionResult(true, "", List.of());
    }

    public static RegistryExecutionResult ok(String message) {
        return new RegistryExecutionResult(true, message, List.of());
    }

    public static RegistryExecutionResult failure(String message) {
        return new RegistryExecutionResult(false, message, List.of());
    }

    public RegistryExecutionResult withServerTask(Runnable task) {
        if (task == null) return this;
        java.util.ArrayList<Runnable> tasks = new java.util.ArrayList<>(serverTasks);
        tasks.add(task);
        return new RegistryExecutionResult(success, message, tasks);
    }
}
