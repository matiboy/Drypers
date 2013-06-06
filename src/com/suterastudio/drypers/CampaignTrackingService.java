package com.suterastudio.drypers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpConnection;
import org.apache.http.client.utils.URIBuilder;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class CampaignTrackingService extends Service {
	
	private final String ADVERTISER_ID = "1543905";
	private final String HMACMD5_KEY = "b918782e2b";
	private final String RAW_QUERY = "tp_aid=%s&tp_sid=%s&tp_t=%s";
	private final String ENCODING = "UTF-8";
	private final String CAMPAIGN_TERM = "spiral";
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid){
		String referrer = intent.getStringExtra("referrer");
		if( referrer == null || referrer.length() == 0) {
            return START_NOT_STICKY;
        }
 
        try
        {    // Remove any url encoding
            referrer = URLDecoder.decode(referrer, "UTF-8"); //$NON-NLS-1$
        }
        catch (UnsupportedEncodingException e) { 
    		return START_NOT_STICKY;
		}
        
        Map<String, String> referralParams = new HashMap<String, String>();
   	 
        // Parse the query string, extracting the relevant data
        String[] params = referrer.split("&"); // $NON-NLS-1$
        for (String param : params)
        {
            String[] pair = param.split("="); // $NON-NLS-1$
            referralParams.put(pair[0], pair[1]);
        }
        String source = referralParams.get("utm_source");
        if(source == null) {
        	source = "";        	
        }
        String term = referralParams.get("utm_term");
        if(term == null) {
        	term = "";        	
        }
        if(source.equals(CAMPAIGN_TERM) || term.equals(CAMPAIGN_TERM))
        {
	        
	        URL url = buildUri(referralParams);
	        
	        if(url != null){
	        	try {
					URLConnection connection = url.openConnection();
					InputStream in = connection.getInputStream();
					String encoding = connection.getContentEncoding();
					encoding = encoding == null ? "UTF-8" : encoding;
					String body = IOUtils.toString(in, encoding);
					body = "";
					// TODO Do something with the body?
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
        }   
        
		return START_NOT_STICKY;
	}
	
	private URL buildUri(Map<String, String> params){
		String sharedKey = HMACMD5_KEY; // your shared key

		SecretKeySpec secretKeySpec =null;
		try {
			secretKeySpec = new SecretKeySpec(sharedKey.getBytes("UTF-8"), "hmacMD5");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Mac mac=null;
		try {
			mac = Mac.getInstance(secretKeySpec.getAlgorithm());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			mac.init(secretKeySpec);
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Get Unix timestamp
		long timestamp = System.currentTimeMillis() / 1000;
		String clickId = params.get( "babybabble" );
		if( clickId == null ) {
			clickId = params.get( "utm_term" );
		}
		if( clickId != null ) {
			String query = String.format( RAW_QUERY, ADVERTISER_ID, clickId, String.valueOf(timestamp)); // the query string to sign
	
			byte[] hashBytes=null;
			try {
				hashBytes = mac.doFinal(query.getBytes(ENCODING));
			} catch (IllegalStateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String actualHash = toHex(hashBytes);
			String pixelUrl = "https://tpc.trialpay.com/?" + query + "&tp_v1=" + actualHash;
			try {
				return new URL(pixelUrl);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	private String toHex(byte[] arg) {
		return String.format("%x", new BigInteger(1, arg));
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
