package com.suterastudio.drypers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class ReferralReceiver extends BroadcastReceiver
	{
	    @Override
	    public void onReceive(Context context, Intent intent)
	    {
	    	try
	        {
	            final Bundle extras = intent.getExtras();
	            if (extras != null) {
	                extras.containsKey(null);
	            }
	        }
	        catch (final Exception e) {
	            return;
	        }
	 
	        
	        // Return if this is not the right intent.
	        if (! intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) { //$NON-NLS-1$
	            return;
	        }
	 
	        String referrer = intent.getStringExtra("referrer"); //$NON-NLS-1$
	        
	        Intent service = new Intent(context, CampaignTrackingService.class);
	        service.putExtra("referrer", referrer);
	        context.startService(service);
	    }
	 
	    private final static String[] EXPECTED_PARAMETERS = {
	        "utm_source",
	        "utm_medium",
	        "utm_term",
	        "utm_content",
	        "utm_campaign"
	    };
	    private final static String PREFS_FILE_NAME = "ReferralParamsFile";
	 
	    /*
	     * Stores the referral parameters in the app's sharedPreferences.
	     * Rewrite this function and retrieveReferralParams() if a
	     * different storage mechanism is preferred.
	     */
	    public static void storeReferralParams(Context context, Map<String, String> referralParams)
	    {
	        SharedPreferences storage = context.getSharedPreferences(ReferralReceiver.PREFS_FILE_NAME, Context.MODE_PRIVATE);
	        SharedPreferences.Editor editor = storage.edit();
	 
	        for(String key : ReferralReceiver.EXPECTED_PARAMETERS)
	        {
	            String value = referralParams.get(key);
	            if(value != null)
	            {
	                editor.putString(key, value);
	            }
	        }
	 
	        editor.commit();
	    }
	 
	    /*
	     * Returns a map with the Market Referral parameters pulled from the sharedPreferences.
	     */
	    public static HashMap<String, String> retrieveReferralParams(Context context)
	    {
	        HashMap<String, String> params = new HashMap<String, String>();
	        SharedPreferences storage = context.getSharedPreferences(ReferralReceiver.PREFS_FILE_NAME, Context.MODE_PRIVATE);
	 
	        for(String key : ReferralReceiver.EXPECTED_PARAMETERS)
	        {
	            String value = storage.getString(key, null);
	            if(value != null)
	            {
	                params.put(key, value);
	            }
	        }
	        return params;
	    }

}
