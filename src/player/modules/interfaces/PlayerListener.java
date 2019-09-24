package player.modules.interfaces;

import player.modules.PlayerModule;

public interface PlayerListener {
    void changed(PlayerModule.PlayerStatus staus);
}
