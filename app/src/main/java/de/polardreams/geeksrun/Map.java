package de.polardreams.geeksrun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.transition.Visibility;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Map extends FragmentActivity implements OnTouchListener, OnMapReadyCallback, OnItemClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, OnClickListener, OnCheckedChangeListener{

	//GUI
	private View myv;
	private MapFragment mapfragment = null;
	private GoogleMap map;
	private ListView lv_replay=null;
	private TextView hud_his;
	private Intent i_start;

	//Touch
	private float array[][] = new float[2][99];
	private int  z_touch, ges_zeile, teil_zeile;//y = Anzahl Datensaetze Array Kontainer

	public double[][] kontainer = null;


	private String akt_kontainer ="";
	private double Strecke;
	private String map_name;

	//Performancesteigerung
	private String[] perform_konverter = null;
	private int perform_length =0;
	private short perform_id =0;//Verarbeitungsgeschwindigkeit 29
	private int za_thread =0; //For Schleife fuer die Thread-Nummer
	private Update_data[] map_marker_update = null;
	private short last_perform =0;

	private int akt_bar=0;
	private boolean first_start_bar=false;
	PolygonOptions linie = new PolygonOptions();
	PolygonOptions po = new PolygonOptions();

	//Replay
	private File[] my_enemies = null;//max. 20 gegner
	private String[] entries;
	private int anz_enemy, mza_sel;
	private File meinspeicherort=null;
	private Challenge thechallenge;
	private boolean sim_flag = false;
	private int simulations_nr=0;
	private Handler simu_akt = new Handler();
	private int sim_speed = 1000;
	private boolean history_flag=false;
	private Timer simulation;

	private boolean flag_sim_stop;
	private HashMap<Integer, MarkerOptions> marker_hh = new HashMap<Integer, MarkerOptions>();
	private double[][][] animation_marker = new double[20][99][2];
	private Handler animation_marker_handler = new Handler();
	private boolean animation_flag=true;
	private int anz_animation;//Zusatzpunkte, die als Marker fuer eine fluessigere Animation sorgen
	private int animation_nr;
	private boolean replay_status_simcal = false;


	//Geoplayer Editierung
	private Geoplayer geoplayer;
	private boolean geo_edit_mode = false;
	private Context c;
	private String geo_titel;
	private File geo_meinspeicherort;
	private String[] geo_listeneintraege;
	private Marker akt_marker;
	private String musikordner="";
	private int geo_list_length=0;
	private EditText txt;
	private double toleranz=0.0005;
	private boolean lv_geoplayer_flag=false;
	private boolean lv_replay_flag=false;
	private Button btn_geoplayer_zurueck;
	private String music_ordner_plus="/";

	//Editor
	private boolean editor_mode, editor_search = false;
	private int edit_koords_z = 0;

	//SliderDrawer
	private SlidingDrawer map_slide_drawer;
	private Switch geo_switch, mapeditor_switch;
	private Button
			map_slide_btn_replay_change,
			map_slide_btn_geo_safe,
			map_slide_btn_edit_safe;
	private ToggleButton map_slide_btn_replay_start;

	//Zeitgeschwindigkeit
	private int time_line = 0;
	private TextView txt_time, anime_txt;
	private SeekBar time_seek, anime_seek;
	private float slide_x;
	private LinearLayout slide_content;

	//Last Button Footer for View
	private Button btn_replay_last, btn_geocancle_last;

	//Hinweise
	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;

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
	private ImageView lokalize_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		c=this;
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		hud_his = (TextView)findViewById(R.id.textView1);
		lokalize_view = (ImageView)findViewById(R.id.imageView1);
		lokalize_view.setOnTouchListener(this);

		//Slidebar
		map_slide_drawer = (SlidingDrawer)findViewById(R.id.slidingDrawer1);
		slide_content = (LinearLayout)findViewById(R.id.map_content);
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
							map_slide_drawer.animateOpen();
						}//if (array[0][0] < array[0][z_touch]) {
						if (array[0][0] > array[0][z_touch]) {
							map_slide_drawer.animateClose();
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

		geo_switch = (Switch)findViewById(R.id.mapslide_switch1);
		geo_switch.setOnCheckedChangeListener(this);
		mapeditor_switch = (Switch)findViewById(R.id.mapslide_switch2);
		mapeditor_switch.setOnCheckedChangeListener(this);

		map_slide_btn_replay_start = (ToggleButton)findViewById(R.id.mapslide_tooglebutton1);
		map_slide_btn_replay_start.setOnClickListener(this);

		map_slide_btn_replay_change = (Button)findViewById(R.id.mapslide_button1);
		map_slide_btn_replay_change.setOnClickListener(this);
		map_slide_btn_geo_safe = (Button)findViewById(R.id.mapslide_button2);
		map_slide_btn_geo_safe.setOnClickListener(this);
		map_slide_btn_geo_safe.setEnabled(false);
		map_slide_btn_geo_safe.setTextColor(Color.GRAY);

		map_slide_btn_edit_safe = (Button)findViewById(R.id.mapslide_button3);
		map_slide_btn_edit_safe.setOnClickListener(this);
		map_slide_btn_edit_safe.setEnabled(false);
		map_slide_btn_edit_safe.setTextColor(Color.GRAY);

		txt_time = (TextView)findViewById(R.id.mapslide_textView5);
		time_seek = (SeekBar)findViewById(R.id.mapslide_seekbar);
		time_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				if (progress!=0) {
					sim_speed=progress;
					txt_time.setText("Frames aller: "+String.valueOf(progress)+" ms");
				}//if (progress!=0) {
			}
		});
		sim_speed=time_seek.getProgress();

		anime_txt = (TextView)findViewById(R.id.mapslide_textView6);
		anime_seek = (SeekBar)findViewById(R.id.mapslideanimation_seekbar);
		anime_seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				if (progress!=0) {
					anz_animation=progress;
					anime_txt.setText("zusaetzliche kuenstliche Geopunkte: "+String.valueOf(progress));
				}//if (progress!=0) {
			}
		});
		anz_animation=anime_seek.getProgress();

		lv_replay =(ListView)findViewById(R.id.listView_replay);
		lv_replay.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lv_replay.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				lv_replay.setVisibility(View.GONE);
			}
		});//lv.post(new Runnable() {

		//Map
		try {
			mapfragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
			mapfragment.getMapAsync(this);

		} catch (NullPointerException e) {
			e.printStackTrace();
		}//try


		//Intenuebergaben
		Intent intentvars = getIntent();
		//von History
		if (intentvars.getStringExtra("historytext")!=null) {
			akt_kontainer =intentvars.getStringExtra("historytext");
			history_flag=true;
		}//if (intentvars.getStringExtra("historytext")!=null) {
		//von HUD
		if (intentvars.getStringExtra("hudtext")!=null) {
			akt_kontainer =intentvars.getStringExtra("hudtext");
		}//if (intentvars.getStringExtra("hudtext")!=null) {
		if (intentvars.getStringExtra("historyname")!=null) {
			map_name =intentvars.getStringExtra("historyname");
			map_name=map_name.substring(0, map_name.length()-4);
			this.setTitle("Karte - "+map_name);
		}//if (intentvars.getStringExtra("hudtext")!=null) {
		//String zu Array und Anzeige
		if (akt_kontainer != "" && akt_kontainer != null) {
			//Ladebildschirm oeffnen
			i_start= new Intent(c, Procress.class);
			startActivity(i_start);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
			SharedPreferences.Editor editor = settings.edit();
			int progress_hilf=0;
			editor.putInt("load_progress",progress_hilf);
			editor.commit();
			try {
				verlagerung();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//if (akt_kontainer != "" || akt_kontainer != null)

		if (history_flag==false) {
			map_slide_drawer.setVisibility(View.GONE);
			slide_content.setVisibility(View.GONE);
		}//if (history_flag==true) {


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

	private void verlagerung() throws InterruptedException {
		Thread.sleep(1000);
		kontainer_string2array(akt_kontainer);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if (geo_edit_mode==false) {

			final int pos=position;
			lv_replay.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv_replay.setItemChecked(pos, true);
					if (mza_sel<20) {
						boolean tip_flag=false;
						if (mza_sel>0) {
							for (int pf=0; pf<mza_sel;pf++) {
								if (entries[pos-1].equals(my_enemies[pf].toString())) {
									Toast.makeText(Map.this, "Klicke bitte etwas langsamer, dieses Item hast du bereits angeklickt.", Toast.LENGTH_LONG).show();
									tip_flag=true;
								}//if (entries[pos].equals(my_enemies[pf].toString())) {
							}//for (int pf=0; pf<mza_sel;pf++) {
							if (tip_flag==false) {
								my_enemies[mza_sel]=new File(entries[pos-1]);
								mza_sel++;
							}//if (tip_flag==false) {
						} else {
							my_enemies[mza_sel]=new File(entries[pos-1]);
							mza_sel++;
						}//if (mza_sel>0) {

					} else {
						Toast.makeText(Map.this, "Bitte waehle nicht mehr als 20 Gegner aus!", Toast.LENGTH_SHORT).show();
					}//if (mza_sel<20) {

				}//run
			});//lv.post(new Runnable()

		}//if (geo_edit_mode==false) {
		if (geo_edit_mode==true) {
			lv_geoplayer_flag=false;
			final int pos=position;
			lv_replay.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub

					//Ordner anklicken und focus aendern
					SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					String music_file = info.getString("music_location", "Music");
					File test_ordner = new File(music_file, music_ordner_plus+geo_listeneintraege[pos-2]);

					if (test_ordner.getName().contains(".")==false && !test_ordner.isFile()) {

						if (!music_ordner_plus.equals(music_file+"/"+test_ordner.getName()+"/")) {
							music_ordner_plus=music_ordner_plus+test_ordner.getName()+"/";
						} else {
							music_ordner_plus="/";
						}//if (!music_ordner_plus.equals(music_ordner_plus+"/"+test_ordner.getName()+"/")) {
						File explor_focus = new File(music_file+music_ordner_plus);
						String[] s = new String[explor_focus.list().length];
						//s=explor_focus.list();
						s=explor_focus.list();
						geo_listeneintraege =new String[explor_focus.list().length];
						for (int n =0; n<explor_focus.list().length;n++) {
							geo_listeneintraege[n]=s[n];
						}//for (int n =0; n<explor_focus.list().length;n++) {
						ArrayAdapter geo_aa = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, geo_listeneintraege);
						lv_replay.setAdapter(geo_aa);

						//Toast.makeText(HUD.this, "Das war ein Ordner", Toast.LENGTH_SHORT).show();
					} else {
						lv_replay.setItemChecked(pos, true);
						geo_titel=geo_listeneintraege[pos-2];
						lv_replay.setVisibility(View.GONE);
						map.addMarker(new MarkerOptions()
								.position(akt_marker.getPosition())
								.title(geo_titel)
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.m))
						);
						//Trigger Anzeige
						LatLng npoint = new LatLng(akt_marker.getPosition().latitude+toleranz,akt_marker.getPosition().longitude);
						LatLng spoint = new LatLng(akt_marker.getPosition().latitude-toleranz,akt_marker.getPosition().longitude);
						LatLng wpoint = new LatLng(akt_marker.getPosition().latitude,akt_marker.getPosition().longitude-toleranz);
						LatLng opoint = new LatLng(akt_marker.getPosition().latitude,akt_marker.getPosition().longitude+toleranz);
						map.addMarker(new MarkerOptions().position(npoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
						map.addMarker(new MarkerOptions().position(spoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
						map.addMarker(new MarkerOptions().position(opoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
						map.addMarker(new MarkerOptions().position(wpoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

						PolygonOptions linie1 = new PolygonOptions().add(npoint,opoint);
						PolygonOptions linie2 = new PolygonOptions().add(opoint,spoint);
						PolygonOptions linie3 = new PolygonOptions().add(spoint,wpoint);
						PolygonOptions linie4 = new PolygonOptions().add(wpoint,npoint);
						map.addPolygon(linie1);
						map.addPolygon(linie2);
						map.addPolygon(linie3);
						map.addPolygon(linie4);

						geoplayer.set_geopoint(akt_marker.getPosition().longitude, akt_marker.getPosition().latitude, musikordner+music_ordner_plus+geo_titel, toleranz);
						geo_list_length++;
						lv_replay.removeHeaderView(btn_geocancle_last);
						lv_replay.removeHeaderView(btn_geoplayer_zurueck);
						music_ordner_plus="/";
					}//if (test_ordner.getName().contains(".")==false && !test_ordner.isFile()) {

				}//run
			});//lv.post(new Runnable()

		}//if (geo_edit_mode==true) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (z_touch <98) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				array[0][0]=(float) event.getX();
				array[1][0]=(float) event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				array[0][z_touch]=event.getX();
				array[1][z_touch]=event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_MOVE)
			if (event.getAction() == MotionEvent.ACTION_UP) {
				array[0][z_touch]=event.getX();
				array[1][z_touch]=event.getY();

				if (array[0][0] < array[0][z_touch]) {
					finish();//HUD
				}//if (array[0][0] < array[0][z_touch]) {

				if (array[0][0] > array[0][z_touch]) {

					Intent i = new Intent(this, Lap.class);
					String hudtext= akt_kontainer;
					if (hudtext != null) {
						i.putExtra("hudtext", hudtext);
						startActivity(i);
						//finish();
					}//if (hudtext != null) {

				}//if (array[0][0] > array[0][z_touch]) {
				z_touch=0;
				for (int n=0; n<z_touch; n++) {
					array[0][n]=0;
					array[1][n]=0;
				}//for (int n=0; n<z_touch; n++)
			}//if (event.getAction() == MotionEvent.ACTION_UP)
			z_touch++;
		} else {
			Toast.makeText(this, "Ich bin verwirrt, in welches Menu moechtest du?", Toast.LENGTH_SHORT).show();
			z_touch=0;
		}//if (z_touch < 98) {
		return true;
	}//public boolean onTouch(View v, MotionEvent event)

	@Override
	public void onMapReady(GoogleMap arg0) {
		// TODO Auto-generated method stub
		map = arg0;
		map.setOnMapClickListener(this);
		map.setOnMarkerClickListener(this);
		map.clear();
	}

	/*
	 * Methoden
	 *
	 * Die Methoden muessen auf Grund ihres Auswertungscharakter nicht weiter
	 * gesichert werden mit Try-Catch, weil diese in den vorherigen Activities
	 * bereit berechnet wurden
	 */

	public void kontainer_string2array(String var_string) {
		try {
			if (var_string != null || var_string=="") {
				//Perform_id Berechnen
				ges_zeile = 0;
				for (int x = 0; x<var_string.length(); x++){
					if (var_string.charAt(x)=='\n') {ges_zeile++;}
				}//for (int x = 0; x<var_string.length(); x++){
				//Performance
				//kontainer= new double[6][ges_zeile+10];!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				kontainer= new double[6][9000];
				if (ges_zeile>1000) {
					perform_id=(short) (ges_zeile/350);
					/*
					 * Grad der Lestungssteigerung (bzw. Zuteiung
					 * der Zeilenverarbeitung fuer die einzelnen Handler)
					 */
				} else {
					if (ges_zeile>100) {
						perform_id=(short) (ges_zeile/20);
					} else {
						if (ges_zeile>25) {
							perform_id=(short) (ges_zeile/5);
						} else {
							perform_id=(short) (ges_zeile/2);
						}//if (ges_zeile>25) {
					}//if (ges_zeile>100) {
				}//if (ges_zeile>1000) {

				int index_arrays=Integer.valueOf(ges_zeile/perform_id);
				perform_konverter = new String[index_arrays+10];
				map_marker_update = new Update_data[index_arrays+10];

				teil_zeile =0;
				Boolean perform_flag=true;
				int m=0;
				while (perform_flag) {
					if (m != -1) {
						if (var_string.charAt(m) == '\n') {
							/*
							 * m = -1 wenn perform_id == 1 also nur eine Zeile genommen wird
							 * (die letzte Zeile verursacht dann einen Fehler :(
							 */
							teil_zeile++;
							/*
							 * Hinweis
							 * perform_length ist die Anzahl der benoetigten Handler bzw. Teile des Strings
							 * perform_konverter ist die Laenger der Teile des Strings
							 * perform_id ist die Anzahl der Zeilen, die in einen Teil des String hineinkommen
							 * last_perform ist die Anzahl der letzten Zeilen im letzten Teil
							 */
							if (teil_zeile==perform_id) {
								perform_konverter[perform_length]=var_string.substring(0, m);
								var_string=var_string.substring(m+1, var_string.length());
								m=0;
								teil_zeile=0;
								perform_length++;
							} else {
								if (m==var_string.length()-1) {
									perform_konverter[perform_length]=var_string.substring(0, var_string.length());//letzten Teil in perform_konverter einfuegen
									last_perform=(short)teil_zeile;
									perform_flag=false;
								} else {
									if (var_string.substring(m+1,var_string.length()-1).indexOf('\n') == -1 ) {
										perform_konverter[perform_length]=var_string.substring(0, var_string.length());//letzten Teil in perform_konverter einfuegen
										last_perform=(short)teil_zeile;
										perform_flag=false;
									}//if (var_string.indexOf('\n') == -1  )
								}//if (m==var_string.length()) {
							}//Zeilen zaehlen
						}//if (var_string.charAt(m) == '\n') {
						/**String mit Koordinaten in Teilabschnitte untergliedern **/
						m++;
					} else {
						perform_flag=false;
					}//if (m != -1) {
				}//while (perform_flag)

				if (ges_zeile<9000){
					Strecke=0;
					//Performance Steigerung
					if (ges_zeile<1000) {
						for (za_thread = 0; za_thread<perform_length+1; za_thread++) {
							map_marker_update[za_thread]=new Update_data((short)za_thread);
							map_marker_update[za_thread].execute(0);

						}//for (int za_thread = 0; za_thread<30; za_thread++) {
					} else {
						for (za_thread = 0; za_thread<perform_length+1; za_thread++) {
							map_marker_update[za_thread]=new Update_data((short)za_thread);
							map_marker_update[za_thread].execute(0);

						}//for (int za_thread = 0; za_thread<30; za_thread++) {
					}//if (ges_zeile<1000) {
				}//if (ges_zeile<9000){
			}//if (var_string != null || var_string=="") {
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(c, "Beim Laden des Trainings ist etwas schief gelaufen. Kehre zurueck ins Hauptmenu.", Toast.LENGTH_LONG).show();
			Toast.makeText(c, "Schau nach, ob das Training vielleicht beschaedigt ist.", Toast.LENGTH_LONG).show();
		}
	}//public void kontainer_array2string() {

	private class Update_data extends AsyncTask<Integer, String, String> {
		private short var_za;
		private short var_performid;

		Update_data(short var) {
			var_za = var;
			var_performid = var;
		}

		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			try {
				short perform_za=(short) var_za;
				String xtxt;
				short zahl;
				short zahl_ende;
				if (perform_length != perform_za) {
					zahl =(short) ((var_za*perform_id));
					zahl_ende =(short) (perform_id+zahl);
				} else {
					zahl =(short) ((var_za*perform_id));
					zahl_ende =(short) (last_perform+zahl);//die letzten Zeilen machen
				}//if (perform_length != var_za) {
				while(zahl<zahl_ende) {
					short zeilenumbruch_index = (short) (perform_konverter[perform_za].indexOf('\n'));
					if (zeilenumbruch_index != -1) {
						xtxt=perform_konverter[perform_za].substring(0,zeilenumbruch_index);
					} else {
						xtxt=perform_konverter[perform_za];
					}//if (zeilenumbruch_index != -1) {

					kontainer[0][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					kontainer[1][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					kontainer[2][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					kontainer[3][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					kontainer[4][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					kontainer[5][zahl]=Double.valueOf(xtxt);//.substring(0, historytext.indexOf('\n'))

					char[] inputBuffer = new char[90000];
					inputBuffer=perform_konverter[perform_za].toCharArray();
					for (int n=0; n<perform_konverter[perform_za].indexOf('\n')+1;n++) {
						inputBuffer[n]='\u0000';
					}//For
					perform_konverter[perform_za]=new String(inputBuffer);
					xtxt="";
					zahl++;

				}//while
				//Ladebildschirm aktualisieren
				/*
				 * ueber die XML preferences
				 */
				if (perform_length==perform_za) {
					try{
						//map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(kontainer[1][1][0][0][0][0],kontainer[0][1][0][0][0][0]),15));
					} catch (NullPointerException e) {
						e.printStackTrace();
					}//try
				}//if (perform_length==perform_za) {
			}catch(Exception e) {
				e.printStackTrace();
				Toast.makeText(Map.this, "Fehler im Umwandlungsprozess der Daten.", Toast.LENGTH_SHORT).show();
			}//Try

			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			try {
				short perform_za=(short) var_za;
				String xtxt;
				short zahl;
				short zahl_ende;
				if (perform_length != perform_za) {
					zahl =(short) ((var_za*perform_id));
					zahl_ende =(short) (perform_id+zahl);
				} else {
					zahl =(short) ((var_za*perform_id));
					zahl_ende =(short) (last_perform+zahl);//die letzten Zeilen machen
				}//if (perform_length != var_za) {
				try{

					while(zahl<zahl_ende) {

						if (zahl==0) {
							map.addMarker(new MarkerOptions()
									.position(new LatLng(kontainer[1][zahl],kontainer[0][zahl]))
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start)))

									.setTitle("Strecke: "+String.format("%.2f",Strecke)+"km"+
											" Zeit: "+String.format("%.0f",kontainer[2][zahl])+":"
											+String.format("%.0f",kontainer[3][zahl])
											+":"+String.format("%.0f",kontainer[4][zahl])
											+" "+"Hoehenmeter: "+String.format("%.0f",kontainer[5][zahl])+"m");

						} else {
							if (zahl==ges_zeile-2) {
								map.addMarker(new MarkerOptions()
										.position(new LatLng(kontainer[1][zahl],kontainer[0][zahl]))
										.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_ziel)))

										.setTitle("Strecke: "+String.format("%.2f",Strecke)+"km"+
												" Zeit: "+String.format("%.0f",kontainer[2][zahl])+":"
												+String.format("%.0f",kontainer[3][zahl])
												+":"+String.format("%.0f",kontainer[4][zahl])
												+" "+"Hoehenmeter: "+String.format("%.0f",kontainer[5][zahl])+"m");

							}//if (zahl==ges_zeile) {
						}//if (zahl==0) {

						if (zahl>0) {
							if (zahl >1 && zahl<ges_zeile) {
								//Strecke berechnen
								double hilf1=(2*Math.PI*Math.cos(Math.toRadians(kontainer[1][zahl]))*6371)/(2*Math.PI);//Die anderen Koordinaten muessen in der Naehe sein
								double hilf2=hilf1*Math.PI/180;
								double hilf3=Math.sqrt(Math.pow(6371*Math.PI/180*(kontainer[1][zahl-1]-kontainer[1][zahl]),2)+
										Math.pow(hilf2*(kontainer[0][zahl-1]-kontainer[0][zahl]),2));
								Strecke = Strecke + hilf3;
							} else {
								Strecke = 0;
							}//if (zahl >1 && zahl<ges_zeile) {

							linie.add(new LatLng(kontainer[1][zahl-1],kontainer[0][zahl-1]),new LatLng(kontainer[1][zahl],kontainer[0][zahl]));

							map.addMarker(new MarkerOptions()
									.position(new LatLng(kontainer[1][zahl],kontainer[0][zahl]))
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))

									.setTitle("Strecke: "+String.format("%.2f",Strecke)+"km"+
											" Zeit: "+String.format("%.0f",kontainer[2][zahl])+":"
											+String.format("%.0f",kontainer[3][zahl])
											+":"+String.format("%.0f",kontainer[4][zahl])
											+" "+"Hoehenmeter: "+String.format("%.0f",kontainer[5][zahl])+"m");

					/*
					po.add(new LatLng (kontainer[1][zahl]-0.0002,kontainer[0][zahl]-0.0004),new LatLng (kontainer[1][zahl]-0.0002,kontainer[0][zahl]+0.0004));
					po.add(new LatLng (kontainer[1][zahl]-0.0002,kontainer[0][zahl]-0.0004),new LatLng (kontainer[1][zahl]+0.0002,kontainer[0][zahl]-0.0004));
					po.add(new LatLng (kontainer[1][zahl]+0.0002,kontainer[0][zahl]-0.0004),new LatLng (kontainer[1][zahl]+0.0002,kontainer[0][zahl]+0.0004));
					po.add(new LatLng (kontainer[1][zahl]+0.0002,kontainer[0][zahl]+0.0004),new LatLng (kontainer[1][zahl]-0.0002,kontainer[0][zahl]+0.000));
					*/

						}//	if (zahl>0) {
						zahl++;
						akt_bar++;
					}//while

					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
					SharedPreferences.Editor editor = settings.edit();
					int progress_hilf=0;
					try {
						progress_hilf=(akt_bar*100)/ges_zeile;
					}catch (Exception e) {
						e.printStackTrace();
					}//try
					editor.putInt("load_progress",progress_hilf);
					editor.commit();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}//try
				//Last Runnable
				if (za_thread-1  == var_za) {
					//Ladebildschirm schliessen
					map.addPolygon(linie);

					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
					SharedPreferences.Editor editor = settings.edit();
					int progress_hilf=100;
					editor.putInt("load_progress",progress_hilf);
					editor.commit();
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(kontainer[1][1],kontainer[0][1]),15));
					destroy_all_marker();
					this.cancel(true);
				}//if (za_thread-1  == var_za) {

			} catch(Exception e) {
				e.printStackTrace();
				Toast.makeText(Map.this, "Fehler beim berechnen und setzen der Marker von GoogleMaps", Toast.LENGTH_SHORT).show();
			}//Try

			super.onPostExecute(result);
		}
	}

	@Override
	public void onMapClick(final LatLng arg0) {
		// TODO Auto-generated method stub
		if (geo_edit_mode==true) {
			AlertDialog.Builder set_pos_question = new AlertDialog.Builder(c);
			set_pos_question.setTitle("GeoPlayer Editor");
			set_pos_question.setMessage("Moechtest Du diesen Punkt setzen?");
			set_pos_question.setPositiveButton("ja", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					map.addMarker(new MarkerOptions()
							.position(arg0)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.no_m)));
				}
			});
			set_pos_question.setNegativeButton("nein", null);
			set_pos_question.show();
		}//if (geo_edit_mode==true) {
		if (editor_mode == true && editor_search==true) {
			AlertDialog.Builder set_pos_question = new AlertDialog.Builder(c);
			set_pos_question.setTitle("Editor");
			set_pos_question.setMessage("Moechtest Du einen neuen Punkt setzen?");
			set_pos_question.setPositiveButton("ja", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					kontainer[0][edit_koords_z]=arg0.longitude;
					kontainer[1][edit_koords_z]=arg0.latitude;

					map.addMarker(new MarkerOptions()
							.position(arg0)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
					if (edit_koords_z>0) {
						PolygonOptions linie1 = new PolygonOptions()
								.add(new LatLng(kontainer[1][edit_koords_z-1],kontainer[0][edit_koords_z-1]),new LatLng(kontainer[1][edit_koords_z],kontainer[0][edit_koords_z]));
						linie1.strokeColor(Color.BLUE);
						map.addPolygon(linie1);
					}//if (edit_koords_z>0) {
					if (edit_koords_z<ges_zeile) {
						PolygonOptions linie2 = new PolygonOptions()
								.add(new LatLng(kontainer[1][edit_koords_z+1],kontainer[0][edit_koords_z+1]),new LatLng(kontainer[1][edit_koords_z],kontainer[0][edit_koords_z]));
						linie2.strokeColor(Color.BLUE);
						map.addPolygon(linie2);
					}//if (edit_koords_z<ges_zeile) {
					editor_search=false;
				}
			});
			set_pos_question.setNegativeButton("nein", null);
			set_pos_question.show();
		}//if (editor_mode == true) {
	}

	@Override
	public boolean onMarkerClick(final Marker arg0) {
		// TODO Auto-generated method stub
		if (geo_edit_mode==true) {

			lv_replay.setBackgroundResource(R.color.geek_white);
			lv_replay.setAlpha((float)0.8);
			lv_geoplayer_flag=true;
			lv_replay.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv_replay.setVisibility(View.VISIBLE);
				}
			});//lv.post(new Runnable() {
			//Ladenroutine
			try {
				SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				musikordner =info.getString("music_location", "Music");

				geo_meinspeicherort = new File(musikordner);
				geo_listeneintraege = new String[geo_meinspeicherort.list().length];

				for (int mza=0; mza<geo_meinspeicherort.list().length;mza++) {
					geo_listeneintraege[mza]=geo_meinspeicherort.list()[mza];
				}//for (int mza=0; mza<meinspeicherort.length();mza++) {

			}catch(Exception e){
				Toast.makeText(this, "Geeksrun konnte den Musikordner leider nicht finden.", Toast.LENGTH_LONG).show();

			}//Try
			ArrayAdapter geo_aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, geo_listeneintraege);
			lv_replay.setAdapter(geo_aa);
			btn_geocancle_last = new Button(this);
			btn_geocancle_last.setText("Abbruch");
			btn_geocancle_last.setBackgroundResource(R.drawable.btn_dark);
			btn_geocancle_last.setTextColor(Color.GREEN);

			btn_geoplayer_zurueck = new Button(this);
			btn_geoplayer_zurueck.setText("zurueck");
			btn_geoplayer_zurueck.setBackgroundResource(R.drawable.btn_dark);
			btn_geoplayer_zurueck.setTextColor(Color.GREEN);

			btn_geocancle_last.setOnClickListener(this);
			lv_replay.addHeaderView(btn_geocancle_last);

			btn_geoplayer_zurueck.setOnClickListener(this);
			lv_replay.addHeaderView(btn_geoplayer_zurueck);


			lv_replay.setOnItemClickListener(this);
			akt_marker=arg0;
			arg0.remove();
		}//if (geo_edit_mode==true) {
		if (editor_mode == true && editor_search==false) {
			AlertDialog.Builder set_pos_question = new AlertDialog.Builder(c);
			set_pos_question.setTitle("Editor");
			set_pos_question.setMessage("Moechtest Du diesen Punkt bearbeiten?");
			set_pos_question.setPositiveButton("ja", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					int zeile = 0;
					boolean flag_edit_search = true;
					while (flag_edit_search) {
						if (kontainer[1][zeile]==arg0.getPosition().latitude && kontainer[0][zeile]==arg0.getPosition().longitude) {
							flag_edit_search=false;
							edit_koords_z=zeile;
						} else {
							zeile++;
						}
					}//while (flag_edit_search) {
					arg0.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_delete));
					editor_search=true;
				}
			});
			set_pos_question.setNegativeButton("nein", null);
			set_pos_question.show();

		}//if (editor_mode == true) {
		return false;
	}

	public String kontainer_array2string(double kontainer[][]) {
		//einlesen des Arrays in einen String
		int za=0;
		String text="";
		while (za<ges_zeile) {
			text=text
					+String.valueOf(kontainer[0][za])
					+";"+String.valueOf(kontainer[1][za])
					+";"+String.valueOf(kontainer[2][za])
					+";"+String.valueOf(kontainer[3][za])
					+";"+String.valueOf(kontainer[4][za])
					+";"+String.valueOf(kontainer[5][za])
					+"\n";//das Trennungszeichen ; ist fuer Chatprotokoll unguenstig, bitte "," verwenden
			za++;
		}
		return text;
	}//public void kontainer_array2string() {

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v==map_slide_btn_replay_start) {
			map_slide_drawer.animateClose();
			if (map_slide_btn_replay_start.isChecked()) {
				//Gegner Wahl Zwang
				if (my_enemies!=null) {
					if (my_enemies[0]==null) {
						my_enemies=null;
					}//if (my_enemies[0]==null) {
				}//if (my_enemies!=null) {
				if (thechallenge==null && my_enemies!=null) {
					String[] hilf = new String[20];
					hilf[0]=map_name+".txt";
					for (int za=1; za<anz_enemy+1;za++) {
						hilf[za]=my_enemies[za-1].toString();
					}

					thechallenge = new Challenge(hilf,anz_enemy,meinspeicherort, this, true);
					thechallenge.start_sequenz();
					sim_flag=true;
					flag_sim_stop=true;
					animation_nr=anz_animation;
					thechallenge.set_pos(kontainer[0][simulations_nr], kontainer[1][simulations_nr],
							(int)kontainer[2][simulations_nr], (int)kontainer[3][simulations_nr], (int)kontainer[4][simulations_nr]);
					simulation = new Timer();
					simulation.schedule(new TimerTask() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							while(sim_flag) {
								try {
									Thread.sleep(sim_speed);
								}catch(Exception e) {
									e.printStackTrace();
								}//try

								if (flag_sim_stop==true) {
									if (replay_status_simcal==false && thechallenge.ende==true ) {
										try {
											thechallenge.set_pos(kontainer[0][simulations_nr], kontainer[1][simulations_nr],
													(int)kontainer[2][simulations_nr], (int)kontainer[3][simulations_nr], (int)kontainer[4][simulations_nr]);
										} catch(Exception e) {
											e.printStackTrace();
										}//try
										simulations_nr++;
										replay_status_simcal=true;
									}//if (replay_status_simcal==false) {

									simu_akt.post(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub
											try {
												if (animation_nr>anz_animation-1) {
													ui_calculation_replay();
													animation_nr=0;
													replay_status_simcal=false;
												} else {
													map.clear();
													for (int ui_z=0; ui_z<thechallenge.getanz_spieler()+1; ui_z++) {
														if (ui_z==0) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[0][animation_nr][1],animation_marker[0][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_0)));
														}
														if (ui_z==1) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[1][animation_nr][1],animation_marker[1][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_1)));
														}
														if (ui_z==2) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[2][animation_nr][1],animation_marker[2][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2)));
														}
														if (ui_z==3) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[3][animation_nr][1],animation_marker[3][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_3)));
														}
														if (ui_z==4) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[4][animation_nr][1],animation_marker[4][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_4)));
														}
														if (ui_z==5) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[5][animation_nr][1],animation_marker[5][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_5)));
														}
														if (ui_z==6) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[6][animation_nr][1],animation_marker[6][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_6)));
														}
														if (ui_z==7) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[7][animation_nr][1],animation_marker[7][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_7)));
														}
														if (ui_z==8) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[8][animation_nr][1],animation_marker[8][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_8)));
														}
														if (ui_z==9) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[9][animation_nr][1],animation_marker[9][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_9)));
														}
														if (ui_z==10) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[10][animation_nr][1],animation_marker[10][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_10)));
														}
														if (ui_z==11) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[11][animation_nr][1],animation_marker[11][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_11)));
														}
														if (ui_z==12) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[12][animation_nr][1],animation_marker[12][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_12)));
														}
														if (ui_z==13) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[13][animation_nr][1],animation_marker[13][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_13)));
														}
														if (ui_z==14) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[14][animation_nr][1],animation_marker[14][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_14)));
														}
														if (ui_z==15) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[15][animation_nr][1],animation_marker[15][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_15)));
														}
														if (ui_z==16) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[16][animation_nr][1],animation_marker[16][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_16)));
														}
														if (ui_z==17) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[17][animation_nr][1],animation_marker[17][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_17)));
														}
														if (ui_z==18) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[18][animation_nr][1],animation_marker[18][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_18)));
														}
														if (ui_z==19) {
															map.addMarker(new MarkerOptions()
																	.position(new LatLng(animation_marker[19][animation_nr][1],animation_marker[19][animation_nr][0]))
																	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_19)));
														}
													}//for (ui_z=0; ui_z<thechallenge.getanz_spieler()+1; ui_z++) {
													animation_nr++;
												}//if (animation_nr>anz_animation-1) {
											}catch(Exception e) {
												e.printStackTrace();
											}//try
											hud_his.setText("HUD/History "+thechallenge.stunden+":"+thechallenge.minuten+":"+thechallenge.sekunden+" Richtung: "+thechallenge.akt_himmelsrichtung);
										}//run
									});//simu_akt
								}//if (flag_sim_stop==true) {
							}//while
						}//run
					},0);//Timer

				} else {
					flag_sim_stop=true;
					if (my_enemies==null) {
						Toast.makeText(this, "Bitte waehle ein paar Gegner zur passenden Strecke!", Toast.LENGTH_SHORT).show();
						map_slide_btn_replay_start.setChecked(false);
					}//if (my_enemies!=null) {
				}//if (thechallenge==null && my_enemies!=null) {
			} else {
				flag_sim_stop=false;
			}//if (!map_slide_btn_replay_start.isChecked()) {
		}//if (v==map_slide_btn_replay_start) {

		if (v == map_slide_btn_replay_change && geo_edit_mode==false) {
			map_slide_drawer.animateClose();
			my_enemies=new File[20];
			lv_replay_flag=true;
			lv_replay.setBackgroundResource(R.color.geek_white);
			lv_replay.setAlpha((float)0.8);

			lv_replay.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv_replay.setVisibility(View.VISIBLE);
				}
			});//lv.post(new Runnable() {

			//Ladenroutine
			try {
				meinspeicherort = this.getDir("archiv",MODE_PRIVATE);
				entries= new String[meinspeicherort.list().length];
				for (int mza=0; mza<meinspeicherort.list().length;mza++) {
					entries[mza]=meinspeicherort.list()[mza];
				}//for (int mza=0; mza<meinspeicherort.length();mza++) {

			}catch(Exception e){
				Toast.makeText(this, "Geeksrun konnte den Ordner mit den Trainingseinheiten leider nicht finden.", Toast.LENGTH_LONG).show();

			}//Try
			ArrayAdapter aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, entries);
			lv_replay.setAdapter(aa);
			btn_replay_last= new Button(this);
			btn_replay_last.setText("OK");
			btn_replay_last.setBackgroundResource(R.drawable.btn_dark);
			btn_replay_last.setTextColor(Color.GREEN);

			btn_replay_last.setOnClickListener(this);
			lv_replay.addHeaderView(btn_replay_last);
			lv_replay.setOnItemClickListener(this);
		}//if (v == map_slide_btn_replay_change && geo_edit_mode==false) {

		if (v == map_slide_btn_geo_safe) {
			map_slide_drawer.animateClose();
			AlertDialog.Builder geo_save = new AlertDialog.Builder(c);
			txt = new EditText(this);
			txt.setText("Geo_"+map_name);
			geo_save.setView(txt);
			geo_save.setTitle("GeoPlayer Editor");
			geo_save.setMessage("Moechtest Du die GeoPlayerliste speichern?");
			geo_save.setPositiveButton("ja", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					geoplayer.save_geolist(txt.getText().toString(), geo_list_length);
					geo_edit_mode=false;
				}
			});
			geo_save.setNegativeButton("abbrechen", null);
			geo_save.show();
		}//if (v == map_slide_btn_geo_safe) {

		if (v == map_slide_btn_edit_safe) {
			map_slide_drawer.animateClose();
			final EditText safe_txt = new EditText(this);
			safe_txt.setText("new_"+map_name);
			final AlertDialog.Builder ed_menu = new AlertDialog.Builder(this);
			ed_menu.setView(safe_txt);
			ed_menu.setTitle("Speichern");
			ed_menu.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//Speicherroumtine
					String safe=String.valueOf(safe_txt.getText());
					try {
						File ed_meinspeicherort = c.getDir("archiv",MODE_PRIVATE);
						if (ed_meinspeicherort.canWrite() == true) {//Speicherort verfuegbar und Daten sind erhoben
							File file = new File(ed_meinspeicherort, safe+".txt");
							FileOutputStream fos = new FileOutputStream(file);
							OutputStreamWriter osw = new OutputStreamWriter(fos);
							short za = 0;
							String text="";
							text=kontainer_array2string(kontainer);
							//Einbetten des String in eine Textdatei
							osw.write(text);
							osw.flush();
							Toast.makeText(c, "Die editierte Strecke wurde gespeichert unter: "+safe, Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(c, "Die editierte Strecke konnte nicht am angegebenen Ort gespeichert werden.", Toast.LENGTH_SHORT).show();
						}//if (meinspeicherort.canWrite() == true && z!=0)
					} catch (IOException e) {
						e.printStackTrace();
						Toast.makeText(c, "Es gab leider einen Fehler in der Speicherroutine.", Toast.LENGTH_LONG).show();
					}//Try
					editor_mode=false;
				}
			});
			ed_menu.setNegativeButton("abbrechen", null);
			AlertDialog alert = ed_menu.create();
			alert.show();
		}//if (v == map_slide_btn_edit_safe) {

		if (v == btn_replay_last) {
			anz_enemy=mza_sel;
			mza_sel=0;
			lv_replay_flag=false;
			lv_replay.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv_replay.setVisibility(View.GONE);
					lv_replay_flag=false;
				}//run
			});//lv.post(new Runnable() {
			lv_replay.removeHeaderView(btn_replay_last);
		}//if (v == btn_replay_last) {

		if (v == btn_geocancle_last) {
			lv_geoplayer_flag=false;
			lv_replay.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv_replay.setVisibility(View.GONE);
				}//run
			});//lv.post(new Runnable() {
			geo_titel="kein Song vorhanden";//leer
			map.addMarker(new MarkerOptions()
					.position(akt_marker.getPosition())
					.title(geo_titel)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.no_m))
			);
			lv_replay.removeHeaderView(btn_geocancle_last);
			lv_replay.removeHeaderView(btn_geoplayer_zurueck);
		}//if (v == btn_geocancle_last) {

		if (v == btn_geoplayer_zurueck) {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String music_file = info.getString("music_location", "Music");
			File explor_focus = new File(music_file+music_ordner_plus);
			if (!explor_focus.getAbsolutePath().equals(music_file)) {
				File backup_file=explor_focus;
				try {
					explor_focus = new File(explor_focus.toString().substring(0,explor_focus.toString().lastIndexOf("/")));
					music_ordner_plus=music_ordner_plus.toString().substring(0,music_ordner_plus.toString().lastIndexOf("/"));
					music_ordner_plus=music_ordner_plus.toString().substring(0,music_ordner_plus.toString().lastIndexOf("/"));
					music_ordner_plus=music_ordner_plus+"/";
					String[] s = new String[explor_focus.list().length];
					s=explor_focus.list();
					geo_listeneintraege =new String[explor_focus.list().length];
					for (int n =0; n<explor_focus.list().length;n++) {
						geo_listeneintraege[n]=s[n];
					}//for (int n =0; n<explor_focus.list().length;n++) {

					ArrayAdapter geo_aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, geo_listeneintraege);
					lv_replay.setAdapter(geo_aa);
				} catch (Exception e1) {
					e1.printStackTrace();
					Toast.makeText(c, "oooops, soweit kannst du nicht zurueck.", Toast.LENGTH_SHORT).show();
					explor_focus=backup_file;
				}

			} else {
				music_ordner_plus="/";
				Toast.makeText(c, "Bitte bleibe in deinem Musikordner oder zeige auf einen anderen Ordner in deinen Einstellungen.", Toast.LENGTH_SHORT).show();
			}//if (!music_ordner_plus.equals(music_ordner_plus+"/"+test_ordner.getName()+"/")) {
		}//if (v == btn_geoplayer_zurueck) {

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView==geo_switch) {
			if (isChecked==true) {
				geo_edit_mode=true;
				mapeditor_switch.setEnabled(false);
				geoplayer = new Geoplayer(this);
				map_slide_btn_geo_safe.setEnabled(true);
				map_slide_btn_geo_safe.setTextColor(Color.WHITE);
			} else {
				geo_edit_mode=false;
				mapeditor_switch.setEnabled(true);
				geoplayer=new Geoplayer(this);
				map_slide_btn_geo_safe.setEnabled(false);
				map_slide_btn_geo_safe.setTextColor(Color.GRAY);
			}//if (isChecked==true) {
		}//if (buttonView==geo_switch) {

		if (buttonView==mapeditor_switch) {
			if (isChecked==true) {
				editor_mode=true;
				geo_switch.setEnabled(false);
				map_slide_btn_edit_safe.setEnabled(true);
				map_slide_btn_edit_safe.setTextColor(Color.WHITE);
			} else {
				editor_mode=false;
				geo_switch.setEnabled(true);
				map_slide_btn_edit_safe.setEnabled(false);
				map_slide_btn_edit_safe.setTextColor(Color.GRAY);
			}//if (isChecked==true) {
		}//if (buttonView==geo_switch) {
	}

	private void destroy_all_marker () {
		for (int n =0; n<perform_length+1; n++) {
			map_marker_update[n].cancel(true);
			map_marker_update[n]=null;
		}//for (int n =0; n<perform_length+1; n++) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if (history_flag==true) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.map_slide_button, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == R.id.map_slide_btn) {
			map_slide_drawer.animateOpen();
		}//if (item.getItemId() == R.id.hud_slide_item) {

		return super.onOptionsItemSelected(item);
	}

	private void ui_calculation_replay() {

		if (marker_hh.get(0)!=null) {

			try {
				thechallenge.set_pos(kontainer[0][simulations_nr], kontainer[1][simulations_nr],
						(int)kontainer[2][simulations_nr], (int)kontainer[3][simulations_nr], (int)kontainer[4][simulations_nr]);
			} catch(Exception e) {
				e.printStackTrace();
			}//try
			simulations_nr++;
			for (int nr=0;nr<thechallenge.getanz_spieler()+1;nr++) {
				//Umrechnen von Grad in KM - Trigonomie
				double lat1 =marker_hh.get(nr).getPosition().latitude;
				double long1 =marker_hh.get(nr).getPosition().longitude;
				double lat2 =thechallenge.akt_pos_spieler[nr][1][0];
				double long2=thechallenge.akt_pos_spieler[nr][0][0];

				double lat1km=(6371*Math.PI*lat1)/180;
				double long1km=((Math.cos(Math.toRadians(lat1))*6371)*long1*Math.PI)/180;
				double lat2km=(6371*Math.PI*lat2)/180;
				double long2km=((Math.cos(Math.toRadians(lat2))*6371)*long2*Math.PI)/180;

				double difflat=lat2km-lat1km;
				double difflong=long2km-long1km;

				double addlat =difflat/anz_animation;
				double addlong=difflong/anz_animation;

				for (int x =0; x<anz_animation; x++) {
					double latx =((lat1km+(x*addlat))*180)/(6371*Math.PI);
					double longx =((long1km+(x*addlong))*180)/((Math.cos(Math.toRadians(lat1))*6371)*Math.PI);
					animation_marker[nr][x][1]=latx;//lat
					animation_marker[nr][x][0]=longx;//long
				}//for (int x =0; x<30; x++) {
			}//for (int nr=0;nr<thechallenge.getanz_spieler()+1;nr++) {

			for (int ui_x=0; ui_x<thechallenge.getanz_spieler()+1; ui_x++) {
				if (ui_x==0) {
					marker_hh.put(0, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[0][1][0],thechallenge.akt_pos_spieler[0][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_0)));
				}
				if (ui_x==1) {
					marker_hh.put(1, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[1][1][0],thechallenge.akt_pos_spieler[1][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_1)));
				}
				if (ui_x==2) {
					marker_hh.put(2, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[2][1][0],thechallenge.akt_pos_spieler[2][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2)));
				}
				if (ui_x==3) {
					marker_hh.put(3, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[3][1][0],thechallenge.akt_pos_spieler[3][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_3)));
				}
				if (ui_x==4) {
					marker_hh.put(4, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[4][1][0],thechallenge.akt_pos_spieler[4][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_4)));
				}
				if (ui_x==5) {
					marker_hh.put(5, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[5][1][0],thechallenge.akt_pos_spieler[5][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_5)));
				}
				if (ui_x==6) {
					marker_hh.put(6, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[6][1][0],thechallenge.akt_pos_spieler[6][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_6)));
				}
				if (ui_x==7) {
					marker_hh.put(7, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[7][1][0],thechallenge.akt_pos_spieler[7][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_7)));
				}
				if (ui_x==8) {
					marker_hh.put(8, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[8][1][0],thechallenge.akt_pos_spieler[8][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_8)));
				}
				if (ui_x==9) {
					marker_hh.put(9, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[9][1][0],thechallenge.akt_pos_spieler[9][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_9)));
				}
				if (ui_x==10) {
					marker_hh.put(10, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[10][1][0],thechallenge.akt_pos_spieler[10][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_10)));
				}
				if (ui_x==11) {
					marker_hh.put(11, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[11][1][0],thechallenge.akt_pos_spieler[11][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_11)));
				}
				if (ui_x==12) {
					marker_hh.put(12, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[12][1][0],thechallenge.akt_pos_spieler[12][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_12)));
				}
				if (ui_x==13) {
					marker_hh.put(13, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[13][1][0],thechallenge.akt_pos_spieler[13][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_13)));
				}
				if (ui_x==14) {
					marker_hh.put(14, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[14][1][0],thechallenge.akt_pos_spieler[14][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_14)));
				}
				if (ui_x==15) {
					marker_hh.put(15, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[15][1][0],thechallenge.akt_pos_spieler[15][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_15)));
				}
				if (ui_x==16) {
					marker_hh.put(16, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[16][1][0],thechallenge.akt_pos_spieler[16][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_16)));
				}
				if (ui_x==17) {
					marker_hh.put(17, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[17][1][0],thechallenge.akt_pos_spieler[17][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_17)));
				}
				if (ui_x==18) {
					marker_hh.put(18, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[18][1][0],thechallenge.akt_pos_spieler[18][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_18)));
				}
				if (ui_x==19) {
					marker_hh.put(19, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[19][1][0],thechallenge.akt_pos_spieler[19][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_19)));
				}
			}//for (int ui_x=0; ui_x<thechallenge.getanz_spieler()+1; ui_x++) {

			//Zeichnung der Marker
		} else {
			for (int ui_x1=0; ui_x1<thechallenge.getanz_spieler()+1; ui_x1++) {
				if (ui_x1==0) {
					marker_hh.put(0, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[0][1][0],thechallenge.akt_pos_spieler[0][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_0)));
				}
				if (ui_x1==1) {
					marker_hh.put(1, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[1][1][0],thechallenge.akt_pos_spieler[1][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_1)));
				}
				if (ui_x1==2) {
					marker_hh.put(2, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[2][1][0],thechallenge.akt_pos_spieler[2][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_2)));
				}
				if (ui_x1==3) {
					marker_hh.put(3, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[3][1][0],thechallenge.akt_pos_spieler[3][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_3)));
				}
				if (ui_x1==4) {
					marker_hh.put(4, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[4][1][0],thechallenge.akt_pos_spieler[4][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_4)));
				}
				if (ui_x1==5) {
					marker_hh.put(5, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[5][1][0],thechallenge.akt_pos_spieler[5][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_5)));
				}
				if (ui_x1==6) {
					marker_hh.put(6, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[6][1][0],thechallenge.akt_pos_spieler[6][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_6)));
				}
				if (ui_x1==7) {
					marker_hh.put(7, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[7][1][0],thechallenge.akt_pos_spieler[7][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_7)));
				}
				if (ui_x1==8) {
					marker_hh.put(8, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[8][1][0],thechallenge.akt_pos_spieler[8][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_8)));
				}
				if (ui_x1==9) {
					marker_hh.put(9, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[9][1][0],thechallenge.akt_pos_spieler[9][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_9)));
				}
				if (ui_x1==10) {
					marker_hh.put(10, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[10][1][0],thechallenge.akt_pos_spieler[10][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_10)));
				}
				if (ui_x1==11) {
					marker_hh.put(11, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[11][1][0],thechallenge.akt_pos_spieler[11][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_11)));
				}
				if (ui_x1==12) {
					marker_hh.put(12, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[12][1][0],thechallenge.akt_pos_spieler[12][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_12)));
				}
				if (ui_x1==13) {
					marker_hh.put(13, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[13][1][0],thechallenge.akt_pos_spieler[13][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_13)));
				}
				if (ui_x1==14) {
					marker_hh.put(14, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[14][1][0],thechallenge.akt_pos_spieler[14][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_14)));
				}
				if (ui_x1==15) {
					marker_hh.put(15, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[15][1][0],thechallenge.akt_pos_spieler[15][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_15)));
				}
				if (ui_x1==16) {
					marker_hh.put(16, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[16][1][0],thechallenge.akt_pos_spieler[16][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_16)));
				}
				if (ui_x1==17) {
					marker_hh.put(17, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[17][1][0],thechallenge.akt_pos_spieler[17][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_17)));
				}
				if (ui_x1==18) {
					marker_hh.put(18, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[18][1][0],thechallenge.akt_pos_spieler[18][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_18)));
				}
				if (ui_x1==19) {
					marker_hh.put(19, new MarkerOptions()
							.position(new LatLng(thechallenge.akt_pos_spieler[19][1][0],thechallenge.akt_pos_spieler[19][0][0]))
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_19)));
				}
			}//for (int ui_x1=0; ui_x1<thechallenge.getanz_spieler()+1; ui_x1++) {
			simulations_nr++;
		}//if (marker_hh.get(0)!=null) {

	}//private void ui_calculation_replay() {

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (lv_geoplayer_flag==true || lv_replay_flag==true) {
			Toast.makeText(this, "Bitte verwende die Navigationsbuttons um abzubrechen.", Toast.LENGTH_LONG).show();
		}else {
			super.onBackPressed();
		}//if (lv_flag==true) {
	}

}
