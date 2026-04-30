package dev.zenologia.zrestart.config;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;

public final class SoundResolver {
    private SoundResolver() {
    }

    static Optional<RestartConfig.SoundReference> resolve(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String trimmed = raw.trim();
        String bukkitSoundName = resolveBukkitSoundName(trimmed);
        if (bukkitSoundName != null) {
            return Optional.of(new RestartConfig.SoundReference(trimmed, bukkitSoundName, null));
        }

        NamespacedKey key = NamespacedKey.fromString(trimmed);
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(new RestartConfig.SoundReference(trimmed, null, key.asString()));
    }

    public static Optional<Sound> bukkitSound(String bukkitSoundName) {
        try {
            Field field = Sound.class.getField(bukkitSoundName);
            if (field.getType() != Sound.class) {
                return Optional.empty();
            }
            return Optional.of((Sound) field.get(null));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            return Optional.empty();
        }
    }

    private static String resolveBukkitSoundName(String raw) {
        String fieldName = raw.trim().toUpperCase(Locale.ROOT);
        try {
            Field field = Sound.class.getField(fieldName);
            if (field.getType() != Sound.class) {
                return null;
            }
            return fieldName;
        } catch (NoSuchFieldException ex) {
            return null;
        }
    }
}
