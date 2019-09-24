package player.modules;

import player.modules.interfaces.OptionListener;
import player.Player;

public class OptionsModule extends BasicModule<OptionListener> {

    public enum OptionStatus{
        RANDOM,
        REPEAT,
        SINGLE,
        XFADE,
        VOLUME,
        UPDATE;

        @Override
        public String toString() {
            if(this == UPDATE){
                return "updating_db";
            }
            return super.toString();//rest works fine;
        }
    }

    private Integer options[] = new Integer[OptionStatus.values().length];

    public OptionsModule(Player player){
        super(player);
        OptionStatus.values();
    }

    @Override
    public void handleResult(PlayerCommandResult result){
        if(result.getCommand().equals("status")){
            boolean db_up = false;
            for(String line:result.getResults()) {
                for(int i = 0; i < OptionStatus.values().length; i++) {
                    if (line.startsWith(OptionStatus.values()[i].toString().toLowerCase())) {
                        int val = Integer.parseInt(line.split(":")[1].trim());
                        if(i == OptionStatus.UPDATE.ordinal()){
                            db_up = true;
                            val*=-1;//set negative -> so update is in progress
                        }
                        if (options[i] == null) {
                            options[i] = val;
                            if (player.getCallLisntenersByInit()) {
                                callListeners(OptionStatus.values()[i], val);
                            }
                        } else {
                            if (options[i] != val) {
                                options[i] = val;
                                callListeners(OptionStatus.values()[i], val);
                            }
                        }
                    }
                }
            }

            if(!db_up && options[OptionStatus.UPDATE.ordinal()] < 0){//need to be chacked manmulay because is not set if finished
                options[OptionStatus.UPDATE.ordinal()] = options[OptionStatus.UPDATE.ordinal()] * -1;
                callListeners(OptionStatus.UPDATE, options[OptionStatus.UPDATE.ordinal()]);
            }
        }
    }

    private void callListeners(OptionStatus stat, int val){
        for(OptionListener lis: listeners){
            lis.changed(stat,val);
        }
    }


    public boolean isRandom(){
        return getStatus(OptionStatus.RANDOM) != null && getStatus(OptionStatus.RANDOM) == 1;
    }

    public boolean isRepeat(){
        return getStatus(OptionStatus.REPEAT) != null && getStatus(OptionStatus.REPEAT) == 1;
    }

    public boolean isSingle(){
        return getStatus(OptionStatus.SINGLE) != null && getStatus(OptionStatus.SINGLE) == 1;
    }

    public Integer getCrossfade(){
        return getStatus(OptionStatus.XFADE);
    }

    public Integer getVolume(){
        return getStatus(OptionStatus.VOLUME);
    }

    public Integer getStatus(OptionStatus stats){
        return options[stats.ordinal()];
    }

    public void querryRandom(boolean val){
        querryStatus(OptionStatus.RANDOM,val?1:0);
    }

    public void querrySingle(boolean val){
        querryStatus(OptionStatus.SINGLE,val?1:0);
    }

    public void querryRepeat(boolean val){
        querryStatus(OptionStatus.REPEAT,val?1:0);
    }

    public void querryCrossfade(int val){
        querryStatus(OptionStatus.XFADE,val);
    }

    public void querryVolume(int val){
        player.Querry("setvol "+val);
    }

    public void querryStatus(OptionStatus stat, int val){
        player.Querry(stat.toString().toLowerCase()+" "+val);
    }


    @Override
    public void reset() {
        for(int i = 0; i< options.length; i++){
            options[i]=null;
        }
        options[OptionStatus.UPDATE.ordinal()] = 0;
    }
}
