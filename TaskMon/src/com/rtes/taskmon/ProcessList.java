package com.rtes.taskmon;

import java.util.Vector;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class ProcessList extends Vector<ProcessInfo> implements Parcelable {

	private static final long serialVersionUID = -1843579755992305343L;

	public ProcessList() {

	}

	public ProcessList(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.clear();

		int size = in.readInt();

		// Order is fundamental
		for (int i = 0; i < size; i++) {

			ProcessInfo p = new ProcessInfo();
			p.setPid(in.readInt());
			p.setcSec(in.readInt());
			p.setcNanoSec(in.readLong());
			p.settSec(in.readInt());
			p.settNanoSec(in.readLong());
			p.setRtPrio(in.readInt());
			p.setColor(in.readInt());

			this.add(p);
		}
	}

	public static final Parcelable.Creator<ProcessList> CREATOR = new Parcelable.Creator<ProcessList>() {
		public ProcessList createFromParcel(Parcel in) {
			return new ProcessList(in);
		}

		public ProcessList[] newArray(int arg0) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int size = this.size();
		dest.writeInt(size);

		for (int i = 0; i < size; i++) {

			ProcessInfo pr = this.get(i);

			dest.writeInt(pr.getPid());
			dest.writeInt(pr.getcSec());
			dest.writeLong(pr.getcNanoSec());
			dest.writeInt(pr.gettSec());
			dest.writeLong(pr.gettNanoSec());
			dest.writeInt(pr.getRtPrio());
			dest.writeInt(pr.getColor());

		}
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	
	
	
	public ProcessInfo getByPID(int pid) {
		
		Iterator<ProcessInfo> it = this.iterator();
		ProcessInfo  curProcessInfo;
		while (it.hasNext()) {
			curProcessInfo = it.next();
			if(curProcessInfo.getPid() == pid) {
				
				System.out.println("getByPID(" + pid + ") = " + curProcessInfo.toString());
				return curProcessInfo;
				
			}
		}
		return null;
	}
	
	
	
	public long getMinT() { 
		
		
		if(this.isEmpty())
			return Long.MAX_VALUE;
		
		long minT = this.get(0).gettTotal();
		
		for(ProcessInfo process : this) {
			
			if(process.gettTotal() < minT)
				minT = process.gettTotal();
		}
		return minT;
	}
	
public long getMaxT() { 
		
		
		if(this.isEmpty())
			return Long.MAX_VALUE;
		
		long maxT = this.get(0).gettTotal();
		//System.out.println("maxT: " + maxT );
		
		for(ProcessInfo process : this) {
			
			if(process.gettTotal() > maxT)
				maxT = process.gettTotal();
		}
		
		return maxT;
	}
	
	
	

}
