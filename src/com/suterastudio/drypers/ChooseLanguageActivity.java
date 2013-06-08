package com.suterastudio.drypers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.suterastudio.android.helpers.ContextHelper;
import com.suterastudio.drypers.data.LanguagePreference;

public class ChooseLanguageActivity extends Activity {
	private Button mBackButton;
	private Button mMenuButton;
	
	private OnClickListener mTemplateClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
				// API call for templates here
				final CharSequence[] items = { "Loving Your Baby Groove",
						"Baby, You're My Sunshine", "Shake it N Love it",
						"Dancing Baby", "Hip Hop Baby" };
				new AlertDialog.Builder(ChooseLanguageActivity.this)
						.setSingleChoiceItems(items, 0, null)
						.setPositiveButton(R.string.ok_button_label,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
										int selectedPosition = ((AlertDialog) dialog)
												.getListView()
												.getCheckedItemPosition();
										CharSequence text = items[selectedPosition];
										mTemplateText.setText(text);
										Toast.makeText(
												ChooseLanguageActivity.this,
												"Template changes will take effect when next you record a song.",
												Toast.LENGTH_LONG).show();
										ContextHelper.setSelectedSong(mTemplateText.getText().toString());
									}
								}).show();
			}
		
	};
	
	private OnClickListener mMenuClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(ChooseLanguageActivity.this,
					MenuCreatorActivity.class));
		}
	};
	private OnClickListener mBackClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	private ImageView mTemplateSelect;
	private TextView mTemplateText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.choose_language);
		createViews();
		String template=ContextHelper.getSelectedSong();
		if (template!=null &&!template.isEmpty())
			mTemplateText.setText(template);
	}

	private void createViews() {
		// Element Bindings
		mBackButton = (Button) findViewById(R.id.back_recording);
		mMenuButton = (Button) findViewById(R.id.menu_recording);

		// Button Click Listeners
		mBackButton.setOnClickListener(mBackClickListener);
		mMenuButton.setOnClickListener(mMenuClickListener);
		mTemplateSelect = (ImageView) findViewById(R.id.active_template);
		mTemplateText = (TextView) findViewById(R.id.template_name);
		mTemplateSelect.setOnClickListener(mTemplateClickListener);

		findViewById(R.id.chooselanguage_image_bahasa).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ContextHelper.freezeChosenLanguage(LanguagePreference.BAHASA_MALAYSIA);
						startActivity(new Intent(ChooseLanguageActivity.this,
								LearningActivity.class));

					}
				});
		findViewById(R.id.chooselanguage_image_english).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ContextHelper.freezeChosenLanguage(LanguagePreference.ENGLISH);
						startActivity(new Intent(ChooseLanguageActivity.this,
								LearningActivity.class));
					}
				});

	}
	
}
