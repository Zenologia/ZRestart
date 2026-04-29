package dev.zenologia.zrestart.restart;

import dev.zenologia.zrestart.ZRestartPlugin;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.util.TextRenderer;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;

public final class RestartScriptChecker {
    private final ZRestartPlugin plugin;
    private final TextRenderer renderer;

    public RestartScriptChecker(ZRestartPlugin plugin, TextRenderer renderer) {
        this.plugin = plugin;
        this.renderer = renderer;
    }

    public void check() {
        File serverRoot = this.plugin.getServer().getWorldContainer();
        File spigotYml = new File(serverRoot, "spigot.yml");
        if (!spigotYml.exists()) {
            this.renderer.console("console.restart-script-missing", PlaceholderContext.empty());
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(spigotYml);
        String restartScript = config.getString("settings.restart-script", "");
        if (restartScript == null || restartScript.isBlank()) {
            this.renderer.console("console.restart-script-missing", PlaceholderContext.empty());
            return;
        }

        File script = new File(restartScript);
        if (!script.isAbsolute()) {
            script = new File(serverRoot, restartScript);
        }
        if (!script.exists()) {
            this.renderer.console(
                "console.restart-script-not-found",
                PlaceholderContext.builder()
                    .put("entry", restartScript)
                    .build()
            );
        }
    }
}
