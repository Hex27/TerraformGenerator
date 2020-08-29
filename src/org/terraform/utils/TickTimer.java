
package org.terraform.utils;

import java.util.HashMap;

public class TickTimer {
	
	public static HashMap<String, Long> timings = new HashMap<>();
	
	private String key;
	private long start = 0;
	private long duration = -1;
	
	public TickTimer(String key){
		this.key = key;
		this.start = System.currentTimeMillis();
	}
	
	public void finish(){
		duration = System.currentTimeMillis() - this.start;
		if(!timings.containsKey(key))
			TickTimer.timings.put(key, duration);
		else
			TickTimer.timings.put(key, duration + timings.get(key));
	}

}
