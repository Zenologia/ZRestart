package dev.zenologia.zrestart.config;

import java.util.List;

public record ConfigLoadResult(RestartConfig config, List<ConfigWarning> warnings, List<String> errors) {
    public boolean successful() {
        return this.errors.isEmpty();
    }

    public String errorSummary() {
        return String.join("; ", this.errors);
    }
}
