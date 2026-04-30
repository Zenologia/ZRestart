package dev.zenologia.zrestart.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class SoundResolverTest {
    @Test
    void resolvesBukkitSoundNamesCaseInsensitively() {
        Optional<RestartConfig.SoundReference> resolved = SoundResolver.resolve("block_note_block_pling");

        assertTrue(resolved.isPresent());
        assertTrue(resolved.get().usesBukkitSound());
        assertEquals("BLOCK_NOTE_BLOCK_PLING", resolved.get().bukkitSoundName());
    }

    @Test
    void resolvesMinecraftNamespacedKeys() {
        Optional<RestartConfig.SoundReference> resolved = SoundResolver.resolve("minecraft:block.note_block.pling");

        assertTrue(resolved.isPresent());
        assertFalse(resolved.get().usesBukkitSound());
        assertEquals("minecraft:block.note_block.pling", resolved.get().namespacedKey());
    }

    @Test
    void rejectsInvalidNamespacedKeys() {
        Optional<RestartConfig.SoundReference> resolved = SoundResolver.resolve("Bad:Sound:Key");

        assertTrue(resolved.isEmpty());
    }
}
