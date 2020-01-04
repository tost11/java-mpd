package de.tostsoft.mpdclient.tools;

public class Pair<T1,T2> {
    T1 key;
    T2 value;

    public Pair(T1 t1,T2 t2){
        key = t1;
        value = t2;
    }

    public T1 getKey(){
        return key;
    }

    public T2 getValue(){
        return value;
    }

    public T1 getFirst(){
        return key;
    }

    public T2 getSecond(){
        return value;
    }
}
