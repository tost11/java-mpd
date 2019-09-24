package player.modules.interfaces;

import player.modules.StatModule;

public interface StatListener {
    void changed(StatModule.StatStatus status, int value);
}
