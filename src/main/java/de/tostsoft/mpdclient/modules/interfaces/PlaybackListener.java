package de.tostsoft.mpdclient.modules.interfaces;

import de.tostsoft.mpdclient.modules.PlaybackModule;

public interface PlaybackListener{
    void changed(PlaybackModule.PlaybackEvent ev, Object ptr);
}
