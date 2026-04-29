package dev.zenologia.zrestart;

import dev.zenologia.zrestart.command.ZRestartCommandRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZRestartBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> ZRestartCommandRegistrar.register(event.registrar())
        );
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new ZRestartPlugin();
    }
}
