package de.tostsoft.mpdclient.tools;

public class Timer
{
    boolean counting =false;
    long lastTime = 0;
    long timeLastFrame = 0;
    long countTime;

    public Timer()
    {
        Update();
    }

    public Timer(boolean counting)
    {
        Update();
        this.counting =counting;
        countTime =0;
    }

    public Timer(long StartTime)
    {
        Update();
        counting =true;
        countTime = StartTime;
    }

    public void Update()
    {
        long Actualtime = System.nanoTime();
        timeLastFrame = Actualtime- lastTime;
        lastTime = Actualtime;

        if(counting ==true)
        {
            countTime += timeLastFrame;
        }
    }

    public void UpdateAndStartCounter()
    {
        Update();
        startcounter();
    }

    public long getFrameNanosecond()
    {
        return timeLastFrame;
    }

    public double getFrameMilliseconds()
    {
        return (double) timeLastFrame /1000000;
    }

    public double getFrameSeconds()
    {
        return (double) timeLastFrame /1000000000l;
    }

    static public long getTimestampNanoseconds()
    {
        return System.nanoTime();
    }

    public long getCounterNanoseconds()
    {
        return countTime;
    }

    public double getCounterMilliseconds()
    {
        return (double) countTime /1000000;
    }
    public double getCounterSeconds()
    {
        return (double) countTime /1000000000;
    }

    public boolean isCounting()
    {
        return counting;
    }

    public void startcounter(long start)
    {
        counting = true;
        countTime = start;
    }

    public void startCounterandUpdate(long start)
    {
        Update();
        counting =true;
        countTime =start;
    }

    public void StartCounterMS(double start)
    {
        counting =true;
        countTime =(long)(start*1000000);
    }

    public void startCounterS(double start)
    {
        counting =true;
        countTime =(long)(start*1000000000);
    }

    public void updateAndStartCounterS(double start)
    {
        Update();
        counting =true;
        countTime =(long)(start*1000000000);
    }

    public void startCounterMSandUpdate(double start)
    {
        Update();
        counting =true;
        countTime =(long)(start*1000000);
    }

    public void startcounter()
    {
        counting =true;
        countTime =0;
    }

    public void startCounterandUpdate()
    {
        Update();
        counting =true;
        countTime =0;
    }

    public void resetCounter()
    {
        countTime =0;
    }
}
