package de.polardreams.geeksrun;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Pokal_Kalorien extends Activity {
	private ImageView im1, im2, im3, im4, im5, im6, im7;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokal__kalorien);
		
		im1 = (ImageView)findViewById(R.id.imageView1);
		im2 = (ImageView)findViewById(R.id.imageView2);
		im3 = (ImageView)findViewById(R.id.imageView3);
		im4 = (ImageView)findViewById(R.id.imageView4);
		im5 = (ImageView)findViewById(R.id.imageView5);
		im6 = (ImageView)findViewById(R.id.imageView6);
		im7 = (ImageView)findViewById(R.id.imageView7);
		
		
		im1.setVisibility(View.GONE);
		im2.setVisibility(View.GONE);
		im3.setVisibility(View.GONE);
		im4.setVisibility(View.GONE);
		im5.setVisibility(View.GONE);
		im6.setVisibility(View.GONE);
		im7.setVisibility(View.GONE);
		
		//Laden der leistungsdaten
				try {
				SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				double perf_kalorie = Double.valueOf(info.getString("performance_kalorien", "0"));

				if (perf_kalorie>=1000.0) {
					im1.setVisibility(View.VISIBLE);
				}
				if (perf_kalorie>=3000.0) {
					im2.setVisibility(View.VISIBLE);
				}
				if (perf_kalorie>=9000.0) {
					im3.setVisibility(View.VISIBLE);
				}
				if (perf_kalorie>=27000.0) {
					im4.setVisibility(View.VISIBLE);
				}
				if (perf_kalorie>=81000.0) {
					im5.setVisibility(View.VISIBLE);
				}
				if (perf_kalorie>=243000.0) {
					im6.setVisibility(View.VISIBLE);
				}
				if (perf_kalorie>=800000.0) {
					im7.setVisibility(View.VISIBLE);
				}
				
				} catch(Exception e) {
					e.printStackTrace();
				}//try
	
	}

}
