package player.modules.interfaces;

import player.modules.DatabaseModule;

public interface DatabaseListener {
    void chagned(DatabaseModule.EN_MusikDataType type);
}
