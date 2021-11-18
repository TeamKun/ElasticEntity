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

        int status = game.start(p.getLocation());
        if (status == 0) {
            ctx.success("ゲームをスタートしました.");
        } else if (status == 1) {
            ctx.fail("ゲームはすでにスタートされています.");
        } else {
            ctx.fail("ゲームの参加者が0人だったためスタートされませんでした.");
        }
    }
}

