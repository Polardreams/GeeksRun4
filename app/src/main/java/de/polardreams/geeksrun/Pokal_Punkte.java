package de.polardreams.geeksrun;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class Pokal_Punkte extends Activity {
	private ImageView im1, im2, im3, im4, im5, im6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokal__punkte);
		
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
					int perf_challenge_bronxe=Integer.valueOf(info.getString("performance_challenge_bronxe", "0"));
					int perf_challenge_silber=Integer.valueOf(info.getString("performance_challenge_silber", "0"));
					int perf_challenge_gold=Integer.valueOf(info.getString("performance_challenge_gold", "0"));
				
				int pkt=(perf_challenge_gold*3)+(perf_challenge_silber*2)+(perf_challenge_bronxe);	
				if (pkt>=3) {
					im1.setVisibility(View.VISIBLE);
				}
				if (pkt>=9) {
					im2.setVisibility(View.VISIBLE);
				}
				if (pkt>=27) {
					im3.setVisibility(View.VISIBLE);
				}
				if (pkt>=81) {
					im4.setVisibility(View.VISIBLE);
				}
				if (pkt>=243) {
					im5.setVisibility(View.VISIBLE);
				}
				if (pkt>=800) {
					im6.setVisibility(View.VISIBLE);
				}
				} catch(Exception e) {
					e.printStackTrace();
				}//try
		
	}
}
