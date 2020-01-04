package de.tostsoft.mpdclient.modules.interfaces;

import de.tostsoft.mpdclient.modules.DatabaseModule;

public interface DatabaseListener {
    void chagned(DatabaseModule.EN_MusikDataType type);
}
