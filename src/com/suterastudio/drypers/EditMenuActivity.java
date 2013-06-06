package com.suterastudio.drypers;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.suterastudio.drypers.data.Babble;
import com.suterastudio.drypers.data.BabbleHandler;
import com.suterastudio.drypers.network.SessionHelper;
import com.suterastudio.drypers.network.SessionHelper.Gender;

public class EditMenuActivity extends GenericActivity implements
		OnTouchListener, BabbleHandler {
	private Rect rectf = new Rect();
	private Integer offset;
	private int[] coords;
	private Babble listedBabble;
	private String songTitle;
	private String playBackLocation;
	private String babblerName;
	private String babblerID;
	private String photo;
	private View dialog;
	private View container;
	private Context thisContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_menu);

		createViews();
	}

	private void createViews() {
		// Get Extra Intent Bundled Params
		offset = getIntent().getExtras().getInt("Index");
		coords = getIntent().getExtras().getIntArray("Coords");

		// set the string values
		listedBabble = (Babble) getIntent().getExtras().getSerializable(
				"babbleObject");
		songTitle = listedBabble.title;
		babblerName = listedBabble.babbler_name;
		babblerID = listedBabble.id;

		// Bindings
		final Button editBtn = (Button) findViewById(R.id.babble_edit);
		final Button deleteBtn = (Button) findViewById(R.id.babble_delete);

		// view listener
		View fullScreen = (View) findViewById(R.id.sharing_menu);
		fullScreen.setOnTouchListener(this);

		// User Menu Buttons
		editBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getNewTitle();
			}
		});

		deleteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//no commands yet...
			}

		});
	}

	public void getNewTitle(){
		AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
		// Add text entry field for Babble title
		LayoutInflater factory = LayoutInflater.from(this);
		final TextView textEntryView = (TextView) factory.inflate(
				R.layout.babble_name, null);
		builder.setView(textEntryView);
		// Add the buttons
		builder.setPositiveButton("Save!",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String userBabbleTitle = (String) textEntryView
								.getText().toString();

						SessionHelper.updateBabble(userBabbleTitle, Gender.valueOf(listedBabble.gender.toUpperCase()), EditMenuActivity.this);
					}
				});
		builder.setNegativeButton("Go Back",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
		builder.setTitle("Rename Babble");
		builder.setMessage("Please name this babble..");

		// Create the AlertDialog
		builder.show();
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent mev) {
		float xCoord = mev.getX();
		float yCoord = mev.getY();
		if (xCoord > rectf.left && xCoord < rectf.right && yCoord > rectf.top
				&& yCoord < rectf.bottom) {
			// NOOP
		} else {
			finish();
		}

		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		offset = getIntent().getExtras().getInt("Index");
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		// move the window code below...

		// bindings...
		View container = (View) findViewById(R.id.sharing_menu_container);
		final ImageView triangleTop = (ImageView) findViewById(R.id.tri_indic_sharing_Top);
		final ImageView triangleBottom = (ImageView) findViewById(R.id.tri_indic_sharing_Bottom);

		// set the location offset
		Integer[] screen = getMidDisplayLocation();
		
		container.setX((float) screen[0]/2);
		
		if (coords[1] > screen[1]) {
			triangleTop.setVisibility(View.INVISIBLE);
			triangleBottom.setVisibility(View.VISIBLE);
			container.setY((float) (coords[1] - container.getHeight() + 20));
		} else {
			triangleTop.setVisibility(View.VISIBLE);
			triangleBottom.setVisibility(View.INVISIBLE);
			container.setY((float) coords[1] + 40);

		}

		// set the boundary of the menu
		rectf.set((int) container.getX(),
				(int) (container.getY() + (container.getHeight() * 0.15)),
				(int) (container.getX() + (container.getWidth() * 0.9)),
				(int) container.getY() + container.getHeight());
	}

	public Integer[] getMidDisplayLocation() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		Integer[] midPoint = new Integer[2];
		midPoint[0] = size.x / 2;
		midPoint[1] = size.y / 2;
		return midPoint;
	}

	@Override
	public void onException(DrypersException exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgress(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBabbleLiked(Babble babble) {

	}

	@Override
	public void onBabbleUploaded(Babble babble) {
		// save title and inform user
		Toast.makeText(thisContext,
				"Name changed. Refresh babblebox to see changes.",
				Toast.LENGTH_SHORT).show();
		finish();

	}

	@Override
	public void onBabbles(List<Babble> babbles) {

	}

	@Override
	public void onMyBabbles(List<Babble> babbles) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFriendsBabbles(List<Babble> babbles) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBabble(Babble babble) {
		// TODO Auto-generated method stub
	}

}
