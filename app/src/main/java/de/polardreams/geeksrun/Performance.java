package de.polardreams.geeksrun;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;


public class Performance extends TabActivity implements OnClickListener, OnItemClickListener {

	private TabHost geeksgalleries;
	private boolean import_mode=false;
	private Button btn_import_abbruch;
	private File im_meinspeicherort;
	private String[] entries;
	private ListView lv = null;
	private Context c=this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_performance);
		geeksgalleries=(TabHost)findViewById(android.R.id.tabhost);
		TabSpec tab1 = geeksgalleries.newTabSpec("tab1");
		TabSpec tab2 = geeksgalleries.newTabSpec("tab2");

		tab1.setIndicator("Deine Leistungen").setContent(new Intent (this, Leistungsprofil.class));
		tab2.setIndicator("Deine Pokale").setContent(new Intent (this, Geekspokal.class));

		geeksgalleries.addTab(tab1);
		geeksgalleries.addTab(tab2);

		lv = (ListView)findViewById(R.id.listView1);
		lv.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lv.setVisibility(View.GONE);
			}
		});//lv.post(new Runnable() {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		if (item.getItemId()==R.id.item_im) {
			//Import
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			int progress_hilf=0;
			editor.putBoolean("back_press_flag",true);
			editor.commit();

			try {
				SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				im_meinspeicherort = new File(info.getString("import_location", "My Documents")+"/");

				String[] s = im_meinspeicherort.list();
				entries = new String[im_meinspeicherort.list().length];
				for(int n=0; n<im_meinspeicherort.list().length; n++) {
					entries[n]=s[n];
				}//for(int n=0; n<im_meinspeicherort.list().length; n++) {
				ArrayAdapter aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entries);
				lv.setAdapter(aa);
				btn_import_abbruch = new Button (this);
				btn_import_abbruch.setText("Abbruch");
				btn_import_abbruch.setBackgroundResource(R.drawable.btn_dark);
				btn_import_abbruch.setTextColor(Color.BLACK);
				btn_import_abbruch.setOnClickListener(this);
				lv.addHeaderView(btn_import_abbruch);
				lv.setOnItemClickListener(this);
				import_mode=true;

				lv.setBackgroundResource(R.color.geek_white);
				lv.setAlpha((float)0.9);

				lv.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						lv.setVisibility(View.VISIBLE);
					}
				});//lv.post(new Runnable() {

			} catch(Exception e) {
				e.printStackTrace();
			}//try
		}//if (item.getItemId()==R.id.item_im) {

		if (item.getItemId()==R.id.item_ex) {
			//Export
			try {
				SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				double perf_km = Double.valueOf(info.getString("performance_km", "0"));
				double perf_kalorie = Double.valueOf(info.getString("performance_kalorien", "0"));
				int perf_zeit = Integer.valueOf(info.getString("performance_time", "0"));
				int perf_trainings = Integer.valueOf(info.getString("performance_training", "0"));
				int perf_challenges = Integer.valueOf(info.getString("performance_challenge", "0"));
				int perf_challenge_bronxe=Integer.valueOf(info.getString("performance_challenge_bronxe", "0"));
				int perf_challenge_silber=Integer.valueOf(info.getString("performance_challenge_silber", "0"));
				int perf_challenge_gold=Integer.valueOf(info.getString("performance_challenge_gold", "0"));
				String ex_info_performance="";
				ex_info_performance=
						String.valueOf(perf_km)+";"+
								String.valueOf(perf_kalorie)+";"+
								String.valueOf(perf_zeit)+";"+
								String.valueOf(perf_trainings)+";"+
								String.valueOf(perf_challenges)+";"+
								String.valueOf(perf_challenge_bronxe)+";"+
								String.valueOf(perf_challenge_silber)+";"+
								String.valueOf(perf_challenge_gold)+"#";
				//ExportRoutine
				try {
					SharedPreferences info1 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					File meinspeicherort_ex = new File(info1.getString("export_localtion", "My Documents")+"/");
					if (meinspeicherort_ex.canRead() == false) {
						meinspeicherort_ex.mkdir();
						Toast.makeText(this, "Der angegebene Export-Ordner konnte nicht gefunden werden. Deswegen wurde ein neuer Ordner erstellt.", Toast.LENGTH_LONG).show();
					}//if (meinspeicherort_ex.canRead() == false) {

					Format f = new SimpleDateFormat("dd_MM_yy");
					Date date=new Date();
					String s = f.format(date);
					String s1 ="Spielstand_"+s+".txt";
					File file = new File(meinspeicherort_ex, s1);
					FileOutputStream fos = new FileOutputStream(file);
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					//Einbetten des String in eine Textdatei
					osw.write(ex_info_performance);
					osw.flush();
					Toast.makeText(this, s1+" erfolgreich exportiert nach: "+meinspeicherort_ex.toString(), Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Toast.makeText(this, "Export fehlgeschlagen.", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}//Try

			} catch(Exception e) {
				e.printStackTrace();
				Toast.makeText(this, "Exportroutine fehlgeschlagen.", Toast.LENGTH_LONG).show();
			}//try
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.performance_im_ex, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn_import_abbruch) {
			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.GONE);
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
					SharedPreferences.Editor editor = settings.edit();
					int progress_hilf=0;
					editor.putBoolean("back_press_flag",false);
					editor.commit();
					import_mode=false;
				}//run
			});//lv.post(new Runnable() {
			lv.removeHeaderView(btn_import_abbruch);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if (import_mode==true) {
			//Laderoutine
			char[] inputBuffer = new char[999999];
			File add = new File(im_meinspeicherort, entries[position-1]);//entries[Index des Ausgew�hlten Item]
			String historytext="";
			try {
				FileInputStream fileinputstream = new FileInputStream(add);
				InputStreamReader inputstreamreader = new InputStreamReader(fileinputstream);//Zeiger melden an den leser
				inputstreamreader.read(inputBuffer);//Leser liest in einen Puffer (Char Typ )
				historytext = new String(inputBuffer);//der Puffer wird der Textvariablen �bergeben
				inputstreamreader.close();//Datei geschlossen
			} catch(Exception e){
				Toast.makeText(this, "Geeksrun konnt die zu importierende Datei nicht laden.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}//Try

			//herausfiltern von F�llzeichen inputBuffer
			try{
				int hilf = historytext.indexOf('\u0000');
				String hilf1x = historytext.substring(0, hilf);
				historytext=hilf1x;
			} catch (Exception e1) {
				Toast.makeText(this, "Verarbeitung des Eintrags fehlgeschlagen.", Toast.LENGTH_SHORT).show();
				e1.printStackTrace();
			}//Try

			try {
				double perf_km = Double.valueOf(historytext.substring(0, historytext.indexOf(";")));
				historytext=historytext.substring(historytext.indexOf(";")+1, historytext.length());
				double perf_kalorie =Double.valueOf(historytext.substring(0, historytext.indexOf(";")));
				historytext=historytext.substring(historytext.indexOf(";")+1, historytext.length());
				int perf_zeit = Integer.valueOf(historytext.substring(0, historytext.indexOf(";")));
				historytext=historytext.substring(historytext.indexOf(";")+1, historytext.length());
				int perf_trainings = Integer.valueOf(historytext.substring(0, historytext.indexOf(";")));
				historytext=historytext.substring(historytext.indexOf(";")+1, historytext.length());

				int perf_challenges = Integer.valueOf(historytext.substring(0, historytext.indexOf(";")));
				historytext=historytext.substring(historytext.indexOf(";")+1, historytext.length());
				int perf_challenge_bronxe =Integer.valueOf(historytext.substring(0, historytext.indexOf(";")));
				historytext=historytext.substring(historytext.indexOf(";")+1, historytext.length());
				int perf_challenge_silber =Integer.valueOf(historytext.substring(0, historytext.indexOf(";")));
				historytext=historytext.substring(historytext.indexOf(";")+1, historytext.length());
				int perf_challenge_gold =Integer.valueOf(historytext.substring(0, historytext.indexOf("#")));

				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("performance_km",String.valueOf(perf_km));
				editor.putString("performance_kalorien",String.valueOf(perf_kalorie));
				editor.putString("performance_time",String.valueOf(perf_zeit));
				editor.putString("performance_training",String.valueOf(perf_trainings));
				editor.putString("performance_challenge",String.valueOf(perf_challenges));
				editor.putString("performance_challenge_bronxe",String.valueOf(perf_challenge_bronxe));
				editor.putString("performance_challenge_silber",String.valueOf(perf_challenge_silber));
				editor.putString("performance_challenge_gold",String.valueOf(perf_challenge_gold));
				editor.commit();

				//Backpress Button
				int progress_hilf=0;
				editor.putBoolean("back_press_flag",false);
				editor.commit();

				finish();
				startActivity(getIntent());
			} catch(Exception e) {
				e.printStackTrace();

				lv.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						lv.setVisibility(View.GONE);
						import_mode=false;
					}//run
				});//lv.post(new Runnable() {
				lv.removeHeaderView(btn_import_abbruch);
				Toast.makeText(this, "Verarbeitung des Spielstandes fehlgeschlagen.", Toast.LENGTH_SHORT).show();
				//Backpress Button falls etwas schief geht 
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("back_press_flag",false);
				editor.commit();
			}
		}//if (import_mode==true) {
	}
}
