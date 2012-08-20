package test.level0;

import javax.realtime.*;

public final class CyclicSchedule {
	
	public final static class Frame {
		
		private RelativeTime duration;
		private Task[] taskSet;
		
		public Frame(RelativeTime duration, Task[] taskSet) {
			this.duration = duration;
			this.taskSet = taskSet;
		}

		final RelativeTime getDuration() {
			return duration;
		}

		final Task[] getTaskSet() {
			return taskSet;
		}
	}
	
	Frame[] frames;

	public CyclicSchedule(Frame[] frames) {
		this.frames = frames; 
	}
	
	RelativeTime cycleDuration = new RelativeTime();

	final RelativeTime getCycleDuration() {
		
		for(int i=0; i<frames.length; i++){
			cycleDuration.add(frames[i].getDuration(), cycleDuration);
		}
		return cycleDuration;
	}

	final Frame[] getFrames() {
		return frames;
	}
}
