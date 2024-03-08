package com.shdwfghtr.asset;



import java.util.ArrayList;

public class TimeService {
    private static float TIME;
    private static final ArrayList<Timer> TIMERS = new ArrayList<>();

    public static float GetTime(){
        return TIME;
    }

    public static Timer[] getTimers() {
        Timer[] array = new Timer[TIMERS.size()];
        return TIMERS.toArray(array);
    }

    public static void Update(float delta) {
        TIME += delta;
        //This helps avoid a ConcurrentModificationException
        Timer[] activeTimers = new Timer[TIMERS.size()];
        activeTimers = TIMERS.toArray(activeTimers);

        for(Timer t : activeTimers)
            if(t.update()) TIMERS.remove(t);
    }

    public static void addTimer(Timer timer){
        TIMERS.add(timer);
    }

    public static void removeTimer(Timer timer) {
        TIMERS.remove(timer);
    }

    public static void clearTimers() {
        TIMERS.clear();
    }

    public static boolean contains(Timer timer) {
        return TIMERS.contains(timer);
    }

    public static void remove(Timer timer) {
        TIMERS.remove(timer);
    }

    public abstract static class Timer {
        public float start, duration;

        public Timer(float duration) {
            this.start = TIME;
            this.duration = duration;
        }

        public abstract boolean onCompletion();

        public boolean isComplete() {
            return TIME > start + duration;
        }

        public boolean update() {
            if(isComplete())
                return onCompletion();
            return false;
        }

        public void reset() {
            start = TIME;
            if(!TIMERS.contains(this)) TIMERS.add(this);
        }

        public void reset(float duration) {
            this.start = TIME;
            this.duration = duration;
            if(!TIMERS.contains(this)) TIMERS.add(this);
        }
    }
}
