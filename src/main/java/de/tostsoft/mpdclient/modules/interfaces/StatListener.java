package de.tostsoft.mpdclient.modules.interfaces;

import de.tostsoft.mpdclient.modules.StatModule;

public interface StatListener {
    void changed(StatModule.StatStatus status, int value);
}
