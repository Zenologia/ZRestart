package dev.zenologia.zrestart.config;

public record ReloadDecision(boolean accepted, RestartConfig activeConfig, String error) {
}
