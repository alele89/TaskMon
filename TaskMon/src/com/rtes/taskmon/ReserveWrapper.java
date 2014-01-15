package com.rtes.taskmon;

public class ReserveWrapper {
	public static native int setreserve(int pid, int c_sec, long c_nsec,
			int t_sec, long t_nsec, int rt_prio);

	public static native int cancelreserve(int pid);

	static {
		System.loadLibrary("reserve");
	}
}
