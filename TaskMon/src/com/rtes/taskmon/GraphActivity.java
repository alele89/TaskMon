package com.rtes.taskmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView.BufferType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class GraphActivity extends Activity {

	Vector<ProcessInfo> al1 = new Vector<ProcessInfo>();
	private static ArrayAdapter<ProcessInfo> pListAA;
	private static ProcessList pList;
	TextView tv1;

	String totalE = "0";

	// x axis parameters
	static int xLength = 20;
	static long xStep = 1;
	// update graph every X ms
	static long graphUpdateInterval = 1000;

	static long startX = 0;
	static long endX;
	static long offsetX;

	GraphViewSeries graphViewSeriesData = null;

	private GraphMode graphMode = GraphMode.CONTEXTSWITCH;

	private enum GraphMode {
		UTILIZATION, CONTEXTSWITCH;
	}

	public enum AxisMode {
		AUTO, MSEC_100, MSEC_500, SEC_1, SEC_2, SEC_5, SEC_10, SEC_20;
	}

	static HashMap<AxisMode, SingleAxisMode> axisModeMap = null;
	private SingleAxisMode axisMode;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph);

		tv1 = (TextView) findViewById(R.id.textView3);
		tv1.setText("0");

		Bundle b = getIntent().getExtras(); // Get the intent's extras
		pList = b.getParcelable("ProcessInfo"); // get our list

		ListView lv = (ListView) findViewById(R.id.listView);

		pListAA = new ArrayAdapter<ProcessInfo>(this,
				android.R.layout.simple_list_item_1, pList);

		lv.setAdapter(pListAA);

		// Edit Process
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parentAdapter, View view,
					int position, long id) {

				Intent i = new Intent(getApplicationContext(),
						MainActivity.class);
				i.putExtra("id", (int) id);
				startActivity(i);

			}
		});

		GraphView graphView = new LineGraphView(this, "GraphViewUtil");

		LinearLayout layout = (LinearLayout) findViewById(R.id.graphlayout);
		layout.addView(graphView);

		Button addBtn = (Button) findViewById(R.id.addBtn);
		addBtn.setOnClickListener(addListener);

		Button switchBtn = (Button) findViewById(R.id.switchBtn);
		switchBtn.setOnClickListener(switchListener);

		new Thread() {
			public void run() {
				while (true) {
					try {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String tEnergy = "0";
								drawGraph();
								pListAA.notifyDataSetChanged();
								tEnergy = getTotalEnergy();
								tv1.setText(tEnergy + " mJ");
							}
						});
						Thread.sleep(graphUpdateInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		setupAxisModes();

		axisMode = axisModeMap.get(AxisMode.SEC_2);

		updateAxis();
		// startX = 0;
		// endX = startX + offsetX;

		// Set up dropdown spinner for x axis selection
		Spinner spinner = (Spinner) findViewById(R.id.xaxis_spinner);
		ArrayAdapter<AxisMode> gModeAA = new ArrayAdapter<AxisMode>(this,
				android.R.layout.simple_spinner_dropdown_item,
				AxisMode.values());
		gModeAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(gModeAA);
		spinner.setOnItemSelectedListener(changeAxisListener);

	}

	private String getTotalEnergy() {
		String filename = "//sys//rtes//energy";
		String content = "";
		FileInputStream fis;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(new File(filename));
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			content = br.readLine();
			if (content != null && content.compareTo("empty") != 0) {
				fis.close();
				return content;
			} else {
				fis.close();
				return "No Energy!";
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return content;
	}

	private void updateAxis() {

		if (axisMode.getAxisMode() == AxisMode.AUTO) {
			xStep = pList.getMaxT();
			offsetX = (long) (xStep * xLength) / 1000000;
		} else {
			offsetX = (long) axisMode.getMsec();
		}
		System.out.println("Offset = " + offsetX);
		startX = 0;
		endX = startX + offsetX;
	}

	OnItemSelectedListener changeAxisListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			axisMode = axisModeMap
					.get((AxisMode) parent.getItemAtPosition(pos));
			updateAxis();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			axisMode = axisModeMap.get(AxisMode.AUTO);
			updateAxis();

		}

	};

	OnClickListener addListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			Bundle b = new Bundle();
			b.putParcelable("ProcessInfo", pList);
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			i.putExtras(b);
			startActivity(i);
		}

	};

	OnClickListener switchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (graphMode == GraphMode.UTILIZATION) {
				graphMode = GraphMode.CONTEXTSWITCH;
				updateAxis();

			} else if (graphMode == GraphMode.CONTEXTSWITCH) {
				graphMode = GraphMode.UTILIZATION;
				updateAxis();
			}

		}
	};

	@SuppressLint("NewApi")
	private void drawGraph() {

		// check if rteslogger has been loaded
		String filename = "//sys//rtes//tasks";
		File rtesDir = new File(filename);
		if (!rtesDir.exists()) {
			Toast errorToast = Toast
					.makeText(
							getApplicationContext(),
							"RTES Logger directory not found. Did you load the module?",
							Toast.LENGTH_SHORT);
			errorToast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL,
					0, 0);
			errorToast.show();
			return;
		}

		// check if rteslogger has been loaded
		/*
		 * filename = "//sys//rtes//config//trace_ctx"; rtesDir = new
		 * File(filename);
		 * 
		 * FileInputStream fis; try { fis = new FileInputStream(new
		 * File(filename)); InputStreamReader isr = new InputStreamReader(fis);
		 * BufferedReader br = new BufferedReader(isr); String content =
		 * br.readLine();
		 * 
		 * 
		 * System.out.println("trace_ctx = " + content);
		 * 
		 * if (content != null && content.compareTo("0") == 0) { Toast
		 * errorToast = Toast .makeText( getApplicationContext(),
		 * "config/trace_ctx = 0!! You should probably enable it!",
		 * Toast.LENGTH_SHORT); errorToast.setGravity(Gravity.CENTER |
		 * Gravity.CENTER_HORIZONTAL, 0, 0); errorToast.show(); } } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * e) { e.printStackTrace(); }
		 */

		LinearLayout layout = (LinearLayout) findViewById(R.id.graphlayout);

		// / LayoutTransition trans = new LayoutTransition();
		// trans.enableTransitionType(LayoutTransition.CHANGING);
		// layout.setLayoutTransition(trans);

		GraphView graphView = new LineGraphView(this, "Process Utilization");

		// System.out.println("xStep = " + xStep + ", updateFreq = " +
		// updateFreq);

		for (ProcessInfo process : pList) {

			int pid = process.getPid();
			if (graphMode == GraphMode.UTILIZATION) {
				graphViewSeriesData = process.getUtilData();
				if (graphViewSeriesData == null) {
					pList.remove(pList.getByPID(pid));
				}
				graphView.setTitle("Process Utilization");
			} else if (graphMode == GraphMode.CONTEXTSWITCH) {
				graphViewSeriesData = process.getCtxData();
				if (graphViewSeriesData == null) {
					pList.remove(pList.getByPID(pid));
				}
				graphView.setTitle("Context Switch");
			}

			// recompute x-axis
			if (graphViewSeriesData != null) {
				graphView.addSeries(graphViewSeriesData);
			}
		}
		
		
		if (graphViewSeriesData != null && !graphViewSeriesData.isEmpty()
				&& graphViewSeriesData.getLastX() > endX) {

			startX = (long) graphViewSeriesData.getLastX();
			endX = startX + offsetX;

			System.out.println("Updated X Axis! startX: " + startX
					+ ", endX: " + endX);
		}

		graphView.setViewPort(startX, offsetX);
		graphView.setManualYAxisBounds(105, 0);
		layout.removeAllViews();

		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(50);
		graphView.getGraphViewStyle().setNumHorizontalLabels(5);
		graphView.getGraphViewStyle().setTextSize(20.0f);
		graphView.setScrollable(false);
		graphView.setShowLegend(true);
		layout.addView(graphView, 0);

		graphView.redrawAll();

	}

	private void setupAxisModes() {
		axisModeMap = new HashMap<AxisMode, SingleAxisMode>();
		axisModeMap.put(AxisMode.AUTO, new SingleAxisMode(AxisMode.AUTO, 0,
				"Auto"));
		axisModeMap.put(AxisMode.MSEC_100, new SingleAxisMode(
				AxisMode.MSEC_100, 100, "100ms"));
		axisModeMap.put(AxisMode.MSEC_500, new SingleAxisMode(
				AxisMode.MSEC_500, 500, "500ms"));
		axisModeMap.put(AxisMode.SEC_1, new SingleAxisMode(AxisMode.SEC_1,
				1000, "1s"));
		axisModeMap.put(AxisMode.SEC_2, new SingleAxisMode(AxisMode.SEC_2,
				2000, "2s"));
		axisModeMap.put(AxisMode.SEC_5, new SingleAxisMode(AxisMode.SEC_5,
				5000, "5s"));
		axisModeMap.put(AxisMode.SEC_10, new SingleAxisMode(AxisMode.SEC_10,
				10000, "10s"));
		axisModeMap.put(AxisMode.SEC_20, new SingleAxisMode(AxisMode.SEC_20,
				20000, "20s"));
	}

}
