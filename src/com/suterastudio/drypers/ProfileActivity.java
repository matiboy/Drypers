package com.suterastudio.drypers;

import junit.framework.Assert;

import org.customsoft.stateless4j.StateMachine;
import org.customsoft.stateless4j.delegates.Action;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suterastudio.android.helpers.ContextHelper;
import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.BabblerHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.data.ScoreHandler;
import com.suterastudio.drypers.network.SessionHelper;
import com.suterastudio.drypers.network.SessionHelper.ScoreType;

public class ProfileActivity extends GenericActivity implements BabblerHandler,
		ScoreHandler {
	final Context context = this;
	String userID = null;
	private Context profileDataContext = this;
	// State machine
	private StateMachine<State, Trigger> machine;
	// edit profile state indicator
	protected boolean isEditing;
	// new profile? for score awarding
	protected boolean isComplete;

	private enum State {
		IDLING, EDITING, CONFIRMING, SAVING, DATING
	}

	private enum Trigger {
		IDLE, EDIT, CONFIRM, SAVE, DATE
	}

	public ProfileActivity() {

		machine = new StateMachine<State, Trigger>(State.IDLING);

		Action idler = new Action() {
			@Override
			public void doIt() {
			}
		};

		Action editor = new Action() {
			@Override
			public void doIt() {
				updateStatus();
			}
		};

		Action confirmer = new Action() {
			@Override
			public void doIt() {
				saveProfile();
			}
		};

		Action saver = new Action() {
			@Override
			public void doIt() {
			}
		};
		Action picking = new Action() {
			@Override
			public void doIt() {
				dateDialogFunction();
			}
		};

		try {
			machine.Configure(State.IDLING).OnEntry(idler)
					.Permit(Trigger.EDIT, State.EDITING);

			machine.Configure(State.EDITING).OnEntry(editor)
					.Permit(Trigger.CONFIRM, State.CONFIRMING)
					.Permit(Trigger.DATE, State.DATING);
			
			machine.Configure(State.DATING).OnEntry(picking)
				.Permit(Trigger.EDIT, State.EDITING);
			
			machine.Configure(State.CONFIRMING).OnEntry(confirmer)
					.Permit(Trigger.SAVE, State.SAVING)
					.Permit(Trigger.EDIT, State.EDITING);

			machine.Configure(State.SAVING).OnEntry(saver)
					.Permit(Trigger.IDLE, State.IDLING);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.babbleprofile);
		createViews();
		// if (machine.CanFire(Trigger.IDLE)) {
		// try {
		// machine.Fire(Trigger.IDLE);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// Assert.assertEquals(State.IDLING, machine.getState());
		// }
	}

	@Override
	protected void onStart() {
		super.onStart();

		// group initiation
		final ViewGroup group = (ViewGroup) findViewById(R.id.group1);
		ContextHelper.clearForm(group, false); // disable all textview
	}

	private void createViews() {
		updateBabblerInfo();

		// Bindings
		final Button backBtn = (Button) findViewById(R.id.back_babblebox);
		final Button menuButton = (Button) findViewById(R.id.menu_babblebox);
		final Button editbtn = (Button) findViewById(R.id.btn_edit);
		final Button updatebtn = (Button) findViewById(R.id.btn_changepic);

		// field box bindings
		final TextView title = (TextView) findViewById(R.id.profile_title);
		final TextView dateBox = (TextView) findViewById(R.id.txt_dob);

		// babble font
		title.setTypeface(font);

		// Grab headshot resource (if it exists)
		final ImageView babyHead = (ImageView) findViewById(R.id.imageView_profile);

		isEditing = false;
		isComplete = true;

		if (DrypersResources.BabyHead.exists()) {
			Bitmap savedHead = (Bitmap) BitmapFactory
					.decodeFile(DrypersResources.BabyHead.getAbsolutePath());
			Drawable headDrawable = new BitmapDrawable(getResources(),
					savedHead);
			babyHead.setImageDrawable(headDrawable);
		}

		// User Menu Buttons
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		menuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ProfileActivity.this,
						MenuCreatorActivity.class));
			}
		});

		// profile buttons
		editbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// check editing status
				if (machine.IsInState(State.EDITING)) {
					if (machine.CanFire(Trigger.CONFIRM)) {
						try {
							machine.Fire(Trigger.CONFIRM);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Assert.assertEquals(State.CONFIRMING,
								machine.getState());
					}
				} else {
					if (machine.CanFire(Trigger.EDIT)) {
						try {
							machine.Fire(Trigger.EDIT);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Assert.assertEquals(State.EDITING, machine.getState());
					}
					isEditing = !isEditing;
				}
			}
		});

		updatebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ProfileActivity.this,
						BabbleActivity.class));
				finish();
			}
		});
		
		dateBox.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					
					if (machine.CanFire(Trigger.DATE)) {
						try {
							machine.Fire(Trigger.DATE);
						} catch (Exception e) {
							e.printStackTrace();
						}
						Assert.assertEquals(State.DATING,
								machine.getState());
					}
				}
			}
		});
			
		dateBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (machine.CanFire(Trigger.DATE)) {
					try {
						machine.Fire(Trigger.DATE);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Assert.assertEquals(State.DATING,
							machine.getState());
				}
			}
		});
	}

	private void saveProfile() {
		// field box bindings
		final EditText txtName = (EditText) findViewById(R.id.txt_name);
		// final EditText txtPassword = (EditText)
		// findViewById(R.id.txt_password);
		final EditText txtDoB = (EditText) findViewById(R.id.txt_dob);
		final EditText txtEmail = (EditText) findViewById(R.id.txt_email);
		final EditText txtPostcode = (EditText) findViewById(R.id.txt_postcode);
		final EditText txtAddress = (EditText) findViewById(R.id.txt_address);
		final EditText txtState = (EditText) findViewById(R.id.txt_state);
		final EditText txtCountry = (EditText) findViewById(R.id.txt_country);
		final CheckBox cbxFeature = (CheckBox) findViewById(R.id.checkBox1);
		final CheckBox cbxUpdate = (CheckBox) findViewById(R.id.checkBox2);

		final String cbxFeatureAnswer;
		final String cbxUpdateAnswer;

		// boolean status to string
		if (cbxFeature.isChecked()) {
			cbxFeatureAnswer = "yes";
		} else {
			cbxFeatureAnswer = "no";
		}
		if (cbxUpdate.isChecked()) {
			cbxUpdateAnswer = "yes";
		} else {
			cbxUpdateAnswer = "no";
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(
				profileDataContext);

		// Add the buttons
		builder.setPositiveButton("Update",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (machine.CanFire(Trigger.SAVE)) {
							try {
								machine.Fire(Trigger.SAVE);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Assert.assertEquals(State.SAVING,
									machine.getState());
						}
						// If profile data retrieved was not complete
						if (!isComplete) {
							final ViewGroup group = (ViewGroup) findViewById(R.id.group1);
							
							int check = 0;
							
							check = ContextHelper.checkForm(group, check);
							if ( check == 7) // 7 fields to complete
								{
								isComplete = true; //disable it for awarding points
								connectionDialog(ScoreType.PROFILE);
							}
						}

						// update to server
						SessionHelper.UpdateProfile(txtEmail.getText()
								.toString(), txtName.getText().toString(),
								txtAddress.getText().toString(), txtDoB
										.getText().toString(), txtPostcode
										.getText().toString(), txtState
										.getText().toString(), txtCountry
										.getText().toString(),
								ProfileActivity.this);
						isEditing = false;
						if (machine.CanFire(Trigger.IDLE)) {
							try {
								machine.Fire(Trigger.IDLE);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Assert.assertEquals(State.IDLING,
									machine.getState());
						}
						updateStatus();
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
						isEditing = true;
						if (machine.CanFire(Trigger.EDIT)) {
							try {
								machine.Fire(Trigger.EDIT);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Assert.assertEquals(State.EDITING,
									machine.getState());
						}
						updateStatus();
					}
				});
		builder.setTitle("Update Babble Profile");
		builder.setMessage("Confirm updating Babble info?");

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		builder.show();
	}

	private void updateStatus() {
		// group initiation
		final ViewGroup group = (ViewGroup) findViewById(R.id.group1);
		final Button editbtn = (Button) findViewById(R.id.btn_edit);

		// change text field settings and button image
		ContextHelper.clearForm(group, machine.IsInState(State.EDITING));

		if (machine.IsInState(State.EDITING)) {
			editbtn.setBackgroundResource(R.drawable.savebtn);
		} else {
			editbtn.setBackgroundResource(R.drawable.editprofile_selector);
		}
		if (machine.CanFire(Trigger.IDLE)) {
			try {
				machine.Fire(Trigger.IDLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Assert.assertEquals(State.IDLING, machine.getState());
		}
	}

	public void updateBabblerInfo() {
		// alert downloading... msg
		triggerProgressDialog("Update", "Getting latest info..");
		// update babbler info
		try {
			if (DrypersResources.getBabbler() != null) {
				SessionHelper.LookupBabbler(
						DrypersResources.getBabbler().email,
						ProfileActivity.this);
			} else {
				Toast.makeText(context, "Connection Failure", Toast.LENGTH_LONG)
				.show();
				startActivity(new Intent(ProfileActivity.this,
						LoginMasterActivity.class));
				finish();
			}
		} catch (DrypersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void showBabblerInfo() {
		// bindings
		final EditText txtName = (EditText) findViewById(R.id.txt_name);
		// final EditText txtPassword = (EditText)
		// findViewById(R.id.txt_password);
		final EditText txtDoB = (EditText) findViewById(R.id.txt_dob);
		final EditText txtEmail = (EditText) findViewById(R.id.txt_email);
		final EditText txtPostcode = (EditText) findViewById(R.id.txt_postcode);
		final EditText txtAddress = (EditText) findViewById(R.id.txt_address);
		final EditText txtState = (EditText) findViewById(R.id.txt_state);
		final EditText txtCountry = (EditText) findViewById(R.id.txt_country);

		// Get Babbler object for logged in user
		Babbler babbler = DrypersResources.getBabbler();

		// Live Attributes
		boolean liveFeatured = false;
		boolean liveUpdate = false;
		String liveName = "";
		String liveEmail = "";
		String liveDoB = "";
		String liveAddress = "";
		String livePostcode = "";
		String liveState = "";
		String liveCountry = "";

		// Set Babbler Profile values;
		// check null value... convert to blanks
		if (! (babbler.name.equals("null") || (babbler.name.equals(""))) ) {
			liveName = babbler.name;
		} else {
			isComplete = false;
		}
		if (! (babbler.email.equals("null") || (babbler.email.equals(""))) ) {
			liveEmail = babbler.email;
		} else {
			isComplete = false;
		}
		if (! (babbler.address.equals("null") || (babbler.address.equals(""))) ) {
			liveAddress = babbler.address;
		} else {
			isComplete = false;
		}
		if (! (babbler.state.equals("null") || (babbler.state.equals(""))) ) {
			liveState = babbler.state;
		} else {
			isComplete = false;
		}
		if (! (babbler.country.equals("null") || (babbler.country.equals(""))) ) {
			liveCountry = babbler.country;
		} else {
			isComplete = false;
		}
		if (! (babbler.postcode.equals("null") || (babbler.postcode.equals(""))) ) {
			livePostcode = babbler.postcode;
		} else {
			isComplete = false;
		}
		if (! (babbler.dob.equals("null") ||  (babbler.dob.equals(""))) ) {
			liveDoB = babbler.dob;
		} else {
			isComplete = false;
		}

		// update data onto the fields
		txtName.setText(liveName);
		// txtPassword.setText("");
		txtDoB.setText(liveDoB);
		txtEmail.setText(liveEmail);
		txtAddress.setText(liveAddress);
		txtPostcode.setText(livePostcode);
		txtState.setText(liveState);
		txtCountry.setText(liveCountry);
	}
	
	public void dateDialogFunction() {
		
		Dialog dateDialog = new Dialog(ProfileActivity.this, R.style.MyActivityDialogTheme);
		dateDialog.setTitle("Pick your Birth Date");
		setupDateDialog(dateDialog);
		dateDialog.show();
	}

	public void setupDateDialog(final Dialog dialog) {
		// Create inflatable programmatic layout for dialog
		View dialogLayout = LayoutInflater.from(this).inflate(
				R.layout.babbleprofile_date_picker, null);
		
		// Set layout params here
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		dialogLayout.setLayoutParams(lp);
		// Add the view
		// container.addView(dialogLayout);
		// Set the dialog content view
		dialog.setContentView(dialogLayout);
		// Element bindings
		final DatePicker datePicker = (DatePicker) dialogLayout.findViewById(R.id.datePicker1);
		final Button okBtn = (Button) dialogLayout.findViewById(R.id.btn_ok);
		final Button cancelBtn = (Button) dialogLayout.findViewById(R.id.btn_cancel);


		
		// Add click listener for redeem button
		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				datePicker.clearChildFocus(getCurrentFocus());
				setDate(datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear());
				
				//reset state to editing
				if (machine.CanFire(Trigger.EDIT)) {
					try {
						machine.Fire(Trigger.EDIT);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Assert.assertEquals(State.EDITING, machine.getState());
				}
				
				dialog.dismiss();
			}

		});
		// Add click listener for again button
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(	context,
						"Date selection cancelled.",
						Toast.LENGTH_SHORT).show();
				
				//reset state to editing
				if (machine.CanFire(Trigger.EDIT)) {
					try {
						machine.Fire(Trigger.EDIT);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Assert.assertEquals(State.EDITING, machine.getState());
				}
				
				dialog.dismiss();
			}

		});
	}
	
	private void setDate(int day, int month, int year){
		final EditText txtDoB = (EditText) findViewById(R.id.txt_dob);
		txtDoB.setText(year + "-" + month + "-" + day);
		Toast.makeText(	context,
				"Date set...",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onException(DrypersException exception) {
		exception.printStackTrace();
		Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void onProgress(String message) {
	}

	@Override
	public void onLogin(Babbler babbler) {
	}

	@Override
	public void onLogout(Babbler babbler) {
	}

	@Override
	public void onProfile(Babbler babbler) {
		isEditing = false;
		if (machine.CanFire(Trigger.IDLE)) {
			try {
				machine.Fire(Trigger.IDLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Assert.assertEquals(State.IDLING, machine.getState());
		}
		updateStatus();
	}

	@Override
	public void onLookup(Babbler babbler) {
		DrypersResources.setBabbler(babbler);
		dismissProgressDialog();
		showBabblerInfo();
	}

	@Override
	public void onUpdate(Babbler babbler) {
	}

	@Override
	public void onRegister(Babbler babbler) {
	}
}
