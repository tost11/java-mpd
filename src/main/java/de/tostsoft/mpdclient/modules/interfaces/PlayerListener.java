package de.tostsoft.mpdclient.modules.interfaces;

import de.tostsoft.mpdclient.modules.PlayerModule;

public interface PlayerListener {
    void changed(PlayerModule.PlayerStatus staus);
}
