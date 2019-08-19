package de.polardreams.geeksrun;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Lap extends Activity implements OnTouchListener, OnCheckedChangeListener  {
	//GUI
	private View myv;
	private RadioGroup radiogroup1;
	private RadioButton r1, r2, r3, r4;
	//Touch
	private float array[][] = new float[2][99];
	//Tabelle
	private LinearLayout sp1, sp2, sp3, sp4;
	private EditText[] ed = null;
	//Variablen
	private int z, z1, z2, z_touch;
	private double[][] kontainer = null;


	private String akt_kontainer ="";
	private int y, ges_zeile, teil_zeile;
	private double Strecke=0;
	private double hilfsstrecke =0;
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
	private Handler[] map_marker_akt = null;
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
		setContentView(R.layout.activity_lap);
		this.setTitle("Runden");
		z=0;
		z_touch=0;
		lokalize_view = (ImageView)findViewById(R.id.imageView1);
		lokalize_view.setOnTouchListener(this);

		radiogroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
		radiogroup1.check(R.id.radio4);
		radiogroup1.setOnCheckedChangeListener(this);
		r1=(RadioButton)findViewById(R.id.radio4);
		r1.setTextColor(Color.WHITE);
		r2=(RadioButton)findViewById(R.id.radio5);
		r2.setTextColor(Color.WHITE);
		r3=(RadioButton)findViewById(R.id.radio6);
		r3.setTextColor(Color.WHITE);
		r4=(RadioButton)findViewById(R.id.radio3);
		r4.setTextColor(Color.WHITE);

		Intent intentvars = getIntent();
		if (intentvars.getStringExtra("hudtext")!=null) {
			akt_kontainer =intentvars.getStringExtra("hudtext");
		}//if (intentvars.getStringExtra("hudtext")!=null) {
		//von History
		if (intentvars.getStringExtra("historytext")!=null) {
			akt_kontainer =intentvars.getStringExtra("historytext");
		}//if (intentvars.getStringExtra("historytext")!=null) {
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		//String zu Array und Anzeige
		sp1 = (LinearLayout)findViewById(R.id.Spalte1);
		sp2 = (LinearLayout)findViewById(R.id.Spalte2);
		sp3 = (LinearLayout)findViewById(R.id.Spalte3);
		sp4 = (LinearLayout)findViewById(R.id.Spalte4);

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
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Die Einstellungen konnten leiden nicht geladen werden. Lap l�uft auf Werkseinstellungen.", Toast.LENGTH_LONG).show();
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
		if (z_touch < 98) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				array[0][0]=(float) event.getX();
				array[1][0]=(float) event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_DOWN) {

			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				array[0][z_touch]=event.getX();
				array[1][z_touch]=event.getY();
			}//if (event.getAction() == MotionEvent.ACTION_MOVE) {

			if (event.getAction() == MotionEvent.ACTION_UP) {
				array[0][z_touch]=event.getX();
				array[1][z_touch]=event.getY();
				if (array[0][0] < array[0][z_touch]) {
					finish();//Map
				}//if (array[0][0] < array[0][z_touch]) {
				if (array[0][0] > array[0][z_touch]) {



					Intent i = new Intent(this, Statistics.class);
					String hudtext= akt_kontainer;
					if (hudtext != null) {
						i.putExtra("hudtext", hudtext);
					}//if (hudtext != null) {
					startActivity(i);
					//finish();
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
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		//String zu Array und Anzeige
		if (akt_kontainer.length() != 0 ) {
			lap_datenverarbeitung(checkedId);
		}//if (akt_kontainer != "" || akt_kontainer != null)
	}//public void onCheckedChanged(RadioGroup group, int checkedId) {

	/*
	 * Methoden
	 *
	 * Die Methoden m�ssen auf Grund ihres Auswertungscharakter nicht weiter
	 * gesichert werden mit Try-Catch, weil diese in den vorherigen Activities
	 * bereit berechnet wurden
	 */

	public void kontainer_string2array(String var_string) {
		if (var_string != null || var_string=="") {
			//Perform_id Berechnen
			ges_zeile = 0;
			for (int x = 0; x<var_string.length(); x++){
				if (var_string.charAt(x)=='\n') {ges_zeile++;}
			}//for (int x = 0; x<var_string.length(); x++){
			kontainer=new double [6][ges_zeile+10];
			//Performance
			if (ges_zeile>1000) {
				perform_id=(short) (ges_zeile/350);
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
						}//if (teil_zeile==perform_id) { ... Zeilen z�hlen
					}//if (var_string.charAt(m) == '\n') {
					/*String mit Koordinaten in Teilabschnitte untergliedern*/
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
					 * siehe Kommentar in Map zu diesem Thema progress
					 */
				}//if (ges_zeile<1000) {
			}//If y<9000
		}//if (var_string != null || var_string=="") {
	}//public void kontainer_array2string() {


	public void lap_datenverarbeitung(int id_check) {
		//Erm�glichung des restarts
		sp1.removeAllViews();
		sp2.removeAllViews();
		sp3.removeAllViews();
		sp4.removeAllViews();
		ed = null;
		ed = new EditText[352];
		double [][] lap_daten_array =null;
		lap_daten_array = new double [99][4];//nur f�r 0-98 Runden geeignet
		double[] hoehenmeter =  new double[9000];
		round=0;
		z2=0;
		z1=1;
		Strecke=0;
		hilfsstrecke=0;

		y=((perform_length)*perform_id)+last_perform;
		for (int x =0; x<y;x++) {
			//Berechnung und Anzeige der gewonnen Daten
			if (z2>1) { //GPS Suchkoords weglassen, wegen verf�lschter Ergebnisse
				double hilf1=0;
				double hilf2=0;
				double hilf3=0;
				//Strecke berechnen
				hilf1=(2*Math.PI*Math.cos(Math.toRadians(kontainer[1][z2]))*6371)/(2*Math.PI);//Die anderen Koordinaten m�ssen in der N�he sein
				hilf2=hilf1*Math.PI/180;
				hilf3=Math.sqrt(Math.pow(6371*Math.PI/180*(kontainer[1][z2-1]-kontainer[1][z2]),2)+
						Math.pow(hilf2*(kontainer[0][z2-1]-kontainer[0][z2]),2));
				Strecke = Strecke + hilf3;//Kilometerangabe
				hilfsstrecke = hilfsstrecke + hilf3;

				//H�henmeter
				if (kontainer[5][z2-1]<kontainer[5][z2] && kontainer[5][z2]!= 0 && kontainer[5][z2-1]!=0) {
					//H�henmeterfilter
					double hoehelaengeverhaeltnis=(hilf3*1000)/(kontainer[5][z2]-kontainer[5][z2-1]);
					if (hoehelaengeverhaeltnis>1) {
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

			}//if (z2>1) 
			z2++;
			int rad1 = r1.getId();
			int rad2 = r2.getId();
			int rad3 = r3.getId();
			int rad4 = r4.getId();

			if (id_check == rad1) {
				if (hilfsstrecke > 0.5) {
					lap_daten_array[round][0]=Strecke;//Km

					lap_daten_array[round][1]=((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/60;//Minuten

					lap_daten_array[round][2]=hilfsstrecke/(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//Km/h
					//Energieberechnung
					//http://www.klinikschoensicht.de/InfoWeb/erna2.htm
					if (energie_hoehenmeter == true) {
						double hilfe1 =(hilfsstrecke)+(((hoehenmeter[z2-1]-hoehenmeter[z1])/1000)*7);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					} else {
						double hilfe1 =(hilfsstrecke);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					}//if (energie_hoehenmeter == true) {
					hilfsstrecke=0;
					z1=z2;
					round++;
				}//if (hilfsstrecke > 0.5) {
			}//if (id_check == rad1) {

			if (id_check == rad2) {
				if (hilfsstrecke > 1) {
					lap_daten_array[round][0]=Strecke;//Km

					lap_daten_array[round][1]=((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/60;//Minuten

					lap_daten_array[round][2]=hilfsstrecke/(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//Km/h
					//Energieberechnung
					//http://www.klinikschoensicht.de/InfoWeb/erna2.htm
					if (energie_hoehenmeter == true) {
						double hilfe1 =(hilfsstrecke)+(((hoehenmeter[z2-1]-hoehenmeter[z1])/1000)*7);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					} else {
						double hilfe1 =(hilfsstrecke);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					}//if (energie_hoehenmeter == true) {

					/*
					 * Berechnen der kcal ohne H�henmeter
					 * lap_daten_array[round][3]=(((grundumsatz/24)*((lap_daten_array[round][2])*1.05))
					 * -(grundumsatz/24))*(lap_daten_array[round][1]/60);
					 */

					hilfsstrecke=0;
					z1=z2;
					round++;
				}//if (hilfsstrecke > 1) {
			}//if (id_check == rad2) {	

			if (id_check == rad3) {
				if (hilfsstrecke > 2) {
					lap_daten_array[round][0]=Strecke;//Km

					lap_daten_array[round][1]=((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/60;//Minuten

					lap_daten_array[round][2]=hilfsstrecke/(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//Km/h
					//Energieberechnung
					if (energie_hoehenmeter == true) {
						double hilfe1 =(hilfsstrecke)+(((hoehenmeter[z2-1]-hoehenmeter[z1])/1000)*7);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					} else {
						double hilfe1 =(hilfsstrecke);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					}//if (energie_hoehenmeter == true) {
					hilfsstrecke=0;
					z1=z2;
					round++;
				}//if (hilfsstrecke > 2)
			}//if (id_check == rad3) {

			if (id_check == rad4) {
				if (hilfsstrecke > 5) {
					lap_daten_array[round][0]=Strecke;//Km

					lap_daten_array[round][1]=((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/60;//Minuten

					lap_daten_array[round][2]=hilfsstrecke/(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
							(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//Km/h
					//Energieberechnung
					if (energie_hoehenmeter == true) {
						double hilfe1 =(hilfsstrecke)+(((hoehenmeter[z2-1]-hoehenmeter[z1])/1000)*7);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					} else {
						double hilfe1 =(hilfsstrecke);//km
						double hilfe2 =(((kontainer[2][z2]*3600+kontainer[3][z2]*60+kontainer[4][z2])-
								(kontainer[2][z1]*3600+kontainer[3][z1]*60+kontainer[4][z1]))/3600);//h
						lap_daten_array[round][3]=(((grundumsatz/24)*((hilfe1/hilfe2)*1.05))-(grundumsatz/24))*(hilfe2);
					}//if (energie_hoehenmeter == true) {
					hilfsstrecke=0;
					z1=z2;
					round++;
				}//if (hilfsstrecke > 5) 
			}//if (id_check == rad4) {
		}//for (int x =0; x<y;x++) {

		//GUI - Tabelle
		ed[0]=new EditText(this);
		ed[0].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ed[0].setText("Km___");
		ed[0].setFocusable(false);
		ed[0].setTextSize(12);
		ed[0].setTextColor(Color.WHITE);
		sp1.addView(ed[0]);

		ed[1]=new EditText(this);
		ed[1].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ed[1].setText("Minuten");
		ed[1].setFocusable(false);
		ed[1].setTextSize(12);
		ed[1].setTextColor(Color.WHITE);
		sp2.addView(ed[1]);

		ed[2]=new EditText(this);
		ed[2].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ed[2].setText("m. Km/h");
		ed[2].setFocusable(false);
		ed[2].setTextSize(12);
		ed[2].setTextColor(Color.WHITE);
		sp3.addView(ed[2]);

		ed[3]=new EditText(this);
		ed[3].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ed[3].setText("kcal mit H�henmeter");
		ed[3].setFocusable(false);
		ed[3].setTextSize(12);
		ed[3].setTextColor(Color.WHITE);
		sp4.addView(ed[3]);
		z=4;
		for (int n=0; n<4; n++) {
			for (int m=0; m<round;m++) {//je nach Datenlage
				switch(n) {
					case 0:
						ed[z]=new EditText(this);
						ed[z].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
						ed[z].setText(String.format("%.2f", lap_daten_array[m][n]));
						ed[z].setFocusable(false);
						ed[z].setTextSize(12);
						ed[z].setTextColor(Color.WHITE);
						ed[z].setBackgroundResource(R.drawable.lap_tabelle);
						sp1.addView(ed[z]);
						break;
					case 1:
						ed[z]=new EditText(this);
						ed[z].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
						ed[z].setText(String.format("%.2f", lap_daten_array[m][n]));
						ed[z].setFocusable(false);
						ed[z].setTextSize(12);
						ed[z].setTextColor(Color.WHITE);
						ed[z].setBackgroundResource(R.drawable.lap_tabelle);
						sp2.addView(ed[z]);
						break;
					case 2:
						ed[z]=new EditText(this);
						ed[z].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
						ed[z].setText(String.format("%.2f", lap_daten_array[m][n]));
						ed[z].setFocusable(false);
						ed[z].setTextSize(12);
						ed[z].setTextColor(Color.WHITE);
						ed[z].setBackgroundResource(R.drawable.lap_tabelle);
						sp3.addView(ed[z]);
						break;
					case 3:
						ed[z]=new EditText(this);
						ed[z].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
						ed[z].setText(String.format("%.2f", lap_daten_array[m][n]));
						ed[z].setFocusable(false);
						ed[z].setTextSize(12);
						ed[z].setTextColor(Color.WHITE);
						ed[z].setBackgroundResource(R.drawable.lap_tabelle);
						sp4.addView(ed[z]);
						break;
				}//Switch
				z++;
			}//for (int m=0; n<4;m++) {
		}//for (int n=0; n<5; n++) {

		//Zeile der Durschnittsangaben
		ed[z+1]=new EditText(this);
		ed[z+1].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+1].setText(String.valueOf("g. Km"));
		ed[z+1].setFocusable(false);
		ed[z+1].setTextSize(12);
		ed[z+1].setTextColor(Color.WHITE);
		sp1.addView(ed[z+1]);
		ed[z+2]=new EditText(this);
		ed[z+2].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+2].setText(String.valueOf("g. Min"));
		ed[z+2].setFocusable(false);
		ed[z+2].setTextSize(12);
		ed[z+2].setTextColor(Color.WHITE);
		sp2.addView(ed[z+2]);
		ed[z+3]=new EditText(this);
		ed[z+3].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+3].setText(String.valueOf("g. Km/h"));
		ed[z+3].setFocusable(false);
		ed[z+3].setTextSize(12);
		ed[z+3].setTextColor(Color.WHITE);
		sp3.addView(ed[z+3]);
		ed[z+4]=new EditText(this);
		ed[z+4].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+4].setText(String.valueOf("g. kcal"));
		ed[z+4].setFocusable(false);
		ed[z+4].setTextSize(12);
		ed[z+4].setTextColor(Color.WHITE);
		sp4.addView(ed[z+4]);
		//Durchschnittsangaben
		//Strecke
		ed[z+5]=new EditText(this);
		ed[z+5].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+5].setText(String.format("%.2f", Strecke));
		ed[z+5].setFocusable(false);
		ed[z+5].setTextSize(12);
		ed[z+5].setTextColor(Color.WHITE);
		ed[z+5].setBackgroundResource(R.drawable.lap_tabelle);
		sp1.addView(ed[z+5]);
		//durchn. Zeit
		ed[z+6]=new EditText(this);
		ed[z+6].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+6].setText(String.format("%.2f", (kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/60));
		ed[z+6].setFocusable(false);
		ed[z+6].setTextSize(12);
		ed[z+6].setTextColor(Color.WHITE);
		ed[z+6].setBackgroundResource(R.drawable.lap_tabelle);
		sp2.addView(ed[z+6]);
		//durchschn. geschwindigkeit
		ed[z+7]=new EditText(this);
		ed[z+7].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+7].setText(String.format("%.2f", Strecke / ((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600) ));
		ed[z+7].setFocusable(false);
		ed[z+7].setTextSize(12);
		ed[z+7].setTextColor(Color.WHITE);
		ed[z+7].setBackgroundResource(R.drawable.lap_tabelle);
		sp3.addView(ed[z+7]);
		ed[z+8]=new EditText(this);
		ed[z+8].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		//Kilocalorien
		//ohne H�henmeter
		ed[z+8].setText(String.format("%.2f", lap_daten_array[round][3]=(((grundumsatz/24)*((Strecke / ((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600))*1.05))-(grundumsatz/24))*( (kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600)));
		if (energie_hoehenmeter == true) {
			ed[z+8].setText(String.format("%.2f", (((grundumsatz/24)*(((Strecke+(((hoehenmeter[z2-1])*7)/1000)) / ((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600))*1.05))-(grundumsatz/24))*((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600)));
		} else {
			ed[z+8].setText(String.format("%.2f", (((grundumsatz/24)*(((Strecke) / ((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600))*1.05))-(grundumsatz/24))*((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600)));
		}//if (energie_hoehenmeter == true) {
		ed[z+8].setFocusable(false);
		ed[z+8].setTextSize(12);
		ed[z+8].setTextColor(Color.WHITE);
		ed[z+8].setBackgroundResource(R.drawable.lap_tabelle);
		sp4.addView(ed[z+8]);
		//H�henmeter
		ed[z+9]=new EditText(this);
		ed[z+9].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+9].setText(String.format("%.2f", hoehenmeter[z2-1]));
		ed[z+9].setFocusable(false);
		ed[z+9].setTextSize(12);
		ed[z+9].setTextColor(Color.WHITE);
		ed[z+9].setBackgroundResource(R.drawable.lap_tabelle);
		sp4.addView(ed[z+9]);
		ed[z+10]=new EditText(this);
		ed[z+10].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+10].setText("H�he(m):");
		ed[z+10].setFocusable(false);
		ed[z+10].setTextSize(12);
		ed[z+10].setTextColor(Color.WHITE);
		ed[z+10].setBackgroundResource(R.drawable.lap_tabelle);
		sp3.addView(ed[z+10]);
		//kcal pauschal in g (Energie in Masse)
		ed[z+11]=new EditText(this);
		ed[z+11].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		if (energie_hoehenmeter == true) {
			ed[z+11].setText(String.format("%.2f", ((((grundumsatz/24)*(((Strecke+(((hoehenmeter[z2-1])*7)/1000)) / ((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600))*1.05))-(grundumsatz/24))*((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600))/9.3));//http://www.tabelle.info/kalorien_9.html
		} else {
			ed[z+11].setText(String.format("%.2f", ((((grundumsatz/24)*(((Strecke) / ((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600))*1.05))-(grundumsatz/24))*((kontainer[2][z2-1]*3600+kontainer[3][z2-1]*60+kontainer[4][z2-1])/3600))/9.3));//http://www.tabelle.info/kalorien_9.html	
		}//if (energie_hoehenmeter == true) {
		ed[z+11].setFocusable(false);
		ed[z+11].setTextSize(12);
		ed[z+11].setTextColor(Color.WHITE);
		ed[z+11].setBackgroundResource(R.drawable.lap_tabelle);
		sp4.addView(ed[z+11]);
		ed[z+12]=new EditText(this);
		ed[z+12].setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ed[z+12].setText("Gew.(g):");
		ed[z+12].setFocusable(false);
		ed[z+12].setTextSize(12);
		ed[z+12].setTextColor(Color.WHITE);
		ed[z+12].setBackgroundResource(R.drawable.lap_tabelle);
		sp3.addView(ed[z+12]);
	}//public void lap_datenverarbeitung() {

	class new_runnable implements Runnable {
		private short var_za;
		private short  var_performid;

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
						perform_ready=true;
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
				Toast.makeText(Lap.this, "Fehler im Umwandlungsprozess der Daten.", Toast.LENGTH_SHORT).show();
			}//Try
			if (perform_ready == true && perform_length == var_za) {
				lap_datenverarbeitung(radiogroup1.getCheckedRadioButtonId());
			}
			//Last Runnable
			if (za_thread-1  == var_za) {
				//Ladeprozess zuende

			}//if (za_thread-1  == var_za) {
		}//run
	}//class new_runnable implements Runnable {
}
