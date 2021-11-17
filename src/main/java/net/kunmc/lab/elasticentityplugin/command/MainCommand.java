package net.kunmc.lab.elasticentityplugin.command;

import dev.kotx.flylib.command.Command;
import net.kunmc.lab.configlib.command.ConfigCommand;
import org.jetbrains.annotations.NotNull;

public class MainCommand extends Command {
    public MainCommand(@NotNull String name, ConfigCommand configCommand) {
        super(name);
        children(configCommand, new StartCommand());
    }
}
