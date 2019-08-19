package de.polardreams.geeksrun;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Procress extends Activity{
	private ProgressBar bar ;
	private TextView txt;
	private Handler update_bar = new Handler();
	private boolean close = true;
	private int progress_hilf=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_procress);
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		this.setTitle("Laden ...");
		bar = (ProgressBar)findViewById(R.id.progressBar1);
		txt = (TextView)findViewById(R.id.progress_txt);
		//txt.setText("Deine Daten werden geladen ... 0%");
		txt.setText("Deine Daten werden geladen ...");
		txt.setTextColor(Color.WHITE);
		/*
		ProgressDialog pdialog = new ProgressDialog(this);
		pdialog.setTitle("Hinweis");
		pdialog.setMessage("Deine Daten werden geladen");
		pdialog.show();
		*/
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int hilf = 0;
				while(close) {
				//progress_hilf++;
				//if (progress_hilf==99) {close=false;}
					if (progress_hilf>hilf) {
					update_bar.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							bar.setProgress(progress_hilf);
							//txt.setText("Deine Daten werden geladen ... "+String.valueOf(progress_hilf)+"%");
							txt.setText("Deine Daten werden geladen ... ");//Weil keine Animation zu stande kommt
						}
					});
					hilf=progress_hilf;
					}
				}
			}
		}).start();
		Update_data update = new Update_data();
		update.execute(0);
	}//create
	
	private class Update_data extends AsyncTask<Integer, Integer, String> {
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			while(close){
				try {
					SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					progress_hilf = info.getInt("load_progress", params[0]);
				}catch (Exception e) {
					e.printStackTrace();
				}//try
				if (progress_hilf==100) {
					close=false;
					finish();
				}else {
				}//if (progress_hilf==100) {
				}//while(close)
			return null;
		}//while
	}//private class Update_data extends AsyncTask<Integer, Integer, String> {
	
	
}
