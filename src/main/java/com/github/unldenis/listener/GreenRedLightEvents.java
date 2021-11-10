package com.github.unldenis.listener;

import com.github.unldenis.SquidGame;
import com.github.unldenis.gamelogic.GameStatus;
import com.github.unldenis.gamelogic.game.GreenRedLightGame;
import com.github.unldenis.gamelogic.MainGame;
import com.github.unldenis.helper.Events;
import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

/**
 * Class that records the events of the GreenRedLightGames.
 * My library is used to record and manage events.
 * @see <a href="https://github.com/unldenis/UnldenisHelper">https://github.com/unldenis/UnldenisHelper</a>
 */
public class GreenRedLightEvents {

    private final SquidGame plugin;

    public GreenRedLightEvents(@NonNull SquidGame plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        /*
            PlayerMoveEvent
         */
        Events.subscribe(PlayerMoveEvent.class)
        .filter(event -> event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ())
        .handler(event -> {
            Player player = event.getPlayer();
            Optional<MainGame> mainGameOptional = plugin.getGameManager().find(player);
            if(mainGameOptional.isEmpty()) return;
            MainGame mainGame = mainGameOptional.get();
            if(mainGame.getGameStatus().equals(GameStatus.PLAYING)) {
                if(mainGame.getCurrentGame() instanceof GreenRedLightGame) {
                    GreenRedLightGame greenRedLight = (GreenRedLightGame) mainGame.getCurrentGame();
                    if(greenRedLight.isCheckPlayers() && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                        greenRedLight.fire(player);
                    }else{
                        if(greenRedLight.getEndRegion()!=null)
                            //because if he is checking you can't move so you can't arrive at end
                            greenRedLight.checkEnd(player);
                    }
                }
            }
        }).bindWith(plugin);
    }





}
