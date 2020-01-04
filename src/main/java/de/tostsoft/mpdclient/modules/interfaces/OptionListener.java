package de.tostsoft.mpdclient.modules.interfaces;

import de.tostsoft.mpdclient.modules.OptionsModule;

public interface OptionListener {
    void changed(OptionsModule.OptionStatus status, int value);
}
