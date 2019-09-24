package player.modules.interfaces;

import player.modules.PlaybackModule;

public interface PlaybackListener{
    void changed(PlaybackModule.PlaybackEvent ev,Object ptr);
}
