package com.rtes.taskmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import com.jjoe64.graphview.GraphView.GraphViewData;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int pid, cSec, tSec, rtPrio;
	private long cNanoSec, tNanoSec;

	private Toast errorToast;

	public static ProcessList pList = new ProcessList();
	public static Vector<ProcessInfo> al = new Vector<ProcessInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// pList.add(new ProcessInfo(1, 1, 0, 2, 0));
		// pList.add(new ProcessInfo(2, 1, 0, 3, 0));

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button saveBtn = (Button) findViewById(R.id.saveBtn);

		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(cancelListener);

		Button deleteBtn = (Button) findViewById(R.id.deleteBtn);
		deleteBtn.setOnClickListener(deleteListener);

		Bundle extras = getIntent().getExtras();

		if (extras != null && extras.containsKey("ProcessInfo")) {

			pList = extras.getParcelable("ProcessInfo"); // get our list
		}

		if (extras == null || !extras.containsKey("id")) {

			System.out.println("UI: Add new process");

			saveBtn.setOnClickListener(saveAddListener);
			deleteBtn.setEnabled(false);

		} else {

			System.out.println("UI: Edit process");
			System.out.println(pList);

			ProcessInfo curProcessInfo = pList.get(extras.getInt("id"));

			EditText pidEditText = (EditText) findViewById(R.id.pidEditText);
			pidEditText.setFocusable(false);
			pidEditText.setText(Integer.valueOf(curProcessInfo.getPid())
					.toString());

			EditText cSecEditText = (EditText) findViewById(R.id.cSecEditText);
			cSecEditText.setText(Integer.valueOf(curProcessInfo.getcSec())
					.toString());

			EditText cNanoSecEditText = (EditText) findViewById(R.id.cNanoSecEditText);
			cNanoSecEditText.setText(Long.valueOf(curProcessInfo.getcNanoSec())
					.toString());

			EditText tSecEditText = (EditText) findViewById(R.id.tSecEditText);
			tSecEditText.setText(Integer.valueOf(curProcessInfo.gettSec())
					.toString());

			EditText tNanoSecEditText = (EditText) findViewById(R.id.tNanoSecEditText);
			tNanoSecEditText.setText(Long.valueOf(curProcessInfo.gettNanoSec())
					.toString());

			saveBtn.setOnClickListener(saveEditListener);

		}

	}

	OnClickListener saveAddListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			try {
				EditText pidEditText = (EditText) findViewById(R.id.pidEditText);
				String value = pidEditText.getText().toString();
				pid = Integer.parseInt(value);

				EditText cSecEditText = (EditText) findViewById(R.id.cSecEditText);
				String value1 = cSecEditText.getText().toString();
				cSec = Integer.parseInt(value1);

				EditText cNanoSecEditText = (EditText) findViewById(R.id.cNanoSecEditText);
				String value2 = cNanoSecEditText.getText().toString();
				cNanoSec = Long.parseLong(value2);

				EditText tSecEditText = (EditText) findViewById(R.id.tSecEditText);
				String value3 = tSecEditText.getText().toString();
				tSec = Integer.parseInt(value3);

				EditText tNanoSecEditText = (EditText) findViewById(R.id.tNanoSecEditText);
				String value4 = tNanoSecEditText.getText().toString();
				tNanoSec = Long.parseLong(value4);

				rtPrio = 0;

			} catch (NumberFormatException e) {

				errorToast = Toast.makeText(getApplicationContext(),
						"Invalid number entered", Toast.LENGTH_SHORT);
				errorToast.setGravity(Gravity.CENTER
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				errorToast.show();

				return;

			}

			// check if rteslogger has been loaded
			String filename = "//sys//rtes//tasks";
			File rtesDir = new File(filename);
			if (!rtesDir.exists()) {
				errorToast = Toast
						.makeText(
								getApplicationContext(),
								"RTES Logger directory not found. Did you load the module?",
								Toast.LENGTH_SHORT);
				errorToast.setGravity(Gravity.CENTER
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				errorToast.show();

				return;
			}

			if (pList.getByPID(pid) != null) {

				errorToast = Toast.makeText(getApplicationContext(),
						"PID already exists!!!", Toast.LENGTH_SHORT);
				errorToast.setGravity(Gravity.CENTER
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				errorToast.show();

			} else if (pid == 0) {

				errorToast = Toast.makeText(getApplicationContext(),
						"PID is zero. Not supported!!!", Toast.LENGTH_SHORT);
				errorToast.setGravity(Gravity.CENTER
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				errorToast.show();

			} else if (pid == android.os.Process.myPid()) {

				errorToast = Toast.makeText(getApplicationContext(),
						"Dont use TaskMon on itself!!!", Toast.LENGTH_SHORT);
				errorToast.setGravity(Gravity.CENTER
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				errorToast.show();

			} else {

				ReserveWrapper.setreserve(pid, cSec, cNanoSec, tSec, tNanoSec,
						rtPrio);

				Log.i("Setting new reserve ",
						String.format(
								"pid: %d, cSec: %d, cNanoSec: %d,  tSec:%d, tNanoSec: %d",
								pid, cSec, cNanoSec, tSec, tNanoSec));

				ProcessInfo pInfo = new ProcessInfo(pid, cSec, cNanoSec, tSec,
						tNanoSec);

				pList.add(pInfo);
				Bundle b = new Bundle();
				b.putParcelable("ProcessInfo", pList);

				Intent i = new Intent(getApplicationContext(),
						GraphActivity.class);
				i.putExtras(b);
				startActivity(i);
			}
		}
	};

	OnClickListener saveEditListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// Parcel in = new Parcel();

			try {
				EditText pidEditText = (EditText) findViewById(R.id.pidEditText);
				String value = pidEditText.getText().toString();
				pid = Integer.parseInt(value);

				EditText cSecEditText = (EditText) findViewById(R.id.cSecEditText);
				String value1 = cSecEditText.getText().toString();
				cSec = Integer.parseInt(value1);

				EditText cNanoSecEditText = (EditText) findViewById(R.id.cNanoSecEditText);
				String value2 = cNanoSecEditText.getText().toString();
				cNanoSec = Integer.parseInt(value2);

				EditText tSecEditText = (EditText) findViewById(R.id.tSecEditText);
				String value3 = tSecEditText.getText().toString();
				tSec = Integer.parseInt(value3);

				EditText tNanoSecEditText = (EditText) findViewById(R.id.tNanoSecEditText);
				String value4 = tNanoSecEditText.getText().toString();
				tNanoSec = Integer.parseInt(value4);
			} catch (NumberFormatException e) {

				errorToast = Toast.makeText(getApplicationContext(),
						"Invalid number entered", Toast.LENGTH_SHORT);
				errorToast.setGravity(Gravity.CENTER
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				errorToast.show();

				return;

			}

			ReserveWrapper.setreserve(pid, cSec, cNanoSec, tSec, tNanoSec,
					rtPrio);

			Log.i("Setting new reserve ", String.format(
					"pid: %d, cSec: %d, cNanoSec: %d,  tSec:%d, tNanoSec: %d",
					pid, cSec, cNanoSec, tSec, tNanoSec));

			ProcessInfo pInfo = pList.getByPID(pid);

			pInfo.setcSec(cSec);
			pInfo.setcNanoSec(cNanoSec);
			pInfo.settSec(tSec);
			pInfo.settNanoSec(tNanoSec);

			Bundle b = new Bundle();
			b.putParcelable("ProcessInfo", pList);

			Intent i = new Intent(getApplicationContext(), GraphActivity.class);
			i.putExtras(b);
			startActivity(i);

			// startActivity(i);
		}

	};

	OnClickListener deleteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			EditText pidEditText = (EditText) findViewById(R.id.pidEditText);
			String value = pidEditText.getText().toString();
			pid = Integer.parseInt(value);

			ReserveWrapper.cancelreserve(pid);

			Log.i("Deleting process, ", String.format("pid: %d", pid));

			pList.remove(pList.getByPID(pid));

			Bundle b = new Bundle();
			b.putParcelable("ProcessInfo", pList);
			Intent i = new Intent(getApplicationContext(), GraphActivity.class);
			i.putExtras(b);
			startActivity(i);
		}

	};

	OnClickListener cancelListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Bundle b = new Bundle();
			b.putParcelable("ProcessInfo", pList);
			Intent i = new Intent(getApplicationContext(), GraphActivity.class);
			i.putExtras(b);
			startActivity(i);
		}

	};

}
