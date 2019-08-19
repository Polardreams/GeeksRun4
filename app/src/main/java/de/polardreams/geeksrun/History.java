package de.polardreams.geeksrun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class History extends Activity implements OnItemClickListener, OnClickListener, OnItemLongClickListener{
	private File meinspeicherort;
	private File im_meinspeicherort;
	private ListView lv = null;
	private String[] entries;
	private Intent mapintent =null;
	private boolean import_mode =false;
	private Context c = this;
	private Button his_dia_umbenennen;
	private Button his_dia_export ;
	private Button his_dia_laden ;
	private Button his_dia_loeschen;
	private Button his_dia_abbruch;
	private Button btn_import_abbruch;
	private boolean flag_sort_name, flag_sort_date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		lv = (ListView)findViewById(R.id.listView1);
		this.setTitle("Trainingsverlauf");
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		meinspeicherort = this.getDir("archiv",MODE_PRIVATE);
		
		try {
		entries = meinspeicherort.list();
		
		ArrayAdapter aa = new ArrayAdapter<String>(this, R.layout.listview_white, R.id.listview_content, entries);
		lv.setAdapter(aa);
		lv.setOnItemClickListener(this);
		lv.setOnItemLongClickListener(this);
		} catch(Exception e) {
			Toast.makeText(this, "GeeksRun konnte Eintruege des Laufarchives nicht finden.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}//Try
		ActionBar action = getActionBar();
		action.setDisplayHomeAsUpEnabled(true);
		flag_sort_name=true;
		flag_sort_date=true;
	}

	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId()==R.id.import_item) {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			im_meinspeicherort = new File(info.getString("import_location", "My Documents")+"/");
			try {
			String[] s = im_meinspeicherort.list();
			entries = new String[im_meinspeicherort.list().length];
			for(int n=0; n<im_meinspeicherort.list().length; n++) {
				entries[n]=s[n];
			}//for(int n=0; n<im_meinspeicherort.list().length; n++) {
			ArrayAdapter aa = new ArrayAdapter<String>(this, R.layout.listview_white, R.id.listview_content, entries);
			lv.setAdapter(aa);
			btn_import_abbruch = new Button (this);
			btn_import_abbruch.setText("Abbruch");
			btn_import_abbruch.setBackgroundResource(R.drawable.btn_dark);
			btn_import_abbruch.setTextColor(Color.WHITE);
			btn_import_abbruch.setOnClickListener(this);
			lv.addHeaderView(btn_import_abbruch);
			lv.setOnItemClickListener(this);
			import_mode=true;
			} catch(Exception e) {
				Toast.makeText(this, "GeeksRun konnte Eintr�ge des Importordners nicht finden.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}//Try
			
		} else {
			finish();
		}//if (item.getItemId()==R.id.import_item) {
		return true;
	}//public boolean onOptionsItemSelected(MenuItem item){
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		final int hilf1=position;
		if (import_mode==false) {
		
		final Dialog his_dialog = new Dialog(this);
		his_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		his_dialog.setContentView(R.layout.dialog_history);
		his_dia_umbenennen = (Button)his_dialog.findViewById(R.id.his_dia_button1);
		his_dia_export = (Button)his_dialog.findViewById(R.id.his_dia_button2);
		his_dia_laden =  (Button)his_dialog.findViewById(R.id.his_dia_button3);
		his_dia_loeschen = (Button)his_dialog.findViewById(R.id.his_dia_button4);
		his_dia_abbruch = (Button)his_dialog.findViewById(R.id.his_dia_button5);
		
		
		
		his_dia_umbenennen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					
					AlertDialog.Builder umben_dia = new AlertDialog.Builder(c);
					final EditText safe_txt = new EditText(c);
					safe_txt.setText(entries[hilf1]);
					umben_dia.setTitle("Hinweis");
					umben_dia.setMessage("Bitte gib einen neuen Namen ein");
					umben_dia.setView(safe_txt);
					umben_dia.setPositiveButton("umbenennen", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							File add = new File(meinspeicherort, entries[hilf1]);//entries[Index des Ausgewaehlten Item]
							add.renameTo(new File(meinspeicherort,safe_txt.getText().toString()+".txt"));
							finish();
							startActivity(getIntent());
						}
					});
					umben_dia.setNegativeButton("abbrechen",null);
					AlertDialog alert = umben_dia.create();
					alert.show();
				} catch(Exception e){
					Toast.makeText(History.this, "GeeksRun konnte den Eintrag nicht umbenennen.", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}//Try
			}
		});
		
		his_dia_export.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Laderoutine
				char[] inputBuffer = new char[999999];
				File add = new File(meinspeicherort, entries[hilf1]);//entries[Index des Ausgew�hlten Item]
				String historytext="";
				try {
				FileInputStream fileinputstream = new FileInputStream(add);
				InputStreamReader inputstreamreader = new InputStreamReader(fileinputstream);//Zeiger melden an den leser
				inputstreamreader.read(inputBuffer);//Leser liest in einen Puffer (Char Typ )
				historytext = new String(inputBuffer);//der Puffer wird der Textvariablen uebergeben
				inputstreamreader.close();//Datei geschlossen
				} catch(Exception e){
					Toast.makeText(History.this, "Geeksrun konnt die zu exportierende Datei nicht laden.", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}//Try
				
				//herausfiltern von F�llzeichen inputBuffer
				try{
				int hilf = historytext.indexOf('\u0000');
				String hilf1x = historytext.substring(0, hilf);
				historytext=hilf1x;
				} catch (Exception e1) {
					Toast.makeText(History.this, "Verarbeitung des Eintrags fehlgeschlagen.", Toast.LENGTH_SHORT).show();
					e1.printStackTrace();
				}//Try
				
				//ExportRoutine
				try {
					SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					File meinspeicherort_ex = new File(info.getString("export_localtion", "My Documents")+"/");
					
					if (meinspeicherort_ex.canRead() == false) {
						meinspeicherort_ex.mkdir();
						Toast.makeText(History.this, "Der angegebene Export-Ordner konnte nicht gefunden werden. Deswegen wurde ein neuer Ordner erstellt.", Toast.LENGTH_LONG).show();
					}//if (meinspeicherort_ex.canRead() == false) {
					
					String s = entries[hilf1];
					File file = new File(meinspeicherort_ex, s);		
					FileOutputStream fos = new FileOutputStream(file);
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					//Einbetten des String in eine Textdatei
					

					
					osw.write(historytext);
					osw.flush();
					} catch (IOException e) {
						Toast.makeText(History.this, "Export fehlgeschlagen.", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}//Try
				his_dialog.cancel();
				Toast.makeText(History.this, "Training wurde erfolgreich exportiert", Toast.LENGTH_SHORT).show();
			}
		});
		
		his_dia_laden.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				char[] inputBuffer = new char[999999];
				File add = new File(meinspeicherort, entries[hilf1]);//entries[Index des Ausgewaehlten Item]
				String historytext="";
				//Laderoutine
				try {
				FileInputStream fileinputstream = new FileInputStream(add);
				InputStreamReader inputstreamreader = new InputStreamReader(fileinputstream);//Zeiger melden an den leser
				inputstreamreader.read(inputBuffer);//Leser liest in einen Puffer (Char Typ )
				historytext = new String(inputBuffer);//der Puffer wird der Textvariablen �bergeben
				inputstreamreader.close();//Datei geschlossen
				} catch(Exception e){
					Toast.makeText(History.this, "GeeksRun konnte den Eintrag nicht lesen.", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}//Try
				//herausfiltern von Fuellzeichen inputBuffer, dient der Verkleinerung der String-Datei
				try {
				int hilf = historytext.indexOf('\u0000');
				String hilf1 = historytext.substring(0, hilf);
				historytext=hilf1;
				} catch (Exception e1) {
					Toast.makeText(History.this, "Verarbeitung des Eintrags fehlgeschlagen.", Toast.LENGTH_SHORT).show();
					e1.printStackTrace();
				}//Try
				his_dialog.cancel();
				mapintent = new Intent(History.this, Map.class);
				mapintent.putExtra("historytext", historytext);
				mapintent.putExtra("historyname", entries[hilf1]);
				startActivity(mapintent);
				//finish();
			}
		});
		
		his_dia_loeschen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					File add = new File(meinspeicherort, entries[hilf1]);//entries[Index des Ausgewaehlten Item]
					add.delete();
				} catch(Exception e){
					Toast.makeText(History.this, "GeeksRun konnte den Eintrag nicht loeschen.", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}//Try
				finish();
				startActivity(getIntent());
			}
		});
		
		his_dia_abbruch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				his_dialog.cancel();
			}
		});
		
		his_dialog.show();

		} else {
					File imp = new File(im_meinspeicherort.toString()+"/"+entries[hilf1-1]);
					File output = new File(meinspeicherort.toString()+"/"+entries[hilf1-1]);
					String[] ueberschreib_test =meinspeicherort.list();
					boolean flag_filedoppel=false;
					for (int m=0; m<meinspeicherort.list().length;m++) {
						if (entries[hilf1-1].equals(ueberschreib_test[m])) {
							flag_filedoppel=true;
						}//if (entries[position]==ueberschreib_test[m]) {
					}//for (int m=0; m<meinspeicherort.list().length;m++) {
					
					if (flag_filedoppel==false) {
					try{
					FileInputStream in = new FileInputStream(imp);
					FileOutputStream out = new FileOutputStream(output);
					FileChannel inchannel = in.getChannel();
					FileChannel outchannel = out.getChannel();
					inchannel.transferTo(0, inchannel.size(), outchannel);
					inchannel.close();
					outchannel.close();
					} catch(IOException e) {
						e.printStackTrace();
					}//try
					Toast.makeText(c, entries[hilf1-1]+" wurde importiert.", Toast.LENGTH_LONG).show();
					} else {
					Toast.makeText(c, entries[hilf1-1]+" wurde nicht importiert. Es existiert bereits!", Toast.LENGTH_LONG).show();	
					}//if (flag_filedoppel==false) {
					try {
						entries = meinspeicherort.list();
						ArrayAdapter aa = new ArrayAdapter<String>(this, R.layout.listview_white, R.id.listview_content, entries);
						lv.setAdapter(aa);
						lv.setOnItemClickListener(this);
					} catch(Exception e) {
							Toast.makeText(c, "GeeksRun konnte Eintr�ge des Laufarchives nicht finden.", Toast.LENGTH_LONG).show();
							e.printStackTrace();
					}//Try
					lv.removeHeaderView(btn_import_abbruch);
					import_mode=false;
		}//if (import_mode==false) {
		
	}//public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.his_import_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v==btn_import_abbruch) {
			meinspeicherort = c.getDir("archiv",MODE_PRIVATE);
			import_mode=false;
			try {
			entries = meinspeicherort.list();
			ArrayAdapter aa = new ArrayAdapter<String>(this, R.layout.listview_white, R.id.listview_content, entries);
			lv.setAdapter(aa);
			lv.setOnItemClickListener(this);
			} catch(Exception e) {
				Toast.makeText(c, "GeeksRun konnte Eintraege des Laufarchives nicht finden.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}//Try
			lv.removeHeaderView(btn_import_abbruch);
		}//if (v==btn_import_abbruch) {
	}



	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		AlertDialog.Builder sortverfahren = new AlertDialog.Builder(c);
		sortverfahren.setTitle("Hinweis");
		sortverfahren.setMessage("Du kannst deine Trainings sortieren nach:");
		
		sortverfahren.setNegativeButton("Name", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				meinspeicherort = c.getDir("archiv",MODE_PRIVATE);
				if (flag_sort_name==true) {
					flag_sort_name=false;
				}else {
					flag_sort_name=true;
				}//if (flag_sort_name==true) {
				try {
				entries = meinspeicherort.list();
				ArrayAdapter<String> aa = new ArrayAdapter<String>(c, R.layout.listview_white, R.id.listview_content, entries);
				
				aa.sort(new Comparator<String>() {

					@Override
					public int compare(String lhs, String rhs) {
						// TODO Auto-generated method stub
						if (flag_sort_name==true) {
							return rhs.compareTo(lhs);
						}else {
							return lhs.compareTo(rhs);
						}//if (flag_sort_name==true) {
					}
				});
				aa.notifyDataSetChanged();
				lv.setAdapter(aa);
				} catch(Exception e) {
					Toast.makeText(c, "GeeksRun konnte Eintr�ge des Laufarchives nicht finden.", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}//Try
			}
		});
		
		sortverfahren.setNeutralButton("Datum", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				meinspeicherort = c.getDir("archiv",MODE_PRIVATE);
				try {
				entries = meinspeicherort.list();
				Date[] entries_dat = new Date[entries.length];
				for (int n=0; n<entries.length; n++) {
					entries_dat[n]=new Date(new File(meinspeicherort+"/"+entries[n]).lastModified());
				}//for (int n=0; n<entries.length-1; n++) {
				if (flag_sort_date==true) {
					flag_sort_date=false;
				}else {
					flag_sort_date=true;
				}//if (flag_sort_name==true) {
				ArrayAdapter<Date> aa = new ArrayAdapter<Date>(c, R.layout.listview_white, R.id.listview_content, entries_dat);
				
				aa.sort(new Comparator<Date>() {

					@Override
					public int compare(Date lhs, Date rhs) {
						// TODO Auto-generated method stub
						if (flag_sort_date==true) {
							return rhs.compareTo(lhs);
						}else {
							return lhs.compareTo(rhs);
						}//if (flag_sort_name==true) {
					}
				});
				String[] entries_aktuel=new String[entries.length];
				for (int n=0; n<aa.getCount(); n++) {
					for(int m=0; m<entries.length;m++) {
					if (aa.getItem(n).toString().equals(new Date(new File(meinspeicherort+"/"+entries[m]).lastModified()).toString())) {
						entries_aktuel[n]=entries[m];
					}//if (aa.getItem(n)==new Date(new File(meinspeicherort+entries[n]).lastModified())) {
					}//while
				}//for(int m=0; m<entries.length-1;m++) {
				entries=entries_aktuel;
				ArrayAdapter<String> bb = new ArrayAdapter<String>(c, R.layout.listview_white, R.id.listview_content, entries_aktuel);
				lv.setAdapter(null);
				lv.setAdapter(bb);
				
				} catch(Exception e) {
					Toast.makeText(c, "GeeksRun konnte Eintraege des Laufarchives nicht finden.", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}//Try
			}
			
			
		});
		
		sortverfahren.setPositiveButton("abbrechen", null);
		
		sortverfahren.show();
		return false;
	}
	
	 @Override
	 public void onBackPressed() {
	 	// TODO Auto-generated method stub
	 	if (import_mode==true) {
	 		Toast.makeText(this, "Bitte verwende die Navigationsbuttons um abzubrechen.", Toast.LENGTH_LONG).show();
	 	}else {
	 	 super.onBackPressed();
	 	}//if (lv_flag==true) {
	 }
}
