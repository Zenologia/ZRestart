package dev.zenologia.zrestart.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;

public final class ZRestartCommandRegistrar {
    private ZRestartCommandRegistrar() {
    }

    public static void register(Commands commands) {
        LiteralCommandNode<CommandSourceStack> node = new ZRestartCommand().build();
        commands.register(node, "ZRestart automatic restart scheduler and countdown controls.", List.of());
    }
}
