package com.rtes.taskmon;

import com.rtes.taskmon.GraphActivity.AxisMode;

public class SingleAxisMode {
	
	
	private AxisMode axisMode;
	private int msec;
	private String Description;

	
	public SingleAxisMode(AxisMode axisMode, int msec, String description) {
		super();
		this.axisMode = axisMode;
		this.msec = msec;
		Description = description;
	}
	
	
	public AxisMode getAxisMode() {
		return axisMode;
	}
	public void setAxisMode(AxisMode axisMode) {
		this.axisMode = axisMode;
	}
	public int getMsec() {
		return msec;
	}
	public void setMsec(int msec) {
		this.msec = msec;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	
	
	
	
	

}
