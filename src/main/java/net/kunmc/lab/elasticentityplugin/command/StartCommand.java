package net.kunmc.lab.elasticentityplugin.command;

import dev.kotx.flylib.command.Command;
import dev.kotx.flylib.command.CommandContext;
import net.kunmc.lab.elasticentityplugin.ElasticEntityPlugin;
import net.kunmc.lab.elasticentityplugin.Game;
import org.bukkit.entity.Player;

public class StartCommand extends Command {
    public StartCommand() {
        super("start");
    }

    @Override
    public void execute(CommandContext ctx) {
        Game game = ElasticEntityPlugin.game;
        Player p = ctx.getPlayer();

        if (p == null) {
            ctx.fail("このコマンドはプレイヤーから実行してください.");
            return;
        }

        if (game.start(p.getLocation())) {
            ctx.success("ゲームをスタートしました.");
        } else {
            ctx.fail("ゲームはすでにスタートされています.");
        }
    }
}

