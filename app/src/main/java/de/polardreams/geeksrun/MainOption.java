package de.polardreams.geeksrun;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainOption extends Activity implements OnItemClickListener{
	private ListView lv = null;
	private Context c;
	private Geekspolra expolrer;
	private Handler handler = new Handler();

	//Sound
	private int sound_music = 0;
	private int sound_challenge = 0;
	private int sound_geoplayer = 0;
	private TextView txt_music, txt_challenge, txt_geo;
	private boolean lv_flag=false;

	TextView licence_txt;
	TextView version_txt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_option);
		this.setTitle("Einstellungen");
		c = this;
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		get_menu();
	}

	public void get_menu() {
		String[] entries = new String[6];
		entries[0]="Importordner";
		entries[1]="Exportordner";
		entries[2]="Musikordner";
		entries[3]="Lautst�rken";
		entries[4]="weitere Einstellungen";
		entries[5]="Lizenz";
		try {
			lv = null;
			lv = (ListView)findViewById(R.id.listView_main_option);


			ArrayAdapter aa = new ArrayAdapter<String>(this,R.layout.listview_white_option, R.id.listview_content_option, entries);

			lv.setAdapter(aa);
			lv.setOnItemClickListener(this);
			lv.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.VISIBLE);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}//try
	}//public void get_menu() {

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if (position == 0) {
			lv_flag=true;
			expolrer= new Geekspolra(this,lv);
			expolrer.explore();
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(!expolrer.get_process()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//try
					}//while
					if (expolrer.get_process()==true){
						handler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
								SharedPreferences.Editor editor = settings.edit();
								editor.putString("import_location",expolrer.get_adress());
								editor.commit();
								lv_flag=false;
								get_menu();
							}
						});
					}
				}
			}, 0);
		}//Importordner

		if (position == 1) {
			lv_flag=true;
			expolrer= new Geekspolra(this,lv);
			expolrer.explore();
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(!expolrer.get_process()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//try
					}//while
					if (expolrer.get_process()==true){
						handler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
								SharedPreferences.Editor editor = settings.edit();
								editor.putString("export_localtion",expolrer.get_adress());
								editor.commit();
								lv_flag=false;
								get_menu();
							}
						});
					}
				}
			}, 0);
		}//Exportordner

		if (position == 2) {
			lv_flag=true;
			expolrer= new Geekspolra(this,lv);
			expolrer.explore();
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(!expolrer.get_process()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//try
					}//while
					if (expolrer.get_process()==true){
						handler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
								SharedPreferences.Editor editor = settings.edit();
								editor.putString("music_location",expolrer.get_adress());
								editor.commit();
								lv_flag=false;
								get_menu();
							}
						});
					}
				}
			}, 0);
		}//Musikordner

		if (position == 3) {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			sound_music=Integer.valueOf(info.getString("sound_musicplayer", "80"));
			sound_challenge=Integer.valueOf(info.getString("sound_challenge", "80"));
			sound_geoplayer=Integer.valueOf(info.getString("sound_geoplayer", "80"));

			final Dialog sound_menu = new Dialog (this);
			sound_menu.requestWindowFeature(Window.FEATURE_NO_TITLE);
			sound_menu.setContentView(R.layout.sound_menu);
			sound_menu.setTitle("Soundeinstellungen");
			txt_music =(TextView)sound_menu.findViewById(R.id.sound_textView1);
			txt_music.setText("Lautst�rke Musikplayer: "+String.valueOf(sound_music)+"%");
			txt_challenge = (TextView)sound_menu.findViewById(R.id.sound_textView2);
			txt_challenge.setText("Lautst�rke Kommentatoren: "+String.valueOf(sound_challenge)+"%");
			txt_geo = (TextView)sound_menu.findViewById(R.id.sound_textView3);
			txt_geo.setText("Lautst�rke Geoplayer: "+String.valueOf(sound_geoplayer)+"%");

			SeekBar sb_music = (SeekBar)sound_menu.findViewById(R.id.sound_seekBar1);
			sb_music.setMax(100);
			sb_music.setProgress(sound_music);
			sb_music.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// TODO Auto-generated method stub
					sound_music=progress;
					txt_music.setText("Lautst�rke Musikplayer: "+String.valueOf(sound_music)+"%");
				}
			});

			SeekBar sb_challenge = (SeekBar)sound_menu.findViewById(R.id.sound_seekBar2);
			sb_challenge.setMax(100);
			sb_challenge.setProgress(sound_challenge);
			sb_challenge.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// TODO Auto-generated method stub
					sound_challenge=progress;
					txt_challenge.setText("Lautst�rke Kommentatoren: "+String.valueOf(sound_challenge)+"%");
				}
			});

			SeekBar sb_geo = (SeekBar)sound_menu.findViewById(R.id.sound_seekBar3);
			sb_geo.setMax(100);
			sb_geo.setProgress(sound_geoplayer);
			sb_geo.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// TODO Auto-generated method stub
					sound_geoplayer=progress;
					txt_geo.setText("Lautst�rke Geoplayer: "+String.valueOf(sound_geoplayer)+"%");
				}
			});

			Button btn_sound_menu = (Button) sound_menu.findViewById(R.id.sound_button1);
			btn_sound_menu.setText("OK");
			btn_sound_menu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("sound_musicplayer",String.valueOf(sound_music));
					editor.commit();

					editor = settings.edit();
					editor.putString("sound_challenge",String.valueOf(sound_challenge));
					editor.commit();

					editor = settings.edit();
					editor.putString("sound_geoplayer",String.valueOf(sound_geoplayer));
					editor.commit();

					sound_menu.cancel();
				}
			});
			sound_menu.show();

		}//if (position == 3) {

		if (position == 4) {
			Intent i = new Intent(this, Options.class);
			startActivity(i);
		}//weitere Einstellungen
		if (position == 5) {
			final Dialog licence = new Dialog (this);
			licence.requestWindowFeature(Window.FEATURE_NO_TITLE);
			licence.setContentView(R.layout.licence);
			licence.setTitle("Lizenz");
			licence_txt = (TextView)licence.findViewById(R.id.graphview_liecense);
			version_txt = (TextView)licence.findViewById(R.id.textView7);
			String versionName ="";
			try {
				PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				versionName = pinfo.versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				versionName = "Versionsname nicht lesbar.";
			}//try
			try {
				version_txt.setText("GeeksRun "+versionName);
			} catch(Exception e) {
				e.printStackTrace();
			}//try
			Button btn_close = (Button)licence.findViewById(R.id.button1);
			btn_close.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					licence.cancel();
				}
			});

			licence_txt.setText(getString(R.string.graphview_licence));
			licence.show();

			/*
			 * Bitte ausklammern!!!
			 */

			//Activity intent f�r Lizenz
			//Toast.makeText(c, "GeeksRun Beta Update 2", Toast.LENGTH_SHORT).show();
			//Toast.makeText(c, "Die Trialware ist bis zum 30.Juni.2016 verwendtbar", Toast.LENGTH_LONG).show();
			//Toast.makeText(c, "Danach speert sich das Training (Startbildschirm)", Toast.LENGTH_SHORT).show();
		}//Lizenz

	}//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (lv_flag==true) {
			Toast.makeText(this, "Bitte verwende die Navigationsbuttons um abzubrechen.", Toast.LENGTH_LONG).show();
		}else {
			super.onBackPressed();
		}//if (lv_flag==true) {
	}
}
