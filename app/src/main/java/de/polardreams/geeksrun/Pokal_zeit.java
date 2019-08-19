package de.polardreams.geeksrun;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class Pokal_zeit extends Activity {
	private ImageView im1, im2, im3, im4, im5, im6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokal_zeit);
		im1 = (ImageView)findViewById(R.id.imageView1);
		im2 = (ImageView)findViewById(R.id.imageView2);
		im3 = (ImageView)findViewById(R.id.imageView3);
		im4 = (ImageView)findViewById(R.id.imageView4);
		im5 = (ImageView)findViewById(R.id.imageView5);
		im6 = (ImageView)findViewById(R.id.imageView6);
		
		im1.setVisibility(View.GONE);
		im2.setVisibility(View.GONE);
		im3.setVisibility(View.GONE);
		im4.setVisibility(View.GONE);
		im5.setVisibility(View.GONE);
		im6.setVisibility(View.GONE);
		
		//Laden der leistungsdaten
				try {
				SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				int perf_zeit = Integer.valueOf(info.getString("performance_time", "0"));
				
				if (perf_zeit>=3600) {//1h
					im1.setVisibility(View.VISIBLE);
				}
				if (perf_zeit>=36000) {//10h
					im2.setVisibility(View.VISIBLE);
				}
				if (perf_zeit>=86400) {//1tag
					im3.setVisibility(View.VISIBLE);
				}
				if (perf_zeit>=259200) {//3tage
					im4.setVisibility(View.VISIBLE);
				}
				if (perf_zeit>=604800) {//1woche
					im5.setVisibility(View.VISIBLE);
				}
				if (perf_zeit>=2419200) {//1monat
					im6.setVisibility(View.VISIBLE);
				}
				
				} catch(Exception e) {
					e.printStackTrace();
				}//try
		
	}
}
