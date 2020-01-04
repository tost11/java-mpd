package de.tostsoft.mpdclient.modules;


import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.modules.interfaces.CustomCommandListener;
import de.tostsoft.mpdclient.tools.Pair;

import java.util.ArrayList;
import java.util.Collection;

public class CustomCommandModule extends BasicModule<CustomCommandListener> {

    int nextId;
    int handleId;

    private ArrayList<Pair<Integer,Collection<String>>> querrys = new ArrayList<>();

    public CustomCommandModule(MpdClient player) {
        super(player);
        nextId = 0;
        handleId = 0;
    }

    @Override
    public void handleResult(PlayerCommandResult result) {
        if(!querrys.isEmpty() && querrys.get(0).getKey() == result.getId()){
            //is our requested querry
            Collection<String> filters = querrys.get(0).getValue();
            querrys.remove(0);

            ArrayList<String> res = new ArrayList<>();
            if(res.isEmpty()){
                res.addAll(result.getResults());
            }else{
                for(String it_res:result.getResults()){
                    for(String it_fil:filters){
                        if(it_res.startsWith(it_fil)){
                            res.add(it_res);
                        }
                    }
                }
            }
            callListeners(res, handleId++);
        }
    }

    public void callListeners(ArrayList<String> res,int id){
        for(CustomCommandListener it: listeners){
            it.call(res,id);
        }
    }

    public int addCustomQuerry(String querr){
        return addCustomQuerry(querr,new ArrayList<>());
    }

    public int addCustomQuerry(String quer, Collection<String> filters){
        int res =  player.querry(quer);
        if(res >= 0){
            querrys.add(new Pair(res,filters));
            return nextId++;
        }
        return -1;
    }

    @Override
    public void reset() {
        querrys.clear();
        nextId =0;
        handleId =0;
    }
}
