package com.mordor.creepme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FriendSelectorFragment extends Fragment {
	private static final String TAG = "FriendSelectorFragment";
	private static final int REQUEST_CONTACT = 2;

	private Creep mCreep;

	private Button mFriendButton;

	/* Builds main fragment view for FriendSelector */
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.fragment_friend_selector, parent,
				false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mFriendButton = (Button)v.findViewById(R.id.friend_selectorButton);
		mFriendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(i, REQUEST_CONTACT);
			}
		});

		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK) return;

		if (requestCode == REQUEST_CONTACT) {
			Uri contactUri = data.getData();
			String[] queryFields = new String[] {
					ContactsContract.Contacts.DISPLAY_NAME
			};

			Cursor c = getActivity().getContentResolver()
					.query(contactUri, queryFields, null, null, null);

			if(c.getCount() == 0) {
				c.close();
				return;
			}

			mCreep = new Creep();
			c.moveToFirst();
			String name = c.getString(0);
			mCreep.setName(name);
			setCreepView(name);
			c.close();
		}
	}

	public void setCreepView(String text) {
		TextView textView = (TextView) getView().findViewById(
				R.id.friend_nameText);
		textView.setText(text);
	}

	/* Builds the Activity Bar Menu */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_main_options, menu);
	}

	/* Deals with Activity Bar and Menu item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
