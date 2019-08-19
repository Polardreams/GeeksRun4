package de.polardreams.geeksrun;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Timer;
import java.util.TimerTask;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Statistics extends Activity implements OnTouchListener, OnCheckedChangeListener {
	//GUI
	private RadioGroup radiogroup1, radiogroup2;
	private View myv;
	private GraphView graph;
	private RadioButton r1, r2, r3;
	private RadioButton ch1, ch2, ch3;
	//Touch
	private float array[][] = new float[2][99];
	//Hilfsvariable
	private int z;
	//Variablen
	private int z1, z2, t_touch;
	private double [][] kontainer=null;
	private String akt_kontainer ="";
	private int y, ges_zeile, teil_zeile;
	private double Strecke=0;
	private double hilfsstrecke =0;
	private double hilfszeit =0;
	private double zeit =0;
	private double grundumsatz =0;
	private double koerpergroesse;//in cm
	private double koerpergewicht;
	private double alter;
	private String geschlecht;
	private boolean energie_hoehenmeter;
	private short round=0;
	//Performancesteigerung
	private String[] perform_konverter = null;
	private int perform_length =0;
	private short perform_id =0;//Verarbeitungsgeschwindigkeit 29
	private int za_thread =0; //For Schleife f�r die Thread-Nummer
	private Handler[] map_marker_akt = null;//40 willk�hrlich festgelegt
	private short last_perform=0;
	private Boolean perform_ready = false;

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
		setContentView(R.layout.activity_statistics);
		lokalize_view = (ImageView)findViewById(R.id.imageView1);
		lokalize_view.setOnTouchListener(this);
		this.setTitle("Statistik");
		z=0;
		t_touch=0;
		radiogroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
		radiogroup1.check(R.id.radio4);
		radiogroup1.setOnCheckedChangeListener(this);
		r1=(RadioButton)findViewById(R.id.radio4);
		r2=(RadioButton)findViewById(R.id.radio5);
		r3=(RadioButton)findViewById(R.id.radio6);
		graph = (GraphView)findViewById(R.id.graph);
		radiogroup2 = (RadioGroup)findViewById(R.id.radioGroup2);
		radiogroup2.check(R.id.radio3);
		radiogroup2.setOnCheckedChangeListener(this);
		ch1 = (RadioButton)findViewById(R.id.radio1);
		ch2 = (RadioButton)findViewById(R.id.radio2);
		ch3 = (RadioButton)findViewById(R.id.radio3);

		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		r1.setTextColor(Color.WHITE);
		r2.setTextColor(Color.WHITE);
		r3.setTextColor(Color.WHITE);
		ch1.setTextColor(Color.WHITE);
		ch2.setTextColor(Color.WHITE);
		ch3.setTextColor(Color.WHITE);
		graph.setTitleColor(Color.WHITE);
		graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
		graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);

		//von Lap
		Intent intentvars = getIntent();
		if (intentvars.getStringExtra("hudtext")!=null) {
			akt_kontainer =intentvars.getStringExtra("hudtext");
		}//if (intentvars.getStringExtra("hudtext")!=null) {

		//String zu Array und Anzeige
		if (akt_kontainer.length() != 0 ) {
			kontainer_string2array(akt_kontainer);
		}//if (akt_kontainer != "" || akt_kontainer != null)

		//Preferenz
		try {
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			koerpergewicht=Integer.valueOf(info.getString("pers_weight", "70"));
			koerpergroesse=Integer.valueOf(info.getString("pers_height", "170"));
			alter=Integer.valueOf(info.getString("pers_age", "25"));
			geschlecht=info.getString("pers_sexx", "Mann");
			energie_hoehenmeter=info.getBoolean("hud_height_energie", true);
			if (geschlecht == "2") {
				grundumsatz=66.47+13.7*koerpergewicht+5*koerpergroesse-6.8*alter;
			} else {
				grundumsatz=655+9.6*koerpergewicht+1.8*koerpergroesse-4.7*alter;
			}//if (geschlecht == "Mann") {
		} catch (Exception e) {
			e.printStackTrace();
			koerpergewicht=70;
			koerpergroesse=170;
			alter=25;
			geschlecht="2";
			energie_hoehenmeter=true;
			if (geschlecht == "2") {
				grundumsatz=66.47+13.7*koerpergewicht+5*koerpergroesse-6.8*alter;
			} else {
				grundumsatz=655+9.6*koerpergewicht+1.8*koerpergroesse-4.7*alter;
			}//if (geschlecht == "Mann") {
		}//try
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (t_touch < 98) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				array[0][0]=(float) event.getX();
				array[1][0]=(float) event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				array[0][t_touch]=event.getX();
				array[1][t_touch]=event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				array[0][t_touch]=event.getX();
				array[1][t_touch]=event.getY();
				if (array[0][0] < array[0][t_touch]) {
					finish();//Lap
				}//if (array[0][0] < array[0][t_touch]) {
				if (array[0][0] > array[0][t_touch]) {}//if (array[0][0] > array[0][t_touch])
				t_touch=0;
				for (int n=0; n<t_touch; n++) {
					array[0][n]=0;
					array[1][n]=0;
				}//for (int n=0; n<t_touch; n++) {
			}//if (event.getAction() == MotionEvent.ACTION_UP) {
			t_touch++;
		} else {
			Toast.makeText(this, "Ich bin verwirrt, in welches Menu m�chtest du?", Toast.LENGTH_SHORT).show();
			t_touch=0;
		}//if (z_touch < 98) {
		return true;
	}//public boolean onTouch(View v, MotionEvent event) 

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		//String zu Array und Anzeige
		if (akt_kontainer.length() != 0 ) {
			stat_datenverarbeitung(checkedId);
		}//if (akt_kontainer != "" || akt_kontainer != null)
	}//public void onCheckedChanged(RadioGroup group, int checkedId) 

	public void kontainer_string2array(String var_string) {
		if (var_string != null || var_string=="") {
			//Perform_id Berechnen
			ges_zeile = 0;
			for (int x = 0; x<var_string.length(); x++){
				if (var_string.charAt(x)=='\n') {ges_zeile++;}
			}//for (int x = 0; x<var_string.length(); x++){
			//ges_zeile--;
			kontainer = new double[6][9000];
			//Performance
			if (ges_zeile>1000) {
				perform_id=(short) (ges_zeile*0.01);//Grad der Lestungssteigerung (bzw. Zuteiung der Zeilenverarbeitung f�r die einzelnen Handler)
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
			map_marker_akt = new Handler[index_arrays+10];

			teil_zeile =0;
			Boolean perform_flag=true;
			int m=0;
			while (perform_flag) {
				if (m != -1) {
					if (var_string.charAt(m) == '\n') {
						teil_zeile++;
						if (teil_zeile==perform_id) {
							perform_konverter[perform_length]=var_string.substring(0, m);
							var_string=var_string.substring(m+1, var_string.length());
							m=0;
							teil_zeile=0;
							perform_length++;
						} else {
							if (m==var_string.length()-1) {
								perform_konverter[perform_length]=var_string.substring(0, var_string.length());//letzten Teil in perform_konverter einf�gen
								last_perform=(short)teil_zeile;
								perform_flag=false;
							} else {
								if (var_string.substring(m+1,var_string.length()-1).indexOf('\n') == -1 ) {
									perform_konverter[perform_length]=var_string.substring(0, var_string.length());//letzten Teil in perform_konverter einf�gen
									last_perform=(short)teil_zeile;
									perform_flag=false;
								}//if (var_string.substring(m+1,var_string.length()-1).indexOf('\n') == -1 ) { 
							}//if (m==var_string.length()-1) {
						}//if (teil_zeile==perform_id) {...Zeilen z�hlen
					}//if (var_string.charAt(m) == '\n') { ... String mit Koordinaten in Teilabschnitte untergliedern 
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
						map_marker_akt[za_thread]=new Handler();
						map_marker_akt[za_thread].post(new new_runnable((short)za_thread) {} );
					}//for (int za_thread = 0; za_thread<30; za_thread++) {
				} else {
					for (za_thread = 0; za_thread<perform_length+1; za_thread++) {
						map_marker_akt[za_thread]=new Handler();
						map_marker_akt[za_thread].postDelayed(new new_runnable((short)za_thread) {}, 1000 );
					}//for (int za_thread = 0; za_thread<30; za_thread++) {
					/*
					 * vgl. bei Lap
					 */
				}//if (ges_zeile<1000) {
			}//if (ges_zeile<9000){
		}//if (var_string != null || var_string=="") {
	}//public void kontainer_array2string() {


	public void stat_datenverarbeitung(int id_check) {
		//Erm�glichung des restarts
		double [][][][][] stat_data_array =null;
		stat_data_array = new double [460][460][2][2][2];//nur f�r 0-98 Runden geeignet

		int rad1 = r1.getId();
		int rad2 = r2.getId();
		int rad3 = r3.getId();
		round=0;
		stat_data_array[round][0][0][0][0]=0;//m
		stat_data_array[round][0][1][0][0]=0;//Km/h
		stat_data_array[round][0][0][1][0]=0;//Joul
		stat_data_array[round][0][0][0][1]=kontainer[5][z1];//H�he in m
		stat_data_array[0][round][0][0][1]=kontainer[5][z1];//H�he in m
		round=1;
		z2=0;
		z1=1;
		double[] hoehenmeter =  new double[9000];
		Strecke=0;
		zeit =0;
		hilfsstrecke=0;
		hilfszeit=0;
		y=ges_zeile;
		y=((perform_length)*perform_id)+last_perform;
		for (int x =0; x<y;x++) {
			//Berechnung und Anzeige der gewonnen Daten
			if (z2>1 && z2<9000) { //GPS Suchkoords weglassen, wegen verf�lschter Ergebnisse
				double hilf1=0;
				double hilf2=0;
				double hilf3=0;
				//Strecke berechnen
				hilf1=(2*Math.PI*Math.cos(Math.toRadians(kontainer[1][z2]))*6371)/(2*Math.PI);//Die anderen Koordinaten m�ssen in der N�he sein
				hilf2=hilf1*Math.PI/180;
				hilf3=Math.sqrt(Math.pow(6371*Math.PI/180*(kontainer[1][z2-1]-kontainer[1][z2]),2)+
						Math.pow(hilf2*(kontainer[0][z2-1]-kontainer[0][z2]),2));
				hilfsstrecke = hilfsstrecke + (hilf3*1000);//Meterangaben f�r die Abfragen
				Strecke = Strecke + hilf3;//Eintrag in Array
				hilfszeit= hilfszeit+((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
						(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/60;//Minuten
				zeit =(kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])/60;
				//H�henmeter
				if (kontainer[5][z2-1]<kontainer[5][z2] && kontainer[5][z2]!= 0 && kontainer[5][z2-1]!=0) {
					//H�henmeterfilter
					double hoehelaengeverhaeltnis=(hilf3*1000)/(kontainer[5][z2]-kontainer[5][z2-1]);
					if (hoehelaengeverhaeltnis>=1) {
						hoehenmeter[z2]= hoehenmeter[z2-1] + (kontainer[5][z2]-kontainer[5][z2-1]);
					} else {
						//Fehlerverfahren ...
						hoehenmeter[z2]=hoehenmeter[z2-1];
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
					hoehenmeter[z2]=hoehenmeter[z2-1];
				}//if (kontainer[0][z1][0][0][0][1]<kontainer[0][z2][0][0][0][1]) {

			}//if (z2>1 && z2<9000) { 
			z2++;
			if (radiogroup1.getCheckedRadioButtonId() == rad1) {
				if (hilfsstrecke > 100) {//m
					stat_data_array[round][0][0][0][0]=Strecke*1000;//m

					stat_data_array[round][0][1][0][0]=(hilfsstrecke/1000)/(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//Km/h

					//Energieberechnung
					if (energie_hoehenmeter == true) {
						double hilfe1 =((hilfsstrecke/1000)+(((hoehenmeter[z2-1]-hoehenmeter[z1])*7)/1000));
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						stat_data_array[round][0][0][1][0]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);
					} else {
						double hilfe1 =(hilfsstrecke/1000);
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						stat_data_array[round][0][0][1][0]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);
					}//if (energie_hoehenmeter = true) {

					//GPS-H�he mit Ausfallsystem
					if (kontainer[5][z2] == 0) {
						boolean flag_find_hoehe = true;
						int find_hoehe = 0;
						while (flag_find_hoehe) {
							if (z2+find_hoehe+1<ges_zeile) {
								if (kontainer[5][z2+find_hoehe] != 0) {
									stat_data_array[round][0][0][0][1]=kontainer[5][z2+find_hoehe];//H�he in m
									flag_find_hoehe=false;
								}//if (kontainer[0][z2+find_hoehe][0][0][0][1] != 0 && find_hoehe+z2<9000) {
								find_hoehe++;
							} else {
								flag_find_hoehe=false;
								stat_data_array[round][0][0][0][1]=kontainer[5][z2];
							}//if (z2+find_hoehe<ges_zeile) {
						}//while (flag_find_hoehe) {
					} else {
						stat_data_array[round][0][0][0][1]=kontainer[5][z2];//H�he in m
					}//if (kontainer[0][z2][0][0][0][1] == 0) {

					hilfsstrecke=0;
					z1=z2;
					round++;
				}//if (hilfsstrecke > 100)
			}//if (id_check == rad1) {

			if (radiogroup1.getCheckedRadioButtonId() == rad2) {
				if (hilfsstrecke > 1000) {//1km
					stat_data_array[round][0][0][0][0]=Strecke;//m

					stat_data_array[round][0][1][0][0]=(hilfsstrecke/1000)/(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//Km/h

					//Energieberechnung
					if (energie_hoehenmeter == true) {
						double hilfe1 =((hilfsstrecke/1000)+(((hoehenmeter[z2-1]-hoehenmeter[z1])*7)/1000));
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						stat_data_array[round][0][0][1][0]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);
					} else {
						double hilfe1 =(hilfsstrecke/1000);
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						stat_data_array[round][0][0][1][0]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);
					}//if (energie_hoehenmeter = true) {

					//GPS-H�he mit Ausfallsystem
					if (kontainer[5][z2] == 0) {
						boolean flag_find_hoehe = true;
						int find_hoehe = 0;
						while (flag_find_hoehe) {
							if (z2+find_hoehe+1<ges_zeile) {
								if (kontainer[5][z2+find_hoehe] != 0) {
									stat_data_array[round][0][0][0][1]=kontainer[5][z2+find_hoehe];//H�he in m
									flag_find_hoehe=false;
								}//if (kontainer[0][z2+find_hoehe][0][0][0][1] != 0 && find_hoehe+z2<9000) {
								find_hoehe++;
							} else {
								flag_find_hoehe=false;
								stat_data_array[round][0][0][0][1]=kontainer[5][z2];
							}//if (z2+find_hoehe<ges_zeile) {
						}//while (flag_find_hoehe) {
					} else {
						stat_data_array[round][0][0][0][1]=kontainer[5][z2];//H�he in m
					}//if (kontainer[0][z2][0][0][0][1] == 0) {

					hilfsstrecke=0;
					z1=z2;
					round++;
				}//if (hilfsstrecke > 1000)
			}//if (id_check == rad1) {

			if (radiogroup1.getCheckedRadioButtonId() == rad3) {
				if (hilfszeit > 10) {//10 minute
					stat_data_array[0][round][0][0][0]=zeit;

					stat_data_array[0][round][1][0][0]=(hilfsstrecke/1000)/(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//Km/h

					//Energieberechnung
					if (energie_hoehenmeter == true) {
						double hilfe1 =((hilfsstrecke/1000)+(((hoehenmeter[z2-1]-hoehenmeter[z1])*7)/1000));
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						stat_data_array[0][round][0][1][0]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);
					} else {
						double hilfe1 =(hilfsstrecke/1000);
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						stat_data_array[0][round][0][1][0]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);
					}//if (energie_hoehenmeter == true) {

					//GPS-H�he mit Ausfallsystem
					if (kontainer[5][z2] == 0) {
						boolean flag_find_hoehe = true;
						int find_hoehe = 0;
						while (flag_find_hoehe) {
							if (z2+find_hoehe+1<ges_zeile) {
								if (kontainer[5][z2+find_hoehe]!= 0) {
									stat_data_array[0][round][0][0][1]=kontainer[5][z2+find_hoehe];//H�he in m
									flag_find_hoehe=false;
								}//if (kontainer[0][z2+find_hoehe][0][0][0][1] != 0 && find_hoehe+z2<9000) {
								find_hoehe++;
							} else {
								flag_find_hoehe=false;
								stat_data_array[round][0][0][0][1]=kontainer[5][z2];
							}//if (z2+find_hoehe<ges_zeile) {
						}//while (flag_find_hoehe) {
					} else {
						stat_data_array[0][round][0][0][1]=kontainer[5][z2];//H�he in m
					}//if (kontainer[0][z2][0][0][0][1] == 0) {

					hilfszeit=0;
					hilfsstrecke=0;
					z1=z2;
					round++;
				}//if (hilfszeit > 10) 
			}//if (id_check == rad1) {
		}//for (int x =0; x<y;x++) {

		DataPoint stat_data_geschw[] = new DataPoint[round];
		DataPoint stat_data_energie[] = new DataPoint[round];
		DataPoint stat_data_hoehe[] = new DataPoint[round];

		DecimalFormat statistic_format = new DecimalFormat("0.0");
		DecimalFormatSymbols symbols_format = new DecimalFormatSymbols();
		symbols_format.setDecimalSeparator('.');
		statistic_format.setDecimalFormatSymbols(symbols_format);


		for (int n=0; n<round;n++) {
			if (radiogroup1.getCheckedRadioButtonId()==rad1) {
				stat_data_geschw[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[n][0][1][0][0])));
				stat_data_energie[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[n][0][0][1][0])));
				stat_data_hoehe[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][1])));
			}//if (id_check==rad1 || id_check == rad2) {

			if (radiogroup1.getCheckedRadioButtonId() == rad2) {
				stat_data_geschw[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[n][0][1][0][0])));
				stat_data_energie[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[n][0][0][1][0])));
				stat_data_hoehe[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[n][0][0][0][1])));
			}//if (id_check==rad1 || id_check == rad2) {

			if (radiogroup1.getCheckedRadioButtonId() ==rad3) {
				stat_data_geschw[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[0][n][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[0][n][1][0][0])));
				stat_data_energie[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[0][n][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[0][n][0][1][0])));
				stat_data_hoehe[n] = new DataPoint(Double.valueOf(statistic_format.format(stat_data_array[0][n][0][0][0])),Double.valueOf(statistic_format.format(stat_data_array[0][n][0][0][1])));
			}//if (id_check==rad3) {
		}//for (int n=0; n<round;n++) 


		//Statistic formatieren
		int rad4 = ch1.getId();
		int rad5 = ch2.getId();
		int rad6 = ch3.getId();
		double min=0;
		double max=0;
		graph.getViewport().setXAxisBoundsManual(true);
		graph.getViewport().setYAxisBoundsManual(true);

		if (radiogroup1.getCheckedRadioButtonId()==rad1 || radiogroup1.getCheckedRadioButtonId() == rad2) {
			graph.getViewport().setMaxX(stat_data_array[round-1][0][0][0][0]);
			graph.getViewport().setMinX(stat_data_array[1][0][0][0][0]);
			if (radiogroup2.getCheckedRadioButtonId() == rad4) {
				for (int m=0; m<round;m++) {
					if (stat_data_array[m][0][1][0][0]>max) {
						max=stat_data_array[m][0][1][0][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				min=max;
				for (int m=0; m<round;m++) {
					if (stat_data_array[m][0][1][0][0]<min && stat_data_array[m][0][1][0][0]!=0) {
						min=stat_data_array[m][0][1][0][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				graph.getViewport().setMaxY(max);
				graph.getViewport().setMinY(min);
				max=0;
				min=0;
			}//if (radiogroup2.getCheckedRadioButtonId() == rad4) {
			if (radiogroup2.getCheckedRadioButtonId() == rad5) {
				for (int m=0; m<round;m++) {
					if (stat_data_array[m][0][0][1][0]>max) {
						max=stat_data_array[m][0][0][1][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				min=max;
				for (int m=0; m<round;m++) {
					if (stat_data_array[m][0][0][1][0]<min && stat_data_array[m][0][0][1][0]!=0) {
						min=stat_data_array[m][0][0][1][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				graph.getViewport().setMaxY(max);
				graph.getViewport().setMinY(min);
				max=0;
				min=0;
			}//if (radiogroup2.getCheckedRadioButtonId() == rad5) {

			if (radiogroup2.getCheckedRadioButtonId() == rad6) {
				for (int m=0; m<round;m++) {
					if (stat_data_array[m][0][0][0][1]>max) {
						max=stat_data_array[m][0][0][0][1];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				min=max;
				for (int m=0; m<round;m++) {
					if (stat_data_array[m][0][0][0][1]<min && stat_data_array[m][0][0][0][1]!=0) {
						min=stat_data_array[m][0][0][0][1];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				graph.getViewport().setMaxY(max);
				graph.getViewport().setMinY(min);
				max=0;
				min=0;
			}//if (radiogroup2.getCheckedRadioButtonId() == rad6) {
		}//if (radiogroup1.getCheckedRadioButtonId()==rad1 || radiogroup1.getCheckedRadioButtonId() == rad2) {

		if (radiogroup1.getCheckedRadioButtonId() ==rad3) {
			graph.getViewport().setMaxX((stat_data_array[0][round-1][0][0][0]));
			graph.getViewport().setMinX(stat_data_array[0][1][0][0][0]);

			if (radiogroup2.getCheckedRadioButtonId() == rad4) {
				for (int m=0; m<round;m++) {
					if (stat_data_array[0][m][1][0][0]>max) {
						max=stat_data_array[0][m][1][0][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				min=max;
				for (int m=0; m<round;m++) {
					if (stat_data_array[0][m][1][0][0]<min && stat_data_array[0][m][1][0][0]!=0) {
						min=stat_data_array[0][m][1][0][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				graph.getViewport().setMaxY(max);
				graph.getViewport().setMinY(min);
				max=0;
				min=0;
			}//if (radiogroup2.getCheckedRadioButtonId() == rad4) {
			if (radiogroup2.getCheckedRadioButtonId() == rad5) {
				for (int m=0; m<round;m++) {
					if (stat_data_array[0][m][0][1][0]>max) {
						max=stat_data_array[0][m][0][1][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				min=max;
				for (int m=0; m<round;m++) {
					if (stat_data_array[0][m][0][1][0]<min && stat_data_array[0][m][0][1][0]!=0) {
						min=stat_data_array[0][m][0][1][0];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				graph.getViewport().setMaxY(max);
				graph.getViewport().setMinY(min);
				max=0;
				min=0;
			}//if (radiogroup2.getCheckedRadioButtonId() == rad5) {

			if (radiogroup2.getCheckedRadioButtonId() == rad6) {
				for (int m=0; m<round;m++) {
					if (stat_data_array[0][m][0][0][1]>max) {
						max=stat_data_array[0][m][0][0][1];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				min=max;
				for (int m=0; m<round;m++) {
					if (stat_data_array[0][m][0][0][1]<min && stat_data_array[0][m][0][0][1]!=0) {
						min=stat_data_array[0][m][0][0][1];
					}//if (stat_data_array[m][0][1][0][0]>max) {
				}//for (int m=0; m<round;m++) {
				graph.getViewport().setMaxY(max);
				graph.getViewport().setMinY(min);
				max=0;
				min=0;
			}//if (radiogroup2.getCheckedRadioButtonId() == rad6) {
		}//if (radiogroup1.getCheckedRadioButtonId() ==rad3) {

		LineGraphSeries<DataPoint> dat_geschw = new LineGraphSeries<DataPoint>(stat_data_geschw);
		dat_geschw.setColor(Color.RED);
		dat_geschw.setTitle("Geschwindigkeit");
		LineGraphSeries<DataPoint> dat_energie = new LineGraphSeries<DataPoint>(stat_data_energie);
		dat_energie.setColor(Color.YELLOW);
		dat_energie.setTitle("Energieverbrauch in kcal");
		LineGraphSeries<DataPoint> dat_hoehe = new LineGraphSeries<DataPoint>(stat_data_hoehe);
		dat_hoehe.setColor(Color.BLUE);
		dat_hoehe.setTitle("H�he");
		graph.removeAllSeries();
		graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(statistic_format,statistic_format));
		try {
			if (radiogroup2.getCheckedRadioButtonId() == rad4) {
				graph.addSeries(dat_geschw);
				graph.setTitle("Geschwindigkeit in km/h");
			}//if (radiogroup2.getCheckedRadioButtonId() == rad4) {
			if (radiogroup2.getCheckedRadioButtonId() == rad5) {
				graph.addSeries(dat_energie);
				if (energie_hoehenmeter == true) {
					graph.setTitle("phy. Energie und H�henmeter in kcal");
				} else {
					graph.setTitle("phy. Energie in kcal");
				}//if (energie_hoehenmeter == true) {
			}//if (radiogroup2.getCheckedRadioButtonId() == rad5) {
			if (radiogroup2.getCheckedRadioButtonId() == rad6) {
				graph.addSeries(dat_hoehe);
				graph.setTitle("H�he in m");
			}//if (radiogroup2.getCheckedRadioButtonId() == rad6) {
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Die berechneten Daten k�nnen nicht angezeigt werden.", Toast.LENGTH_LONG).show();
		}//try
	}//public void lap_datenverarbeitung() {


	class new_runnable implements Runnable {
		short var_za;
		short var_performid;

		new_runnable(short var) {
			var_za = var;
			var_performid = var;
		}//Constructor

		@Override
		public void run() {
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
						perform_ready =true;
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
			} catch(Exception e) {
				e.printStackTrace();
				Toast.makeText(Statistics.this, "Daten k�nnen nicht berechnet werden.", Toast.LENGTH_LONG).show();
			}//Try
			if (perform_ready == true && perform_length == var_za) {
				stat_datenverarbeitung(radiogroup1.getCheckedRadioButtonId());
			}//if (perform_ready == true && perform_length == var_za) {
			//Last Runnable
			if (za_thread-1  == var_za) {
				//Ladeprozess zuende
			}//if (za_thread-1  == var_za) {
		}//run
	}//class new_runnable implements Runnable {

}
