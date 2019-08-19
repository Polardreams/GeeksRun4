package de.polardreams.geeksrun;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
//import de.polardreams.geeksrun.Map.new_runnable;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Challenge  {

	private static double [][][] koords_ki = null;
	//[lfdnr][1-5][?]
	private File meinspeicherort;
	private String[] entries;
	private int teil_zeile;
	private int[] first_ges_zeile = new int[20];//y = Anzahl Datenstze Array Kontainer
	private int ges_zeile[]= new int[20];
	/*
	 *  Strecke [0x,1y][2h][3m][4s][5fortschritt] befuellen
	 *  Spieler [Spielernummer][Fortschritt][Platz][durch. Tempo, Favoritenplatz]
	 *  koords_ki [lfdnr des Spielers][x,y,h,m,s,Hoehe][zeile]
	 */
	private int [][][][] Spieler  = new int [99][2][2][3];
	private double[][] Strecke = null;
	private double Streckenlaegne=0;

	private int anz_spieler=0;
	public String ki_h, ki_m, ki_s;

	//Performancesteigerung
	private String[][] perform_konverter = new String[20][150];//Performance
	private int perform_length =0;
	private short perform_id =0;//Verarbeitungsgeschwindigkeit 29
	private int za_thread =0; //For Schleife fuer die Thread Nummer
	private Handler[] map_marker_akt = new Handler[150];//Performance
	private short last_perform =0;

	//Event und Player
	private MediaPlayer audioplayer, zuschauer;
	private int akt_sequenz=0;
	private boolean flag_sequenz=false;
	private Context c;
	private boolean flag_ueberholt_positiv=false;
	private boolean flag_ueberholt_negativ=false;
	private int report_countdown = 0;
	private Timer tim_platz_report = new Timer();
	public double[][][] akt_pos_spieler = new double[20][2][2];//max. Spieler
	private int[][] spieler_akt_place= new int[20][2];
	private int[][] spieler_performance_place= null;//Platzvergabe fuer beste Durchschnittsgeschwindigkeiten
	private boolean spielerperformanceansage=true;
	private int last_zahl[] = new int[20];
	public boolean report_flag=true;
	private int report_countdown_place=0;
	private boolean zielgerade_ansage=false;
	private boolean mittelgerade_ansage=false;
	private boolean streckeninfo=false;
	public boolean ende;
	public String akt_himmelsrichtung="";
	private boolean kom_wait=false;
	private Timer kom_wait_threard = new Timer();
	private boolean kom_wait_flag=true;
	private int kom_wait_za=0;

	private boolean simuhilf_flag=false;
	private int simutemp=0;

	private int searchfokus_gegner=25;//25 ... Suchfokus fuer alle virtuellen Gegner
	private int searchfokus_spieler=25;//25
	private double horizontaltoleranz=0.0006;
	private double vertikaltoleranz=0.0002;

	//private int simu_rueck=0;
	private boolean[] simhilf_gegner=new boolean[20];
	private int[] simutemp_gegner = new int[10];

	private int[][] finale = new int[20][4];
	private boolean finale_laeufer_flag=false;

	private boolean challenge_mode;
	public boolean ladevorgang=false;
	private int[][] l_geg = new int[20][2];
	public int stunden, minuten, sekunden;

	private int sprueche_za=0;
	private boolean siegerehrung=false;
	//private boolean sprueche_flag=false;

	int test =0;//zaehlt die GPS Treffer

	public Challenge(String[] args, int anz_gegner, File load, Context con, boolean replaymode) {
		// TODO Auto-generated method stub
		//laden des Trainings, was als Gegener fungieren soll
		if (replaymode==true) {
			challenge_mode=false;
		}else {
			challenge_mode=true;
		}//if (replaymode==true) {
		c=con;
		ende=false;
		ki_h="";
		ki_m="";
		ki_s="";
		anz_spieler=anz_gegner;
		koords_ki = new double[20][6][9000];
		entries=args;
		if (challenge_mode==true) {
			for (int n=0; n<anz_gegner;n++) {
				char[] inputBuffer = new char[999999];
				meinspeicherort = load;
				File add = new File(meinspeicherort, entries[n]);//entries[Index des Ausgewaehlten Item]
				String historytext="";
				//Laderoutine
				try {
					FileInputStream fileinputstream = new FileInputStream(add);
					InputStreamReader inputstreamreader = new InputStreamReader(fileinputstream);//Zeiger melden an den leser
					inputstreamreader.read(inputBuffer);//Leser liest in einen Puffer (Char Typ )
					historytext = new String(inputBuffer);//der Puffer wird der Textvariablen uebergeben
					inputstreamreader.close();//Datei geschlossen
				} catch(Exception e){
					e.printStackTrace();
				}//Try
				//herausfiltern von Fuellzeichen inputBuffer, dient der Verkleinerung der String-Datei
				try {
					int hilf = historytext.indexOf('\u0000');
					String hilf1 = historytext.substring(0, hilf);
					historytext=hilf1;
				} catch (Exception e1) {
					e1.printStackTrace();
				}//Try
				kontainer_string2array(historytext,n);
				spieler_akt_place[n][0]=0;
				l_geg[n][0]=0;
			}//for
		} else {
			for (int n=0; n<anz_gegner+1;n++) {
				/*zusaetzlich zaehlt die Strecke (Map) als eigener "Gegner" wird aber als
				 * Strecke danach ausgegeben
				 */
				char[] inputBuffer = new char[999999];
				meinspeicherort = load;
				File add = new File(meinspeicherort, entries[n]);//entries[Index des Ausgewaehlten Item]
				String historytext="";
				//Laderoutine
				try {
					FileInputStream fileinputstream = new FileInputStream(add);
					InputStreamReader inputstreamreader = new InputStreamReader(fileinputstream);//Zeiger melden an den leser
					inputstreamreader.read(inputBuffer);//Leser liest in einen Puffer (Char Typ )
					historytext = new String(inputBuffer);//der Puffer wird der Textvariablen uebergeben
					inputstreamreader.close();//Datei geschlossen
				} catch(Exception e){
					e.printStackTrace();
				}//Try
				//herausfiltern von Fuellzeichen inputBuffer, dient der Verkleinerung der String-Datei
				try {
					int hilf = historytext.indexOf('\u0000');
					String hilf1 = historytext.substring(0, hilf);
					historytext=hilf1;
				} catch (Exception e1) {
					e1.printStackTrace();
				}//Try
				kontainer_string2array(historytext,n);
				spieler_akt_place[n][0]=0;
				l_geg[n][0]=0;
			}//for
		}//if (challenge_mode==false) {
		report_flag=true;
		//Platzberichte

		tim_platz_report.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(report_flag) {
					if (ladevorgang==true) {
						try{
							Thread.sleep(1000);
						}catch(Exception e) {
							e.printStackTrace();
						}//try

						if (report_countdown >= 240) {//240 aller 4 Minuten
							report_countdown=0;
							challenge_report();
						} else {
							if (report_countdown_place >= 180) {// aller 3 Minuten
								challenge_place_report();
								report_countdown_place=0;
							} else {
								report_countdown_place++;
							}//if (report_countdown_place >= 2)
							report_countdown++;
						}//if (report_countdown >= 600) {
					}//if (report_countdown >= 300)
					if (flag_sequenz==false && Spieler[0][1][0][0]>500) {
						if (sprueche_za>=60) {//jede Minute
							sprueche_za=0;
							sprueche_katalog();
						} else {
							sprueche_za++;
						}//if (sprueche_za>=180)
					}//if (flag_sequenz==false) {
				}//while
			}//public void run() {
		},0);
	}//public void main(String[] args, int anz_gegner) {

	public int getanz_spieler() {
		return anz_spieler;
	}//public int getanz_spieler() {

	boolean register_process() {
		//Streckenregistrierung durch Prozentvergabe
		int anz_zeilen=first_ges_zeile[0];
		double hilf = (float) 10000/anz_zeilen;
		for (int n=0; n<anz_zeilen;n++) {
			Strecke[5][n] =(int)((n+1)*hilf);//damit die erste Koordinate nicht 0% ist
		}//for (int n=0; n<anz_zeilen;n++) {
		ladevorgang=true;
		return true;//Beweis, dass die Funktion ausgefuehrt wurde
	}//Boolean register_process() {

	public void set_pos(double x, double y, int hour, int minute, int second){
		//Algorhythmus Koordinatenvergleich
		if (ladevorgang==true) {
			if (ende==false) {
				double kx = x;
				double ky = y;
				stunden = hour;
				minuten = minute;
				sekunden = second;
				int l=0;
				//Fortschritt des Laeufers feststellen
				boolean ln_flag=true;
				int lza=0;
				while(ln_flag) {
					if (Spieler[0][1][0][0]==Strecke[5][lza] && lza<first_ges_zeile[0]) {
						l=lza;
						ln_flag=false;
					}//if (Spieler[0][1][0]==Strecke[0][lza][0][0][0][1]) {
					if (Spieler[0][1][0][0]==0){
						l=0;
						ln_flag=false;
					}//if (Spieler[0][1][0]==0){
					lza++;
				}//while(ln_flag) {
				akt_himmelsrichtung=minmax_toleranz(l);
				boolean pos_flag =true;
				int l1_out=l;

				//Fortschritt des Laeufers feststellen
				if (challenge_mode==true) {
					if (x!=0 && y!=0) {
						//int search_hilf =Math.round(searchfokus_spieler/2);//hier ist eine Kallibierung noetig!
						//searchfokus_spieler = search_hilf;
						while (pos_flag) {
							if (l<first_ges_zeile[0] && (l<l1_out+searchfokus_spieler)) {
								if (((kx >= Strecke[0][l]+(-1*horizontaltoleranz))&&(kx <= Strecke[0][l]+(+1*horizontaltoleranz)))&&
										((ky >= Strecke[1][l]+(-1*vertikaltoleranz))&&(ky <= Strecke[1][l]+(+1*vertikaltoleranz)))) {
									pos_flag=false;
									//Algorhythmus Spieleraktualisierung
									Spieler[0][1][0][0]= (int) Strecke[5][l];
									akt_pos_spieler[0][0][0]=Strecke[0][l];//Simulations Variablen
									akt_pos_spieler[0][1][0]=Strecke[1][l];//Simulations Variablen
									//Toast.makeText(c, "GPS_Challenge, Fort: "+String.valueOf(Spieler[0][1][0][0]), Toast.LENGTH_SHORT).show();
									//hier war mal der virtuelle Gegner Skript
								}else {
									l++;
									//Zielmanagement
									try{
										if (l==first_ges_zeile[0]-1 && l1_out>first_ges_zeile[0]-5) {
											/*
											 * Die zusaetzliche Sicherung " ... && l1_out>first_ges_zeile[0]-5) "
											 * sorgt dafuer, dass der Searchfokus nicht 500m vor dem ziel das Zielmanagement aktiviert.
											 * eventuell muss 25 als searchfokus fuer das Training noch kleiner gemacht werden. Es muss jedoch beachtet
											 * werden dass in der Challenge weniger GPS-Abfregen mit neuen GPS kommen als im Replay.
											 */
											report_flag=false;
											pos_flag=false;
											ende=true;
											boolean fin_flag=true;
											int fin=0;
											while(fin_flag) {
												if (finale[fin][0]==0) {
													finale[fin][0]=1;
													finale[fin][1]=Spieler[0][0][0][0];
													finale[fin][2]=Spieler[0][0][0][2];
													finale[fin][3]=fin+1;
													fin_flag=false;
												} else {
													fin++;
												}//if (finale[fin][0]==0) {
											}//while(fin_flag) {
										}//if (l==first_ges_zeile-1) {
									} catch(Exception e) {
										e.printStackTrace();
										Toast.makeText(c, "Crash Spieler", Toast.LENGTH_LONG).show();
									}//try
								}//if (((kx >= Strecke[0][l][0][0][0][0]-0.0001)&&(kx <= Strecke[0][l][0][0][0][0]+0.0001))&& ...
							} else {
								pos_flag=false;
							}//if (l<first_ges_zeile && (l<+l1_out+30)) {
						}//while (pos_flag) {
					} //if (x!=0 && y!=0) {
				} else {
					/*
					 * Das Replay als Programm mit einer Searchfokussimulation (SFS) versetzt eigenmaechtig den Spieler.
					 * In der Realitaet im HUD ist er aber nicht versetzt, dass bedeutet, Sie muss ersteinmal soviel
					 * Datenlieferung (GPS) ignorieren, wie sie vorgesprungen ist. Das bedeutet vom Simulationsfortschritt bis zum Searchfokus
					 * werden alle Moves simuliert (Laeufer wird blind gesetzt, in der Hoffnung, dass im naechsten Searchfokus der richtige GPS Punkt dabei ist).
					 * Dadurch spare ich Rechen- bzw. Suchleistung.
					 *
					 * Der grosse nachteil besteht darin, dass
					 * keine berechnungen bzw. GPS-Punkte gefunden werden auf der Strecke und alles simuliert wird.
					 * Die Zeit die ein GPS Punkt hat wird somit ignoriert, da einfach nur Punkt fuer Punkt simuliert wird.
					 * Der Laeufer wird unmenschlich schnell, schneller als seine Gegner.
					 */

					/*
					 * Im Replay ist es daher gut, wenn keine Simulation stattfindet, da sowohl Zeit als auch Geo-Punkt mit
					 * jeder Aktualisierung der GUI bzw. der Simulationsrate kommt. Nur der Streckenfortschritt des Spielers muss
					 * mitziehen, da sonst keine Geo-Punkte mehr gefunden werden.
					 */
					while (pos_flag) {
						if (l<first_ges_zeile[0] && (l<l1_out+searchfokus_spieler)) {
							if (((kx >= Strecke[0][l]+(-1*horizontaltoleranz))&&(kx <= Strecke[0][l]+(+1*horizontaltoleranz)))&&
									((ky >= Strecke[1][l]+(-1*vertikaltoleranz))&&(ky <= Strecke[1][l]+(+1*vertikaltoleranz)))) {
								pos_flag=false;
								//Algorhythmus Spieleraktualisierung
								Spieler[0][1][0][0]= (int) Strecke[5][l];
								akt_pos_spieler[0][0][0]=Strecke[0][l];//Simulations Variablen
								akt_pos_spieler[0][1][0]=Strecke[1][l];//Simulations Variablen
								//hier war mal der virtuelle Gegner Skript
							}else {
								l++;
								//Zielmanagement
								try{
									if (l==first_ges_zeile[0]-1) {
										report_flag=false;
										pos_flag=false;
										ende=true;
										boolean fin_flag=true;
										int fin=0;
										while(fin_flag) {
											if (finale[fin][0]==0) {
												finale[fin][0]=1;
												finale[fin][1]=Spieler[0][0][0][0];
												finale[fin][2]=Spieler[0][0][0][2];
												finale[fin][3]=fin+1;
												fin_flag=false;
											} else {
												fin++;
											}//if (finale[fin][0]==0) {
										}//while(fin_flag) {
									}//if (l==first_ges_zeile-1) {

								} catch(Exception e) {
									e.printStackTrace();
									Toast.makeText(c, "Crash Spieler", Toast.LENGTH_LONG).show();
								}//try
							}//if (((kx >= Strecke[0][l][0][0][0][0]-0.0001)&&(kx <= Strecke[0][l][0][0][0][0]+0.0001))&& ...
						} else {
							pos_flag=false;
						}//if (l<first_ges_zeile && (l<+l1_out+30)) {
					}//while (pos_flag) {
				}//if (challenge_mode==true) {


				if (challenge_mode==false) {
					//Fortschritt der virtuellen Gegner feststellen - Replay
					int koords_sec_akt=(stunden*3600)+(minuten*60)+sekunden;//Aktuelle Zeit
					for(int m=1; m<anz_spieler+1;m++) {
						boolean time_flag =true;
						int l1 =l_geg[m][0];

						boolean fin_test=true;
						for (int fin_nr=0; fin_nr<anz_spieler;fin_nr++) {
							if (m==finale[fin_nr][1]){
								fin_test=false;
							}//if (m==finale[fin_nr][1]){
						}//for (int fin_nr=0; fin_nr<anz_spieler;fin_nr++) {

						if(fin_test==true) {
							while(time_flag) {
								int koords_sec_ki =((int)koords_ki[m][2][l1]*3600)+((int)koords_ki[m][3][l1]*60)+(int)koords_ki[m][4][l1];

								/*
								 * Nummer 3, die nicht fertig war, hatte bei koords_ki[zeit] = 0
								 * das fuehrt zu einer Endlosschleife
								 */

								if (koords_sec_akt<=koords_sec_ki && Spieler[m][1][0][0]!=10000 ) {
									//... Koordinatenvergleich und danach Spielerarray aktualisieren
									/*
									 * Der Zeitvergleich wird benoetigt, um den Fortschritt
									 * jedes Spielers herauszufinden. Zuerst wird also geschaut,
									 * welche aktuelle Zeit in der aktuellen Challenge vorhanden ist
									 * danach vergleicht man die Zeit mit der Strecke des virteullen Gegners
									 * dann wird der Fortschritt der Strecke des virtuellen Gegners
									 * auf die aktuelle Strecke (Challenge) uebertragen und die Position ermiuttelt
									 *
									 * auf ein Suchabbruch, falls keine Zeit gefunden wird, wurde verzichtet, weil
									 * dies eher unwahrscheinlich ist. Anders ist es mit den Koordinaten.
									 * Diese gehen teils stark auseinander und benoetigen deshlab eine Toleranz
									 * die eben einen Suchalgorithmus benoetigt.
									 */
									l_geg[m][0]=l1;
									ki_h=String.valueOf(koords_ki[m][2][l1]);
									ki_m=String.valueOf(koords_ki[m][3][l1]);
									ki_s=String.valueOf(koords_ki[m][4][l1]);

									kx=koords_ki[m][0][l1];//man muss eine Koordinate vorher nehmen,
									ky=koords_ki[m][1][l1];//weil die naechste zu frueh waere (siehe If-Anweisung mit Zeit
									time_flag=false;
									int l2=0;

									//Fortschrittsspeicher
									ln_flag=true;
									lza=0;
									while(ln_flag) {
										if (Spieler[m][1][0][0]==Strecke[5][lza] && lza<first_ges_zeile[0]) {
											l2=lza;
											ln_flag=false;
										}//if (Spieler[0][1][0]==Strecke[0][lza][0][0][0][1]) {
										if (Spieler[m][1][0][0]==0){
											l2=0;
											ln_flag=false;
										}//if (Spieler[0][1][0]==0){
										lza++;
									}//while(ln_flag) {
									akt_himmelsrichtung=minmax_toleranz(l2);
									//Fortschrittsspeicher
									boolean pos_flag_ki =true;
									int l2_out=l2;
									while (pos_flag_ki) {
										if (l2<9000 && (l2<l2_out+searchfokus_gegner)) {
											if (((kx >= Strecke[0][l2]+(-1*horizontaltoleranz))&&(kx <= Strecke[0][l2]+(+1*horizontaltoleranz)))&&
													((ky >= Strecke[1][l2]+(-1*vertikaltoleranz))&&(ky <= Strecke[1][l2]+(+1*vertikaltoleranz)))) {
												Spieler[m][1][0][0]=(int) Strecke[5][l2];
												akt_pos_spieler[m][0][0]=Strecke[0][l2];//Simulations Variablen
												akt_pos_spieler[m][1][0]=Strecke[1][l2];//Simulations Variablen
												test++;
												pos_flag_ki=false;
											}else{
												l2++;
												if (l2==first_ges_zeile[0]-1 && l2_out>first_ges_zeile[0]-5) {
													/*
													 * Achtung hier wir dauch die Strecke geschaut, aber wieviel hat der gegner selber an infos in seinem
													 * eigenen Array
													 */
													//Zielmanagement
													try {
														time_flag=false;
														pos_flag=false;
														boolean fin_flag=true;
														if (Spieler[m][1][0][0]>9000) {
															int fin=0;
															while(fin_flag) {
																if (finale[fin][1]!=Spieler[m][0][0][0]) {
																	if (finale[fin][0]==0) {
																		finale[fin][0]=1;
																		finale[fin][1]=Spieler[m][0][0][0];
																		finale[fin][2]=Spieler[m][0][0][2];
																		finale[fin][3]=fin+1;
																		finale_laeufer_flag=true;
																		fin_flag=false;
																		pos_flag_ki=false;
																		time_flag=false;
																	} else {
																		fin++;
																	}//if (finale[fin][0]==0) {
																} else {
																	fin_flag=false;
																}//if (finale[fin][1]!=Spieler[m][0][0][0]) {
															}//while(fin_flag) {
														}//if (Spieler[m][1][0][0]>9000) {
													} catch(Exception e) {
														e.printStackTrace();
														Toast.makeText(c, "Crash Gegner", Toast.LENGTH_LONG).show();
													}//try
												}//if (l2==first_ges_zeile[0]-1 && l2_out>first_ges_zeile[0]-5) {
											}//if (((kx >= Strecke[0][l2]+(-1*horizontaltoleranz))&&(kx <= Strecke[0][l2]+(+1*horizontaltoleranz)))&& ...
										} else {
											pos_flag_ki=false;
											time_flag=false;
										}//if (l2<9000 && (l2<l2_out+searchfokus_gegner)) {
									}//while (pos_flag_ki) {
								}else {
									l1++;
								}//if (koords_sec_akt<koords_sec_ki && Spieler[m][1][0][0]!=10000)
							}//while(time_flag) {
						}//if(finale[m][0]==0) {
					}//for(int m=0; m<anz_spieler;m++) {
				} else {
					//Fortschritt der virtuellen Gegner feststellen - Challenge
					//searchfokus_gegner=50;
					/*
					 * ev. weniger Koords, da bei Replay die Zeit schneller voranschreitet
					 * somit kann auch sdie Simulation gemacht werden, heisst
					 * es gibt durch eine sich mehr veraendernde zeit mehr l1 (erste Routine) kx,ky Punkte
					 * und durch die Simulation und durch die Simualtion gibt es mehr kx,ky!
					 */
					int koords_sec_akt=(stunden*3600)+(minuten*60)+sekunden;//Aktuelle Zeit
					for(int m=1; m<anz_spieler+1;m++) {
						boolean time_flag =true;
						int l1 =l_geg[m][0];
						boolean fin_test=true;
						for (int fin_nr=0; fin_nr<anz_spieler;fin_nr++) {
							if (m==finale[fin_nr][1]){
								fin_test=false;
							}//if (m==finale[fin_nr][1]){
						}//for (int fin_nr=0; fin_nr<anz_spieler;fin_nr++) {

						if(fin_test==true) {

							while(time_flag) {
								int koords_sec_ki =((int)koords_ki[m-1][2][l1]*3600)+((int)koords_ki[m-1][3][l1]*60)+(int)koords_ki[m-1][4][l1];
								if (koords_sec_akt<=koords_sec_ki && Spieler[m][1][0][0]!=10000 ) {
									//... Koordinatenvergleich und danach Spielerarray aktualisieren
									/**
									 * Der Zeitvergleich wird benoetigt, um den Fortschritt
									 * jedes Spielers herauszufinden. Zuerst wird also geschaut,
									 * welche aktuelle Zeit in der aktuellen Challenge vorhanden ist
									 * danach vergleicht man die Zeit mit der Strecke des virteullen Gegners
									 * dann wird der Fortschritt der Strecke des virtuellen Gegners
									 * auf die aktuelle Strecke (Challenge) uebertragen und die Position ermiuttelt
									 *
									 * auf ein Suchabbruch, falls keine Zeit gefunden wird, wurde verzichtet, weil
									 * dies eher unwahrscheinlich ist. Anders ist es mit den Koordinaten.
									 * Diese gehen teils stark auseinander und benoetigen deshlab eine Toleranz
									 * die eben einen Suchalgorithmus benoetigt.
									 */
									l_geg[m][0]=l1;
									ki_h=String.valueOf(koords_ki[m-1][2][l1]);
									ki_m=String.valueOf(koords_ki[m-1][3][l1]);
									ki_s=String.valueOf(koords_ki[m-1][4][l1]);

									kx=koords_ki[m-1][0][l1];//man muss eine Koordinate vorher nehmen,
									ky=koords_ki[m-1][1][l1];//weil die naechste zu frueh waere (siehe If-Anweisung mit Zeit
									time_flag=false;
									int l2=0;

									//Fortschrittsspeicher
									ln_flag=true;
									lza=0;
									while(ln_flag) {
										if (Spieler[m][1][0][0]==Strecke[5][lza] && lza<first_ges_zeile[0]) {
											l2=lza;
											ln_flag=false;
										}//if (Spieler[0][1][0]==Strecke[0][lza][0][0][0][1]) {
										if (Spieler[m][1][0][0]==0){
											l2=0;
											ln_flag=false;
										}//if (Spieler[0][1][0]==0){
										lza++;
									}//while(ln_flag) {
									akt_himmelsrichtung=minmax_toleranz(l2);
									//Fortschrittsspeicher
									boolean pos_flag_ki =true;
									int l2_out=l2;
									while (pos_flag_ki) {
										if (l2<9000 && (l2<l2_out+searchfokus_gegner)) {
											if (((kx >= Strecke[0][l2]+(-1*horizontaltoleranz))&&(kx <= Strecke[0][l2]+(+1*horizontaltoleranz)))&&
													((ky >= Strecke[1][l2]+(-1*vertikaltoleranz))&&(ky <= Strecke[1][l2]+(+1*vertikaltoleranz)))) {
												Spieler[m][1][0][0]=(int) Strecke[5][l2];
												akt_pos_spieler[m][0][0]=Strecke[0][l2];//Simulations Variablen
												akt_pos_spieler[m][1][0]=Strecke[1][l2];//Simulations Variablen
												test++;
												pos_flag_ki=false;
											}else{
												l2++;
												//Zielmanagement
												if (l2==first_ges_zeile[0]-1 && l2_out>first_ges_zeile[0]-5) {
													try {
														time_flag=false;
														pos_flag=false;
														boolean fin_flag=true;
														if (Spieler[m][1][0][0]>9000) {
															int fin=0;
															while(fin_flag) {
																if (finale[fin][1]!=Spieler[m][0][0][0]) {
																	if (finale[fin][0]==0) {
																		finale[fin][0]=1;
																		finale[fin][1]=Spieler[m][0][0][0];
																		finale[fin][2]=Spieler[m][0][0][2];
																		finale[fin][3]=fin+1;
																		finale_laeufer_flag=true;
																		fin_flag=false;
																	} else {
																		fin++;
																	}//if (finale[fin][0]==0) {
																} else {
																	fin_flag=false;
																}//if (finale[fin][1]!=Spieler[m][0][0][0]) {
															}//while(fin_flag) {
														}
													} catch(Exception e) {
														e.printStackTrace();
														Toast.makeText(c, "Crash Gegner", Toast.LENGTH_LONG).show();
													}//try
												}//if (l2==first_ges_zeile[0]-1 && l2_out>first_ges_zeile[0]-5) {
											}//if (((kx >= Strecke[0][l2]+(-1*horizontaltoleranz))&&(kx <= Strecke[0][l2]+(+1*horizontaltoleranz)))&& ...
										} else {
											pos_flag_ki=false;
											time_flag=false;
											/*
											 * Es wurde gaenzlich auf eine Simulationshilfe verzichtet. Urspruenglich war angedacht, bei einer
											 * fehlgeschlagenen Searchfokus-Routine 5 Punkte zu simulieren und dann den Fortschritt auf den letzten
											 * Punkt zu setzen. Somit sollte eine groessere Varianz an GeoPunkten und Zeit-Staenden erreicht werden,
											 * um den Searchfokus mehr anzuregen. Dies hat sich jedoch als falsch herausgesetllt, da die Simulation, der
											 * 5 Punkte tatsaechlich die simulierte Zeit im gegensatz zur Real Aufgenommen Zeit veroelangsamt, denn innerhalb der
											 * 5 simulierten Punkte, koennte der Gegner ja schon weiter sein, was sich auch herausgestellt hat.
											 *
											 * Somit wurde die Simulation nicht mehr verwendet, da auch der Quelltext im Map.java geaendert wurde, denn urspruenglich
											 * hat die GUI und auch die Challenge nur gerechnet, wenn sich auch der Laeufer bewegt hat. Jetzt ist aber eine automatische
											 * Aktualisierung und damit eine Berechnung implementiert, die genuegend GPS-Punkte und Zeit-Staende liefert, so dass
											 * fehlgeschlagene Searchfokus-Routinen einfach abgebrochen werden koennen, da beim naechsten versuch, wieder neue GPS-Punkte
											 * da sind, zudem ist der Searchfokus relativ hoch und die Toleranzen relativ weit, so dass fast immer etwas gefunden werden muss
											 * da die Simulationszeiten immer auch an die Streckenzeiten des Gegners angepasst werden, ist die grafische Darstellung immer auch die
											 * richtige.
											 */

										}//if (l2<9000 && (l2<l2_out+searchfokus_gegner)) {
									}//while (pos_flag_ki) {
								}else {
									l1++;
								}//if (koords_sec_akt<koords_sec_ki && Spieler[m][1][0][0]!=10000)
							}//while(time_flag) {
						}//if(finale[m][0]==0) {
					}//for(int m=0; m<anz_spieler;m++) {
				}//if (challenge_mode==true) {

				//Spielerplatz Sortierverfahren
				for (int n=0;n<anz_spieler+1;n++){
					Spieler[n][0][1][0]=anz_spieler+1;
				}//for (int n=0;n<anz_spieler+1;n++){

				for (int n=0;n<anz_spieler+1;n++) {
					for (int m=0; m<anz_spieler+1;m++) {
						if (n!=m) {
							if (Spieler[n][1][0][0]!=Spieler[m][1][0][0]) {
								if (Spieler[n][1][0][0]>Spieler[m][1][0][0]) {
									Spieler[n][0][1][0]--;
								}//if (Spieler[n][1][0]>Spieler[n+1][1][0]) {
							}//if (Spieler[n][1][0]!=Spieler[m][1][0]) {
						}//if (n!=m) {
					}//for (int m=0; m<anz_spieler-1;m++) {
				}//for (int n=0;n<anz_spieler-2;n++) {

				//ueberholpruefung
				flag_ueberholt_negativ=false;
				flag_ueberholt_positiv=false;
				if (spieler_akt_place[0][0]!=0) {//fuer eine Erstbehandlung
					for (int placex=0; placex<anz_spieler+1;placex++) {
						spieler_akt_place[placex][1]=spieler_akt_place[placex][0]-Spieler[placex][0][1][0];
					}//for (int placex=0; x<anz_spieler+1;x++) {
					if (spieler_akt_place[0][1]>0) {
						for (int placez=1; placez<(anz_spieler+1);placez++) {
							if (spieler_akt_place[0][1]>spieler_akt_place[placez][1]) {
								if ((Spieler[0][0][1][0]+1)==Spieler[placez][0][1][0]) {
									if (kom_wait==false) {
										flag_ueberholt_positiv=true;
										kom_wait=true;
										kom_wait_flag=true;
										TimerTask run = new kom_wait_run();
										kom_wait_threard.schedule(run, 0);
									}else {
										kom_wait_za++;
									}//if (kom_wait==false) {
								}//if (spieler_akt_place[0][0]-1==spieler_akt_place[placez][0]) {
							}//if (spieler_akt_place[0][1]<spieler_akt_place[placez][1]) {
						}//for (int placez=0; placez<anz_spieler+1;placez++) {
					}//if (spieler_akt_place[0][1]>0) {
					if (spieler_akt_place[0][1]<0) {
						for (int placez=1; placez<(anz_spieler+1);placez++) {
							if (spieler_akt_place[0][1]<spieler_akt_place[placez][1]) {
								if ((Spieler[0][0][1][0]-1)==Spieler[placez][0][1][0]) {
									if (kom_wait==false) {
										flag_ueberholt_negativ=true;
										kom_wait=true;
										kom_wait_flag=true;
										TimerTask run = new kom_wait_run();
										kom_wait_threard.schedule(run, 0);
									}else {
										kom_wait_za++;
									}//if (kom_wait==false) {
								}//if (spieler_akt_place[0][0]-1==spieler_akt_place[placez][0]) {
							}//if (spieler_akt_place[0][1]<spieler_akt_place[placez][1]) {
						}//for (int placez=0; placez<anz_spieler+1;placez++) {
					}//if (spieler_akt_place[0][1]>0) {
				}//if (spieler_akt_place[0][1]<0) {
				if (spieler_akt_place[0][1]==0) {
					for (int placez=1; placez<(anz_spieler+1);placez++) {
						if (spieler_akt_place[placez][0]==spieler_akt_place[0][0]) {
							if ((Spieler[0][0][1][0]-1)==Spieler[placez][0][1][0]) {
								if (kom_wait==false) {
									flag_ueberholt_negativ=true;
									kom_wait=true;
									kom_wait_flag=true;
									TimerTask run = new kom_wait_run();
									kom_wait_threard.schedule(run, 0);
								}else {
									kom_wait_za++;
								}//if (kom_wait==false) {
							}//if (spieler_akt_place[0][0]-1==spieler_akt_place[placez][0]) {
						}//if (spieler_akt_place[0][1]<spieler_akt_place[placez][1]) {
					}//for (int placez=0; placez<anz_spieler+1;placez++) {
				}
				//aktueller Platz speichern
				for (int placey=0; placey<anz_spieler+1;placey++) {
					spieler_akt_place[placey][0]=Spieler[placey][0][1][0];
				}//for (int placey=0; placey<anz_spieler+1;placey++) {


				//Spielerperformanceplatz Sortierverfahren
				if (anz_spieler>2 && Spieler[0][1][0][0]<450) {
					//40, weil der Fortschritt auf Tausendstel berechnet ist

					spieler_performance_place= new int[20][4];

					for (int n=1;n<anz_spieler+1;n++){
						Spieler[n][0][0][2]=anz_spieler;
					}//for (int n=0;n<anz_spieler+1;n++){

					for (int n=1;n<anz_spieler+1;n++) {
						for (int m=1; m<anz_spieler+1;m++) {
							if (n!=m) {
								if (Spieler[n][0][0][1]!=Spieler[m][0][0][1]) {
									if (Spieler[n][0][0][1]>Spieler[m][0][0][1]) {
										Spieler[n][0][0][2]--;
									}//if (Spieler[n][1][0]>Spieler[n+1][1][0]) {
								}//if (Spieler[n][1][0]!=Spieler[m][1][0]) {
							}//if (n!=m) {
						}//for (int m=0; m<anz_spieler-1;m++) {
					}//for (int n=0;n<anz_spieler-2;n++) {

					for (int placey=1; placey<anz_spieler+1;placey++) {
						if (Spieler[placey][0][0][2]==1) {
							if (spieler_performance_place[0][0]==0) {
								spieler_performance_place[0][0]=Spieler[placey][0][0][0];
								spieler_performance_place[0][1]=(int)(Spieler[placey][0][0][1]/10);
							} else {
								spieler_performance_place[0][2]=Spieler[placey][0][0][0];
								spieler_performance_place[0][3]=(int)(Spieler[placey][0][0][1]/10);
							}
						}//if (Spieler[placey][0][1][0]==1) {
						if (Spieler[placey][0][0][2]==2) {
							if (spieler_performance_place[1][0]==0) {
								spieler_performance_place[1][0]=Spieler[placey][0][0][0];
								spieler_performance_place[1][1]=(int)(Spieler[placey][0][0][1]/10);
							} else {
								spieler_performance_place[1][2]=Spieler[placey][0][0][0];
								spieler_performance_place[1][3]=(int)(Spieler[placey][0][0][1]/10);
							}
						}//if (Spieler[placey][0][1][0]==1) {
						if (Spieler[placey][0][0][2]==3) {
							if (spieler_performance_place[2][0]==0) {
								spieler_performance_place[2][0]=Spieler[placey][0][0][0];
								spieler_performance_place[2][1]=(int)(Spieler[placey][0][0][1]/10);
							} else {
								spieler_performance_place[2][2]=Spieler[placey][0][0][0];
								spieler_performance_place[2][3]=(int)(Spieler[placey][0][0][1]/10);
							}//if (spieler_performance_place[2][0]==0) {
						}//if (Spieler[placey][0][0][2]==3) {
					}//for (int placey=0; placey<anz_spieler+1;placey++) {



				}//if (anz_spieler>2 && Spieler[0][1][0][0]<40) {
				challenge_events();
			} else {
				stop_sequenz();
			}//if (ende==false) {
		}//if (ladevorgang==true) {
	}//public void set_pos(double koords[][][][][][][], int hour, int minute, int second){


	public void kontainer_string2array(String var_string, int lfdn_kontainer) {
		try {
			perform_length=0;//initialisiert fuer neue Strecke
			if (var_string != null || var_string=="") {
				//Perform_id Berechnen
				ges_zeile[lfdn_kontainer] = 0;
				for (int x = 0; x<var_string.length(); x++){
					if (var_string.charAt(x)=='\n') {ges_zeile[lfdn_kontainer]++;}
				}//for (int x = 0; x<var_string.length(); x++){
				Strecke=new double [6][9000];
				//Performance
				if (ges_zeile[lfdn_kontainer]>1000) {
					perform_id=(short) (ges_zeile[lfdn_kontainer]*0.01);
					/*
					 * Grad der Lestungssteigerung (bzw. Zuteiung
					 * der Zeilenverarbeitung fuer die einzelnen Handler)
					 */
				} else {
					if (ges_zeile[lfdn_kontainer]>100) {
						perform_id=(short) (ges_zeile[lfdn_kontainer]/20);
					} else {
						if (ges_zeile[lfdn_kontainer]>25) {
							perform_id=(short) (ges_zeile[lfdn_kontainer]/5);
						} else {
							perform_id=(short) (ges_zeile[lfdn_kontainer]/2);
						}//if (ges_zeile>25) {
					}//if (ges_zeile>100) {
				}//if (ges_zeile>1000) {
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
								perform_konverter[lfdn_kontainer][perform_length]=var_string.substring(0, m);
								var_string=var_string.substring(m+1, var_string.length());
								m=0;
								teil_zeile=0;
								perform_length++;
							} else {
								if (m==var_string.length()-1) {
									perform_konverter[lfdn_kontainer][perform_length]=var_string.substring(0, var_string.length());//letzten Teil in perform_konverter einfuegen
									last_perform=(short)teil_zeile;
									perform_flag=false;
								} else {
									if (var_string.substring(m+1,var_string.length()-1).indexOf('\n') == -1 ) {
										perform_konverter[lfdn_kontainer][perform_length]=var_string.substring(0, var_string.length());//letzten Teil in perform_konverter einfuegen
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

				if (ges_zeile[lfdn_kontainer]<9000){
					//Performance Steigerung
					if (ges_zeile[lfdn_kontainer]<1000) {
						for (za_thread = 0; za_thread<perform_length+1; za_thread++) {
							map_marker_akt[za_thread]=new Handler();
							map_marker_akt[za_thread].post(new new_runnable((short)za_thread,lfdn_kontainer));
						}//for (int za_thread = 0; za_thread<30; za_thread++) {
					} else {
						for (za_thread = 0; za_thread<perform_length+1; za_thread++) {
							map_marker_akt[za_thread]=new Handler();
							map_marker_akt[za_thread].postDelayed(new new_runnable((short)za_thread,lfdn_kontainer) {}, 1000);
						}//for (int za_thread = 0; za_thread<30; za_thread++) {
					}//if (ges_zeile<1000) {
				}//if (ges_zeile<9000){
				if (lfdn_kontainer==0) {
					first_ges_zeile[lfdn_kontainer]=ges_zeile[lfdn_kontainer];
				}//if (lfdn_kontainer==0) {
			}//if (var_string != null || var_string=="") {

			for (int x=0; x<anz_spieler+1; x++) {
				Spieler[x][0][0][0]=x;
			}//for (int x=0; anz_spieler+1; x++) {
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(c, "Beim Laden des Trainings ist etwas schief gelaufen. Kehre zurueck ins Hauptmenu.", Toast.LENGTH_LONG).show();
			Toast.makeText(c, "Schau nach, ob das Training vielleicht beschaedigt ist.", Toast.LENGTH_LONG).show();
		}
	}//public void kontainer_array2string() {

	private void zuschauerplay(boolean start_stop) {
		/*
		 * ist start_stop gleich true, dann wird die Startsequenz des Jubelns eingeleitet
		 * ansonsten die vom Ende
		 */
		if (start_stop) {
			Random random = new Random();
			int vorstell_zufall = random.nextInt(2);
			if (vorstell_zufall==0) {
				zuschauer=new MediaPlayer().create(c, R.raw.zuschauer1);
			}//if (vorstell_zufall==0) {
			if (vorstell_zufall==1) {
				zuschauer=new MediaPlayer().create(c, R.raw.zuschauer2);
			}//if (vorstell_zufall==1) {
		} else {
			Random random = new Random();
			int vorstell_zufall = random.nextInt(2);
			if (vorstell_zufall==0) {
				zuschauer=new MediaPlayer().create(c, R.raw.zuschauer1);
			}//if (vorstell_zufall==0) {
			if (vorstell_zufall==1) {
				zuschauer=new MediaPlayer().create(c, R.raw.zuschauer2);
			}//if (vorstell_zufall==1) {
		}//if (start_stop) {

		SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(c);
		int volumen =Integer.valueOf(info.getString("sound_challenge", "0"));

		int MaxVolume = 100;
		float volume= (float) (Math.log(MaxVolume-(volumen-1))/Math.log(MaxVolume));
		audioplayer.setVolume(1-(volume/3), 1-(volume/3));

		//zuschauer.setVolume((float)volumen/200, (float)volumen/200);//bitte leiser als die Kommentatoren
		zuschauer.start();
		zuschauer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				zuschauer.stop();
			}
		});
	}//private void zuschauerplay(boolean start_stop) {

	private void audiosequenzplay(final int[] sequenz, final int anz_sequenzen) {
		flag_sequenz=true;
		try {
			audioplayer=new MediaPlayer().create(c, sequenz[akt_sequenz]);

			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(c);
			int volumen =Integer.valueOf(info.getString("sound_challenge", "0"));

			int MaxVolume = 100;
			float volume= (float) (Math.log(MaxVolume-(volumen-1))/Math.log(MaxVolume));
			audioplayer.setVolume(1-volume, 1-volume);

			//audioplayer.setVolume((float)volumen/100, (float)volumen/100);
			audioplayer.start();
			audioplayer.setOnCompletionListener(new OnCompletionListener() {

				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					try {
						akt_sequenz++;
						if (akt_sequenz<anz_sequenzen) {
							play_next(sequenz);
						} else {
							akt_sequenz=0;
							flag_sequenz=false;
							audioplayer.stop();
						}//if (akt_lied<anz_lieder) {
					} catch(Exception e) {
						e.printStackTrace();
						akt_sequenz=0;
						flag_sequenz=false;
						audioplayer.stop();
					}//try
				}//public void onCompletion(MediaPlayer mp) {
			});//geeksplay.setOnCompletionListener(new OnCompletionListener() {
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag_sequenz=false;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag_sequenz=false;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag_sequenz=false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			flag_sequenz=false;
		}//Try
	}//private void audiosequenzplay(File[] sequenz, final int anz_sequenzen) {

	private void play_next(int[] sequenz) {
		audioplayer.stop();
		audioplayer.reset();
		try {
			audioplayer.setDataSource(c, Uri.parse("android.resource://de.polardreams.geeksrun/"+sequenz[akt_sequenz]));
			audioplayer.prepare();
			audioplayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag_sequenz=false;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag_sequenz=false;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag_sequenz=false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag_sequenz=false;
		}//try
	}//private void play_next() {

	class new_runnable implements Runnable {
		private short var_za;
		private short var_performid;
		private int lfdn;

		new_runnable(short var, int laufendenummer) {
			var_za = var;
			var_performid = perform_id;
			lfdn=laufendenummer;
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
					zahl =(short) ((var_za*var_performid));
					zahl_ende =(short) (var_performid+zahl);
				} else {
					zahl =(short) ((var_za*var_performid));
					zahl_ende =(short) (last_perform+zahl);//die letzten Zeilen machen
				}//if (perform_length != var_za) {

				while(zahl<zahl_ende && zahl<ges_zeile[lfdn]) {
					short zeilenumbruch_index = (short) (perform_konverter[lfdn][perform_za].indexOf('\n'));
					if (zeilenumbruch_index != -1) {
						xtxt=perform_konverter[lfdn][perform_za].substring(0,zeilenumbruch_index);
					} else {
						xtxt=perform_konverter[lfdn][perform_za];
					}//if (zeilenumbruch_index != -1) {


					/*
					 * Achtung!
					 *
					 * Ist der letzte angegebene Lauf kleiner von der Datensatzzahl her
					 * als andere, dann wird dieser eher fertig als alle andere (Thread-prinzip Uhrwerk)
					 * Dies fuehrt dazu, dass der Ladevorgang eher beendet wird, jedoch
					 * die anderen Strecken nicht fertig geladen sind und somit zu einer
					 * Fehlermeldung in der Findung des letzten Datensatzes ges_zeile[]
					 *
					 * Deswegen waren die vorherigen lfdnr nicht existent! Die werte von koords_ki[lfdn][0][zahl]
					 * sind stets 0.0!!!
					 */



					koords_ki[lfdn][0][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					koords_ki[lfdn][1][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					koords_ki[lfdn][2][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					koords_ki[lfdn][3][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					koords_ki[lfdn][4][zahl]=Double.valueOf(xtxt.substring(0, xtxt.indexOf(";")));
					xtxt=xtxt.substring(xtxt.indexOf(";")+1, xtxt.length());
					koords_ki[lfdn][5][zahl]=Double.valueOf(xtxt);//.substring(0, historytext.indexOf('\n'))

					if (lfdn==0) {
						Strecke[0][zahl]=koords_ki[0][0][zahl];
						Strecke[1][zahl]=koords_ki[0][1][zahl];
						//Strecke berechnen
						if (zahl>0 && zahl<zahl_ende) {
							if (Strecke[0][zahl-1]!=0 && Strecke[1][zahl-1]!=0 && Strecke[0][zahl]!=0 && Strecke[1][zahl]!=0) {
								double hilf1=(2*Math.PI*Math.cos(Math.toRadians(Strecke[1][zahl]))*6371)/(2*Math.PI);//Die anderen Koordinaten muessen in der Naehe sein
								double hilf2=hilf1*Math.PI/180;
								double hilf3=Math.sqrt(Math.pow(6371*Math.PI/180*(Strecke[1][zahl-1]-Strecke[1][zahl]),2)+
										Math.pow(hilf2*(Strecke[0][zahl-1]-Strecke[0][zahl]),2));

								Streckenlaegne = Streckenlaegne + hilf3;//Kilometerangabe
							}//if (Strecke[0][zahl-1]!=0 || Strecke[1][zahl-1]!=0) {
						}//if (zahl>0 && zahl<ges_zeile[lfdn]) {

						Strecke[2][zahl]=koords_ki[0][2][zahl];
						Strecke[3][zahl]=koords_ki[0][3][zahl];
						Strecke[4][zahl]=koords_ki[0][4][zahl];
					}//if (lfdn==0) {

					char[] inputBuffer = new char[90000];
					inputBuffer=perform_konverter[lfdn][perform_za].toCharArray();
					for (int n=0; n<perform_konverter[lfdn][perform_za].indexOf('\n')+1;n++) {
						inputBuffer[n]='\u0000';
					}//For
					perform_konverter[lfdn][perform_za]=new String(inputBuffer);
					xtxt="";
					zahl++;
				}//while

				int anz_hilf=0;
				if (challenge_mode==true) {
					anz_hilf=anz_spieler-1;
				}else {
					anz_hilf=anz_spieler;
				}//if (challenge_mode==true) {
				if (perform_length == perform_za && lfdn==anz_hilf) {
					for (int hilf=0; hilf<lfdn+1; hilf++) {
						boolean test_zahl=true;
						int test_za=8999;//Achtung abhaengig vom Speichergroesse des Arrays kords_ki ...
						while (test_zahl) {
							if (koords_ki[hilf][4][test_za]>0) {
								test_zahl=false;
								last_zahl[hilf]=test_za;
								first_ges_zeile[hilf]=test_za;
							} else {
								test_za--;
							}//if (koords_ki[hilf][3][test_za]>0) {
						}//while
					}//for (int hilf=0; hilf<lfdn; hilf++) {
					//last_zahl[] gibt die Laenge des Arrays an, weil sie fuer unt. lfdn unt. Laengen gibt.

					for (int x=1; x<anz_spieler+1; x++) {
						// Spieler 0 ist der erste Gegner, (es gibt nicht den Spieler bzw. Laeufer in diesem Array)
						if (challenge_mode==true) {
							Spieler[x][0][0][1]= Integer.valueOf((int) (Streckenlaegne*1000/((koords_ki[x-1][2][last_zahl[x-1]]*3600+koords_ki[x-1][3][last_zahl[x-1]]*60+koords_ki[x-1][4][last_zahl[x-1]]))*36));//m/s oder x*36 fuer *10 km/h, weil halt kein Double
						} else {
							Spieler[x][0][0][1]= Integer.valueOf((int) (Streckenlaegne*1000/((koords_ki[x][2][last_zahl[x]]*3600+koords_ki[x][3][last_zahl[x]]*60+koords_ki[x][4][last_zahl[x]]))*36));//m/s oder x*36 fuer *10 km/h, weil halt kein Double
						}//if (challenge_mode==true) {
					}//for (int x=1; x<anz_spieler; x++) {
					register_process();
				}//if (perform_length == perform_za) {
			}catch(Exception e) {
				e.printStackTrace();
			}//Try

		}//run
	}//class new_runnable implements Runnable {

	/*
	 * bitte bei allen Events nach Abspiel
	 * der Audiodateien "flag_sequenz" auf false
	 * setzen
	 */


	//Start Sven und Jul
	public void start_sequenz(){
		if (flag_sequenz!=true) {
			Random random = new Random();
			int vorstell_zufall = random.nextInt(6);
			if (vorstell_zufall==0) {
				int[] audiosequenz = new int[3];
				audiosequenz[0]=R.raw.start0_a;
				audiosequenz[1]=inttoresid_jul(anz_spieler+1,false);//Weil der Laeufer auch mitzaehlt
				audiosequenz[2]=R.raw.start0_b;
				audiosequenzplay(audiosequenz, 3);
			}//if (vorstell_zufall==0) {
			if (vorstell_zufall==1) {
				int[] audiosequenz = new int[2];
				audiosequenz[0]=R.raw.start1_a;
				audiosequenz[1]=R.raw.start1_b;
				audiosequenzplay(audiosequenz, 2);
			}//if (vorstell_zufall==0) {
			if (vorstell_zufall==2) {
				int[] audiosequenz = new int[3];
				audiosequenz[0]=R.raw.start2_a;
				audiosequenz[1]=inttoresid_jul(anz_spieler+1,false);//Weil der Laeufer auch mitzaehlt
				audiosequenz[2]=R.raw.start2_b;
				audiosequenzplay(audiosequenz, 3);
			}//if (vorstell_zufall==0) {
			if (vorstell_zufall==3) {
				int[] audiosequenz = new int[2];
				audiosequenz[0]=R.raw.start3_a;
				audiosequenz[1]=R.raw.start3_b;
				audiosequenzplay(audiosequenz, 2);
			}//if (vorstell_zufall==0) {
			if (vorstell_zufall==4) {
				int[] audiosequenz = new int[3];
				audiosequenz[0]=R.raw.start4_a;
				audiosequenz[1]=inttoresid_sven(anz_spieler+1,false);//Weil der Laeufer auch mitzaehlt
				audiosequenz[2]=R.raw.start4_b;
				audiosequenzplay(audiosequenz, 3);
			}//if (vorstell_zufall==0) {
			if (vorstell_zufall==5) {
				int[] audiosequenz = new int[2];
				audiosequenz[0]=R.raw.start5_a;
				audiosequenz[1]=R.raw.start5_b;
				audiosequenzplay(audiosequenz, 2);
			}//if (vorstell_zufall==0) {
		}//if (flag_sequenz!=true) {
		zuschauerplay(true);
	}//public void hello(){

	private void challenge_events() {
		if (flag_sequenz!=true) {
			//ueberholt positiv Sven und Jul
			if (flag_ueberholt_positiv==true) {
				int spieler = Spieler[0][0][1][0];
				int ueberholte = 0;
				for (int x=1; x<anz_spieler+1;x++) {
					if (Spieler[x][0][1][0]==spieler+1 || Spieler[x][0][1][0]==spieler+2){
						ueberholte=Spieler[x][0][0][0];
					}//if (Spieler[x][0][1]==spieler-1){
				}//for (int x=1; x<anz_spieler+1;x++) {
				//ueberholte ist die Spieler-ID des ueberholten
				Random random = new Random();
				int vorstell_zufall = random.nextInt(5);
				if (vorstell_zufall==0) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.ueberholt_positiv_a_1;
					audiosequenz[1]=inttoresid_jul(ueberholte, true);
					audiosequenz[2]=R.raw.ueberholt_positiv_b_1;
					audiosequenzplay(audiosequenz, 3);
					flag_ueberholt_positiv=false;
				}//if (vorstell_zufall==0) {
				if (vorstell_zufall==1) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.ueberholt_positiv_a_2;
					audiosequenz[1]=inttoresid_sven(ueberholte, true);
					audiosequenz[2]=R.raw.ueberholt_positiv_b_2;
					audiosequenzplay(audiosequenz, 3);
					flag_ueberholt_positiv=false;
				}//if (vorstell_zufall==0) {
				if (vorstell_zufall==2) {
					int[] audiosequenz = new int[2];
					audiosequenz[0]=R.raw.ueberholt_positiv_a_3;
					audiosequenz[1]=inttoresid_sven(ueberholte, true);
					audiosequenzplay(audiosequenz, 2);
					flag_ueberholt_positiv=false;
				}//if (vorstell_zufall==0) {
				if (vorstell_zufall==3) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.ueberholt_positiv_a_4;
					audiosequenz[1]=inttoresid_sven(ueberholte, true);
					audiosequenz[2]=R.raw.ueberholt_positiv_b_2;
					audiosequenzplay(audiosequenz, 3);
					flag_ueberholt_positiv=false;
				}//if (vorstell_zufall==0) {
			}//if (flag_ueberholt==true) {
		}//if (flag_sequenz!=true) {


		if (flag_sequenz!=true) {
			//Spieler wurde ueberholt
			if (flag_ueberholt_negativ==true) {
				int spieler = Spieler[0][0][1][0];
				int ueberholt = 0;
				for (int x=1; x<anz_spieler+1;x++) {
					if (Spieler[x][0][1][0]==spieler-1 || Spieler[x][0][1][0]==spieler-2){
						ueberholt=Spieler[x][0][0][0];
					}//if (Spieler[x][0][1]==spieler-1){
				}//for (int x=1; x<anz_spieler+1;x++) {
				//ueberholt ist die Spieler-ID des Gegners der ueberholt hat

				Random random = new Random();
				int vorstell_zufall = random.nextInt(4);
				if (vorstell_zufall==0) {
					int[] audiosequenz = new int[4];
					audiosequenz[0]=R.raw.ueberholt_negativ_a_0;
					audiosequenz[1]=inttoresid_jul(21, true);
					audiosequenz[2]=R.raw.ueberholt_negativ_b_0;
					audiosequenz[3]=inttoresid_jul(ueberholt, true);
					audiosequenzplay(audiosequenz, 4);
					flag_ueberholt_negativ=false;
				}//if (vorstell_zufall==0) {
				if (vorstell_zufall==1) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.ueberholt_negativ_a_1;
					audiosequenz[1]=inttoresid_jul(ueberholt, true);
					audiosequenz[2]=R.raw.ueberholt_negativ_b_1;
					audiosequenzplay(audiosequenz, 3);
					flag_ueberholt_negativ=false;
				}//if (vorstell_zufall==0) {
				if (vorstell_zufall==2) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.ueberholt_negativ_a_2;
					audiosequenz[1]=inttoresid_sven(ueberholt, true);
					audiosequenz[2]=R.raw.ueberholt_negativ_b_2;
					audiosequenzplay(audiosequenz, 3);
					flag_ueberholt_negativ=false;
				}//if (vorstell_zufall==0) {
				if (vorstell_zufall==3) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.ueberholt_negativ_a_4;
					audiosequenz[1]=inttoresid_sven(ueberholt, true);
					audiosequenz[2]=R.raw.ueberholt_positiv_b_2;
					audiosequenzplay(audiosequenz, 3);
					flag_ueberholt_negativ=false;
				}//if (vorstell_zufall==0) {
			}//if (flag_ueberholt==true) {
			/*
			 * Variable ueberholte ist die Angabe des Gegners der ueberholt wurde
			 * Variable ueberholt ist die Angabe des Gegners der den Spieler ueberholt hat
			 */
		}//if (flag_sequenz!=true) {

		if (flag_sequenz!=true) {
			//Schlusssequenz Sven und Jul
			if (zielgerade_ansage==false) {
				if (Spieler[0][1][0][0]>9000) {
					Random random = new Random();
					int vorstell_zufall = random.nextInt(5);
					if (vorstell_zufall==0) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.ziel0;
						audiosequenzplay(audiosequenz, 1);
						zielgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==1) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.ziel1;
						audiosequenzplay(audiosequenz, 1);
						zielgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==2) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.ziel2;
						audiosequenzplay(audiosequenz, 1);
						zielgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==3) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.ziel3;
						audiosequenzplay(audiosequenz, 1);
						zielgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==4) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.ziel4;
						audiosequenzplay(audiosequenz, 1);
						zielgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					zuschauerplay(false);
				}//if (Spieler[0][1][0]>=90) {
			}//if (zielgerade_ansage==false) {
		}//if (flag_sequenz!=true) {

		if (flag_sequenz!=true) {
			//Ansage Sven und Jul Mitte erreicht
			if (mittelgerade_ansage==false) {
				if (Spieler[0][1][0][0]>5000) {
					Random random = new Random();
					int vorstell_zufall = random.nextInt(6);
					if (vorstell_zufall==0) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.mittel0;
						audiosequenzplay(audiosequenz, 1);
						mittelgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==1) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.mittel1;
						audiosequenzplay(audiosequenz, 1);
						mittelgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==2) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.mittel2;
						audiosequenzplay(audiosequenz, 1);
						mittelgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==3) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.mittel3;
						audiosequenzplay(audiosequenz, 1);
						mittelgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==4) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.mittel4;
						audiosequenzplay(audiosequenz, 1);
						mittelgerade_ansage=true;
					}//if (vorstell_zufall==0) {
					if (vorstell_zufall==5) {
						int[] audiosequenz = new int[1];
						audiosequenz[0]=R.raw.mittel5;
						audiosequenzplay(audiosequenz, 1);
						mittelgerade_ansage=true;
					}//if (vorstell_zufall==0) {
				}//if (Spieler[0][1][0]>=90) {
			}//if (zielgerade_ansage==false) {
		}//if (flag_sequenz!=true) {

		if (flag_sequenz!=true) {
			if (streckeninfo==false) {
				if (Spieler[0][1][0][0]>500) {
					Calendar c = new GregorianCalendar();
					Date today = c.getTime();
					Random random = new Random();
					int vorstell_zufall = random.nextInt(4);

					if (vorstell_zufall==0) {
						int[] audiosequenz = new int[5];
						int akt_houre = today.getHours();
						if (akt_houre>5 && akt_houre<=10) {
							audiosequenz[0]=R.raw.streckeninfo_frueh0;
						}//if (akt_houre>5 && akt_houre<=10) {

						int akt_day =today.getDay();
						if (akt_houre>10 && akt_houre<=12) {
							if (akt_day == 0) {
								audiosequenz[0]=R.raw.streckeninfo_vormittag_sunday0;
							} else {
								audiosequenz[0]=R.raw.streckeninfo_vormittag0;
							}//if (akt_day != 1) {
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>12 && akt_houre<=14) {
							audiosequenz[0]=R.raw.streckeninfo_mittag0;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>14 && akt_houre<=18) {
							audiosequenz[0]=R.raw.streckeninfo_nachmittag0;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>18 && akt_houre<=23) {
							audiosequenz[0]=R.raw.streckeninfo_abends0;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>23 || akt_houre<=5) {
							audiosequenz[0]=R.raw.streckeninfo_nachts0;
						}//if (akt_houre>5 && akt_houre<=10) {

						if (anz_spieler>0 && anz_spieler<=4) {
							audiosequenz[1]=R.raw.streckeninfo_unbek0;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>4 && anz_spieler<=10) {
							audiosequenz[1]=R.raw.streckeninfo_bek0;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>10 && anz_spieler<=20) {
							audiosequenz[1]=R.raw.streckeninfo_gewohnt0;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						audiosequenz[2]=R.raw.streckeninfo_laenge0;
						audiosequenz[3]=inttoresid_jul((int)Streckenlaegne, false);
						audiosequenz[4]=R.raw.km0;
						audiosequenzplay(audiosequenz, 5);
						streckeninfo=true;
					}//if (vorstell_zufall==0) {

					if (vorstell_zufall==1) {
						int[] audiosequenz = new int[5];
						int akt_houre = today.getHours();
						if (akt_houre>5 && akt_houre<=10) {
							audiosequenz[0]=R.raw.streckeninfo_frueh1;
						}//if (akt_houre>5 && akt_houre<=10) {

						int akt_day =today.getDay();
						if (akt_houre>10 && akt_houre<=12) {
							if (akt_day == 0) {
								audiosequenz[0]=R.raw.streckeninfo_vormittag_sunday1;
							} else {
								audiosequenz[0]=R.raw.streckeninfo_vormittag2;
							}//if (akt_day != 1) {
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>12 && akt_houre<=14) {
							audiosequenz[0]=R.raw.streckeninfo_mittag1;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>14 && akt_houre<=18) {
							audiosequenz[0]=R.raw.streckeninfo_nachmittag1;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>18 && akt_houre<=23) {
							audiosequenz[0]=R.raw.streckeninfo_abends1;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>23 || akt_houre<=5) {
							audiosequenz[0]=R.raw.streckeninfo_nachts1;
						}//if (akt_houre>5 && akt_houre<=10) {

						if (anz_spieler>0 && anz_spieler<=4) {
							audiosequenz[1]=R.raw.streckeninfo_unbek1;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>4 && anz_spieler<=10) {
							audiosequenz[1]=R.raw.streckeninfo_bek1;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>10 && anz_spieler<=20) {
							audiosequenz[1]=R.raw.streckeninfo_gewohnt1;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						audiosequenz[2]=R.raw.streckeninfo_laenge1;
						audiosequenz[3]=inttoresid_jul((int)Streckenlaegne, false);
						audiosequenz[4]=R.raw.km1;
						audiosequenzplay(audiosequenz, 5);
						streckeninfo=true;
					}//if (vorstell_zufall==0) {

					if (vorstell_zufall==2) {
						int[] audiosequenz = new int[5];
						int akt_houre = today.getHours();
						if (akt_houre>5 && akt_houre<=10) {
							audiosequenz[0]=R.raw.streckeninfo_frueh2;
						}//if (akt_houre>5 && akt_houre<=10) {

						int akt_day =today.getDay();
						if (akt_houre>10 && akt_houre<=12) {
							if (akt_day == 0) {
								audiosequenz[0]=R.raw.streckeninfo_vormittag_sunday2;
							} else {
								audiosequenz[0]=R.raw.streckeninfo_vormittag2;
							}//if (akt_day != 1) {
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>12 && akt_houre<=14) {
							audiosequenz[0]=R.raw.streckeninfo_mittag2;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>14 && akt_houre<=18) {
							audiosequenz[0]=R.raw.streckeninfo_nachmittag2;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>18 && akt_houre<=23) {
							audiosequenz[0]=R.raw.streckeninfo_abends2;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>23 || akt_houre<=5) {
							audiosequenz[0]=R.raw.streckeninfo_nachts2;
						}//if (akt_houre>5 && akt_houre<=10) {

						if (anz_spieler>0 && anz_spieler<=4) {
							audiosequenz[1]=R.raw.streckeninfo_unbek2;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>4 && anz_spieler<=10) {
							audiosequenz[1]=R.raw.streckeninfo_bek2;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>10 && anz_spieler<=20) {
							audiosequenz[1]=R.raw.streckeninfo_gewohnt2;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						audiosequenz[2]=R.raw.streckeninfo_laenge2;
						audiosequenz[3]=inttoresid_sven((int)Streckenlaegne, false);
						audiosequenz[4]=R.raw.km2;
						audiosequenzplay(audiosequenz, 5);
						streckeninfo=true;
					}//if (vorstell_zufall==0) {

					if (vorstell_zufall==3) {
						int[] audiosequenz = new int[5];
						int akt_houre = today.getHours();
						if (akt_houre>5 && akt_houre<=10) {
							audiosequenz[0]=R.raw.streckeninfo_frueh3;
						}//if (akt_houre>5 && akt_houre<=10) {

						int akt_day =today.getDay();
						if (akt_houre>10 && akt_houre<=12) {
							if (akt_day == 0) {
								audiosequenz[0]=R.raw.streckeninfo_vormittag_sunday3;
							} else {
								audiosequenz[0]=R.raw.streckeninfo_vormittag3;
							}//if (akt_day != 1) {
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>12 && akt_houre<=14) {
							audiosequenz[0]=R.raw.streckeninfo_mittag3;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>14 && akt_houre<=18) {
							audiosequenz[0]=R.raw.streckeninfo_nachmittag3;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>18 && akt_houre<=23) {
							audiosequenz[0]=R.raw.streckeninfo_abends3;
						}//if (akt_houre>5 && akt_houre<=10) {
						if (akt_houre>23 || akt_houre<=5) {
							audiosequenz[0]=R.raw.streckeninfo_nachts3;
						}//if (akt_houre>5 && akt_houre<=10) {

						if (anz_spieler>0 && anz_spieler<=4) {
							audiosequenz[1]=R.raw.streckeninfo_unbek3;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>4 && anz_spieler<=10) {
							audiosequenz[1]=R.raw.streckeninfo_bek3;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						if (anz_spieler>10 && anz_spieler<=20) {
							audiosequenz[1]=R.raw.streckeninfo_gewohnt3;
						}//if (anz_spieler>2 && anz_spieler<=4) {
						audiosequenz[2]=R.raw.streckeninfo_laenge3;
						audiosequenz[3]=inttoresid_sven((int)Streckenlaegne, false);
						audiosequenz[4]=R.raw.km3;
						audiosequenzplay(audiosequenz, 5);
						streckeninfo=true;
					}//if (vorstell_zufall==0) {
				}//if (Spieler[0][1][0]>=500) {
			}//if (Streckeninfo==false) {
		}//if (flag_sequenz!=true) {

		if (flag_sequenz!=true) {
			//Gebuendelte ueberholmanoever Sven und Jul
			if (kom_wait==true && kom_wait_za>3) {
				kom_wait_za=0;
				if (Spieler[0][1][0][0]<3000) {
					int[] audiosequenz = new int[1];
					Random random = new Random();
					int kom_zufall = random.nextInt(9);
					if (kom_zufall==0) {
						audiosequenz[0]=R.raw.kom_wait_30_1;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==1) {
					if (kom_zufall==1) {
						audiosequenz[0]=R.raw.kom_wait_30_2;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==2) {
						audiosequenz[0]=R.raw.kom_wait_30_3;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==3) {
					if (kom_zufall==3) {
						audiosequenz[0]=R.raw.kom_wait_30_4;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==3) {
					if (kom_zufall==4) {
						audiosequenz[0]=R.raw.kom_wait_30_5;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==3) {
					if (kom_zufall==5) {
						audiosequenz[0]=R.raw.kom_wait_30_6;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==3) {
					if (kom_zufall==6) {
						audiosequenz[0]=R.raw.kom_wait_30_7;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==3) {
					if (kom_zufall==7) {
						audiosequenz[0]=R.raw.kom_wait_30_8;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==3) {
					if (kom_zufall==8) {
						audiosequenz[0]=R.raw.kom_wait_30_9;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==3) {
				}//if (Spieler[0][1][0]<3000) {

				if (Spieler[0][1][0][0]>3000 && Spieler[0][1][0][0]<9000) {
					int[] audiosequenz = new int[1];
					Random random = new Random();
					int kom_zufall = random.nextInt(9);
					if (kom_zufall==0) {
						audiosequenz[0]=R.raw.kom_wait_60_1;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==1) {
					if (kom_zufall==1) {
						audiosequenz[0]=R.raw.kom_wait_60_2;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==2) {
						audiosequenz[0]=R.raw.kom_wait_60_3;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==3) {
						audiosequenz[0]=R.raw.kom_wait_60_4;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==4) {
						audiosequenz[0]=R.raw.kom_wait_60_5;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==5) {
						audiosequenz[0]=R.raw.kom_wait_60_6;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==6) {
						audiosequenz[0]=R.raw.kom_wait_60_7;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==7) {
						audiosequenz[0]=R.raw.kom_wait_60_8;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
					if (kom_zufall==8) {
						audiosequenz[0]=R.raw.kom_wait_60_9;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==2) {
				}//if (Spieler[0][1][0]<6000) {

				if (Spieler[0][1][0][0]>9000) {
					int[] audiosequenz = new int[1];
					Random random = new Random();
					int kom_zufall = random.nextInt(9);
					if (kom_zufall==0) {
						audiosequenz[0]=R.raw.kom_wait_90_1;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==1) {
						audiosequenz[0]=R.raw.kom_wait_90_2;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==2) {
						audiosequenz[0]=R.raw.kom_wait_90_3;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==3) {
						audiosequenz[0]=R.raw.kom_wait_90_4;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==4) {
						audiosequenz[0]=R.raw.kom_wait_90_5;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==5) {
						audiosequenz[0]=R.raw.kom_wait_90_6;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==6) {
						audiosequenz[0]=R.raw.kom_wait_90_7;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==7) {
						audiosequenz[0]=R.raw.kom_wait_90_8;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
					if (kom_zufall==8) {
						audiosequenz[0]=R.raw.kom_wait_90_9;
						audiosequenzplay(audiosequenz, 1);
					}//if (kom_zufall==0) {
				}//if (Spieler[0][1][0]>6000) {
			}//if (kom_wait==true && kom_wait_za>3) {
		}//if (flag_sequenz!=true) {

		if (flag_sequenz!=true) {
			if (spielerperformanceansage==true && Spieler[0][1][0][0]>2500 && anz_spieler>2) {
				Random random = new Random();
				int vorstell_zufall = random.nextInt(6);

				int[][] personen = new int[3][2];
				int pers_za=0;
				for (int n = 2; n>-1; n--) {
					if (spieler_performance_place[n][2]>0) {
						personen[pers_za][0]=spieler_performance_place[n][2];
						personen[pers_za][1]=spieler_performance_place[n][3];
						pers_za++;
					}//if (spieler_performance_place[n][2]>0) {
					if (spieler_performance_place[n][0]>0) {
						personen[pers_za][0]=spieler_performance_place[n][0];
						personen[pers_za][1]=spieler_performance_place[n][1];
						pers_za++;
					}//if (spieler_performance_place[n][0]>0) {
				}//for (int n = 2; n>-1; n--) {

				if (vorstell_zufall==0) {
					//Einleitung
					int[] audiosequenz = new int[16];
					audiosequenz[0]=R.raw.vorstellung_einleitung_v1;
					//Vorstellung Person 3
					audiosequenz[1]=R.raw.vorstellung_3_v1;
					audiosequenz[2]=inttoresid_jul(personen[0][0], true);
					if (personen[0][1]<=10) {
						audiosequenz[3]=R.raw.unt_10_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>10) {
						audiosequenz[3]=R.raw.gr_11_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>11) {
						audiosequenz[3]=R.raw.gr_12_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>12) {
						audiosequenz[3]=R.raw.gr_13_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>13) {
						audiosequenz[3]=R.raw.gr_14_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 2
					audiosequenz[6]=R.raw.vorstellung_2_v1;
					audiosequenz[7]=inttoresid_jul(personen[1][0], true);
					if (personen[1][1]<=10) {
						audiosequenz[8]=R.raw.unt_10_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>10) {
						audiosequenz[8]=R.raw.gr_11_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>11) {
						audiosequenz[8]=R.raw.gr_12_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>12) {
						audiosequenz[8]=R.raw.gr_13_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>13) {
						audiosequenz[8]=R.raw.gr_14_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 1
					audiosequenz[11]=R.raw.vorstellung_1_v1;
					audiosequenz[12]=inttoresid_jul(personen[2][0], true);
					if (personen[2][1]<=10) {
						audiosequenz[13]=R.raw.unt_10_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>10) {
						audiosequenz[13]=R.raw.gr_11_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>11) {
						audiosequenz[13]=R.raw.gr_12_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>12) {
						audiosequenz[13]=R.raw.gr_13_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>13) {
						audiosequenz[13]=R.raw.gr_14_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					audiosequenzplay(audiosequenz, 16);
				}//zufall==0

				if (vorstell_zufall==1) {
					//Einleitung
					int[] audiosequenz = new int[16];
					audiosequenz[0]=R.raw.vorstellung_einleitung_v2;
					//Vorstellung Person 3
					audiosequenz[1]=R.raw.vorstellung_3_v2;
					audiosequenz[2]=inttoresid_jul(personen[0][0], true);
					if (personen[0][1]<=10) {
						audiosequenz[3]=R.raw.unt_10_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>10) {
						audiosequenz[3]=R.raw.gr_11_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>11) {
						audiosequenz[3]=R.raw.gr_12_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>12) {
						audiosequenz[3]=R.raw.gr_13_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>13) {
						audiosequenz[3]=R.raw.gr_14_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 2
					audiosequenz[6]=R.raw.vorstellung_2_v2;
					audiosequenz[7]=inttoresid_jul(personen[1][0], true);
					if (personen[1][1]<=10) {
						audiosequenz[8]=R.raw.unt_10_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>10) {
						audiosequenz[8]=R.raw.gr_11_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>11) {
						audiosequenz[8]=R.raw.gr_12_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>12) {
						audiosequenz[8]=R.raw.gr_13_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>13) {
						audiosequenz[8]=R.raw.gr_14_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 1
					audiosequenz[11]=R.raw.vorstellung_1_v2;
					audiosequenz[12]=inttoresid_jul(personen[2][0], true);
					if (personen[2][1]<=10) {
						audiosequenz[13]=R.raw.unt_10_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>10) {
						audiosequenz[13]=R.raw.gr_11_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>11) {
						audiosequenz[13]=R.raw.gr_12_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>12) {
						audiosequenz[13]=R.raw.gr_13_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>13) {
						audiosequenz[13]=R.raw.gr_14_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					audiosequenzplay(audiosequenz, 16);
				}//zufall==1

				if (vorstell_zufall==2) {
					//Einleitung
					int[] audiosequenz = new int[16];
					audiosequenz[0]=R.raw.vorstellung_einleitung_v3;
					//Vorstellung Person 3
					audiosequenz[1]=R.raw.vorstellung_3_v3;
					audiosequenz[2]=inttoresid_jul(personen[0][0], true);
					if (personen[0][1]<=10) {
						audiosequenz[3]=R.raw.unt_10_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>10) {
						audiosequenz[3]=R.raw.gr_11_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>11) {
						audiosequenz[3]=R.raw.gr_12_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>12) {
						audiosequenz[3]=R.raw.gr_13_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>13) {
						audiosequenz[3]=R.raw.gr_14_jul;
						audiosequenz[4]=inttoresid_jul(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 2
					audiosequenz[6]=R.raw.vorstellung_2_v3;
					audiosequenz[7]=inttoresid_jul(personen[1][0], true);
					if (personen[1][1]<=10) {
						audiosequenz[8]=R.raw.unt_10_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>10) {
						audiosequenz[8]=R.raw.gr_11_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>11) {
						audiosequenz[8]=R.raw.gr_12_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>12) {
						audiosequenz[8]=R.raw.gr_13_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>13) {
						audiosequenz[8]=R.raw.gr_14_jul;
						audiosequenz[9]=inttoresid_jul(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 1
					audiosequenz[11]=R.raw.vorstellung_1_v3;
					audiosequenz[12]=inttoresid_jul(personen[2][0], true);
					if (personen[2][1]<=10) {
						audiosequenz[13]=R.raw.unt_10_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>10) {
						audiosequenz[13]=R.raw.gr_11_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>11) {
						audiosequenz[13]=R.raw.gr_12_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>12) {
						audiosequenz[13]=R.raw.gr_13_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>13) {
						audiosequenz[13]=R.raw.gr_14_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					audiosequenzplay(audiosequenz, 16);
				}//zufall==1

				if (vorstell_zufall==3) {
					//Einleitung
					int[] audiosequenz = new int[16];
					audiosequenz[0]=R.raw.vorstellung_einleitung_v4;
					//Vorstellung Person 3
					audiosequenz[1]=R.raw.vorstellung_3_v4;
					audiosequenz[2]=inttoresid_sven(personen[0][0], true);
					if (personen[0][1]<=10) {
						audiosequenz[3]=R.raw.unt_10_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>10) {
						audiosequenz[3]=R.raw.gr_11_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>11) {
						audiosequenz[3]=R.raw.gr_12_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>12) {
						audiosequenz[3]=R.raw.gr_13_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>13) {
						audiosequenz[3]=R.raw.gr_14_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 2
					audiosequenz[6]=R.raw.vorstellung_2_v4;
					audiosequenz[7]=inttoresid_sven(personen[1][0], true);
					if (personen[1][1]<=10) {
						audiosequenz[8]=R.raw.unt_10_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>10) {
						audiosequenz[8]=R.raw.gr_11_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>11) {
						audiosequenz[8]=R.raw.gr_12_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>12) {
						audiosequenz[8]=R.raw.gr_13_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>13) {
						audiosequenz[8]=R.raw.gr_14_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 1
					audiosequenz[11]=R.raw.vorstellung_1_v1;
					audiosequenz[12]=inttoresid_jul(personen[2][0], true);
					if (personen[2][1]<=10) {
						audiosequenz[13]=R.raw.unt_10_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>10) {
						audiosequenz[13]=R.raw.gr_11_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>11) {
						audiosequenz[13]=R.raw.gr_12_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>12) {
						audiosequenz[13]=R.raw.gr_13_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>13) {
						audiosequenz[13]=R.raw.gr_14_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					audiosequenzplay(audiosequenz, 16);
				}//zufall==1

				if (vorstell_zufall==4) {
					//Einleitung
					int[] audiosequenz = new int[16];
					audiosequenz[0]=R.raw.vorstellung_einleitung_v5;
					//Vorstellung Person 3
					audiosequenz[1]=R.raw.vorstellung_3_v5;
					audiosequenz[2]=inttoresid_sven(personen[0][0], true);
					if (personen[0][1]<=10) {
						audiosequenz[3]=R.raw.unt_10_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>10) {
						audiosequenz[3]=R.raw.gr_11_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>11) {
						audiosequenz[3]=R.raw.gr_12_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>12) {
						audiosequenz[3]=R.raw.gr_13_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>13) {
						audiosequenz[3]=R.raw.gr_14_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 2
					audiosequenz[6]=R.raw.vorstellung_2_v5;
					audiosequenz[7]=inttoresid_sven(personen[1][0], true);
					if (personen[1][1]<=10) {
						audiosequenz[8]=R.raw.unt_10_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>10) {
						audiosequenz[8]=R.raw.gr_11_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>11) {
						audiosequenz[8]=R.raw.gr_12_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>12) {
						audiosequenz[8]=R.raw.gr_13_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>13) {
						audiosequenz[8]=R.raw.gr_14_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 1
					audiosequenz[11]=R.raw.vorstellung_1_v2;
					audiosequenz[12]=inttoresid_jul(personen[2][0], true);
					if (personen[2][1]<=10) {
						audiosequenz[13]=R.raw.unt_10_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>10) {
						audiosequenz[13]=R.raw.gr_11_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>11) {
						audiosequenz[13]=R.raw.gr_12_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>12) {
						audiosequenz[13]=R.raw.gr_13_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>13) {
						audiosequenz[13]=R.raw.gr_14_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					audiosequenzplay(audiosequenz, 16);
				}//zufall==1

				if (vorstell_zufall==5) {
					//Einleitung
					int[] audiosequenz = new int[16];
					audiosequenz[0]=R.raw.vorstellung_einleitung_v6;
					//Vorstellung Person 3
					audiosequenz[1]=R.raw.vorstellung_3_v6;
					audiosequenz[2]=inttoresid_sven(personen[0][0], true);
					if (personen[0][1]<=10) {
						audiosequenz[3]=R.raw.unt_10_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>10) {
						audiosequenz[3]=R.raw.gr_11_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>11) {
						audiosequenz[3]=R.raw.gr_12_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>12) {
						audiosequenz[3]=R.raw.gr_13_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[0][1]>13) {
						audiosequenz[3]=R.raw.gr_14_sven;
						audiosequenz[4]=inttoresid_sven(personen[0][1], false);
						audiosequenz[5]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 2
					audiosequenz[6]=R.raw.vorstellung_2_v6;
					audiosequenz[7]=inttoresid_sven(personen[1][0], true);
					if (personen[1][1]<=10) {
						audiosequenz[8]=R.raw.unt_10_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>10) {
						audiosequenz[8]=R.raw.gr_11_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>11) {
						audiosequenz[8]=R.raw.gr_12_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>12) {
						audiosequenz[8]=R.raw.gr_13_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v2_sven;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[1][1]>13) {
						audiosequenz[8]=R.raw.gr_14_sven;
						audiosequenz[9]=inttoresid_sven(personen[1][1], false);
						audiosequenz[10]=R.raw.kmh_v1_sven;
					}//if (spieler_performance_place[1][1]<12) {
					//Vorstellung Person 1
					audiosequenz[11]=R.raw.vorstellung_1_v3;
					audiosequenz[12]=inttoresid_jul(personen[2][0], true);
					if (personen[2][1]<=10) {
						audiosequenz[13]=R.raw.unt_10_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>10) {
						audiosequenz[13]=R.raw.gr_11_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>11) {
						audiosequenz[13]=R.raw.gr_12_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>12) {
						audiosequenz[13]=R.raw.gr_13_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v2_jul;
					}//if (spieler_performance_place[1][1]<12) {
					if (personen[2][1]>13) {
						audiosequenz[13]=R.raw.gr_14_jul;
						audiosequenz[14]=inttoresid_jul(personen[2][1], false);
						audiosequenz[15]=R.raw.kmh_v1_jul;
					}//if (spieler_performance_place[1][1]<12) {
					audiosequenzplay(audiosequenz, 16);
				}//zufall==1
				spielerperformanceansage=false;
			}//if (spielerperformanceansage==true && Spieler[0][1][0][0]>500 && anz_spieler>2) {
		}//if (flag_sequenz!=true) {

		if (flag_sequenz!=true) {
			if (finale_laeufer_flag==true) {
				Random random = new Random();
				int vorstell_zufall = random.nextInt(4);
				try {
					if (vorstell_zufall==0) {
						boolean fin_flag=true;
						int fin=0;
						while(fin_flag) {
							if (finale[fin][0]==0) {
								fin_flag=false;
								if (anz_spieler>2) {
									if(finale[fin-1][2]==finale[fin-1][3]) {
										//Erwartung erfuellt
										int[] audiosequenz = new int[6];
										audiosequenz[0]=R.raw.finalea0;
										audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
										audiosequenz[2]=R.raw.finaleb0;
										audiosequenz[3]=inttoresid_sven(finale[fin-1][3], true);
										audiosequenz[4]=R.raw.finalec0;
										audiosequenz[5]=R.raw.finaleperforma0;
										audiosequenzplay(audiosequenz, 6);
									} else {
										if (finale[fin-1][2]>finale[fin-1][3]) {
											//Erwartung erfuellt schlecht
											int[] audiosequenz = new int[6];
											audiosequenz[0]=R.raw.finalea0;
											audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb0;
											audiosequenz[3]=inttoresid_sven(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec0;
											audiosequenz[5]=R.raw.finaleperformb_0;
											audiosequenzplay(audiosequenz, 6);
										}else {
											//Erwartung erfuellt gut
											int[] audiosequenz = new int[6];
											audiosequenz[0]=R.raw.finalea0;
											audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb0;
											audiosequenz[3]=inttoresid_sven(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec0;
											audiosequenz[5]=R.raw.finaleperformb_0;
											audiosequenzplay(audiosequenz, 6);
										}//if (finale[fin-1][1]>finale[fin-1][2]) {
									}//if(finale[fin-1][1]==finale[fin-1][2]) {
								} else {
									//es gibt nur 2 Laeufer
									int[] audiosequenz = new int[6];
									audiosequenz[0]=R.raw.finalea0;
									audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
									audiosequenz[2]=R.raw.finaleb0;
									audiosequenz[3]=inttoresid_sven(finale[fin-1][1], true);
									audiosequenz[4]=R.raw.finalec0;
									audiosequenz[5]=R.raw.finaleperforma0;
									audiosequenzplay(audiosequenz, 6);
								}//if (anz_spieler>2) {
							} else {
								fin++;
							}//if (finale[fin][0]==0) {
						}//while(fin_flag) {
					}//if (vorstell_zufall==0) {

					if (vorstell_zufall==1) {
						boolean fin_flag=true;
						int fin=0;
						while(fin_flag) {
							if (finale[fin][0]==0) {
								fin_flag=false;
								if (anz_spieler>2) {
									if(finale[fin-1][2]==finale[fin-1][3]) {
										//Erwartung erfuellt
										int[] audiosequenz = new int[6];
										audiosequenz[0]=R.raw.finalea1;
										audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
										audiosequenz[2]=R.raw.finaleb1;
										audiosequenz[3]=inttoresid_sven(finale[fin-1][3], true);
										audiosequenz[4]=R.raw.finalec1;
										audiosequenz[5]=R.raw.finaleperforma1;
										audiosequenzplay(audiosequenz, 6);
									} else {
										if (finale[fin-1][2]>finale[fin-1][3]) {
											//Erwartung erfuellt schlecht
											int[] audiosequenz = new int[7];
											audiosequenz[0]=R.raw.finalea1;
											audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb1;
											audiosequenz[3]=inttoresid_sven(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec1;
											audiosequenz[5]=R.raw.finaleperformb_1;
											audiosequenz[6]=inttoresid_sven(finale[fin-1][2], true);
											audiosequenzplay(audiosequenz, 7);
										}else {
											//Erwartung erfuellt gut
											int[] audiosequenz = new int[6];
											audiosequenz[0]=R.raw.finalea1;
											audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb1;
											audiosequenz[3]=inttoresid_sven(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec1;
											audiosequenz[5]=R.raw.finaleperformc_1;
											audiosequenzplay(audiosequenz, 6);
										}//if (finale[fin-1][1]>finale[fin-1][2]) {
									}//if(finale[fin-1][1]==finale[fin-1][2]) {
								} else {
									//es gibt nur 2 Laeufer
									int[] audiosequenz = new int[6];
									audiosequenz[0]=R.raw.finalea1;
									audiosequenz[1]=inttoresid_sven(finale[fin-1][1], true);
									audiosequenz[2]=R.raw.finaleb1;
									audiosequenz[3]=inttoresid_sven(finale[fin-1][1], true);
									audiosequenz[4]=R.raw.finalec1;
									audiosequenz[5]=R.raw.finaleperforma1;
									audiosequenzplay(audiosequenz, 6);
								}//if (anz_spieler>2) {
							} else {
								fin++;
							}//if (finale[fin][0]==0) {
						}//while(fin_flag) {
					}//if (vorstell_zufall==1) {

					if (vorstell_zufall==2) {
						boolean fin_flag=true;
						int fin=0;
						while(fin_flag) {
							if (finale[fin][0]==0) {
								fin_flag=false;
								if (anz_spieler>2) {
									if(finale[fin-1][2]==finale[fin-1][3]) {
										//Erwartung erfuellt
										int[] audiosequenz = new int[6];
										audiosequenz[0]=R.raw.finalea2;
										audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
										audiosequenz[2]=R.raw.finaleb2;
										audiosequenz[3]=inttoresid_jul(finale[fin-1][3], true);
										audiosequenz[4]=R.raw.finalec2;
										audiosequenz[5]=R.raw.finaleperforma2;
										audiosequenzplay(audiosequenz, 6);
									} else {
										if (finale[fin-1][2]>finale[fin-1][3]) {
											//Erwartung erfuellt schlecht
											int[] audiosequenz = new int[7];
											audiosequenz[0]=R.raw.finalea2;
											audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb2;
											audiosequenz[3]=inttoresid_jul(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec2;
											audiosequenz[5]=R.raw.finaleperformb_2;
											audiosequenz[6]=inttoresid_jul(finale[fin-1][2], true);
											audiosequenzplay(audiosequenz, 7);
										}else {
											//Erwartung erfuellt gut
											int[] audiosequenz = new int[7];
											audiosequenz[0]=R.raw.finalea2;
											audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb2;
											audiosequenz[3]=inttoresid_jul(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec2;
											audiosequenz[5]=R.raw.finaleperformc_2;
											audiosequenz[6]=inttoresid_jul(finale[fin-1][2], true);
											audiosequenzplay(audiosequenz, 7);
										}//if (finale[fin-1][1]>finale[fin-1][2]) {
									}//if(finale[fin-1][1]==finale[fin-1][2]) {
								} else {
									//es gibt nur 2 Laeufer
									int[] audiosequenz = new int[6];
									audiosequenz[0]=R.raw.finalea2;
									audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
									audiosequenz[2]=R.raw.finaleb2;
									audiosequenz[3]=inttoresid_jul(finale[fin-1][1], true);
									audiosequenz[4]=R.raw.finalec2;
									audiosequenz[5]=R.raw.finaleperforma2;
									audiosequenzplay(audiosequenz, 6);
								}//if (anz_spieler>2) {
							} else {
								fin++;
							}//if (finale[fin][0]==0) {
						}//while(fin_flag) {
					}//if (vorstell_zufall==2) {

					if (vorstell_zufall==3) {
						boolean fin_flag=true;
						int fin=0;
						while(fin_flag) {
							if (finale[fin][0]==0) {
								fin_flag=false;
								if (anz_spieler>2) {
									if(finale[fin-1][2]==finale[fin-1][3]) {
										//Erwartung erfuellt
										int[] audiosequenz = new int[6];
										audiosequenz[0]=R.raw.finalea3;
										audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
										audiosequenz[2]=R.raw.finaleb3;
										audiosequenz[3]=inttoresid_jul(finale[fin-1][3], true);
										audiosequenz[4]=R.raw.finalec3;
										audiosequenz[5]=R.raw.finaleperforma3;
										audiosequenzplay(audiosequenz, 6);
									} else {
										if (finale[fin-1][2]>finale[fin-1][3]) {
											//Erwartung erfuellt schlecht
											int[] audiosequenz = new int[7];
											audiosequenz[0]=R.raw.finalea3;
											audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb3;
											audiosequenz[3]=inttoresid_jul(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec3;
											audiosequenz[5]=R.raw.finaleperformb_3;
											audiosequenz[6]=inttoresid_jul(finale[fin-1][2], true);
											audiosequenzplay(audiosequenz, 7);
										}else {
											//Erwartung erfuellt gut
											int[] audiosequenz = new int[6];
											audiosequenz[0]=R.raw.finalea3;
											audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
											audiosequenz[2]=R.raw.finaleb3;
											audiosequenz[3]=inttoresid_jul(finale[fin-1][3], true);
											audiosequenz[4]=R.raw.finalec3;
											audiosequenz[5]=R.raw.finaleperformc_3;
											audiosequenzplay(audiosequenz, 6);
										}//if (finale[fin-1][1]>finale[fin-1][2]) {
									}//if(finale[fin-1][1]==finale[fin-1][2]) {
								} else {
									//es gibt nur 2 Laeufer
									int[] audiosequenz = new int[6];
									audiosequenz[0]=R.raw.finalea3;
									audiosequenz[1]=inttoresid_jul(finale[fin-1][1], true);
									audiosequenz[2]=R.raw.finaleb3;
									audiosequenz[3]=inttoresid_jul(finale[fin-1][1], true);
									audiosequenz[4]=R.raw.finalec3;
									audiosequenz[5]=R.raw.finaleperforma3;
									audiosequenzplay(audiosequenz, 6);
								}//if (anz_spieler>2) {
							} else {
								fin++;
							}//if (finale[fin][0]==0) {
						}//while(fin_flag) {
					}//if (vorstell_zufall==3) {
					finale_laeufer_flag=false;
				} catch(Exception e) {
					e.printStackTrace();
					finale_laeufer_flag=false;
					flag_sequenz=false;
					int[] audiosequenz = new int[1];
					audiosequenz[0]=R.raw.final_error;
					audiosequenzplay(audiosequenz, 1);
				}//try
			}//if (finale_laeufer_flag==true) {
		}//if (flag_sequenz!=true) {
	}//private void challenge_events() {

	private void challenge_place_report() {
		if (flag_sequenz!=true) {
			Random random = new Random();
			int place_zufall = random.nextInt(10);
			boolean haup_nebensatz=false;
			int haupt_neben_array=2;
			int[] audiosequenz=null;
			if (place_zufall==0) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v0;
				audiosequenz[1]=inttoresid_jul(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==1) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v1;
				audiosequenz[1]=inttoresid_jul(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==2) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v2;
				audiosequenz[1]=inttoresid_jul(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==3) {
				audiosequenz = new int[4];
				audiosequenz[0]=R.raw.platzansage_v3_a;
				audiosequenz[1]=inttoresid_jul(Spieler[0][0][1][0], true);
				audiosequenz[2]=R.raw.platzansage_v3_b;
				haup_nebensatz=true;
			}//if (place_zufall==0) {
			if (place_zufall==4) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v4;
				audiosequenz[1]=inttoresid_jul(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==5) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v5;
				audiosequenz[1]=inttoresid_sven(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==6) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v6;
				audiosequenz[1]=inttoresid_sven(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==7) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v7;
				audiosequenz[1]=inttoresid_sven(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==8) {
				audiosequenz = new int[3];
				audiosequenz[0]=R.raw.platzansage_v8;
				audiosequenz[1]=inttoresid_sven(Spieler[0][0][1][0], true);
			}//if (place_zufall==0) {
			if (place_zufall==9) {
				audiosequenz = new int[4];
				audiosequenz[0]=R.raw.platzansage_v9_a;
				audiosequenz[1]=inttoresid_sven(Spieler[0][0][1][0], true);
				audiosequenz[2]=R.raw.platzansage_v9_b;
				haup_nebensatz=true;
			}//if (place_zufall==0) {

			if (anz_spieler>2 && audiosequenz!=null) {
				double vorn=(anz_spieler+2)/3;
				double mitte=((anz_spieler+2)/3)*2;
				if (haup_nebensatz==true) {
					haupt_neben_array=3;
				}else {
					haupt_neben_array=2;
				}//if (haup_nebensatz==true) {

				Random random1 = new Random();
				int feld = random1.nextInt(6);

				if (Spieler[0][0][1][0]<=vorn) {

					if (feld==0) {
						audiosequenz[haupt_neben_array]=R.raw.feldansage_vorne0;
						audiosequenzplay(audiosequenz, haupt_neben_array+1);
					}//if (feld==0) {
					if (feld==1) {
						audiosequenz[haupt_neben_array]=R.raw.feldansage_vorne1;
						audiosequenzplay(audiosequenz, haupt_neben_array+1);
					}//if (feld==0) {
					if (feld==2) {
						audiosequenz[haupt_neben_array]=R.raw.feldansage_vorne2;
						audiosequenzplay(audiosequenz, haupt_neben_array+1);
					}//if (feld==0) {
					if (feld==3) {
						audiosequenz[haupt_neben_array]=R.raw.feldansage_vorne3;
						audiosequenzplay(audiosequenz, haupt_neben_array+1);
					}//if (feld==0) {
					if (feld==4) {
						audiosequenz[haupt_neben_array]=R.raw.feldansage_vorne4;
						audiosequenzplay(audiosequenz, haupt_neben_array+1);
					}//if (feld==0) {
					if (feld==5) {
						audiosequenz[haupt_neben_array]=R.raw.feldansage_vorne5;
						audiosequenzplay(audiosequenz, haupt_neben_array+1);
					}//if (feld==0) {

				} else {
					if (Spieler[0][0][1][0]<=mitte) {
						if (feld==0) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_mittel0;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==1) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_mittel1;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==2) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_mittel2;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==3) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_mittel3;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==4) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_mittel4;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==5) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_mittel5;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
					} else {
						if (feld==0) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_hinten0;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==1) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_hinten1;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==2) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_hinten2;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==3) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_hinten3;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==4) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_hinten4;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
						if (feld==5) {
							audiosequenz[haupt_neben_array]=R.raw.feldansage_hinten5;
							audiosequenzplay(audiosequenz, haupt_neben_array+1);
						}//if (feld==0) {
					}//if (Spieler[0][0][1][0]<mitte) {
				}//if (Spieler[0][0][1][0]<vorn) {
			}//if (anz_spieler>2) {
		}//if (flag_sequenz!=true) {
	}//private void challenge_place_report() {

	private void challenge_report() {
		if (flag_sequenz!=true) {
			int[] plaetze = new int [20];
			int pl_nr=0;

			for (int x = 0; x<anz_spieler+1; x++) {
				if (Spieler[x][0][1][0]==1) {
					plaetze[pl_nr]=Spieler[x][0][0][0];
					pl_nr++;
				}//if (Spieler[x][0][1]==1) {
			}//for (int x = 0; x<anz_spieler+1; x++) {
			for (int x = 0; x<anz_spieler+1; x++) {
				if (Spieler[x][0][1][0]==2) {
					plaetze[pl_nr]=Spieler[x][0][0][0];
					pl_nr++;
				}//if (Spieler[x][0][1]==1) {
			}//for (int x = 0; x<anz_spieler+1; x++) {
			for (int x = 0; x<anz_spieler+1; x++) {
				if (Spieler[x][0][1][0]==3) {
					plaetze[pl_nr]=Spieler[x][0][0][0];
					pl_nr++;
				}//if (Spieler[x][0][1]==1) {
			}//for (int x = 0; x<anz_spieler+1; x++) {
			for (int x = 0; x<anz_spieler+1; x++) {
				if (Spieler[x][0][1][0]==4) {
					plaetze[pl_nr]=Spieler[x][0][0][0];
					pl_nr++;
				}//if (Spieler[x][0][1]==1) {
			}//for (int x = 0; x<anz_spieler+1; x++) {

			int platz1=plaetze[0];
			int platz2=plaetze[1];
			int platz3=plaetze[2];

			Random random = new Random();
			int raport = random.nextInt(5);

			if (raport==0) {
				int[] audiosequenz = new int[6];
				if (anz_spieler+1==1) {
					audiosequenz[0]=R.raw.report_a_0;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenzplay(audiosequenz, 2);
				}//if (anz_spieler==1) {

				if (anz_spieler+1==2) {
					audiosequenz[0]=R.raw.report_a_0;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenz[2]=R.raw.report_b_0;
					audiosequenz[3]=inttoresid_sven(platz2, true);
					audiosequenzplay(audiosequenz, 4);
				}//if (anz_spieler==2) {

				if (anz_spieler+1>3) {
					audiosequenz[0]=R.raw.report_a_0;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenz[2]=R.raw.report_b_0;
					audiosequenz[3]=inttoresid_sven(platz2, true);
					audiosequenz[4]=R.raw.report_c_0;
					audiosequenz[5]=inttoresid_sven(platz3, true);
					audiosequenzplay(audiosequenz, 6);
				}//if (anz_spieler==3) {
			}//if (raport==0) {

			if (raport==1) {
				int[] audiosequenz = new int[6];
				if (anz_spieler+1==1) {
					audiosequenz[0]=R.raw.report_a_1;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenzplay(audiosequenz, 2);
				}//if (anz_spieler==1) {

				if (anz_spieler+1==2) {
					audiosequenz[0]=R.raw.report_a_1;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenz[2]=R.raw.report_b_1;
					audiosequenz[3]=inttoresid_sven(platz2, true);
					audiosequenzplay(audiosequenz, 4);
				}//if (anz_spieler==2) {

				if (anz_spieler+1>3) {
					audiosequenz[0]=R.raw.report_a_1;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenz[2]=R.raw.report_b_1;
					audiosequenz[3]=inttoresid_sven(platz2, true);
					audiosequenz[4]=R.raw.report_c_1;
					audiosequenz[5]=inttoresid_sven(platz3, true);
					audiosequenzplay(audiosequenz, 6);
				}//if (anz_spieler==3) {
			}//if (raport==0) {

			if (raport==2) {
				int[] audiosequenz = new int[6];
				if (anz_spieler+1==1) {
					audiosequenz[0]=R.raw.report_a_2;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenzplay(audiosequenz, 2);
				}//if (anz_spieler==1) {

				if (anz_spieler+1==2) {
					audiosequenz[0]=R.raw.report_a_2;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenz[2]=R.raw.report_b_2;
					audiosequenz[3]=inttoresid_sven(platz2, true);
					audiosequenzplay(audiosequenz, 4);
				}//if (anz_spieler==2) {

				if (anz_spieler+1>3) {
					audiosequenz[0]=R.raw.report_a_2;
					audiosequenz[1]=inttoresid_sven(platz1, true);
					audiosequenz[2]=R.raw.report_b_2;
					audiosequenz[3]=inttoresid_sven(platz2, true);
					audiosequenz[4]=R.raw.report_c_2;
					audiosequenz[5]=inttoresid_sven(platz3, true);
					audiosequenzplay(audiosequenz, 6);
				}//if (anz_spieler==3) {
			}//if (raport==0) {

			if (raport==3) {
				int[] audiosequenz = new int[6];
				if (anz_spieler+1==1) {
					audiosequenz[0]=R.raw.report_a_3;
					audiosequenz[1]=inttoresid_jul(platz1, true);
					audiosequenzplay(audiosequenz, 2);
				}//if (anz_spieler==1) {

				if (anz_spieler+1==2) {
					audiosequenz[0]=R.raw.report_a_3;
					audiosequenz[1]=inttoresid_jul(platz1, true);
					audiosequenz[2]=R.raw.report_b_3;
					audiosequenz[3]=inttoresid_jul(platz2, true);
					audiosequenzplay(audiosequenz, 4);
				}//if (anz_spieler==2) {

				if (anz_spieler+1>3) {
					audiosequenz[0]=R.raw.report_a_3;
					audiosequenz[1]=inttoresid_jul(platz1, true);
					audiosequenz[2]=R.raw.report_b_3;
					audiosequenz[3]=inttoresid_jul(platz2, true);
					audiosequenz[4]=R.raw.report_c_3;
					audiosequenz[5]=inttoresid_jul(platz3, true);
					audiosequenzplay(audiosequenz, 6);
				}//if (anz_spieler==3) {
			}//if (raport==0) {

			if (raport==4) {
				int[] audiosequenz = new int[6];
				if (anz_spieler+1==1) {
					audiosequenz[0]=R.raw.report_a_4;
					audiosequenz[1]=inttoresid_jul(platz1, true);
					audiosequenzplay(audiosequenz, 2);
				}//if (anz_spieler==1) {

				if (anz_spieler+1==2) {
					audiosequenz[0]=R.raw.report_a_4;
					audiosequenz[1]=inttoresid_jul(platz1, true);
					audiosequenz[2]=R.raw.report_b_4;
					audiosequenz[3]=inttoresid_jul(platz2, true);
					audiosequenzplay(audiosequenz, 4);
				}//if (anz_spieler==2) {

				if (anz_spieler+1>3) {
					audiosequenz[0]=R.raw.report_a_4;
					audiosequenz[1]=inttoresid_jul(platz1, true);
					audiosequenz[2]=R.raw.report_b_4;
					audiosequenz[3]=inttoresid_jul(platz2, true);
					audiosequenz[4]=R.raw.report_c_4;
					audiosequenz[5]=inttoresid_jul(platz3, true);
					audiosequenzplay(audiosequenz, 6);
				}//if (anz_spieler==3) {
			}//if (raport==0) {
		}//if (flag_sequenz!=true) {
	}//private void challenge_report() {

	private void sprueche_katalog() {
		if (flag_sequenz!=true) {
			Random random = new Random();
			int sprueche_zufall = random.nextInt(9);
			int[] audiosequenz = new int[1];
			Calendar c = new GregorianCalendar();
			Date today = c.getTime();
			int akt_day =today.getDay();

			if (anz_spieler>2) {
				try {
					double vorn=(anz_spieler+2)/3;
					double mitte=((anz_spieler+2)/3)*2;

					if (Spieler[0][0][1][0]<=vorn) {//Ordnung nach Feldposition
						if (akt_day == 0) {//Ordnung nach Wochentagen (Sonntag)
							if (sprueche_zufall==0) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_1;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==1) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_2;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==2) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_3;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==3) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_4;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==4) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_5;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==5) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_6;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==6) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_7;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==7) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_8;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==8) {
								audiosequenz[0]=R.raw.sprueche_vorn_sonntag_9;
							}//if (sprueche_zufall==0) {
						}//if (akt_day == 0) {
						if (akt_day == 1) {//Montag
							if (sprueche_zufall==0) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_1;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==1) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_2;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==2) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_3;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==3) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_4;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==4) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_5;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==5) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_6;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==6) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_7;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==7) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_8;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==8) {
								audiosequenz[0]=R.raw.sprueche_vorn_montag_9;
							}//if (sprueche_zufall==0) {
						}
						if (akt_day == 2) {//Dienstags
							if (sprueche_zufall==0) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_1;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==1) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_2;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==2) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_3;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==3) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_4;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==4) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_5;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==5) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_6;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==6) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_7;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==7) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_8;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==8) {
								audiosequenz[0]=R.raw.sprueche_vorn_dienstag_9;
							}//if (sprueche_zufall==0) {
						}
						if (akt_day == 3) {//Mittwochs
							if (sprueche_zufall==0) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_1;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==1) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_2;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==2) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_3;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==3) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_4;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==4) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_5;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==5) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_6;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==6) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_7;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==7) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_8;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==8) {
								audiosequenz[0]=R.raw.sprueche_vorn_mittwoch_9;
							}//if (sprueche_zufall==0) {
						}
						if (akt_day == 4) {//Donnerstag
							if (sprueche_zufall==0) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_1;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==1) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_2;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==2) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_3;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==3) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_4;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==4) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_5;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==5) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_6;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==6) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_7;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==7) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_8;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==8) {
								audiosequenz[0]=R.raw.sprueche_vorn_donnerstag_9;
							}//if (sprueche_zufall==0) {
						}
						if (akt_day == 5) {//Freitag
							if (sprueche_zufall==0) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_1;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==1) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_2;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==2) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_3;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==3) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_4;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==4) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_5;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==5) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_6;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==6) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_7;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==7) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_8;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==8) {
								audiosequenz[0]=R.raw.sprueche_vorn_freitag_9;
							}//if (sprueche_zufall==0) {
						}
						if (akt_day == 6) {//Samstag
							if (sprueche_zufall==0) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_1;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==1) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_2;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==2) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_3;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==3) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_4;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==4) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_5;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==5) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_6;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==6) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_7;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==7) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_8;
							}//if (sprueche_zufall==0) {
							if (sprueche_zufall==8) {
								audiosequenz[0]=R.raw.sprueche_vorn_samstag_9;
							}//if (sprueche_zufall==0) {
						}//if (akt_day == 6) {//Samstag

					} else {//Mittelfeld Sprueche
						if (Spieler[0][0][1][0]<=mitte) {
							if (akt_day == 0) {//Ordnung nach Wochentagen (Sonntag)
								if (sprueche_zufall==0) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_1;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==1) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_2;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==2) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_3;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==3) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_4;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==4) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_5;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==5) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_6;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==6) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_7;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==7) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_8;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==8) {
									audiosequenz[0]=R.raw.sprueche_mitte_sonntag_9;
								}//if (sprueche_zufall==0) {
							}//if (akt_day == 0) {
							if (akt_day == 1) {//Montag
								if (sprueche_zufall==0) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_1;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==1) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_2;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==2) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_3;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==3) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_4;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==4) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_5;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==5) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_6;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==6) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_7;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==7) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_8;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==8) {
									audiosequenz[0]=R.raw.sprueche_mitte_montag_9;
								}//if (sprueche_zufall==0) {
							}
							if (akt_day == 2) {//Dienstags
								if (sprueche_zufall==0) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_1;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==1) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_2;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==2) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_3;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==3) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_4;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==4) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_5;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==5) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_6;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==6) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_7;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==7) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_8;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==8) {
									audiosequenz[0]=R.raw.sprueche_mitte_dienstag_9;
								}//if (sprueche_zufall==0) {
							}
							if (akt_day == 3) {//Mittwochs
								if (sprueche_zufall==0) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_1;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==1) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_2;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==2) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_3;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==3) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_4;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==4) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_5;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==5) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_6;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==6) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_7;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==7) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_8;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==8) {
									audiosequenz[0]=R.raw.sprueche_mitte_mittwoch_9;
								}//if (sprueche_zufall==0) {
							}
							if (akt_day == 4) {//Donnerstag
								if (sprueche_zufall==0) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_1;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==1) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_2;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==2) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_3;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==3) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_4;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==4) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_5;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==5) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_6;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==6) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_7;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==7) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_8;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==8) {
									audiosequenz[0]=R.raw.sprueche_mitte_donnerstag_9;
								}//if (sprueche_zufall==0) {
							}
							if (akt_day == 5) {//Freitag
								if (sprueche_zufall==0) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_1;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==1) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_2;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==2) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_3;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==3) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_4;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==4) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_5;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==5) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_6;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==6) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_7;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==7) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_8;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==8) {
									audiosequenz[0]=R.raw.sprueche_mitte_freitag_9;
								}//if (sprueche_zufall==0) {
							}
							if (akt_day == 6) {//Samstag
								if (sprueche_zufall==0) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_1;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==1) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_2;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==2) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_3;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==3) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_4;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==4) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_5;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==5) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_6;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==6) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_7;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==7) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_8;
								}//if (sprueche_zufall==0) {
								if (sprueche_zufall==8) {
									audiosequenz[0]=R.raw.sprueche_mitte_samstag_9;
								}//if (sprueche_zufall==0) {
							}//if (akt_day == 6) {//Samstag
						} else { //Hinterfeld Sprueche
							if (akt_day == 0) {//Sonntag
								if (akt_day == 0) {//Ordnung nach Wochentagen (Sonntag)
									if (sprueche_zufall==0) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_1;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==1) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_2;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==2) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_3;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==3) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_4;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==4) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_5;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==5) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_6;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==6) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_7;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==7) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_8;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==8) {
										audiosequenz[0]=R.raw.sprueche_hinten_sonntag_9;
									}//if (sprueche_zufall==0) {
								}//if (akt_day == 0) {
								if (akt_day == 1) {//Montag
									if (sprueche_zufall==0) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_1;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==1) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_2;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==2) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_3;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==3) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_4;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==4) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_5;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==5) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_6;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==6) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_7;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==7) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_8;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==8) {
										audiosequenz[0]=R.raw.sprueche_hinten_montag_9;
									}//if (sprueche_zufall==0) {
								}
								if (akt_day == 2) {//Dienstags
									if (sprueche_zufall==0) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_1;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==1) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_2;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==2) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_3;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==3) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_4;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==4) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_5;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==5) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_6;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==6) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_7;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==7) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_8;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==8) {
										audiosequenz[0]=R.raw.sprueche_hinten_dienstag_9;
									}//if (sprueche_zufall==0) {
								}
								if (akt_day == 3) {//Mittwochs
									if (sprueche_zufall==0) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_1;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==1) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_2;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==2) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_3;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==3) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_4;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==4) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_5;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==5) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_6;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==6) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_7;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==7) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_8;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==8) {
										audiosequenz[0]=R.raw.sprueche_hinten_mittwoch_9;
									}//if (sprueche_zufall==0) {
								}
								if (akt_day == 4) {//Donnerstag
									if (sprueche_zufall==0) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_1;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==1) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_2;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==2) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_3;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==3) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_4;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==4) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_5;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==5) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_6;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==6) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_7;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==7) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_8;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==8) {
										audiosequenz[0]=R.raw.sprueche_hinten_donnerstag_9;
									}//if (sprueche_zufall==0) {
								}
								if (akt_day == 5) {//Freitag
									if (sprueche_zufall==0) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_1;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==1) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_2;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==2) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_3;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==3) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_4;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==4) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_5;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==5) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_6;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==6) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_7;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==7) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_8;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==8) {
										audiosequenz[0]=R.raw.sprueche_hinten_freitag_9;
									}//if (sprueche_zufall==0) {
								}
								if (akt_day == 6) {//Samstag
									if (sprueche_zufall==0) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_1;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==1) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_2;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==2) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_3;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==3) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_4;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==4) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_5;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==5) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_6;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==6) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_7;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==7) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_8;
									}//if (sprueche_zufall==0) {
									if (sprueche_zufall==8) {
										audiosequenz[0]=R.raw.sprueche_hinten_samstag_9;
									}//if (sprueche_zufall==0) {
								}//if (akt_day == 6) {//Samstag
							}//if (akt_day == 0) {
						}//if (Spieler[0][0][1][0]<mitte) {
					}//if (Spieler[0][0][1][0]<vorn) {
					audiosequenzplay(audiosequenz, 1);
				} catch (Exception e) {
					e.printStackTrace();
					flag_sequenz=false;
				}//try
			}//if (anz_spieler>2) {
		}//if (flag_sequenz!=true) {
	}//private void sprueche_katalog() {

	public void stop_sequenz() {
		if (flag_sequenz!=true) {
			if (ende==true && siegerehrung==false) {
				boolean fin_flag=true;
				int fin=0;
				while(fin_flag) {
					if (finale[fin][1]==0) {
						fin_flag=false;
					} else {
						fin++;
					}//if (finale[fin][0]==0) {
				}//while(fin_flag) {

				Random random = new Random();
				int stopvarianz = random.nextInt(5);

				if (stopvarianz==0) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.stop_a_0;
					audiosequenz[1]=inttoresid_sven(finale[fin][3], true);
					audiosequenz[2]=R.raw.stop_b_0;
					audiosequenzplay(audiosequenz, 3);
					zuschauerplay(false);
					report_flag=false;
					siegerehrung=true;
				}//if (stopvarianz==0) {

				if (stopvarianz==1) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.stop_a_1;
					audiosequenz[1]=inttoresid_jul(finale[fin][3], true);
					audiosequenz[2]=R.raw.stop_b_1;
					audiosequenzplay(audiosequenz, 3);
					zuschauerplay(false);
					report_flag=false;
					siegerehrung=true;
				}//if (stopvarianz==0) {

				if (stopvarianz==2) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.stop_a_2;
					audiosequenz[1]=inttoresid_sven(finale[fin][3], true);
					audiosequenz[2]=R.raw.stop_b_2;
					audiosequenzplay(audiosequenz, 3);
					zuschauerplay(false);
					report_flag=false;
					siegerehrung=true;
				}//if (stopvarianz==0) {

				if (stopvarianz==3) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.stop_a_3;
					audiosequenz[1]=inttoresid_jul(finale[fin][3], true);
					audiosequenz[2]=R.raw.stop_b_3;
					audiosequenzplay(audiosequenz, 3);
					zuschauerplay(false);
					report_flag=false;
					siegerehrung=true;
				}//if (stopvarianz==0) {

				if (stopvarianz==4) {
					int[] audiosequenz = new int[3];
					audiosequenz[0]=R.raw.stop_a_4;
					audiosequenz[1]=inttoresid_sven(finale[fin][3], true);
					audiosequenz[2]=R.raw.stop_b_4;
					audiosequenzplay(audiosequenz, 3);
					zuschauerplay(false);
					report_flag=false;
					siegerehrung=true;
				}//if (stopvarianz==0) {
			}//if (ende==false) {
		}//if (flag_sequenz!=true) {
	}//private void stop_sequenz() {

	public String get_playerposition_final() {
		String playerpos="";
		try{
			if (ende==true) {
				boolean fin_flag=true;
				int fin=0;
				while(fin_flag) {
					if (finale[fin][1]==0) {
						fin_flag=false;
					} else {
						fin++;
					}//if (finale[fin][0]==0) {
				}//while(fin_flag) {
				playerpos=String.valueOf(finale[fin][3]);
			}//if (ende==false) {
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(c, "Crash get_playerposition", Toast.LENGTH_LONG).show();
		}
		return playerpos;
	}//public String get_playerposition_final() {

	private int inttoresid_jul(int za, boolean eins) {
		int wave_file;
		switch(za) {
			//case 0: wave_file= R.raw.za0_jul;break;
			case 1: wave_file= R.raw.zaein_jul;break;
			case 2: wave_file= R.raw.za2_jul;break;
			case 3: wave_file= R.raw.za3_jul;break;
			case 4: wave_file= R.raw.za4_jul;break;
			case 5: wave_file= R.raw.za5_jul;break;
			case 6: wave_file= R.raw.za6_jul;break;
			case 7: wave_file= R.raw.za7_jul;break;
			case 8: wave_file= R.raw.za8_jul;break;
			case 9: wave_file= R.raw.za9_jul;break;
			case 10: wave_file= R.raw.za10_jul;break;
			case 11: wave_file= R.raw.za11_jul;break;
			case 12: wave_file= R.raw.za12_jul;break;
			case 13: wave_file= R.raw.za13_jul;break;
			case 14: wave_file= R.raw.za14_jul;break;
			case 15: wave_file= R.raw.za15_jul;break;
			case 16: wave_file= R.raw.za16_jul;break;
			case 17: wave_file= R.raw.za17_jul;break;
			case 18: wave_file= R.raw.za18_jul;break;
			case 19: wave_file= R.raw.za19_jul;break;
			case 20: wave_file= R.raw.za20_jul;break;
			case 21: wave_file= R.raw.za21_jul;break;
			case 22: wave_file= R.raw.za22_jul;break;
			case 23: wave_file= R.raw.za23_jul;break;
			case 24: wave_file= R.raw.za24_jul;break;
			case 25: wave_file= R.raw.za25_jul;break;
			case 26: wave_file= R.raw.za26_jul;break;
			case 27: wave_file= R.raw.za27_jul;break;
			case 28: wave_file= R.raw.za28_jul;break;
			case 29: wave_file= R.raw.za29_jul;break;
			case 30: wave_file= R.raw.za30_jul;break;
			case 31: wave_file= R.raw.za31_jul;break;
			case 32: wave_file= R.raw.za32_jul;break;
			case 33: wave_file= R.raw.za33_jul;break;
			case 34: wave_file= R.raw.za34_jul;break;
			case 35: wave_file= R.raw.za35_jul;break;
			case 36: wave_file= R.raw.za36_jul;break;
			case 37: wave_file= R.raw.za37_jul;break;
			case 38: wave_file= R.raw.za38_jul;break;
			case 39: wave_file= R.raw.za39_jul;break;
			case 40: wave_file= R.raw.za40_jul;break;
			case 41: wave_file= R.raw.za41_jul;break;
			case 42: wave_file= R.raw.za42_jul;break;
			case 43: wave_file= R.raw.za43_jul;break;
			case 44: wave_file= R.raw.za44_jul;break;
			case 45: wave_file= R.raw.za45_jul;break;
			case 46: wave_file= R.raw.za46_jul;break;
			case 47: wave_file= R.raw.za47_jul;break;
			case 48: wave_file= R.raw.za48_jul;break;
			case 49: wave_file= R.raw.za49_jul;break;
			case 50: wave_file= R.raw.za50_jul;break;
			default: wave_file=R.raw.outtake3;
		}//switch(za) {
		if (eins==false) {
			return wave_file;
		} else {
			if (za==1) {
				wave_file=R.raw.za1_jul;
				return wave_file;
			} else {
				if (za==0) {
					wave_file=R.raw.za21_jul;
					return wave_file;
				}else {
					return wave_file;
				}//if (za==0) {
			}//if (za==1) {
		}//if (eins==false) {
	}//private int inttoaudio(int za) {

	private int inttoresid_sven(int za, boolean eins) {
		int wave_file;
		switch(za) {
			//case 0: wave_file= R.raw.za0_sven;break;
			case 1: wave_file= R.raw.zaein_sven;break;
			case 2: wave_file= R.raw.za2_sven;break;
			case 3: wave_file= R.raw.za3_sven;break;
			case 4: wave_file= R.raw.za4_sven;break;
			case 5: wave_file= R.raw.za5_sven;break;
			case 6: wave_file= R.raw.za6_sven;break;
			case 7: wave_file= R.raw.za7_sven;break;
			case 8: wave_file= R.raw.za8_sven;break;
			case 9: wave_file= R.raw.za9_sven;break;
			case 10: wave_file= R.raw.za10_sven;break;
			case 11: wave_file= R.raw.za11_sven;break;
			case 12: wave_file= R.raw.za12_sven;break;
			case 13: wave_file= R.raw.za13_sven;break;
			case 14: wave_file= R.raw.za14_sven;break;
			case 15: wave_file= R.raw.za15_sven;break;
			case 16: wave_file= R.raw.za16_sven;break;
			case 17: wave_file= R.raw.za17_sven;break;
			case 18: wave_file= R.raw.za18_sven;break;
			case 19: wave_file= R.raw.za19_sven;break;
			case 20: wave_file= R.raw.za20_sven;break;
			case 21: wave_file= R.raw.za21_sven;break;
			case 22: wave_file= R.raw.za22_sven;break;
			case 23: wave_file= R.raw.za23_sven;break;
			case 24: wave_file= R.raw.za24_sven;break;
			case 25: wave_file= R.raw.za25_sven;break;
			case 26: wave_file= R.raw.za26_sven;break;
			case 27: wave_file= R.raw.za27_sven;break;
			case 28: wave_file= R.raw.za28_sven;break;
			case 29: wave_file= R.raw.za29_sven;break;
			case 30: wave_file= R.raw.za30_sven;break;
			case 31: wave_file= R.raw.za31_sven;break;
			case 32: wave_file= R.raw.za32_sven;break;
			case 33: wave_file= R.raw.za33_sven;break;
			case 34: wave_file= R.raw.za34_sven;break;
			case 35: wave_file= R.raw.za35_sven;break;
			case 36: wave_file= R.raw.za36_sven;break;
			case 37: wave_file= R.raw.za37_sven;break;
			case 38: wave_file= R.raw.za38_sven;break;
			case 39: wave_file= R.raw.za39_sven;break;
			case 40: wave_file= R.raw.za40_sven;break;
			case 41: wave_file= R.raw.za41_sven;break;
			case 42: wave_file= R.raw.za42_sven;break;
			case 43: wave_file= R.raw.za43_sven;break;
			case 44: wave_file= R.raw.za44_sven;break;
			case 45: wave_file= R.raw.za45_sven;break;
			case 46: wave_file= R.raw.za46_sven;break;
			case 47: wave_file= R.raw.za47_sven;break;
			case 48: wave_file= R.raw.za48_sven;break;
			case 49: wave_file= R.raw.za49_sven;break;
			case 50: wave_file= R.raw.za50_sven;break;
			default: wave_file=R.raw.outtake1;
		}//switch(za) {
		if (eins==false) {
			return wave_file;
		} else {
			if (za==1) {
				wave_file=R.raw.za1_sven;
				return wave_file;
			} else {
				if (za==0) {
					wave_file=R.raw.za21_sven;
					return wave_file;
				}else {
					return wave_file;
				}//if (za==0) {
			}//if (za==1) {
		}//if (eins==false) {
	}//private int inttoaudio(int za) {

	private String minmax_toleranz (int streckenabschnitt) {
		double diff_var_x =0.0;
		int vorschau=100;
		if (streckenabschnitt+vorschau>first_ges_zeile[0]-1) {
			vorschau=(first_ges_zeile[0]-1)-streckenabschnitt;
		}//if (streckenabschnitt+vorschau>first_ges_zeile-1) {
		for (int n=streckenabschnitt; n<(streckenabschnitt+vorschau); n++) {
			diff_var_x=Strecke[0][n]-Strecke[0][n+1]+diff_var_x;
		}//for (int n=streckenabschnitt; n<(streckenabschnitt+30); n++) {
		double diff_var_y =0.0;
		for (int n=streckenabschnitt; n<(streckenabschnitt+vorschau); n++) {
			diff_var_y=Strecke[1][n]-Strecke[1][n+1]+diff_var_y;
		}//for (int n=streckenabschnitt; n<(streckenabschnitt+30); n++) {
		String himmelsrichtung="";
		if (Math.abs(diff_var_x)>Math.abs(diff_var_y)) {
			if (diff_var_x>0) {
				himmelsrichtung="w";
			} else {
				himmelsrichtung="o";
			}//if (diff_var_x>0) {
			horizontaltoleranz=0.0001;
			vertikaltoleranz=0.09;
		} else {
			if (diff_var_y>0) {
				himmelsrichtung="s";
			} else {
				himmelsrichtung="n";
			}//if (diff_var_x>0) {
			vertikaltoleranz=0.0001;
			horizontaltoleranz=0.09;
		}//if (diff_var_x>diff_var_y) {
		return himmelsrichtung;
	}//private double x_minmax_toleranz (int spieler_id, int streckenabschnitt) {

	public void setVolumen(int vol) {
		if (audioplayer != null) {
			int MaxVolume = 100;
			float volume= (float) (Math.log(MaxVolume-(vol-1))/Math.log(MaxVolume));
			audioplayer.setVolume(1-volume, 1-volume);
			//audioplayer.setVolume((float)vol/100, (float)vol/100);
		}
	}//public void setVolumen(int vol) {

	private class kom_wait_run extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int za=0;
			while(kom_wait_flag) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (za>80) {
					kom_wait_flag=false;
					kom_wait=false;
				}//if (za==80) {
				za++;
			}//while(kom_wait_flag) {
		}//run
	}//private class kom_wait_run extends TimerTask {
}