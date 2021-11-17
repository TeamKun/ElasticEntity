package net.kunmc.lab.elasticentityplugin.command;

import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;
import net.kunmc.lab.elasticentityplugin.ElasticEntityPlugin;
import net.kunmc.lab.elasticentityplugin.Game;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop");
    }

    @Override
    public void execute(CommandContext ctx) {
        Game game = ElasticEntityPlugin.game;

        if (game.stop()) {
            ctx.success("ゲームを中断しました.");
        } else {
            ctx.fail("ゲームは実行中ではありませんでした.");
        }
    }
}
