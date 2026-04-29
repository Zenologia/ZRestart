package dev.zenologia.zrestart.config;

public final class ReloadDecider {
    public ReloadDecision decide(RestartConfig previousConfig, ConfigLoadResult candidate) {
        if (candidate.successful()) {
            return new ReloadDecision(true, candidate.config(), "");
        }
        return new ReloadDecision(false, previousConfig, candidate.errorSummary());
    }
}
