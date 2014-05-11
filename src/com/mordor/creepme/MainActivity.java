package com.mordor.creepme;

import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "com.mordor.creepme.MainActivity";
	public static CreepLab sLab;
	public static String sPhoneNumber;
	private CreepListAdapter adp1;
	private CreepListAdapter adp2;
	private Handler timerHandler;
	private final int timeInterval = 1000; // Update interval, milliseconds

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set session's creep manager
		sLab = CreepLab.get(this);

		// Get user's phone number
		TelephonyManager telManager = (TelephonyManager) this
		    .getSystemService(Context.TELEPHONY_SERVICE);
		sPhoneNumber = telManager.getLine1Number();

		// Initialize timer handler
		this.timerHandler = new Handler();

		// Define ListViews locally, linked to layout
		ListView lv1 = (ListView) findViewById(R.id.who_you_creepingList);
		ListView lv2 = (ListView) findViewById(R.id.who_creeping_youList);

		// Assign adapters
		this.adp1 = new CreepListAdapter(this, R.layout.creep_list_element,
		    sLab.getCreeps(true));
		this.adp2 = new CreepListAdapter(this, R.layout.creep_list_element,
		    sLab.getCreeps(false));

		// Set custom array adapter to display list items in each ListView
		lv1.setAdapter(this.adp1);
		lv2.setAdapter(this.adp2);

	}

	/* Action taken on Add New Creep Button click */
	public void newFriendSelector(View v) {
		// Defines intent for new creep activity
		Intent i = new Intent(this, FriendSelectorActivity.class);

		// Opens new creep activity
		startActivity(i);
	}

	/* Action taken on Cancel All Selections Button click */
	public void cancelSelections(View v) {
		try {
			// If nothing gets removed, nothing was selected
			if (!sLab.removeSelections()) {
				Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception error at cancelSelections()");
		}

		// Update listView elements
		this.adp1.notifyDataSetChanged();
		this.adp2.notifyDataSetChanged();
	}

	/* Action taken on Map All Selections Button click */
	public void mapSelections(View v) {
		// Check for GPS enabled
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Check for GPS enabled. If not...
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// Dialog and no map
			buildAlertMessageNoGps();
		}

		try {
			ArrayList<UUID> selections = sLab.selectedCreeps();
			if (selections.size() != 0) {
				Intent i = new Intent(this, CreepMapActivity.class);
				i.putExtra("victimsList", selections);
				this.startActivity(i);
			} else {
				Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception error at mapSelections()");
		}
	}

	/* Implements timer to update listView elements */
	Runnable listViewUpdater = new Runnable() {
		@Override
		public void run() {
			sLab.checkForCompletions();
			adp1.notifyDataSetChanged();
			adp2.notifyDataSetChanged();

			timerHandler.postDelayed(listViewUpdater, timeInterval);
		}
	};

	/* Starts listView update timer */
	private void startTimer() {
		listViewUpdater.run();
	}

	/* Stops listView update timer */
	private void stopTimer() {
		this.timerHandler.removeCallbacks(listViewUpdater);
	}

	/* Builds GPS not enabled alert message and provides option to re-enable */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Alert dialog blocks activity from running and opening map,
		// allows user to go direct to enable screen
		builder
		    .setMessage("Your GPS seems to be disabled, do you want to enable it?")
		    .setCancelable(false)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(final DialogInterface dialog, final int id) {
				    startActivity(new Intent(
				        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			    }
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(final DialogInterface dialog, final int id) {
				    dialog.cancel();
			    }
		    });
		final AlertDialog alert = builder.create();
		alert.show();
	}

	/* Action taken when activity is resumed */
	@Override
	public void onResume() {
		super.onResume();
		// Update lists on activity resume
		this.adp1.notifyDataSetChanged();
		this.adp2.notifyDataSetChanged();

		// If there's an instance of CreepMapActivity open, kill it
		if (CreepMapActivity.getInstance() != null) {
			CreepMapActivity.getInstance().finish();
		}

		// ListView update timer restarted
		startTimer();
	}

	/* Action taken when activity is paused or destroyed */
	@Override
	public void onPause() {
		stopTimer();
		super.onPause();
	}

	/* Builds the Activity Bar Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main_options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Deals with Activity Bar and Menu item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View v = findViewById(android.R.id.content);
		switch (item.getItemId()) {
		case R.id.action_map_selections:
			// Opens new map activity with selected creeps mapped
			mapSelections(v);
			return true;
		case R.id.action_delete_selections:
			// Cancels selected creeps
			cancelSelections(v);
			return true;
		case R.id.action_add_creep:
			// Opens new creep activity
			newFriendSelector(v);
			return true;
		case R.id.action_settings:
			// Open settings activity
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
