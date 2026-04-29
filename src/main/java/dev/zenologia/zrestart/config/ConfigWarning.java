package dev.zenologia.zrestart.config;

import dev.zenologia.zrestart.placeholders.PlaceholderContext;

public record ConfigWarning(String messagePath, PlaceholderContext placeholders) {
}
