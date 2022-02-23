package com.shdwfghtr.explore;

public class Timer {
	public float start, duration;
	
	public Timer(float duration) {
		this.start = Asset.TIME;
		this.duration = duration;
	}

	public boolean onCompletion() { return true; }
    
    public boolean isComplete() {
        return Asset.TIME > start + duration;
    }

	public boolean update() {
        return (Asset.TIME > start + duration) && onCompletion();
	}
    
	public void reset() {
		start = Asset.TIME;
		if(!Asset.TIMERS.contains(this)) Asset.TIMERS.add(this);
	}

    public void reset(float duration) {
        this.start = Asset.TIME;
        this.duration = duration;
        if(!Asset.TIMERS.contains(this)) Asset.TIMERS.add(this);
    }
}
