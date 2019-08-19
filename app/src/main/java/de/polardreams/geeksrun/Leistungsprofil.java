package de.polardreams.geeksrun;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class Leistungsprofil extends Activity {

	private TextView t1,t2, t3, t4, t5, t6, t7, t8, t9, t10;
	private boolean lv_flag=false;
	private Boolean backpress_flag=false;
	private Boolean close =true;
	private int backpress_count=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leistungsprofil);
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);

		t1 = (TextView)findViewById(R.id.textView1);
		t2 = (TextView)findViewById(R.id.textView2);
		t3 = (TextView)findViewById(R.id.textView3);
		t4 = (TextView)findViewById(R.id.textView4);
		t5 = (TextView)findViewById(R.id.textView5);
		t6 = (TextView)findViewById(R.id.textView6);
		t7 = (TextView)findViewById(R.id.textView7);
		t8 = (TextView)findViewById(R.id.textView8);
		t9 = (TextView)findViewById(R.id.textView9);
		t10 = (TextView)findViewById(R.id.textView10);
		//Laden der leistungsdaten
		try {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			double perf_km = Double.valueOf(info.getString("performance_km", "0"));
			double perf_kalorie = Double.valueOf(info.getString("performance_kalorien", "0"));
			double perf_zeit = Integer.valueOf(info.getString("performance_time", "0"));
			int perf_trainings = Integer.valueOf(info.getString("performance_training", "0"));
			int perf_challenges = Integer.valueOf(info.getString("performance_challenge", "0"));
			int perf_challenge_bronxe=Integer.valueOf(info.getString("performance_challenge_bronxe", "0"));
			int perf_challenge_silber=Integer.valueOf(info.getString("performance_challenge_silber", "0"));
			int perf_challenge_gold=Integer.valueOf(info.getString("performance_challenge_gold", "0"));

			//Anzahl der Pokale ermitteln
			int pokal=0;
			if (perf_kalorie>=1000.0) {
				pokal++;
			}
			if (perf_kalorie>=3000.0) {
				pokal++;
			}
			if (perf_kalorie>=9000.0) {
				pokal++;
			}
			if (perf_kalorie>=27000.0) {
				pokal++;
			}
			if (perf_kalorie>=81000.0) {
				pokal++;
			}
			if (perf_kalorie>=243000.0) {
				pokal++;
			}
			if (perf_kalorie>=800000.0) {
				pokal++;
			}
			if (perf_km>=5.0) {
				pokal++;
			}
			if (perf_km>=15.0) {
				pokal++;
			}
			if (perf_km>=45.0) {
				pokal++;
			}
			if (perf_km>=135.0) {
				pokal++;
			}
			if (perf_km>=405.0) {
				pokal++;
			}
			if (perf_km>=1215.0) {
				pokal++;
			}
			if (perf_km>=3645.0) {
				pokal++;
			}
			if (perf_km>=10000.0) {
				pokal++;
			}
			int pkt=(perf_challenge_gold*3)+(perf_challenge_silber*2)+(perf_challenge_bronxe);
			if (pkt>=3) {
				pokal++;
			}
			if (pkt>=9) {
				pokal++;
			}
			if (pkt>=27) {
				pokal++;
			}
			if (pkt>=81) {
				pokal++;
			}
			if (pkt>=243) {
				pokal++;
			}
			if (pkt>=800) {
				pokal++;
			}
			if (perf_zeit>=3600) {//1h
				pokal++;
			}
			if (perf_zeit>=36000) {//10h
				pokal++;
			}
			if (perf_zeit>=86400) {//1tag
				pokal++;
			}
			if (perf_zeit>=259200) {//3tage
				pokal++;
			}
			if (perf_zeit>=604800) {//1woche
				pokal++;
			}
			if (perf_zeit>=2419200) {//1monat
				pokal++;
			}

			//Ausgabe der Leistungsdaten
			t1.setText("Dein Leistungsprofil");

			t2.setText("Deine gelaufenen Kilometer: "+String.format("%.2f",perf_km)+" km");
			t3.setText("Deine verbrannten Kalorien: "+String.format("%.2f",perf_kalorie)+" kcal");
			//double hilf_time = Math.round(perf_zeit/3600);
			double hilf_time = perf_zeit/3600;
			t4.setText("Deine investierte Zeit: "+String.format("%.2f", hilf_time)+" Stunden");
			//t4.setText("Deine investierte Zeit: "+String.valueOf(hilf_time)+" Stunden");
			t5.setText("Deine Anzahl an Trainings: "+String.valueOf(perf_trainings));
			t6.setText("Deine Anzahl an Challenges: "+String.valueOf(perf_challenges));

			if (pokal>=0 && pokal<5) {
				if (pokal==1) {
					t7.setText("Rang: Anf�nger "+String.valueOf(pokal)+" Pokal");
				} else {
					t7.setText("Rang: Anf�nger mit "+String.valueOf(pokal)+" Pokalen");
				}

			}
			if (pokal>=5 && pokal<10) {
				t7.setText("Rang: Fortgeschrittener mit "+String.valueOf(pokal)+" Pokalen");
			}
			if (pokal>=10 && pokal<15) {
				t7.setText("Rang: Talentierter mit "+String.valueOf(pokal)+" Pokalen");
			}
			if (pokal>=15 && pokal<20) {
				t7.setText("Rang: Profi mit "+String.valueOf(pokal)+" Pokalen");
			}
			if (pokal>=20 && pokal<25) {
				t7.setText("Rang: Meister mit "+String.valueOf(pokal)+" Pokalen");
			}
			if (pokal>=25 && pokal<27) {
				t7.setText("Rang: Weltmeister mit "+String.valueOf(pokal)+" Pokalen");
			}
			if (pokal>=27) {
				t7.setText("Rang: Halbgott mit genau allen "+String.valueOf(pokal)+" Pokalen");
			}

			t8.setText(String.valueOf(perf_challenge_gold)+"x Gold");
			t9.setText(String.valueOf(perf_challenge_silber)+"x Silber");
			t10.setText(String.valueOf(perf_challenge_bronxe)+"x Bronze");
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Es ist ein Fehler beim Aktualisieren deiner Leistungen aufgetreten! ("+e.getMessage()+"", Toast.LENGTH_LONG).show();
			t7.setText("Fehlermeldung: "+e.getMessage());
		}//try
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("back_press_flag",false);
		editor.commit();

		Update_data update = new Update_data();
		update.execute(0);
	}

	private class Update_data extends AsyncTask<Integer, Integer, String> {
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			while(close){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					backpress_flag = info.getBoolean("back_press_flag", false);
				}catch (Exception e) {
					e.printStackTrace();
				}//try
			}//while(close)
			return null;
		}//while
	}//private class Update_data extends AsyncTask<Integer, Integer, String> {

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (backpress_flag==true) {
			Toast.makeText(this, "Bitte verwende die Navigationsbuttons um abzubrechen.", Toast.LENGTH_LONG).show();
		}else {
			close=false;
			super.onBackPressed();
		}//if (lv_flag==true) {
	}
}
