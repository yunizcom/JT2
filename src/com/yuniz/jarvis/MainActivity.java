package com.yuniz.jarvis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yuniz.jarvis.R;

import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Spinner spinner1;
	private EditText speakReturn;
	private EditText editText1;
	MediaPlayer mMediaPlayer;
	private int SPEECH_REQUEST_CODE = 1234;
	private String previousURL = "";
	private RelativeLayout mainCanvas;
	private ImageView imageButton1;
	private ImageView imageButton2;
	private ImageView imageButton3;
	private TextView textView1;
	
	public int screenWidth = 0;
	public int screenHeight = 0;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//----------detect device setting and adapt environment
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		screenWidth = size.x;
		screenHeight = size.y;
		
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		//----------detect device setting and adapt environment
		
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		speakReturn = (EditText) findViewById(R.id.textView2);
		editText1 = (EditText) findViewById(R.id.editText1);
		textView1 = (TextView) findViewById(R.id.textView1);
		
		mainCanvas = (RelativeLayout) findViewById(R.id.mainCanvas);
		
		imageButton1 = (ImageView) findViewById(R.id.imageView1);
		imageButton2 = (ImageView) findViewById(R.id.imageView2);
		imageButton3 = (ImageView) findViewById(R.id.imageView3);
		
		try 
		{
		    InputStream ims = getAssets().open("bg.jpg");
		    Drawable d = Drawable.createFromStream(ims, null);

		    int sdk = android.os.Build.VERSION.SDK_INT;
		    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
		    	mainCanvas.setBackgroundDrawable(d);
		    } else {
		    	mainCanvas.setBackground(d);
		    }
		    
		    InputStream ims1 = getAssets().open("speechBtn.png");
		    Drawable d1 = Drawable.createFromStream(ims1, null);
		    imageButton1.setImageDrawable(d1);
		    
		    ims1 = getAssets().open("replay.png");
		    d1 = Drawable.createFromStream(ims1, null);
		    imageButton2.setImageDrawable(d1);
		    
		    ims1 = getAssets().open("convert.png");
		    d1 = Drawable.createFromStream(ims1, null);
		    imageButton3.setImageDrawable(d1);
		}
		catch(IOException ex) 
		{
		    return;
		}
		
		//----------auto Adjust UI Elements size----------
		double setNewHeight = screenHeight * 0.05;
		textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
		setNewHeight = screenHeight * 0.45;
		imageButton1.setMinimumHeight((int)setNewHeight);
		imageButton1.setMaxHeight((int)setNewHeight);
		setNewHeight = screenHeight * 0.05;
		spinner1.setMinimumHeight((int)setNewHeight);
		setNewHeight = screenHeight * 0.12;
		double setNewWidth = screenWidth * 0.9;
		speakReturn.setMaxHeight((int)setNewHeight);
		speakReturn.setMinHeight((int)setNewHeight);
		editText1.setMaxHeight((int)setNewHeight);
		editText1.setMinHeight((int)setNewHeight);
		speakReturn.setMaxWidth((int)setNewWidth);
		speakReturn.setMinWidth((int)setNewWidth);
		editText1.setMaxWidth((int)setNewWidth);
		editText1.setMinWidth((int)setNewWidth);
		setNewHeight = screenHeight * 0.09;
		imageButton2.setMinimumHeight((int)setNewHeight);
		imageButton2.setMaxHeight((int)setNewHeight);
		setNewHeight = screenHeight * 0.13;
		setNewWidth = screenWidth * 0.1;
		imageButton3.setMinimumHeight((int)setNewHeight);
		imageButton3.setMaxHeight((int)setNewHeight);
		imageButton3.setMinimumWidth((int)setNewWidth);
		imageButton3.setMaxWidth((int)setNewWidth);
		imageButton3.setPadding(0, (int)(setNewHeight * 0.3), 0, 0);
		//----------auto Adjust UI Elements size----------
		
		mMediaPlayer  = new MediaPlayer();

		if(!isNetworkAvailable()){
			Toast.makeText(getApplicationContext(), "You need a smooth internet connection before you can use this app." , Toast.LENGTH_LONG).show();
		}
		
	}
	
	private void sendRecognizeIntent()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your words.");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public void textConvertButtonClick(View v){
		if(speakReturn.getText().toString().trim().length() > 0){
			combinedSpeakCountry(speakReturn.getText().toString());
		}else{
			Toast.makeText(getApplicationContext(), "Please type some english words before continue." , Toast.LENGTH_LONG).show();
		}
	}
	
	public void speakButtonClick(View v){
		sendRecognizeIntent();
	}
	
	public void openURL(View v) {
		Uri uri = Uri.parse("http://stanly.yuniz.com");
		 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		 startActivity(intent);
	}
	
	public void rpButtonClick(View v){
		if(previousURL != ""){
			
			mMediaPlayer.reset();
	    	mMediaPlayer.release();
	    	mMediaPlayer = null;
	    	
	    	//-------create and load background song
	    	mMediaPlayer = MediaPlayer.create(
			    this,
			    Uri.parse(previousURL));
	    	mMediaPlayer.start();
			//-------create and load background song
	    	
		}else{
			Toast.makeText(getApplicationContext(), "Please touch the big icon to start." , Toast.LENGTH_LONG).show();
		}
	}
	
	 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SPEECH_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                
                if (matches.size() == 0)
                {
                	speakReturn.setText("ERROR : HEARD NOTHING, PLEASE SPEAK AGAIN.");
                }
                else
                {
                    String mostLikelyThingHeard = matches.get(0);
                    speakReturn.setText(mostLikelyThingHeard);
                    combinedSpeakCountry(mostLikelyThingHeard);
                }
            }
            else
            {
            	speakReturn.setText("ERROR : CONNECTION FAILED.");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
	public void combinedSpeakCountry(String words){
		String wordsEncoded = words;
		try {
			wordsEncoded = URLEncoder.encode(words, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		callGoogleTranslateApi(wordsEncoded, currentSelectedCountry());
	}
	 
	public void callGoogleTranslateApi(String words, String country){
		String url = "http://translate.google.com/translate_a/t?client=p&hl=en&sl=en&tl=" + country + "&ie=UTF-8&oe=UTF-8&multires=1&oc=1&otf=1&ssel=0&tsel=0&sc=1&q=" + words;
		//-------load JSON
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        //nameValuePairs.add(new BasicNameValuePair("name", "MAMAMA"));
        
		JSONObject json = getJSONfromURL(url, nameValuePairs);
		try {
			JSONArray  jsoncontacts = json.getJSONArray("sentences");
					
				for(int i=0;i < jsoncontacts.length();i++){						
			
			        	JSONObject e = jsoncontacts.getJSONObject(i);
			        	
			        	String utf8String = "";
						try {
							utf8String = new String(e.getString("trans").getBytes("ISO-8859-1"), "UTF-8");
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			        	
						editText1.setText(utf8String);
						
			        	//Log.v("STANLY DEBUG","http://translate.google.com/translate_tts?ie=UTF-8&q=" + utf8String + "&tl=" + currentSelectedCountry() + "&total=1&idx=0");
			        	previousURL = "http://translate.google.com/translate_tts?ie=UTF-8&q=" + utf8String + "&tl=" + currentSelectedCountry() + "&total=1&idx=0";
			        	
			        	mMediaPlayer.reset();
			        	mMediaPlayer.release();
			        	mMediaPlayer = null;
			        	
			        	//-------create and load background song
			        	mMediaPlayer = MediaPlayer.create(
			    		    this,
			    		    Uri.parse("http://translate.google.com/translate_tts?ie=UTF-8&q=" + utf8String + "&tl=" + currentSelectedCountry() + "&total=1&idx=0"));
			        	mMediaPlayer.start();
			    		//-------create and load background song
	
				}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//-------load JSON
	}
	
	public String currentSelectedCountry(){
		int selectedIndex = spinner1.getSelectedItemPosition();
		String selectedCountry = "zh-CN";
		
		switch(selectedIndex){
			case 0:
				selectedCountry = "zh-CN";
			break;	
			case 1:
				selectedCountry = "id";
			break;
			case 2:
				selectedCountry = "ja";
			break;
			case 3:
				selectedCountry = "ko";
			break;
			case 4:
				selectedCountry = "hi";
			break;
			case 5:
				selectedCountry = "th";
			break;
			case 6:
				selectedCountry = "ru";
			break;
			default:
				selectedCountry = "zh-CN";
			break;	
		}
		
		return selectedCountry;
	}
	
	public static JSONObject getJSONfromURL(String url,List<NameValuePair> postDatas ){

		//initialize
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

		//http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			
	        httppost.setEntity(new UrlEncodedFormEntity(postDatas));
			
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}

		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result=sb.toString();
		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}

		//try parse the string to a JSON object
		try{
	        	jArray = new JSONObject(result);
		}catch(JSONException e){
			Log.e("log_tag", "Error parsing data "+e.toString());
		}

		return jArray;
	} 
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMediaPlayer.stop();
        mMediaPlayer.release();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
