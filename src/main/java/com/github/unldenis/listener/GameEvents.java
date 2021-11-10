package com.github.unldenis.listener;

import com.github.unldenis.SquidGame;
import com.github.unldenis.gamelogic.MainGame;
import com.github.unldenis.helper.Events;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;


/**
 * Class that records the events of the game in general.
 * My library is used to record and manage events.
 * @see <a href="https://github.com/unldenis/UnldenisHelper">https://github.com/unldenis/UnldenisHelper</a>
 */
public class GameEvents {

    private final SquidGame plugin;

    public GameEvents(@NonNull SquidGame plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        /*
            PlayerDeathEvent
         */
        Events.subscribe(PlayerDeathEvent.class).handler(event -> {
            Player player = event.getEntity();
            Optional<MainGame> mainGameOptional = plugin.getGameManager().find(player);
            if(mainGameOptional.isEmpty()) return;
            event.setDeathMessage(null);
            MainGame mainGame = mainGameOptional.get();

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.spigot().respawn();
                    mainGame.eliminate(player);
                }
            }
                    .runTaskLater(plugin, 1L);
        }).bindWith(plugin);
        /*
            PlayerQuitEvent
         */
        Events.subscribe(PlayerQuitEvent.class).handler(event -> {
            Player player = event.getPlayer();
            Optional<MainGame> mainGameOptional = plugin.getGameManager().find(player);
            if(mainGameOptional.isEmpty()) {
                mainGameOptional = plugin.getGameManager().findSpec(player);

                //if is spectating
                if(mainGameOptional.isPresent()) {
                    event.setQuitMessage(null);
                    MainGame mainGame = mainGameOptional.get();
                    mainGame.leaveSpec(player);
                }
                return;
            }
            event.setQuitMessage(null);
            MainGame mainGame = mainGameOptional.get();
            mainGame.leave(player);
        }).bindWith(plugin);
    }

}
