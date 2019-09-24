package player.modules.interfaces;

import player.modules.OptionsModule;

public interface OptionListener {
    void changed(OptionsModule.OptionStatus status, int value);
}
