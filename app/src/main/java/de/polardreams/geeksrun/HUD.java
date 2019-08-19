package de.polardreams.geeksrun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.MapFragment;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Bitmap.Config;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class HUD extends FragmentActivity implements OnTouchListener, OnClickListener, OnMapReadyCallback, TextToSpeech.OnInitListener, OnItemClickListener, OnCheckedChangeListener{
	//GUI
	private ImageView lokalize_view;
	private TextView txt, txt1, time, way, speed, average_speed;
	private Switch btn_start, btn_challenge, btn_geoplayer;
	private ListView lv;
	private boolean lv_flag;
	private Context con;
	private boolean gps_flag=false;

	//Touch Funktion
	private float array[][] = new float[2][99];
	private int z_touch =0;

	//Map
	private MapFragment mapFragment = null;
	private GoogleMap map;
	private LocationManager locmanager;
	private Lokalmesser loclistener;
	private int punktsetzer;
	private int mapzoom;
	private boolean wifi_sensor;
	private boolean first_check_gps=false;


	//Zeit und Trainingstart
	private Handler handler1 = new Handler();
	private Boolean flag_status_training =false;
	private int zsA = 0, zmA = 0, zhA = 0;//Startzeit
	private int zsX = 0, zmX = 0, zhX = 0;//Zwischenrechnung
	private int zsE = 0, zmE = 0, zhE = 0;//aktuelle Zeit
	private Date start_time, akt_time;
	private Format time_format;
	private int countdown;
	private Timer time_timer;
	private TimerTask timer_aufgabe;
	private Handler countdown_handler = new Handler();
	private boolean flag_coun;

	//Training speichern
	private Date date = null;
	private File meinspeicherort;
	private FileOutputStream fos = null;
	private OutputStreamWriter osw = null;

	//aktuelle Werte der Sensoren
	private double strecke=0;
	private double geschw=0;
	private double mittelgeschw=0;
	private String stunde="";
	private String minute="";
	private String sekunde="";
	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;

	//Spracheinstellungen
	private int sprachanzeige=0, sprachstatus=0;
	private TextToSpeech sprachausgabe;
	private String name;//Alle werden Pers�nlich angesprochen
	private Boolean sprach_anzeige;
	private int sprachanzeigenintervall;

	//Speicher - Array
	private double[][][][][][] kontainer = null;
	private double[] hoehenmeter =  null;
	private int z=0;

	//Musikplayer
	private MediaPlayer geeksplay = new MediaPlayer();
	private File[] mymusic = new File[50];
	private String[] entries;
	private short anz_lieder;
	private short akt_lied=0;
	private short mza_sel=0;
	private ActionBar action;
	private ToggleButton btn_music_play;
	private TextView txt2;
	private String music_ordner_plus="";

	//Challenge die Simulationsvariablen brauch ich eigentlic nicht mehr
	private Challenge thechallenge=null;
	private boolean challenge_mode =false;
	private int simulations_nr=0;
	private Handler simu_akt = new Handler();
	private boolean sim_flag=true;
	private boolean lv_challenge=false;

	private File[] my_enemies = null;//max. 20 gegner
	private String[] challenge_entries;
	private int anz_enemy, ch_mza_sel;
	private File ch_meinspeicherort=null;
	private Handler challenge_handler = new Handler();

	//GeoPlayer
	private boolean geoplayer_activate=false;
	private Geoplayer geoplayer;
	private File[] geo_lists;
	private File geo_meinspeicherort;
	private String[] geo_entries;
	private boolean geo_change_list = false;
	private String geo_titel;
	private boolean import_mode = false;
	private File im_meinspeicherort, ex_meinspeicherort;

	//SliderDrawer
	private SlidingDrawer hud_slide_drawer;
	private Button btn_music_playlist,
			btn_challenge_change_runner,
			btn_geo_list,
			btn_sound_option,
			btn_nav_mainmenu,
			btn_nav_map, btn_geo_im, btn_import_abbruch, btn_laufsafe, btn_music_allselect, btn_music_back;

	//Sound
	private int sound_music = 0;
	private int sound_challenge = 0;
	private int sound_geoplayer = 0;
	private TextView txt_music, txt_challenge, txt_geo;
	private float slide_x;
	private LinearLayout slide_content;

	//ListView last Object
	Button btn_music_last, btn_geo_last, btn_ch_last;

	//Touch_Gif
	private long time_touch=0;
	private int touch_pause=0;
	private boolean touch_flag_switch1=false;
	private boolean touch_flag_switch2=true;
	private Timer tim_touch = new Timer();
	private ImageView dummy;
	private Bitmap bmp;
	private Canvas can;
	private Movie movie;
	private boolean touch_flag=true;
	private Handler handler = new Handler();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hud);
		this.setTitle("Display");
		con = this;
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		gps_flag=true;
		action = getActionBar();
		action.setDisplayHomeAsUpEnabled(true);

		txt = (TextView)findViewById(R.id.textView1);
		txt.setTextColor(Color.WHITE);
		txt.setText("Hauptmenu");
		txt1 = (TextView)findViewById(R.id.TextView01);
		txt1.setTextColor(Color.WHITE);
		txt2 = (TextView)findViewById(R.id.textView2);
		txt2.setTextColor(Color.WHITE);
		lokalize_view = (ImageView)findViewById(R.id.imageView1);
		lokalize_view.setOnTouchListener(this);
		lv =(ListView)findViewById(R.id.listView1);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lv.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				lv.setVisibility(View.GONE);
			}
		});//lv.post(new Runnable() {
		//SliderDrawer
		hud_slide_drawer = (SlidingDrawer)findViewById(R.id.slidingDrawer1);

		slide_content = (LinearLayout)findViewById(R.id.content);
		slide_content.setClickable(true);
		slide_content.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (z_touch < 98) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						array[0][0]=(float) event.getX();
						array[1][0]=(float) event.getY();
					}//if (event.getAction() == MotionEvent.ACTION_DOWN)
					if (event.getAction() == MotionEvent.ACTION_MOVE) {
						array[0][z_touch]=event.getX();
						array[1][z_touch]=event.getY();
					}//if (event.getAction() == MotionEvent.ACTION_MOVE)
					if (event.getAction() == MotionEvent.ACTION_UP) {
						array[0][z_touch]=event.getX();
						array[1][z_touch]=event.getY();
						if (array[0][0] < array[0][z_touch]) {
							hud_slide_drawer.animateOpen();
						}//if (array[0][0] < array[0][z_touch]) {
						if (array[0][0] > array[0][z_touch]) {
							hud_slide_drawer.animateClose();
						}//if (array[0][0] > array[0][z_touch]) {
						z_touch=0;
						for (int n=0; n<z_touch; n++) {
							array[0][n]=0;
							array[1][n]=0;
						}//for (int n=0; n<z_touch; n++) {
					}//if (event.getAction() == MotionEvent.ACTION_UP) {
					z_touch++;
				} else {
					z_touch=0;
				}//if (z_touch < 98) {
				return false;
			}
		});
		btn_music_playlist = (Button)findViewById(R.id.hudslide_button1);
		btn_music_playlist.setOnClickListener(this);
		btn_challenge_change_runner = (Button)findViewById(R.id.hudslide_button2);
		btn_challenge_change_runner.setOnClickListener(this);
		btn_geo_list = (Button)findViewById(R.id.hudslide_button3);
		btn_geo_list.setOnClickListener(this);
		btn_sound_option = (Button)findViewById(R.id.hudslide_button4);
		btn_sound_option.setOnClickListener(this);
		btn_nav_mainmenu = (Button)findViewById(R.id.hudslide_button5);
		btn_nav_mainmenu.setOnClickListener(this);
		btn_nav_map = (Button)findViewById(R.id.hudslide_button6);
		btn_nav_map.setOnClickListener(this);
		btn_laufsafe = (Button)findViewById(R.id.hudslide_button7);
		btn_laufsafe.setEnabled(false);
		btn_laufsafe.setTextColor(Color.GRAY);
		btn_laufsafe.setOnClickListener(this);

		//Musik
		btn_music_play = (ToggleButton)findViewById(R.id.toggleButton1);
		btn_music_play.setOnClickListener(this);
		btn_music_play.setEnabled(false);

		btn_start = (Switch)findViewById(R.id.switch_training);
		btn_start.setOnCheckedChangeListener(this);
		btn_start.setTextColor(Color.WHITE);
		btn_challenge = (Switch)findViewById(R.id.switch_challenge);
		btn_challenge.setOnCheckedChangeListener(this);
		btn_challenge.setTextColor(Color.WHITE);
		btn_challenge.setEnabled(false);
		btn_geoplayer = (Switch)findViewById(R.id.switch_geoplayer);
		btn_geoplayer.setOnCheckedChangeListener(this);
		btn_geoplayer.setTextColor(Color.WHITE);
		btn_geoplayer.setEnabled(false);

		time = (TextView)findViewById(R.id.textView3);
		time.setTextColor(Color.WHITE);
		way = (TextView)findViewById(R.id.textView4);
		way.setTextColor(Color.WHITE);
		speed = (TextView)findViewById(R.id.textView5);
		speed.setTextColor(Color.WHITE);
		average_speed = (TextView)findViewById(R.id.TextView02);
		average_speed.setTextColor(Color.WHITE);

		//GPS Service erstellen
		locmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);//Das Objekt LocManager �berwacht das betriebsinterne GPS System
		loclistener = new Lokalmesser();


		//Vorbereiten der Sprachausgabe
		sprachausgabe =new TextToSpeech(this, this);
		sprachausgabe.setPitch((float) 0.9);
		//Preferenz
		try {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			name=info.getString("pers_name", "Player");
			sprach_anzeige=info.getBoolean("hud_speak", true);
			punktsetzer=Integer.valueOf(info.getString("hud_gps_points", "5000"));
			mapzoom=Integer.valueOf(info.getString("hud_map_zoom", "15"));
			countdown=Integer.valueOf(info.getString("hud_time_countdown", "15"));
			sprachanzeigenintervall=Integer.valueOf(info.getString("hud_speak_intervall", "300"));
			if (info.getString("hud_gps_option","ja").equals("ja")) {
				wifi_sensor=true;
			}else {
				wifi_sensor=false;
			}//if (info.getString("hud_gps_option","ja") == "ja") {
		} catch (Exception e) {
			Toast.makeText(this, "Die Einstellungen konnten nicht geladen und vorgenommen werden. Bitte pr�fe deine Einstellungen. Das Display l�uft gerade mit Standardeinstellungen.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			name="Player";
			sprach_anzeige=true;
			punktsetzer=20000;
			mapzoom=15;
			countdown=15;
			sprachanzeigenintervall=300;
		}//Try
		//Vor dem Start das GPS suchen f�r geringere Fehlerquode
		try {
			//Karte erstellen
			mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
			//map.clear();//Alle voreingestellten Marker verschwinden

			locmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, punktsetzer,1, loclistener);//GPS interner Sensor, 0 Meter, Lokalmesser zur anzeige
			if (wifi_sensor==true) {
				locmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, punktsetzer,1f, loclistener);//Funkmasten, Wlan, ect., 0 Meter, Lokalmesser zur anzeige
			}//if (wifi_sensor==true) {
			if (locmanager.isProviderEnabled(Context.LOCATION_SERVICE) == true) {

			} else {
				//dann ist das GPS immer noch aus.
			}//If
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Fehler: "+e.toString(), Toast.LENGTH_LONG).show();//Anzeige der Fehlermeldungen
		}//Try

		timerini();//initialisiert Timer und Timertask f�r Countown und Zeitaufnahme
		ini_touch();
	}//protected void onCreate(Bundle savedInstanceState)


	public void ini_touch() {
		//animiertes Gif
		InputStream is_gif = null;
		try {
			is_gif = this.getResources().openRawResource(R.drawable.touch_light_effekt);
		} catch(Exception e) {
			e.printStackTrace();
		}//try

		movie = Movie.decodeStream(is_gif);
		lokalize_view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		bmp = Bitmap.createBitmap(movie.width(), movie.height(), Config.ARGB_8888);
		can = new Canvas(bmp);

		final long time_start = System.currentTimeMillis();
		tim_touch.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(touch_flag) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (time_touch == 100) {
						time_touch=0;
						touch_flag_switch1=false;
						touch_flag_switch2=true;
					}//if (time == 0) {
					//int now = (int) ((time_start-time)%(movie.duration()));
					int now = (int) ((movie.duration()*time_touch)/(100));

					if ((time_touch>50 && touch_flag_switch1==false) || (time_touch>97 && touch_flag_switch2==false)) {
						touch_pause++;
						if (touch_pause>500 && touch_flag_switch1==false) {
							touch_flag_switch1=true;
							touch_flag_switch2=false;
							touch_pause=0;
						}//if (touch_pause<20) {

						if (touch_pause>500 && touch_flag_switch2==false) {
							touch_flag_switch2=true;
							touch_pause=0;
						}//if (touch_pause<20) {

					} else {
						movie.setTime(now);
						movie.draw(can, 0, 0);
						time_touch=time_touch+2;
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								lokalize_view.setImageBitmap(bmp);
								lokalize_view.invalidate();
							}//run
						});//handler.post(new Runnable() {
					}//

				}//while(touch_flag) {
			}//public void run() {
		}, 0);//tim_touch.schedule(new TimerTask() {

	}

	/*
	 * Alle relevanten Menus
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item)  {
		// TODO Auto-generated method stub
		if (item.getTitle() == action.getTitle()) {
			//hud_slide_drawer.open();
			if (flag_status_training==false) {
				finish();//Main
			} else {
				Toast.makeText(this, "Bitte schlie�en Sie zuerst das Training ab.", Toast.LENGTH_SHORT).show();
			}//if (flag_status_training==false) {
			//Toast.makeText(this, "neue funktion???", Toast.LENGTH_SHORT).show();
		}//if (item.getTitle() == action.getTitle()) {

		if (item.getItemId() == R.id.hud_slide_item) {
			hud_slide_drawer.animateOpen();
		}//if (item.getItemId() == R.id.hud_slide_item) {
		return super.onOptionsItemSelected(item);
	}//public boolean onOptionsItemSelected(MenuItem item) {

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (z_touch < 98) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				array[0][0]=(float) event.getX();
				array[1][0]=(float) event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_DOWN)
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				array[0][z_touch]=event.getX();
				array[1][z_touch]=event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_MOVE)
			if (event.getAction() == MotionEvent.ACTION_UP) {
				array[0][z_touch]=event.getX();
				array[1][z_touch]=event.getY();
				if (array[0][0] < array[0][z_touch]) {
					if (flag_status_training==false) {
						finish();//Main
					} else {
						Toast.makeText(this, "Bitte schlie�en Sie zuerst das Training ab.", Toast.LENGTH_SHORT).show();
					}//if (flag_status_training==false) {
					;}
				if (array[0][0] > array[0][z_touch]) {
					Intent i = new Intent(this, Map.class);
					String hudtext= kontainer_array2string(kontainer);
					if (hudtext != null && z>1) {
						i.putExtra("hudtext", hudtext);
						startActivity(i);
					} else {
						hudtext=null;
						i.putExtra("hudtext", hudtext);
						startActivity(i);
					}//if (hudtext != null) {
				}//if (array[0][0] > array[0][z_touch]) {
				z_touch=0;
				for (int n=0; n<z_touch; n++) {
					array[0][n]=0;
					array[1][n]=0;
				}//for (int n=0; n<z_touch; n++) {
			}//if (event.getAction() == MotionEvent.ACTION_UP) {
			z_touch++;
		} else {
			Toast.makeText(this, "Ich bin verwirrt, in welches Menu m�chtest du?", Toast.LENGTH_SHORT).show();
			z_touch=0;
		}//if (z_touch < 98) {
		return true;
	}//public boolean onTouch(View v, MotionEvent event)

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		// TODO Auto-generated method stub
		if (parent==lv && lv_flag==true && geo_change_list==false) {
			final int pos=position;
			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					//Ordner anklicken und focus �ndern
					SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					String music_file = info.getString("music_location", "Music");
					File test_ordner = new File(music_file, music_ordner_plus+entries[pos-3]);

					if (test_ordner.getName().contains(".")==false && !test_ordner.isFile()) {

						if (!music_ordner_plus.equals(music_file+"/"+test_ordner.getName()+"/")) {
							music_ordner_plus=music_ordner_plus+"/"+test_ordner.getName()+"/";
						} else {
							music_ordner_plus="";
						}//if (!music_ordner_plus.equals(music_ordner_plus+"/"+test_ordner.getName()+"/")) {
						File explor_focus = new File(music_file+music_ordner_plus);
						String[] s = new String[explor_focus.list().length];
						//s=explor_focus.list();
						s=explor_focus.list();
						entries =new String[explor_focus.list().length];
						for (int n =0; n<explor_focus.list().length;n++) {
							entries[n]=s[n];
						}//for (int n =0; n<explor_focus.list().length;n++) {
						ArrayAdapter aa = new ArrayAdapter<String>(con,android.R.layout.simple_list_item_checked, entries);
						lv.setAdapter(aa);

						//Toast.makeText(HUD.this, "Das war ein Ordner", Toast.LENGTH_SHORT).show();
					} else {
						//Musiktitel selektieren

						lv.setItemChecked(pos, true);
						if (mza_sel<49) {
							mymusic[mza_sel]=new File(music_file, music_ordner_plus+entries[pos-3]);
						} else {
							Toast.makeText(HUD.this, "Bitte w�hle nicht mehr als 50 Musiktitel", Toast.LENGTH_SHORT).show();
						}//if (mza_sel<49) {
						mza_sel++;
					}//if (test_ordner.getName().contains(".")==false && !test_ordner.isFile()) {
				}//run
			});//lv.post(new Runnable()

		}//if (parent==lv) {
		if (parent==lv&& lv_flag==false && geo_change_list==false && lv_challenge==true) {

			final int pos=position;
			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setItemChecked(pos, true);
					if (ch_mza_sel<20) {
						boolean tip_flag=false;
						if (ch_mza_sel>0) {
							for (int pf=0; pf<ch_mza_sel;pf++) {
								if (challenge_entries[pos-1].equals(my_enemies[pf].toString())) {
									Toast.makeText(HUD.this, "Klicke bitte etwas langsamer, dieses Item hast du bereits angeklickt.", Toast.LENGTH_LONG).show();
									tip_flag=true;
								}//if (entries[pos].equals(my_enemies[pf].toString())) {
							}//for (int pf=0; pf<mza_sel;pf++) {
							if (tip_flag==false) {
								my_enemies[ch_mza_sel]=new File(challenge_entries[pos-1]);
								ch_mza_sel++;
							}//if (tip_flag==false) {
						} else {
							my_enemies[ch_mza_sel]=new File(challenge_entries[pos-1]);
							ch_mza_sel++;
						}//if (mza_sel>0) {
					} else {
						Toast.makeText(HUD.this, "Bitte w�hle nicht mehr als 20 Gegner aus!", Toast.LENGTH_SHORT).show();
					}
				}//run
			});//lv.post(new Runnable()

		}//if (parent==lv_challenge) {
		if (parent == lv && lv_flag == true && geo_change_list==true && import_mode==false) {
			AlertDialog.Builder geo_submenu = new AlertDialog.Builder(con);
			geo_submenu.setTitle("GeoPlayer");
			geo_submenu.setMessage("Was m�chtest du gerne tun?");
			geo_submenu.setPositiveButton("exportieren", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//Laderoutine
					char[] inputBuffer = new char[999999];
					File geo_meinspeicherort = con.getDir("geoarchiv",0);
					File add = new File(geo_meinspeicherort, geo_entries[position-2]);//entries[Index des Ausgew�hlten Item]
					String historytext="";
					try {
						FileInputStream fileinputstream = new FileInputStream(add);
						InputStreamReader inputstreamreader = new InputStreamReader(fileinputstream);//Zeiger melden an den leser
						inputstreamreader.read(inputBuffer);//Leser liest in einen Puffer (Char Typ )
						historytext = new String(inputBuffer);//der Puffer wird der Textvariablen �bergeben
						inputstreamreader.close();//Datei geschlossen
					} catch(Exception e){
						Toast.makeText(HUD.this, "Geeksrun konnt die zu exportierende Datei nicht laden.", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}//Try

					//herausfiltern von F�llzeichen inputBuffer
					try{
						int hilf = historytext.indexOf('\u0000');
						String hilf1x = historytext.substring(0, hilf);
						historytext=hilf1x;
					} catch (Exception e1) {
						Toast.makeText(HUD.this, "Verarbeitung des Eintrags fehlgeschlagen.", Toast.LENGTH_SHORT).show();
						e1.printStackTrace();
					}//Try

					//ExportRoutine
					try {
						SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						File meinspeicherort_ex = new File(info.getString("export_localtion", "My Documents")+"/");

						if (meinspeicherort_ex.canRead() == false) {
							meinspeicherort_ex.mkdir();
							Toast.makeText(HUD.this, "Der angegebene Export-Ordner konnte nicht gefunden werden. Deswegen wurde ein neuer Ordner erstellt.", Toast.LENGTH_LONG).show();
						}//if (meinspeicherort_ex.canRead() == false) {

						String s = geo_entries[position-2];
						File file = new File(meinspeicherort_ex, s);
						FileOutputStream fos = new FileOutputStream(file);
						OutputStreamWriter osw = new OutputStreamWriter(fos);
						//Einbetten des String in eine Textdatei



						osw.write(historytext);
						osw.flush();
					} catch (IOException e) {
						Toast.makeText(HUD.this, "Export fehlgeschlagen.", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}//Try
					Toast.makeText(HUD.this, "Training wurde erfolgreich exportiert", Toast.LENGTH_SHORT).show();
					lv.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							lv.setVisibility(View.GONE);
							geo_change_list=false;
						}//run
					});//lv.post(new Runnable() {
					geo_titel="kein Song vorhanden";//leer
					geo_change_list=false;
					lv.removeHeaderView(btn_geo_last);
					lv.removeHeaderView(btn_geo_last);
					lv.removeHeaderView(btn_geo_im);
				}//public void onClick(DialogInterface dialog, int which) {
			});

			geo_submenu.setNegativeButton("l�schen", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					try {
						File geo_meinspeicherort = con.getDir("geoarchiv",0);
						File add = new File(geo_meinspeicherort, geo_entries[position-2]);//entries[Index des Ausgew�hlten Item]
						add.delete();
					} catch(Exception e){
						Toast.makeText(HUD.this, "GeeksRun konnte den Eintrag nicht l�schen.", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}//Try
					lv.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							lv.setVisibility(View.GONE);
							geo_change_list=false;
						}//run
					});//lv.post(new Runnable() {
					geo_titel="kein Song vorhanden";//leer
					geo_change_list=false;
					lv.removeHeaderView(btn_geo_last);
					lv.removeHeaderView(btn_geo_last);
					lv.removeHeaderView(btn_geo_im);
				}//public void onClick(DialogInterface dialog, int which) {

			});

			geo_submenu.setNeutralButton("laden", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					final int pos=position;
					lv.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							lv.setItemChecked(pos, true);
							geo_titel=geo_entries[pos-2];
							lv.setVisibility(View.GONE);
							geoplayer.load_geolist(geo_titel);
							String[][][][] hilf_array = geoplayer.getgeoplayerlistarray();
							try {
								for (int n=0; n<geoplayer.Geoplayerlistarraylength(); n++) {
									map.addMarker(new MarkerOptions()
											.position(new LatLng(Double.valueOf(hilf_array[n][1][0][0]),Double.valueOf(hilf_array[n][0][0][0])))
											.icon(BitmapDescriptorFactory.fromResource(R.drawable.m)))
											.setTitle(hilf_array[n][0][1][0].substring(hilf_array[n][0][1][0].lastIndexOf("/")+1, hilf_array[n][0][1][0].length())
											);
								}//for (int n=0; n<geoplayer.Geoplayerlistarraylength(); n++) {
							} catch(Exception e) {
								e.printStackTrace();
							}//try
							geo_change_list=false;
							btn_geoplayer.setEnabled(true);
							lv.removeHeaderView(btn_geo_last);
							lv.removeHeaderView(btn_geo_im);
						}//run
					});//lv.post(new Runnable()
				}//public void onClick(DialogInterface dialog, int which) {

			});
			AlertDialog geo_alert = geo_submenu.create();
			geo_alert.show();
		}//if (parent == lv && lv_flag == true && geo_change_list==true) {

		if (import_mode==true) {
			int hilf1 = position;
			File imp = new File(im_meinspeicherort.toString()+"/"+entries[hilf1-1]);
			File output = new File(geo_meinspeicherort.toString()+"/"+entries[hilf1-1]);//meint nicht den Exportordner, sondern den Zielordner (Geoplayer ORdner)
			String[] ueberschreib_test = geo_meinspeicherort.list();
			boolean flag_filedoppel=false;
			for (int m=0; m<geo_meinspeicherort.list().length;m++) {
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
				Toast.makeText(con, entries[hilf1-1]+" wurde importiert.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(con, entries[hilf1-1]+" wurde nicht importiert. Es existiert bereits!", Toast.LENGTH_LONG).show();
			}//if (flag_filedoppel==false) {
			lv.removeHeaderView(btn_import_abbruch);
			import_mode=false;
			geoplayer = new Geoplayer(this);
			geo_lists = new File[50];
			geo_change_list=true;
			lv_flag=true;
			lv.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.VISIBLE);
				}
			});//lv.post(new Runnable() {

			//Ladenroutine
			try {
				geo_meinspeicherort = this.getDir("geoarchiv",0);
				geo_entries = new String[geo_meinspeicherort.list().length];
				for (int mza=0; mza<geo_meinspeicherort.list().length;mza++) {
					geo_entries[mza]=geo_meinspeicherort.list()[mza];
				}//for (int mza=0; mza<meinspeicherort.length();mza++) {
			}catch(Exception e){
				Toast.makeText(this, "Geeksrun konnte den internen Geolistenordner nicht finden.", Toast.LENGTH_LONG).show();
			}//Try
			ArrayAdapter aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, geo_entries);
			lv.setAdapter(aa);
			btn_geo_last = new Button (this);
			btn_geo_last.setText("abbrechen");
			btn_geo_last.setOnClickListener(this);
			btn_geo_last.setBackgroundResource(R.drawable.btn_dark);


			btn_geo_im = new Button (this);
			btn_geo_im.setText("importieren");
			btn_geo_im.setOnClickListener(this);
			btn_geo_im.setBackgroundResource(R.drawable.btn_dark);

			lv.addHeaderView(btn_geo_last);
			lv.addHeaderView(btn_geo_im);
			lv.setOnItemClickListener(this);
		}//if (import_mode==true) {

	}//public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	/*
	 * Initialisierungen
	 */

	@Override
	public void onMapReady(GoogleMap arg0) {
		// TODO Auto-generated method stub
		map = arg0;//mit der Map arbeiten, nutzen von Markern!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		map.clear();
	}//public void onMapReady(GoogleMap arg0)

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		sprachstatus=status;
	}

	public void timerini() {
		timer_aufgabe = new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(flag_status_training) {
					try {
						Thread.sleep(1000);
					}catch(InterruptedException e) {
						e.printStackTrace();
					}//try
					handler1.post(new Runnable() {
						@SuppressWarnings("deprecation")
						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								akt_time=new Date();
								String zeitstempel1 =time_format.format(akt_time);
								zhX=Integer.valueOf(zeitstempel1.substring(0,zeitstempel1.indexOf("_")));
								zeitstempel1=zeitstempel1.substring(zeitstempel1.indexOf("_")+1, zeitstempel1.length());
								zmX=Integer.valueOf(zeitstempel1.substring(0,zeitstempel1.indexOf("_")));
								zeitstempel1=zeitstempel1.substring(zeitstempel1.indexOf("_")+1, zeitstempel1.length());
								zsX=Integer.valueOf(zeitstempel1);
								//Zeitdifferenz bzw. Laufdauer
								int sec2=(zhX*3600)+(zmX*60)+(zsX);
								int sec1=(zhA*3600)+(zmA*60)+(zsA);
								int secX=sec2-sec1;
								zhE=Integer.valueOf(secX/3600);//ohne aufrunden!
								zmE=Integer.valueOf((secX%3600)/60);
								zsE=(secX%3600)%60;
								if (zhE<10) {stunde="0"+String.valueOf(zhE);} else {stunde=String.valueOf(zhE);}
								if (zmE<10) {minute="0"+String.valueOf(zmE);} else {minute=String.valueOf(zmE);}
								if (zsE<10) {sekunde="0"+String.valueOf(zsE);} else {sekunde=String.valueOf(zsE);}
								time.setText("Zeit: "+stunde+":"+minute+":"+sekunde);
								//live Anzeige im Display der Challenge

								if (challenge_mode==true) {
									try {
										thechallenge.set_pos(kontainer[0][z][0][0][0][0], kontainer[1][z][0][0][0][0], zhE, zmE, zsE);
										marker_update();
									} catch(Exception e) {
										e.printStackTrace();
									}//try
								}//if (challenge_mode==true) {

							} catch (Exception e) {
								e.printStackTrace();
								//Toast.makeText(HUD.this, "Achtung, die aktuelle zeit konnte nicht ermittelt werden, dies kann zu Problemen in der Zeitberechnung f�hren.", Toast.LENGTH_LONG).show();
								//Toast.makeText(HUD.this, "Versuche es erneut!", Toast.LENGTH_SHORT).show();
							}//try

							if (sprachanzeige>sprachanzeigenintervall) {
								sprachanzeige=0;
								if (sprachstatus == TextToSpeech.SUCCESS && sprach_anzeige==true) {
									int n = sprachausgabe.setLanguage(Locale.GERMAN);
									if (n == TextToSpeech.LANG_MISSING_DATA || n == TextToSpeech.LANG_NOT_SUPPORTED) {
										//txt.setText("Hauptmenu	zzt. falsche Sprachdatei");
									} else {
										sprachausgabe_daten();
									}//Pr�fung ob die Sprache vorhanden ist
								} else {
									//txt.setText("Hauptmenu	zzt. keine Sprachfunktion");
								}//Pr�fung des Sprechers
							}//if (sprachanzeige>sprachanzeigenintervall) {
							sprachanzeige++;
						}//run
					});//handler1.post(new Runnable() {
				}//while(flag_status_training) {
			}
		};//TimerTask
	}//public void timerini() {

	/*
	 * Methoden
	 */

	private void play_next() {
		geeksplay.stop();
		geeksplay.reset();
		try {
			geeksplay.setDataSource(this, Uri.fromFile(mymusic[akt_lied]));
			geeksplay.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HUD.this, "Der Musikplayer hat ein Problem festgestellt:"+e.getMessage().toString(), Toast.LENGTH_LONG).show();
			Toast.makeText(HUD.this, "W�hle erneut Musiktitel aus oder starte die App neu.", Toast.LENGTH_SHORT).show();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HUD.this, "Der Musikplayer hat ein Problem festgestellt:"+e.getMessage().toString(), Toast.LENGTH_LONG).show();
			Toast.makeText(HUD.this, "W�hle erneut Musiktitel aus oder starte die App neu.", Toast.LENGTH_SHORT).show();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HUD.this, "Der Musikplayer hat ein Problem festgestellt: "+e.getMessage().toString(), Toast.LENGTH_LONG).show();
			Toast.makeText(HUD.this, "W�hle erneut Musiktitel aus oder starte die App neu.", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(HUD.this, "Der Musikplayer hat ein Problem festgestellt:"+e.getMessage().toString(), Toast.LENGTH_LONG).show();
			Toast.makeText(HUD.this, "W�hle erneut Musiktitel aus oder starte die App neu.", Toast.LENGTH_SHORT).show();
		}//try
		geeksplay.start();
	}//private void play_next() {

	public String kontainer_array2string(double kontainer[][][][][][]) {
		//einlesen des Arrays in einen String
		int za=0;
		String text="";
		while (za<z) {
			text=text
					+String.valueOf(kontainer[0][za][0][0][0][0])
					+";"+String.valueOf(kontainer[1][za][0][0][0][0])
					+";"+String.valueOf(kontainer[0][za][1][0][0][0])
					+";"+String.valueOf(kontainer[0][za][0][1][0][0])
					+";"+String.valueOf(kontainer[0][za][0][0][1][0])
					+";"+String.valueOf(kontainer[0][za][0][0][0][1])
					+"\n";//das Trennungszeichen ; ist f�r Chatprotokoll ung�nstig, bitte "," verwenden
			za++;
		}
		return text;
	}//public void kontainer_array2string() {

	@SuppressWarnings("deprecation")
	public void sprachausgabe_daten() {
		try{
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String id_sex =info.getString("pers_sexx", "2");
			if (id_sex.equals("2")) {
				if (z>0) {
					sprachausgabe.speak(
							"Lieber "+name
									+" du bist"
									+stunde+" Stunden"
									+minute+" Minuten"
									+" und"+sekunde+" Sekunden gelaufen"
									+" Deine aktuelle Geschwindigkeit betr�gt"+String.format("%.2f",geschw)+" Kilometer pro Stunde"
									+" Du hast"+String.format("%.2f",strecke)+" Kilometer zur�ckgelegt"
									+" Deine Durchschnittsgeschwindigkeit betr�gt  "+String.format("%.2f",mittelgeschw)+" Kilometer pro Stunde"
									+" Deine zur�ckgelegten H�henmeter betragen: "+String.format("%.2f",hoehenmeter[z-1])+" Meter"
									+name+" Ich w�nsche dir noch einen angenehmen Lauf"
							,TextToSpeech.QUEUE_FLUSH, null);
				}
			} else {
				if (z>0) {
					sprachausgabe.speak(
							"Liebe "+name
									+" du bist"
									+stunde+" Stunden"
									+minute+" Minuten"
									+" und"+sekunde+" Sekunden gelaufen"
									+" Deine aktuelle Geschwindigkeit betr�gt"+String.format("%.2f",geschw)+" Kilometer pro Stunde"
									+" Du hast"+String.format("%.2f",strecke)+" Kilometer zur�ckgelegt"
									+" Deine Durchschnittsgeschwindigkeit betr�gt  "+String.format("%.2f",mittelgeschw)+" Kilometer pro Stunde"
									+" Deine zur�ckgelegten H�henmeter betragen: "+String.format("%.2f",hoehenmeter[z-1])+" Meter"
									+name+" Ich w�nsche dir noch einen angenehmen Lauf"
							,TextToSpeech.QUEUE_FLUSH, null);
				}
			}//if (str=="Mann") {
		} catch(Exception e) {
			txt.setText("Hauptmenu "+e.getMessage()+" Sprachausgabe");
		}//Try
	}//public void sprachausgabe_daten() {

	/*
	 * Klassen
	 * 	in diesem Fall ist es die LocationListener
	 * 	diese Klasse organisiert die Daten des GPS-Sensors
	 *  Speicehrung in die definierten Arrays
	 */

	public class Lokalmesser implements LocationListener {

		public void onLocationChanged(Location location) {
			//Aktuelle Daten des GPS Sensors werden bearbeitet
			location.setAccuracy((float) 2.0);//auf zwei Meter ist das genaueste!!

			if (first_check_gps==false && location.hasAccuracy()==true) {
				first_check_gps=true;
				mBuilder =
						new NotificationCompat.Builder(con)
								.setSmallIcon(R.drawable.cast_ic_notification_0)//bei dem ersten Notification muss das Icon sein!
								.setContentTitle("GPS bereit")
								.setContentText("Ich empfehle jetzt zu laufen.");

				mNotificationManager =
						(NotificationManager) getSystemService(con.NOTIFICATION_SERVICE);
				// mId allows you to update the notification later on.
				mNotificationManager.notify(1, mBuilder.build());
			}//if (first_check_gps==false && location.hasAccuracy()==true) {

			if (z<9000 && flag_status_training == true) {
				//Die aktuelle Position wird eingetragen in das Array
				/*
				 * Falls dem Operating System Fehlerunterlaufen werden
				 * diese versucht zu ignorieren
				 * H�ufen diese sich, werden die Werte unrealistisch.
				 * Der benutzer sieht eine Erkl�rung als kleine Nachricht in der Touch-Leiste
				 */
				try {
					kontainer[0][z][0][0][0][0]=location.getLongitude();//x long wie L�nge L�ngengrad (Ost-West)
					kontainer[1][z][0][0][0][0]=location.getLatitude();//y Nord S�d
					kontainer[0][z][0][0][0][1]=location.getAltitude();//H�he
				} catch(Exception e) {
					e.printStackTrace();
					txt.setText("Hauptmenu "+e.getMessage()+" GEO-Punkte");//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					kontainer[0][z][0][0][0][0]=kontainer[0][z-1][0][0][0][0];//x long wie L�nge L�ngengrad (Ost-West)
					kontainer[1][z][0][0][0][0]=kontainer[1][z-1][0][0][0][0];//y Nord S�d
					kontainer[0][z][0][0][0][1]=kontainer[0][z-1][0][0][0][1];//H�he
				}//try
				try {
					kontainer[0][z][1][0][0][0]=zhE;//Stunde
					kontainer[0][z][0][1][0][0]=zmE;//Minute
					kontainer[0][z][0][0][1][0]=zsE;//Sekunde
				} catch(Exception e1) {
					e1.printStackTrace();
					txt.setText("Hauptmenu "+e1.getMessage()+" Zeitspeicherung");//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					kontainer[0][z][1][0][0][0]=kontainer[0][z-1][1][0][0][0];//Stunde
					kontainer[0][z][0][1][0][0]=kontainer[0][z-1][0][1][0][0];//Minute
					kontainer[0][z][0][0][1][0]=kontainer[0][z-1][0][0][1][0];//Sekunde
				}//try

				//Berechnung und Anzeige der gewonnen Daten
				if (z>1) { //GPS Suchkoords weglassen, wegen verf�lschter Ergebnisse
					double hilf1=0;
					double hilf2=0;
					double hilf3=0;
					double hilf4=0;
					double hilf5=0;
					double hilf6=0;
					double hilf7=0;
					try {
						//Strecke berechnen
						hilf1=(2*Math.PI*Math.cos(Math.toRadians(kontainer[1][z][0][0][0][0]))*6371)/(2*Math.PI);//Die anderen Koordinaten m�ssen in der N�he sein
						hilf2=hilf1*Math.PI/180;
						hilf3=Math.sqrt(Math.pow(6371*Math.PI/180*(kontainer[1][z-1][0][0][0][0]-kontainer[1][z][0][0][0][0]),2)+
								Math.pow(hilf2*(kontainer[0][z-1][0][0][0][0]-kontainer[0][z][0][0][0][0]),2));//km
						//GPS-Filter (unrealistische Zahlen werden aussortiert)
						hilf4=(hilf3*1000)/((kontainer[0][z][1][0][0][0]*3600+kontainer[0][z][0][1][0][0]*60+kontainer[0][z][0][0][1][0])-
								(kontainer[0][z-1][1][0][0][0]*3600+kontainer[0][z-1][0][1][0][0]*60+kontainer[0][z-1][0][0][1][0]));//m/s
						//H�henmeter
						if (kontainer[0][z-1][0][0][0][1]<kontainer[0][z][0][0][0][1] && kontainer[0][z][0][0][0][1]!= 0 && kontainer[0][z-1][0][0][0][1]!=0) {
							//H�henmeterfilter
							double hoehelaengeverhaeltnis=(hilf3*1000)/(kontainer[0][z][0][0][0][1]-kontainer[0][z-1][0][0][0][1]);
							if (hoehelaengeverhaeltnis>1) {
								hoehenmeter[z]= hoehenmeter[z-1] + (kontainer[0][z][0][0][0][1]-kontainer[0][z-1][0][0][0][1]);
							} else {
								//Fehlerverfahren ...
								hoehenmeter[z]=hoehenmeter[z-1];
							}//if ((hilf3/(kontainer[0][z][0][0][0][1]-kontainer[0][z-1][0][0][0][1])>1)) {
							/*
							 * Der H�henmeterfilter berechnet das Verh�ltnis von
							 * Weg durch H�henmeter
							 * bsp. 1/1 hei�t ich bin 1m gelaufen und 1m in die h�he gelaufen
							 * bsp. 2/1 hei�t ich bin 2m gelaufen und 1m in die h�he gelaufen
							 * bsp. 1/2 = 0.5 hei�t ich bin 1m gelaufen aber 2m in die h�he gegangen
							 * -> dies erachte ich als unrealistisch beim Joggen
							 * deswegen werden alle Werte unter 1 herausgerechnet
							 */
						} else {
							hoehenmeter[z]=hoehenmeter[z-1];
						}//if (kontainer[0][z1][0][0][0][1]<kontainer[0][z2][0][0][0][1]) {

						if (hilf4<10) {//Sprint - Weltrekord 2009 in m/s
							//if (hilf4<30) {//Bahnfahrttest
							//Die aktuelle Geschwindigkeit
							geschw=location.getSpeed()*3.6;//km/h w�re in diesem Fall auch hilf4
							speed.setText("Geschwindigkeit: "+String.format("%.2f",geschw)+" km/h");
							//Strecke anzeigen
							strecke = strecke + hilf3;//Kilometerangabe
							way.setText("Strecke: "+String.format("%.2f",strecke)+" km");
							//Die mittlere Geschwindigkeit
							hilf5 =strecke;
							hilf6 =(kontainer[0][z][1][0][0][0]*3600+kontainer[0][z][0][1][0][0]*60+kontainer[0][z][0][0][1][0])/3600;
							hilf7 =hilf5/hilf6;
							mittelgeschw =hilf7;
							average_speed.setText("mittlere Geschw.: "+String.format("%.2f",mittelgeschw)+" km/h");

							if (geoplayer_activate==true && challenge_mode==false) {
								String[][][][] hilf_array = geoplayer.getgeoplayerlistarray();
								try {
									for (int n=0; n<geoplayer.Geoplayerlistarraylength(); n++) {
										map.addMarker(new MarkerOptions()
												.position(new LatLng(Double.valueOf(hilf_array[n][1][0][0]),Double.valueOf(hilf_array[n][0][0][0])))
												.icon(BitmapDescriptorFactory.fromResource(R.drawable.m)))
												.setTitle(hilf_array[n][0][1][0].substring(hilf_array[n][0][1][0].lastIndexOf("/")+1, hilf_array[n][0][1][0].length())
												);
									}//for (int n=0; n<geoplayer.Geoplayerlistarraylength(); n++) {
								} catch(Exception e) {
									e.printStackTrace();
								}//try
							}//if (geoplayer_activate==true) {
							if (challenge_mode==true) {
								map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(thechallenge.akt_pos_spieler[0][1][0],thechallenge.akt_pos_spieler[0][0][0]),mapzoom));
								thechallenge.set_pos(kontainer[0][z][0][0][0][0], kontainer[1][z][0][0][0][0], (int) kontainer[0][z][1][0][0][0], (int) kontainer[0][z][0][1][0][0], (int) kontainer[0][z][0][0][1][0]);

								//Zeichnen der Marker!
								marker_update();

							}else{
								try{
									map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(kontainer[1][z][0][0][0][0],kontainer[0][z][0][0][0][0]),mapzoom));
									map.addMarker(new MarkerOptions()
											.position(new LatLng(kontainer[1][z][0][0][0][0],kontainer[0][z][0][0][0][0]))
											.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
									PolygonOptions linie = new PolygonOptions()
											.add(new LatLng(kontainer[1][z-1][0][0][0][0],kontainer[0][z-1][0][0][0][0]),new LatLng(kontainer[1][z][0][0][0][0],kontainer[0][z][0][0][0][0]));
									map.addPolygon(linie);

								} catch (NullPointerException e) {
									e.printStackTrace();
								}//Try
							}//if (challenge_mode) {
							if (geoplayer_activate==true) {
								geoplayer.geo_listener(kontainer[0][z][0][0][0][0], kontainer[1][z][0][0][0][0]);
							}//if (geoplayer_activate==true) {
						} else {
							z--;//zur�ckstellung
						}//if (hilf3<40) {
					} catch (Exception e) {
						e.printStackTrace();
						txt.setText("Hauptmenu "+e.getMessage()+" Markeranimation");//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					}//try
				}//if (z>0) {
				z++;
			} else {
				if (z>9000) {txt.setText("Hauptmenu       Speicher voll!");}
				//Vor dem Trainingsstart aktuelle Position
				//Die Aktuelle Position wird markiert und gezoomt
				try {
					map.clear();
					map.addMarker(new MarkerOptions()
							.position(new LatLng(location.getLatitude(),location.getLongitude()))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),mapzoom));

				} catch (NullPointerException e) {
					e.printStackTrace();
				}//try
			}////if (z<9000) {
		}//public void onLocationChanged(Location location)

		public void onProviderDisabled(String provider) {
			// TODO Automatisch generierter Methodenstub
			boolean gps_provider_chance = locmanager.isProviderEnabled(LocationManager.PROVIDERS_CHANGED_ACTION);
			boolean gps_provider = locmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean network_provider = locmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (gps_provider_chance==false && gps_provider==false && network_provider==false) {
				Toast.makeText(con, "GeeksRun: GPS ist aus", Toast.LENGTH_SHORT).show();
				gps_flag=false;
			} else {
				if (gps_provider==true) {
					Toast.makeText(con, "GeeksRun: GPS Sensor aktiviert", Toast.LENGTH_SHORT).show();
				}else {
					if (network_provider==true) {
						Toast.makeText(con, "GeeksRun: Network Sensor aktiviert", Toast.LENGTH_SHORT).show();
					}else {
						//...
					}//if (network_provider==true) {
				}//if (gps_provider==true) {
			}//if gps_provider_chance==false) {

			try {
				if (mNotificationManager!=null) {
					mNotificationManager.cancel(1);
					first_check_gps=false;
				}//if (mNotificationManager!=null) {
			} catch (Exception e) {
				e.printStackTrace();
			}//try
		}

		public void onProviderEnabled(String provider) {
			// TODO Automatisch generierter Methodenstub
			boolean gps_provider_chance = locmanager.isProviderEnabled(LocationManager.PROVIDERS_CHANGED_ACTION);
			boolean gps_provider = locmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean network_provider = locmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (gps_provider_chance==false && gps_provider==true && network_provider==true) {
				Toast.makeText(con, "GeeksRun: GPS ist an", Toast.LENGTH_SHORT).show();
				gps_flag=true;
				if (gps_provider==true && network_provider==true) {
					Toast.makeText(con, "GeeksRun: GPS und Netzwerk Sensor aktiviert", Toast.LENGTH_SHORT).show();
				}//if (gps_provider==true && network_provider==true) {
			} else {
				if (gps_provider==true) {
					Toast.makeText(con, "GeeksRun: GPS Sensor aktiviert", Toast.LENGTH_SHORT).show();
				}else {
					if (network_provider==true) {
						Toast.makeText(con, "GeeksRun: Netzwerk Sensor aktiviert", Toast.LENGTH_SHORT).show();
					}else {
						//..
					}//if (network_provider==true) {
				}//if (gps_provider==true) {
			}//if gps_provider_chance==false) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Automatisch generierter Methodenstub
			/**
			 if (status==0) {
			 gps_flag=true;
			 }else {
			 gps_flag=false;
			 }**/
		}
	}//public class Lokalmesser implements LocationListener


	public void marker_update() {
		try {
			map.clear();
			try {
				if (geoplayer!=null) {
					String[][][][] hilf_array = geoplayer.getgeoplayerlistarray();
					for (int n=0; n<geoplayer.Geoplayerlistarraylength(); n++) {
						map.addMarker(new MarkerOptions()
								.position(new LatLng(Double.valueOf(hilf_array[n][1][0][0]),Double.valueOf(hilf_array[n][0][0][0])))
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.m)))
								.setTitle(hilf_array[n][0][1][0].substring(hilf_array[n][0][1][0].lastIndexOf("/")+1, hilf_array[n][0][1][0].length())
								);
					}//for (int n=0; n<geoplayer.Geoplayerlistarraylength(); n++) {
				}//if (geoplayer!=null) {
			} catch(Exception e) {
				e.printStackTrace();
			}//try
			for (int nr=0;nr<thechallenge.getanz_spieler()+1;nr++) {
				if (nr==0) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_0)));
				}//Spieler
				if (nr==1) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_1)));
				}
				if (nr==2) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2)));
				}
				if (nr==3) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_3)));
				}
				if (nr==4) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_4)));
				}
				if (nr==5) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_5)));
				}
				if (nr==6) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_6)));
				}
				if (nr==7) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_7)));
				}
				if (nr==8) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_8)));
				}
				if (nr==9) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_9)));
				}
				if (nr==10) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_10)));
				}
				if (nr==11) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_11)));
				}
				if (nr==12) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_12)));
				}
				if (nr==13) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_13)));
				}
				if (nr==14) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_14)));
				}
				if (nr==15) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_15)));
				}
				if (nr==16) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_16)));
				}
				if (nr==17) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_17)));
				}
				if (nr==18) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_18)));
				}
				if (nr==19) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_19)));
				}
				if (nr==20) {
					map.addMarker(new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[nr][1][0],thechallenge.akt_pos_spieler[nr][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_20)));
				}

			}//for (int nr=1;nr<thechallenge.getanz_spieler();nr++) {
		}catch(Exception e) {
			e.printStackTrace();
		}//try
	}//public void marker_update()



	/*
	 * Das Ende der Activity HUD
	 */

	@Override
	public void onDestroy() {
		// Don't forget to shutdown tts!
		if (sprachausgabe != null) {
			sprachausgabe.stop();
			sprachausgabe.shutdown();
		}

		if (geeksplay != null) {
			geeksplay.stop();
			geeksplay.release();
		}

		if (mBuilder != null) {
			mNotificationManager.cancelAll();
		}
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView.getId()== R.id.switch_training) {
			if (isChecked== true) {
				//Starteinstellung
				locmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);//Das Objekt LocManager �berwacht das betriebsinterne GPS System
				loclistener = new Lokalmesser();

				if (gps_flag==true) {
					z=0;
					btn_laufsafe.setEnabled(false);
					btn_laufsafe.setTextColor(Color.GRAY);
					kontainer=null;
					kontainer = new double[2][9000][2][2][2][2];
					hoehenmeter =  null;
					hoehenmeter =  new double[9000];
					zsA = 0; zmA = 0; zhA = 0;zsX = 0; zmX = 0; zhX = 0;
					strecke=0;
					geschw=0;
					mittelgeschw=0;
					flag_status_training=false;
					if (challenge_mode==true) {
						File loadfile = con.getDir("archiv",MODE_PRIVATE);
						String[] hilf = new String[20];
						for (int za=0; za<anz_enemy;za++) {
							hilf[za]=my_enemies[za].toString();
						}//for (int za=0; za<anz_enemy;za++) {
						Toast.makeText(this, "Gegner werden geladen, bitte warten.", Toast.LENGTH_SHORT).show();
						Toast.makeText(this, "Du hast die L�ufernummer 21.", Toast.LENGTH_LONG).show();
						thechallenge = new Challenge(hilf,anz_enemy,ch_meinspeicherort, con, false);
					}
					time_format = new SimpleDateFormat("HH_mm_ss");
					try {
						SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						countdown=Integer.valueOf(info.getString("hud_time_countdown", "15"));
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(this, "Fehler mit der Einstellungsdatei.", Toast.LENGTH_SHORT).show();
						countdown=15;
					}//try

					//Pr�fung der Sprachdatei und des Dienstes
					if (sprach_anzeige==true) {
						if (sprachstatus == TextToSpeech.SUCCESS) {
							int n = sprachausgabe.setLanguage(Locale.GERMAN);
							if (n == TextToSpeech.LANG_MISSING_DATA || n == TextToSpeech.LANG_NOT_SUPPORTED) {
								Toast.makeText(HUD.this, "Auf deinem Handy existiert keine Sprachdatei.", Toast.LENGTH_SHORT).show();
							}//if (n == TextToSpeech.LANG_MIssING_DATA || n == TextToSpeech.LANG_NOT_SUPPORTED) {
						} else {
							Toast.makeText(HUD.this, "GeeksRun kann derzeit nicht die Sprachfunktion ausf�hren.", Toast.LENGTH_SHORT).show();
						}//if (sprachstatus == TextToSpeech.SUCCEss) {
					}//if (sprach_anzeige==true) {

					new Timer().schedule(new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							flag_coun = true;
							while (flag_coun) {
								try {
									Thread.sleep(1000);
								}catch(InterruptedException e) {
									e.printStackTrace();
								}
								if (countdown == 0) {
									flag_status_training=true;
									if (flag_status_training==true) {
										//Challenge
										if (challenge_mode) {
											challenge_handler.post(new Runnable() {

												@Override
												public void run() {
													// TODO Auto-generated method stub
													try {
														thechallenge.start_sequenz();
													} catch(Exception e) {
														e.printStackTrace();
													}//try
												}
											});
										}//if (challenge_mode) {

										flag_coun=false;
										if (sprachstatus == TextToSpeech.SUCCESS && sprach_anzeige==true) {
											int n = sprachausgabe.setLanguage(Locale.GERMAN);
											if (n == TextToSpeech.LANG_MISSING_DATA || n == TextToSpeech.LANG_NOT_SUPPORTED) {
												//...
											} else {
												SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
												String id_sex =info.getString("pers_sexx", "2");
												if (id_sex.equals("2")) {
													sprachausgabe.speak(
															"Lieber"+name+" ich begr��e dich zu deinem Lauf und w�nsche Dir viel Spas."
															,TextToSpeech.QUEUE_FLUSH, null);
												} else {
													sprachausgabe.speak(
															"Liebe"+name+" ich begr��e dich zu deinem Lauf und w�nsche Dir viel Spas."
															,TextToSpeech.QUEUE_FLUSH, null);
												}//if (str=="Mann") {

											}//Pr�fung ob die Sprache vorhanden ist
										} else {

										}//Pr�fung des Sprechers
										try {
											start_time=new Date();
											String zeitstempel =time_format.format(start_time);
											zhA=Integer.valueOf(zeitstempel.substring(0,zeitstempel.indexOf("_")));
											zeitstempel=zeitstempel.substring(zeitstempel.indexOf("_")+1, zeitstempel.length());
											zmA=Integer.valueOf(zeitstempel.substring(0,zeitstempel.indexOf("_")));
											zeitstempel=zeitstempel.substring(zeitstempel.indexOf("_")+1, zeitstempel.length());
											zsA=Integer.valueOf(zeitstempel);
										} catch (Exception e) {
											e.printStackTrace();
										}//try
										this.cancel();
										time_timer = new Timer();
										timerini();
										try {
											time_timer.schedule(timer_aufgabe, 1);
										} catch(Exception e) {
											e.printStackTrace();
										}//try
									}//if (flag_status_training==true) {
								} else {

									countdown_handler.post(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub
											time.setText("Zeit:"+String.valueOf(countdown));
										}
									});


									if (sprachstatus == TextToSpeech.SUCCESS && sprach_anzeige==true) {
										int n = sprachausgabe.setLanguage(Locale.GERMAN);
										if (n == TextToSpeech.LANG_MISSING_DATA || n == TextToSpeech.LANG_NOT_SUPPORTED) {

										} else {
											sprachausgabe.speak(String.valueOf(countdown),TextToSpeech.QUEUE_FLUSH, null);
										}//Pr�fung ob die Sprache vorhanden ist
									} else {

									}//Pr�fung des Sprechers
									if (challenge_mode==true) {
										try {
											if(thechallenge.ladevorgang==true) {
												countdown--;
											}//if(thechallenge.ladevorgang==true) {
										} catch(Exception e) {
											e.printStackTrace();
										}//try
									} else {
										countdown--;
									}//if (challenge_mode==true) {
								}//if (countdown == 0) {
							} //While
						}//run
					}, 1);//new Timer().schedule(new TimerTask() {
				} else {
					Toast.makeText(con, "Bitte schalte dein GPS an!", Toast.LENGTH_LONG).show();
					buttonView.setChecked(false);
				}//Kontrolle GPS

			} else {

				btn_laufsafe.setEnabled(true);
				btn_laufsafe.setTextColor(Color.WHITE);
				if (challenge_mode==true) {
					try {
						if(thechallenge != null){
							if (thechallenge.ende==false) {
								thechallenge.stop_sequenz();
							}//if (thechallenge.ende==false) {
						}//if(thechallenge != null){
					} catch(Exception e) {
						e.printStackTrace();
					}
				}//if (challenge_mode) {
				sim_flag=false;
				flag_coun=false;
				flag_status_training = false;//Timer kann nicht mehr Aufgaben erledigen, Training beendet
				thechallenge=null;
				//my_enemies=null;

				if (time_timer!=null) {
					time_timer.cancel();
				}//if (time_timer!=null) {
				if (timer_aufgabe!=null) {
					timer_aufgabe=null;
				}//if (timer_aufgabe!=null) {

				Format f = new SimpleDateFormat("dd_MM_yy");
				date=new Date();
				String s = f.format(date);
				final EditText safe_txt = new EditText(this);
				safe_txt.setText("Lauf_"+s);
				final AlertDialog.Builder stop_menu = new AlertDialog.Builder(this);
				stop_menu.setView(safe_txt);
				stop_menu.setTitle("Speichern");
				stop_menu.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						//Speicherroumtine
						String safe=String.valueOf(safe_txt.getText());
						try {
							meinspeicherort = con.getDir("archiv",MODE_PRIVATE);
							if (meinspeicherort.canWrite() == true && z!=0) {//Speicherort verf�gbar und Daten sind erhoben
								File file = new File(meinspeicherort, safe+".txt");
								fos = new FileOutputStream(file);
								osw = new OutputStreamWriter(fos);
								short za = 0;
								String text="";
								text=kontainer_array2string(kontainer);
								//Einbetten des String in eine Textdatei
								osw.write(text);
								osw.flush();
								Toast.makeText(con, "GPS Aktualisierungen angehalten und Koordinaten gespeichert unter: "+safe, Toast.LENGTH_LONG).show();

								//Das Training ist Zuende, jetzt werden die Leistungen abgespeichert
								try {
									if (z>3) {
										SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
										double koerpergewicht=Integer.valueOf(info.getString("pers_weight", "70"));
										double koerpergroesse=Integer.valueOf(info.getString("pers_height", "170"));
										int alter=Integer.valueOf(info.getString("pers_age", "25"));
										double grundumsatz = 0;
										String geschlecht = info.getString("pers_sexx", "Mann");
										if (geschlecht == "2") {
											grundumsatz=66.47+13.7*koerpergewicht+5*koerpergroesse-6.8*alter;
										} else {
											grundumsatz=655+9.6*koerpergewicht+1.8*koerpergroesse-4.7*alter;
										}//if (geschlecht == "Mann") {
										double perf_km = Double.valueOf(info.getString("performance_km", "0"));
										double perf_kalorie = Double.valueOf(info.getString("performance_kalorien", "0"));
										int perf_zeit = Integer.valueOf(info.getString("performance_time", "0"));
										int perf_trainings = Integer.valueOf(info.getString("performance_training", "0"));
										int perf_challenges = Integer.valueOf(info.getString("performance_challenge", "0"));
										int perf_challenge_bronxe=Integer.valueOf(info.getString("performance_challenge_bronxe", "0"));
										int perf_challenge_silber=Integer.valueOf(info.getString("performance_challenge_silber", "0"));
										int perf_challenge_gold=Integer.valueOf(info.getString("performance_challenge_gold", "0"));

										perf_km=perf_km+strecke;//Kilometerstand
										perf_zeit=perf_zeit+(Integer.valueOf(stunde)*3600)+(Integer.valueOf(minute)*60)+Integer.valueOf(sekunde);//Zeit
										double hilf_zeit = ((Double.valueOf(stunde)*3600)+(Double.valueOf(minute)*60)+Double.valueOf(sekunde))/3600;//Zeit in h
										double hilf_km_kalo = strecke+(((hoehenmeter[z-1])/1000)*7);//Strecke und H�henmeter vereint
										perf_kalorie=perf_kalorie+(((grundumsatz/24)*((hilf_km_kalo/hilf_zeit)*1.05))-(grundumsatz/24))*(hilf_zeit);//Verbrennungsz�hler


										if (challenge_mode==true) {
											try {
												perf_challenges++;
												int pos_player=0;
												if (thechallenge.get_playerposition_final()!="") {

													pos_player=Integer.valueOf(thechallenge.get_playerposition_final());

												}//if (thechallenge.get_playerposition_final()!="") {

												switch(pos_player) {
													case 1:perf_challenge_gold++;
														break;
													case 2:perf_challenge_silber++;
														break;
													case 3:perf_challenge_bronxe++;
														break;
												}//switch(pos_player) {
											} catch(Exception e) {
												e.printStackTrace();
												Toast.makeText(HUD.this, "Fehler beim Speichern des Spielstandes. Bitte wiederholen!", Toast.LENGTH_LONG).show();
											}
										} else {
											perf_trainings++;
										}//if (challenge_mode) {
									
									/*
									perf_km = 0; 
									perf_kalorie = 0;
									perf_zeit = 0;
									perf_trainings = 0;
									perf_challenges = 0;
									perf_challenge_bronxe=0;
									perf_challenge_silber=0;
									perf_challenge_gold=0;
									*/

										SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
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
									}//if (z>3) {, damit auch wirklich gelaufen wurde
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(con, "Es ist ein Fehler beim Aktualisieren deiner Leistungen aufgetreten! ("+e.getMessage()+"", Toast.LENGTH_LONG).show();
								}//Try

							} else {
								Toast.makeText(con, "GPS Aktualisierungen konnten nicht gespeichert werden.", Toast.LENGTH_SHORT).show();
							}//if (meinspeicherort.canWrite() == true && z!=0)
						} catch (IOException e) {
							e.printStackTrace();
							Toast.makeText(con, "Schreib Dir die Ergebnisse auf, es gab leider einen Fehler in der Speicherroutine.", Toast.LENGTH_LONG).show();
						}//Try

					}
				});
				stop_menu.setNegativeButton("Abbrechen", null);
				AlertDialog alert = stop_menu.create();
				alert.show();
			}//if (isChecked== true) {
		}//if (buttonView.getId()== R.id.switch_training) {

		if (buttonView.getId()== R.id.switch_challenge) {
			if (isChecked== true) {
				this.setTitle("Challenge");
				challenge_mode=true;
			} else {
				this.setTitle("Display");
				challenge_mode=false;
			}//if (isChecked== true) {
		}//if (buttonView.getId()== R.id.switch_training) {

		if (buttonView.getId()== R.id.switch_geoplayer) {
			if (isChecked== true) {
				geoplayer_activate=true;
				Toast.makeText(this, "Deine Geoplaylist wurde aktiviert.", Toast.LENGTH_LONG).show();
			} else {
				geoplayer_activate=false;
				Toast.makeText(this, "Deine Geoplaylist wurde deaktiviert.", Toast.LENGTH_LONG).show();
			}//if (isChecked== true) {
		}//if (buttonView.getId()== R.id.switch_geoplayer) {

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn_music_playlist) {
			hud_slide_drawer.animateClose();
			File meinspeicherort=null;
			mymusic = new File[50];
			lv_flag=true;
			lv.setBackgroundResource(R.color.geek_white);
			lv.setAlpha((float)0.8);

			lv.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.VISIBLE);
				}
			});//lv.post(new Runnable() {

			//Ladenroutine
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String music_file = info.getString("music_location", "Music");
			try {
				meinspeicherort = new File(music_file);
				entries= new String[meinspeicherort.list().length];
				for (int mza=0; mza<meinspeicherort.list().length;mza++) {
					entries[mza]=meinspeicherort.list()[mza];
				}//for (int mza=0; mza<meinspeicherort.length();mza++) {
			}catch(Exception e){
				Toast.makeText(this, "Geeksrun konnte den Musikordner '"+music_file+"' nicht finden. Legen sie, falls n�tig einen an oder geben Sie in den Einstellungen einen Musikordner an.", Toast.LENGTH_LONG).show();
			}//Try
			ArrayAdapter aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, entries);
			lv.setAdapter(aa);
			btn_music_last = new Button (this);
			btn_music_last.setText("OK");
			btn_music_last.setBackgroundResource(R.drawable.btn_dark);
			btn_music_last.setTextColor(Color.GREEN);

			btn_music_allselect = new Button (this);
			btn_music_allselect.setText("alles ausw�hlen");
			btn_music_allselect.setBackgroundResource(R.drawable.btn_dark);
			btn_music_allselect.setTextColor(Color.GREEN);

			btn_music_back = new Button (this);
			btn_music_back.setText("zur�ck");
			btn_music_back.setBackgroundResource(R.drawable.btn_dark);
			btn_music_back.setTextColor(Color.GREEN);

			btn_music_last.setOnClickListener(this);
			lv.addHeaderView(btn_music_last);

			btn_music_allselect.setOnClickListener(this);
			lv.addHeaderView(btn_music_allselect);

			btn_music_back.setOnClickListener(this);
			lv.addHeaderView(btn_music_back);

			lv.setOnItemClickListener(this);
		}//if (v == btn_music_playlist) {

		if (v == btn_challenge_change_runner) {
			hud_slide_drawer.animateClose();
			my_enemies=new File[20];
			lv_flag=false;
			lv_challenge=true;
			lv.setBackgroundResource(R.color.geek_white);
			lv.setAlpha((float)0.8);

			lv.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.VISIBLE);
				}
			});//lv.post(new Runnable() {

			//Ladenroutine
			try {
				ch_meinspeicherort = this.getDir("archiv",MODE_PRIVATE);
				challenge_entries = new String[ch_meinspeicherort.list().length];
				for (int ch_mza=0; ch_mza<ch_meinspeicherort.list().length;ch_mza++) {
					challenge_entries[ch_mza]=ch_meinspeicherort.list()[ch_mza];
				}//for (int mza=0; mza<meinspeicherort.length();mza++) {
			}catch(Exception e){
				Toast.makeText(this, "Geeksrun konnte den Ordner mit den Trainingseinheiten leider nicht finden.", Toast.LENGTH_LONG).show();
			}//Try
			ArrayAdapter ch_aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, challenge_entries);
			lv.setAdapter(ch_aa);
			btn_ch_last = new Button (this);
			btn_ch_last.setText("OK");
			btn_ch_last.setBackgroundResource(R.drawable.btn_dark);
			btn_ch_last.setTextColor(Color.GREEN);

			btn_ch_last.setOnClickListener(this);
			lv.addHeaderView(btn_ch_last);
			lv.setOnItemClickListener(this);
		}//if (v == btn_challenge_change_runner) {

		if (v == btn_geo_list) {
			hud_slide_drawer.animateClose();
			geoplayer = new Geoplayer(this);
			geo_lists = new File[50];
			geo_change_list=true;
			map.clear();
			lv_flag=true;
			lv.setBackgroundResource(R.color.geek_white);
			lv.setAlpha((float)0.8);

			lv.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.VISIBLE);
				}
			});//lv.post(new Runnable() {

			//Ladenroutine
			try {
				geo_meinspeicherort = this.getDir("geoarchiv",0);
				geo_entries = new String[geo_meinspeicherort.list().length];
				for (int mza=0; mza<geo_meinspeicherort.list().length;mza++) {
					geo_entries[mza]=geo_meinspeicherort.list()[mza];
				}//for (int mza=0; mza<meinspeicherort.length();mza++) {
			}catch(Exception e){
				Toast.makeText(this, "Geeksrun konnte den internen Geolistenordner nicht finden.", Toast.LENGTH_LONG).show();
			}//Try
			ArrayAdapter aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, geo_entries);
			lv.setAdapter(aa);
			btn_geo_last = new Button (this);
			btn_geo_last.setText("abbrechen");
			btn_geo_last.setOnClickListener(this);
			btn_geo_last.setBackgroundResource(R.drawable.btn_dark);
			btn_geo_last.setTextColor(Color.GREEN);

			btn_geo_im = new Button (this);
			btn_geo_im.setText("importieren");
			btn_geo_im.setOnClickListener(this);
			btn_geo_im.setBackgroundResource(R.drawable.btn_dark);
			btn_geo_im.setTextColor(Color.GREEN);

			lv.addHeaderView(btn_geo_last);
			lv.addHeaderView(btn_geo_im);
			lv.setOnItemClickListener(this);
		}//if (v == btn_geo_list) {

		if (v == btn_sound_option) {
			hud_slide_drawer.animateClose();
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
					//geeksplay.setVolume((float)progress/100, (float)progress/100);
					int MaxVolume = 100;
					float volume= (float) (Math.log(MaxVolume-(progress-1))/Math.log(MaxVolume));
					geeksplay.setVolume(1-volume, 1-volume);
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
					if (thechallenge!=null) {
						thechallenge.setVolumen(progress);}
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
					if (geoplayer!=null) {
						geoplayer.setVolumen(progress);}
				}
			});

			Button btn_sound_menu = (Button) sound_menu.findViewById(R.id.sound_button1);
			btn_sound_menu.setText("OK");
			btn_sound_menu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
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

		}//if (v == btn_sound_option) {

		if (v == btn_nav_mainmenu) {
			hud_slide_drawer.animateClose();
			if (flag_status_training==false) {
				finish();
			} else {
				Toast.makeText(con, "Bitte schlie�e zuerst das Training ab.", Toast.LENGTH_SHORT).show();
			}//if (flag_status_training==false) {
		}//if (v == btn_nav_mainmenu) {

		if (v == btn_nav_map) {
			hud_slide_drawer.animateClose();
			Intent i = new Intent(this, Map.class);
			String hudtext= kontainer_array2string(kontainer);
			if (hudtext != null && z>1) {
				i.putExtra("hudtext", hudtext);
				startActivity(i);
			} else {
				hudtext=null;
				i.putExtra("hudtext", hudtext);
				startActivity(i);
			}//if (hudtext != null) {
		}//if (v == btn_nav_map) {

		if (v == btn_music_play){
			if (btn_music_play.isChecked()) {
				if (anz_lieder>0 && anz_lieder<49) {
					//geeksplay = new MediaPlayer().create(this, Uri.fromFile(mymusic));
					try {
						geeksplay=new MediaPlayer().create(this, Uri.fromFile(mymusic[akt_lied]));
						SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(con);
						int volumen =Integer.valueOf(info.getString("sound_challenge", "0"));
						geeksplay.setVolume(volumen/10, volumen/10);
						geeksplay.start();
						geeksplay.setOnCompletionListener(new OnCompletionListener() {

							public void onCompletion(MediaPlayer mp) {
								// TODO Auto-generated method stub
								akt_lied++;
								if (akt_lied<anz_lieder) {
									play_next();
								} else {
									akt_lied=0;
									btn_music_play.setChecked(false);
								}//if (akt_lied<anz_lieder) {
							}//public void onCompletion(MediaPlayer mp) {
						});//geeksplay.setOnCompletionListener(new OnCompletionListener() {
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}//Try
					//geeksplay.setLooping(true);
				} else {
					Toast.makeText(this, "Bitte w�hle zwischen einem und 49 Musiktiteln aus.", Toast.LENGTH_SHORT).show();
				}//if (anz_lieder>0)
			} else {
				try {
					geeksplay.stop();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}//Try
			}//if (btn_music_play.isActivated()) {
		}//if (v == btn_music_play){

		if (v == btn_ch_last) {
			anz_enemy=ch_mza_sel;
			ch_mza_sel=0;
			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.GONE);
					btn_challenge.setEnabled(true);
					lv_challenge=false;
				}//run
			});//lv.post(new Runnable() {
			lv.removeHeaderView(btn_ch_last);
		}//if (v == btn_music_last) {

		if (v == btn_music_last) {
			anz_lieder=mza_sel;
			mza_sel=0;

			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.GONE);
					btn_music_play.setEnabled(true);
				}//run
			});//lv.post(new Runnable() {
			music_ordner_plus="";
			akt_lied=0;
			lv.removeHeaderView(btn_music_last);
			lv.removeHeaderView(btn_music_allselect);
			lv.removeHeaderView(btn_music_back);
		}//if (v == btn_geo_last) {

		if (v == btn_geo_last) {
			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.GONE);
					geo_change_list=false;
				}//run
			});//lv.post(new Runnable() {
			geo_titel="kein Song vorhanden";//leer
			lv.removeHeaderView(btn_geo_last);
			lv.removeHeaderView(btn_geo_im);
		}//if (v == btn_ch_last

		if (v == btn_geo_im) {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			im_meinspeicherort = new File(info.getString("import_location", "My Documents")+"/");
			ex_meinspeicherort = new File(info.getString("export_location", "My Documents")+"/");

			try {
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
				btn_import_abbruch.setTextColor(Color.GREEN);
				btn_import_abbruch.setOnClickListener(this);
				lv.addHeaderView(btn_import_abbruch);
				lv.setOnItemClickListener(this);
				import_mode=true;
			} catch(Exception e) {
				Toast.makeText(this, "GeeksRun konnte Eintr�ge des Importordners nicht finden.", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}//Try
			lv.removeHeaderView(btn_geo_last);
			lv.removeHeaderView(btn_geo_im);
		}//if (v == btn_geo_im) {

		if (v == btn_import_abbruch) {
			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.GONE);
					geo_change_list=false;
					import_mode=false;
				}//run
			});//lv.post(new Runnable() {
			lv.removeHeaderView(btn_import_abbruch);
		}

		if (v == btn_laufsafe) {
			if (challenge_mode) {
				if(thechallenge != null){
					if (thechallenge.ende==false) {
						thechallenge.stop_sequenz();
					}//if (thechallenge.ende==false) {
				}
			}//if (challenge_mode) {
			sim_flag=false;
			flag_coun=false;
			flag_status_training = false;//Timer kann nicht mehr Aufgaben erledigen, Training beendet
			thechallenge=null;
			//my_enemies=null;

			if (time_timer!=null) {
				time_timer.cancel();
			}//if (time_timer!=null) {
			if (timer_aufgabe!=null) {
				timer_aufgabe=null;
			}//if (timer_aufgabe!=null) {

			Format f = new SimpleDateFormat("dd_MM_yy");
			date=new Date();
			String s = f.format(date);
			final EditText safe_txt = new EditText(this);
			safe_txt.setText("Lauf_"+s);
			final AlertDialog.Builder stop_menu = new AlertDialog.Builder(this);
			stop_menu.setView(safe_txt);
			stop_menu.setTitle("Speichern");
			stop_menu.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//Speicherroumtine
					String safe=String.valueOf(safe_txt.getText());
					try {
						meinspeicherort = con.getDir("archiv",MODE_PRIVATE);
						if (meinspeicherort.canWrite() == true && z!=0) {//Speicherort verf�gbar und Daten sind erhoben
							File file = new File(meinspeicherort, safe+".txt");
							fos = new FileOutputStream(file);
							osw = new OutputStreamWriter(fos);
							short za = 0;
							String text="";
							text=kontainer_array2string(kontainer);
							//Einbetten des String in eine Textdatei
							osw.write(text);
							osw.flush();
							Toast.makeText(con, "GPS Aktualisierungen angehalten und Koordinaten gespeichert unter: "+safe, Toast.LENGTH_LONG).show();
							//Das Training ist Zuende, jetzt werden die Leistungen abgespeichert
							try {
								if (z>3) {
									SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
									double koerpergewicht=Integer.valueOf(info.getString("pers_weight", "70"));
									double koerpergroesse=Integer.valueOf(info.getString("pers_height", "170"));
									int alter=Integer.valueOf(info.getString("pers_age", "25"));
									double grundumsatz = 0;
									String geschlecht = info.getString("pers_sexx", "Mann");
									if (geschlecht == "2") {
										grundumsatz=66.47+13.7*koerpergewicht+5*koerpergroesse-6.8*alter;
									} else {
										grundumsatz=655+9.6*koerpergewicht+1.8*koerpergroesse-4.7*alter;
									}//if (geschlecht == "Mann") {
									double perf_km = Double.valueOf(info.getString("performance_km", "0"));
									double perf_kalorie = Double.valueOf(info.getString("performance_kalorien", "0"));
									int perf_zeit = Integer.valueOf(info.getString("performance_time", "0"));
									int perf_trainings = Integer.valueOf(info.getString("performance_training", "0"));
									int perf_challenges = Integer.valueOf(info.getString("performance_challenge", "0"));
									int perf_challenge_bronxe=Integer.valueOf(info.getString("performance_challenge_bronxe", "0"));
									int perf_challenge_silber=Integer.valueOf(info.getString("performance_challenge_silber", "0"));
									int perf_challenge_gold=Integer.valueOf(info.getString("performance_challenge_gold", "0"));

									perf_km=perf_km+strecke;//Kilometerstand
									perf_zeit=perf_zeit+(Integer.valueOf(stunde)*3600)+(Integer.valueOf(minute)*60)+Integer.valueOf(sekunde);//Zeit
									double hilf_zeit = ((Double.valueOf(stunde)*3600)+(Double.valueOf(minute)*60)+Double.valueOf(sekunde))/3600;//Zeit in h
									double hilf_km_kalo = strecke+(((hoehenmeter[z-1])/1000)*7);//Strecke und H�henmeter vereint
									perf_kalorie=perf_kalorie+(((grundumsatz/24)*((hilf_km_kalo/hilf_zeit)*1.05))-(grundumsatz/24))*(hilf_zeit);//Verbrennungsz�hler


									if (challenge_mode==true) {
										try {
											perf_challenges++;
											int pos_player=0;
											if (thechallenge.get_playerposition_final()!="") {

												pos_player=Integer.valueOf(thechallenge.get_playerposition_final());

											}//if (thechallenge.get_playerposition_final()!="") {

											switch(pos_player) {
												case 1:perf_challenge_gold++;
													break;
												case 2:perf_challenge_silber++;
													break;
												case 3:perf_challenge_bronxe++;
													break;
											}//switch(pos_player) {
										} catch(Exception e) {
											e.printStackTrace();
											Toast.makeText(HUD.this, "Fehler beim Speichern des Spielstandes. Bitte wiederholen!", Toast.LENGTH_LONG).show();
										}
									} else {
										perf_trainings++;
									}//if (challenge_mode) {
								
								/*
								perf_km = 0; 
								perf_kalorie = 0;
								perf_zeit = 0;
								perf_trainings = 0;
								perf_challenges = 0;
								perf_challenge_bronxe=0;
								perf_challenge_silber=0;
								perf_challenge_gold=0;
								*/

									SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
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
								}//if (z>3) {, damit auch wirklich gelaufen wurde
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(con, "Es ist ein Fehler beim Aktualisieren deiner Leistungen aufgetreten! ("+e.getMessage()+"", Toast.LENGTH_LONG).show();
							}//Try
						} else {
							Toast.makeText(con, "GPS Aktualisierungen konnten nicht gespeichert werden.", Toast.LENGTH_SHORT).show();
						}//if (meinspeicherort.canWrite() == true && z!=0)
					} catch (IOException e) {
						e.printStackTrace();
						Toast.makeText(con, "Schreib Dir die Ergebnisse auf, es gab leider einen Fehler in der Speicherroutine.", Toast.LENGTH_LONG).show();
					}//Try

				}
			});
			stop_menu.setNegativeButton("Abbrechen", null);
			AlertDialog alert = stop_menu.create();
			alert.show();
		}//if (v == btn_laufsafe) {

		if (v==btn_music_allselect) {
			lv.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					boolean music_tomuch=false;
					for (int za=0;za<entries.length;za++) {
						if (mza_sel<49) {
							lv.setItemChecked(za+3, true);
							SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
							String music_file = info.getString("music_location", "Music");
							mymusic[mza_sel]=new File(music_file, music_ordner_plus+entries[za]);
							//mymusic[mza_sel]=new File(music_file, entries[za]);
						} else {
							if (music_tomuch==false) {
								music_tomuch=true;
								Toast.makeText(HUD.this, "Bitte w�hle nicht mehr als 50 Musiktitel", Toast.LENGTH_SHORT).show();
							}//if (music_tomuch==true) {
						}//if (mza_sel<49) {
						mza_sel++;
					}//for (int za=0;za<entries.length;za++) {
				}//run
			});//lv.post(new Runnable()
		}//if (v==btn_music_allselect) {

		if (v==btn_music_back) {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String music_file = info.getString("music_location", "Music");
			File explor_focus = new File(music_file+music_ordner_plus);
			if (!explor_focus.getAbsolutePath().equals(music_file)) {
				File backup_file=explor_focus;
				try {
					explor_focus = new File(explor_focus.toString().substring(0,explor_focus.toString().lastIndexOf("/")));
					music_ordner_plus=music_ordner_plus.toString().substring(0,music_ordner_plus.toString().lastIndexOf("/"));
					music_ordner_plus=music_ordner_plus.toString().substring(0,music_ordner_plus.toString().lastIndexOf("/"));

					String[] s = new String[explor_focus.list().length];
					s=explor_focus.list();
					entries =new String[explor_focus.list().length];
					for (int n =0; n<explor_focus.list().length;n++) {
						entries[n]=s[n];
					}//for (int n =0; n<explor_focus.list().length;n++) {

					ArrayAdapter aa = new ArrayAdapter<String>(con,android.R.layout.simple_list_item_checked, entries);
					lv.setAdapter(aa);
				} catch (Exception e1) {
					e1.printStackTrace();
					Toast.makeText(con, "oooops, soweit kannst du nicht zur�ck.", Toast.LENGTH_SHORT).show();
					explor_focus=backup_file;
				}

			} else {
				music_ordner_plus="";
				Toast.makeText(con, "Bitte bleibe in deinem Musikordner oder zeige auf einen anderen Ordner in deinen Einstellungen.", Toast.LENGTH_SHORT).show();
			}//if (!music_ordner_plus.equals(music_ordner_plus+"/"+test_ordner.getName()+"/")) {
		}//if (v==btn_music_back) {
	}//public void onClick(View v) {


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.hud_slide_button, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (lv_flag==true || import_mode==true || geo_change_list==true || lv_challenge==true) {
			Toast.makeText(this, "Bitte verwende die Navigationsbuttons um abzubrechen.", Toast.LENGTH_LONG).show();
		}else {
			super.onBackPressed();
		}//if (lv_flag==true) {
	}

}
