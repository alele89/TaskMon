package com.rtes.taskmon; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewSeries;

public class ProcessInfo {
	private int pid = 0;
	private int cSec = 0;
	private long cNanoSec = 0;
	private int tSec = 0;
	private long tNanoSec = 0;
	private int rtPrio = 0;
	private int energy = 50;
	private int color = Color.rgb(rng.nextInt(255), rng.nextInt(255),
			rng.nextInt(255));
	private long curX = 0;
	private Vector<GraphViewData> utilData = new Vector<GraphViewData>();
	private Vector<GraphViewData> ctxData = new Vector<GraphViewData>();

	private static Random rng = new Random();
	private static int thickness = rng.nextInt(10);
	private static int maxData = 10000;

	public ProcessInfo() {
	}

	public ProcessInfo(int pid, int cSec, long cNanoSec, int tSec, long tNanoSec) {
		this.pid = pid;
		this.cSec = cSec;
		this.cNanoSec = cNanoSec;
		this.tSec = tSec;
		this.tNanoSec = tNanoSec;
	}

	@Override
	public String toString() {
		return "PID = " + pid + ", C = {" + cSec + ", " + cNanoSec
				+"}, T = {" + tSec + ", " + tNanoSec + "}, Energy = " + energy + " mJ";
	}

	public Vector<GraphViewData> getUtilDataVector() {
		return utilData;
	}
	public int getEnergyData() {
		String filename = "//sys//rtes//tasks//" + getPid() + "//energy";
		String content = "";
		FileInputStream fis;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(new File(filename));
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			content = br.readLine();
			if (content != null && content.compareTo("empty") != 0){
				energy = Integer.parseInt(content);
			} else {
				//System.out.println("data empty -> break ");
			}
			fis.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	

	public GraphViewSeries getUtilData() {

		if (utilData.size() > maxData) {
			utilData.clear();
		}
		getEnergyData();
		String filename = "//sys//rtes//tasks//" + getPid() + "//util";
		FileInputStream fis;
		BufferedReader br = null;
		//System.out.println("START reading util data from " + filename);

		String content = "";

		for (int i = 0; i < maxData; i++) {
			try {
				fis = new FileInputStream(new File(filename));
				InputStreamReader isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);
				content = br.readLine();

				if (content != null && content.compareTo("empty") != 0) {
					long raw = Long.parseLong(content);
					long tTotal = gettTotal() / 100;
					long util = (raw) / tTotal;

					utilData.add(new GraphViewData(curX, util));

					System.out.println("x: " + curX + ", util: " + util
							+ ", raw: " + raw + ", tTotal: " + tTotal);

				} else if (content.compareTo("empty") != 0
						&& Integer.valueOf(content) < 0) {

					for (int j = 0; j < Math.abs(Integer.valueOf(content)); j++) {
						utilData.add(new GraphViewData(curX, 0));
					}
				} else {
					//System.out.println("data empty -> break ");
					break;
				}
				curX += (gettTotal() / 1000000);
				fis.close();

			} catch (FileNotFoundException e) {

				//System.out.println("Process file not found...");
				return null;

			} catch (IOException e) {

				// utilData.add(new GraphViewData(curX, rng.nextInt(100)));
				//System.out.println("Process file not found...");
			}
		}

		//System.out.println("END reading util");

		GraphViewData[] rawData = new GraphViewData[utilData.size()];

		for (int i = 0; i < utilData.size(); i++) {
			rawData[i] = utilData.get(i);
		}
		return new GraphViewSeries(Integer.valueOf(pid).toString(),
				new GraphViewSeriesStyle(color, thickness), rawData);

	}

	public GraphViewSeries getCtxData() {

		if (ctxData.size() > maxData) {
			ctxData.clear();
		}
		getEnergyData();
		String filename = "//sys//rtes//tasks//" + getPid() + "//ctx";
		FileInputStream fis;
		BufferedReader br = null;
		//System.out.println("START reading ctx data from " + filename);

		String content = "";

		for (int i = 0; i < maxData; i++) {

			try {

				fis = new FileInputStream(new File(filename));
				InputStreamReader isr = new InputStreamReader(fis);
				br = new BufferedReader(isr); 
				content = br.readLine();
				
				//System.out.println("content= " + content);

				if (content != null && content.compareTo("empty") != 0) {
					
					List<String> ctxStrings = Arrays.asList(content.split(","));
					
					if(ctxStrings.size() < 2)
						break;
					
					long ctxStart = Long.parseLong(ctxStrings.get(0)) / 1000000;
					long ctxEnd = Long.parseLong(ctxStrings.get(1)) / 1000000;
					
					//System.out.println("ctxStart: " + ctxStart + " -  ctxEnd: " + ctxEnd);
					
					curX = ctxStart;
					
					ctxData.add(new GraphViewData(curX , 0));
					ctxData.add(new GraphViewData(curX, 100));
					
					curX = ctxEnd ;
					
					ctxData.add(new GraphViewData(curX, 100));
					ctxData.add(new GraphViewData(curX , 0));

					//System.out.println("x: " + curX + ", ctx: " + ctx + ", raw: " + raw + ", tTotal: " + tTotal);

				} else if (content.compareTo("empty") != 0
						&& Integer.valueOf(content) < 0) {

					for (int j = 0; j < Math.abs(Integer.valueOf(content)); j++) {
						ctxData.add(new GraphViewData(curX, 0));
					}
				} else {
					//System.out.println("data empty -> break ");
					break;
				}
				curX += (gettTotal() / 1000000);
				fis.close();

			} catch (FileNotFoundException e) {

				//System.out.println("Process file not found...");
				return null;

			} catch (IOException e) {

				//System.out.println("Process file not found...");
			}
		}

		//System.out.println("END reading util");

		GraphViewData[] rawData = new GraphViewData[ctxData.size()];

		for (int i = 0; i < ctxData.size(); i++) {
			rawData[i] = ctxData.get(i);
		}
		return new GraphViewSeries(Integer.valueOf(pid).toString(),
				new GraphViewSeriesStyle(color, thickness), rawData);

	}

	public int getColor() {
		return this.color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public long gettTotal() {
		long tTotal = ((long) gettSec() * 1000000000) + gettNanoSec();
		return tTotal;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getPid() {
		return pid;
	}

	public int getcSec() {
		return cSec;
	}

	public void setcSec(int cSec) {
		this.cSec = cSec;
	}

	public long getcNanoSec() {
		return cNanoSec;
	}

	public void setcNanoSec(long cNanoSec) {
		this.cNanoSec = cNanoSec;
	}

	public int gettSec() {
		return tSec;
	}

	public void settSec(int tSec) {
		this.tSec = tSec;
	}

	public long gettNanoSec() {
		return tNanoSec;
	}

	public void settNanoSec(long tNanoSec) {
		this.tNanoSec = tNanoSec;
	}

	public int getRtPrio() {
		return rtPrio;
	}

	public void setRtPrio(int rtPrio) {
		this.rtPrio = rtPrio;
	}

}
