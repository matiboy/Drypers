package com.suterastudio.drypers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.suterastudio.drypers.data.DrypersResources;

public class LoginMasterActivity extends GenericActivity {
	final Context context = this;
	private BroadcastReceiver recieveeStart;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_main);
		createViews();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		/**snip **/
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.package.KillStarter");
	    recieveeStart = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive","kill start in progress");
                finish();
            }
        };
	    registerReceiver(recieveeStart, intentFilter);
	    //** snip **//
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
			unregisterReceiver(recieveeStart);
		}catch(RuntimeException er){
			Log.e(getCallingPackage(), er.getMessage());
		}
	}
	private void createViews() {
		// Bindings
		final Button registerBtn = (Button) findViewById(R.id.btn_register);
		final Button loginBtn = (Button) findViewById(R.id.btn_login);
		
		//primary reset when user reach login to clear stale data
		DrypersResources.Reset(context);
		
		// Startup Buttons
		registerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginMasterActivity.this,
						RegisterActivity.class));
			}
		});

		loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginMasterActivity.this,
						LoginNonFBActivity.class));
			}
		});
	}
}
