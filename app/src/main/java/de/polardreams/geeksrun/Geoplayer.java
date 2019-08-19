package de.polardreams.geeksrun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.LayoutDirection;
import android.view.Display.Mode;
import android.widget.Toast;

public class Geoplayer {

	private String[][][][] geoplayerlist_array= new String [50][2][2][2];
	private File meinspeicherort;
	private Context con;
	private FileOutputStream fos;
	private OutputStreamWriter osw;
	private int z=0;//Anzahl der gezoehlten Eintraege in der Geoplayerlist
	private boolean geo_play_flag=false;
	private MediaPlayer audioplayer;
	private String akt_titel;
	
	public Geoplayer(Context c) {
		con =c;
		meinspeicherort = con.getDir("geoarchiv",0);
		z=0;
	}//public Geoplayer(Context c) {
	
	public String[][][][] getgeoplayerlistarray() {
		return geoplayerlist_array;
	}//public String[][][][] getgeoplayerlistarray() {
	
	public int Geoplayerlistarraylength() {
		return z;
	}//public int Geoplayerlistarraylength() {
	
	public void save_geolist(String save_name, int anz_entries) {
		//Speicherroumtine
		try {
				if (meinspeicherort.canWrite() == true && anz_entries!=0) {//Speicherort verfuegbar und Daten sind erhoben
					File file = new File(meinspeicherort, save_name+".txt");		
					fos = new FileOutputStream(file);
					osw = new OutputStreamWriter(fos);
					short za = 0;
					String text="";
					text=kontainer_array2string();
					//Einbetten des String in eine Textdatei
					osw.write(text);
					osw.flush();
					Toast.makeText(con, "Die Geoplayerliste wurde gespeichert unter: "+save_name, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(con, "Die Geoplayerlist konnten nicht gespeichert werden.", Toast.LENGTH_SHORT).show();
				}//if (meinspeicherort.canWrite() == true && z!=0)
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(con, "Du musst es leider nochmal probieren, es gab leider einen Fehler in der Speicherroutine.", Toast.LENGTH_LONG).show();
			}//Try
	}//public void save_geolist() {
	
	public void load_geolist(String geolist_name) {  
		String load_txt="";
		char[] inputBuffer = new char[999999];
		//Laderoutine
		try {
		FileInputStream fileinputstream = new FileInputStream(meinspeicherort+"/"+geolist_name);
		InputStreamReader inputstreamreader = new InputStreamReader(fileinputstream);//Zeiger melden an den leser
		inputstreamreader.read(inputBuffer);//Leser liest in einen Puffer (Char Typ )
		load_txt = new String(inputBuffer);//der Puffer wird der Textvariablen uebergeben
		inputstreamreader.close();//Datei geschlossen
		} catch(Exception e){
			Toast.makeText(con, "Die Geoplaylisten konnten nicht geladen werden..", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}//Try
		//herausfiltern von Fuellzeichen inputBuffer, dient der Verkleinerung der String-Datei
		try {
		int hilf = load_txt.indexOf('\u0000');
		String hilf1 = load_txt.substring(0, hilf);
		load_txt=hilf1;
		} catch (Exception e1) {
			Toast.makeText(con, "Die Verarbeitung der Geoplayerlisten ist fehlgeschlagen.", Toast.LENGTH_SHORT).show();
			e1.printStackTrace();
		}//Try
		//StringtoArray
		boolean load_flag=true;
		int zahl=0;
		String s="";
		while(load_flag) {
			short zeilenumbruch_index = (short) (load_txt.indexOf('\n'));
			if (zeilenumbruch_index != -1) {
				s=load_txt.substring(0,zeilenumbruch_index);
				geoplayerlist_array[zahl][0][0][0]=s.substring(0, s.indexOf(";"));//x
				s=s.substring(s.indexOf(";")+1, s.length());
				geoplayerlist_array[zahl][1][0][0]=s.substring(0, s.indexOf(";"));//y
				s=s.substring(s.indexOf(";")+1, s.length());
				geoplayerlist_array[zahl][0][1][0]=s.substring(0, s.indexOf(";"));//Titelname
				s=s.substring(s.indexOf(";")+1, s.length());
				geoplayerlist_array[zahl][0][0][1]=s;//Toleranz
				s=s.substring(s.indexOf(";")+1, s.length());
				inputBuffer = new char[90000];
				inputBuffer=load_txt.toCharArray();
				for (int n=0; n<load_txt.indexOf('\n')+1;n++) {
					inputBuffer[n]='\u0000';
				}//For
				load_txt=new String(inputBuffer);
				s="";
				zahl++;
			} else {
				load_flag=false;
			}//if (zeilenumbruch_index != -1) {
			z=zahl;
		}//while

	}//private void load_geolist() {
	
	private String kontainer_array2string() {
		//einlesen des Arrays in einen String
		String text="";
		try {
				int za=0;
				
				while (za<z) {
				text=text
						+geoplayerlist_array[za][0][0][0]//x
						+";"+geoplayerlist_array[za][1][0][0]//y
						+";"+geoplayerlist_array[za][0][1][0]//Titel
						+";"+geoplayerlist_array[za][0][0][1]//Tolersnz
						+"\n";//das Trennungszeichen ; ist fuer Chatprotokoll unguenstig, bitte "," verwenden
				za++;
				}
		} catch (Exception e) {
			e.printStackTrace();
			text="";
		}
		return text;
	}//private void kontainer_array2string(String[][][][] s) {

	public void set_geopoint(double x, double y, String titel, double toleranz) {
		geoplayerlist_array[z][0][0][0]=String.valueOf(x);
		geoplayerlist_array[z][1][0][0]=String.valueOf(y);
		geoplayerlist_array[z][0][1][0]=titel;
		geoplayerlist_array[z][0][0][1]=String.valueOf(toleranz);//sowas wie 0.0001 oder �hnlich!!!!
		z++;
	}//public void set_geopoint(double x, double y, String titel, int toleranz) {
	
	public boolean geo_listener(double x, double y) {
		boolean geo_verification=false;
		int geo_id=0;
		for (int n=0; n<z;n++) {
			if (((x >= Double.valueOf(geoplayerlist_array[n][0][0][0])-Double.valueOf(geoplayerlist_array[n][0][0][1]))&&(x <= Double.valueOf(geoplayerlist_array[n][0][0][0])+Double.valueOf(geoplayerlist_array[n][0][0][1])))&&
					((y >= Double.valueOf(geoplayerlist_array[n][1][0][0])-Double.valueOf(geoplayerlist_array[n][0][0][1]))&&(y <= Double.valueOf(geoplayerlist_array[n][1][0][0])+Double.valueOf(geoplayerlist_array[n][0][0][1])))) {
				geo_verification=true;
				geo_id=n;
			}//Geopunkte werden gepr�ft
		}//for (int n=0; n<z;n++) {
		
		if (geo_verification==true && geo_play_flag==true && !geoplayerlist_array[geo_id][0][1][0].equals(akt_titel)) {
			try {
				audioplayer.stop();
			}catch(Exception e) {
				e.printStackTrace();
			}//try
		try {
			audioplayer=new MediaPlayer().create(con, Uri.fromFile(new File(geoplayerlist_array[geo_id][0][1][0])));
			SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(con);
			int volumen =Integer.valueOf(info.getString("sound_geoplayer", "0"));
			akt_titel=geoplayerlist_array[geo_id][0][1][0];
			audioplayer.setVolume((float)volumen/100, (float)volumen/100);
			audioplayer.start();
			audioplayer.setOnCompletionListener(new OnCompletionListener() {
				
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					audioplayer.stop();
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
	}//if (geo_verification==true && geo_play_flag==false) {
		
		if (geo_verification==true && geo_play_flag==false) {
			try {
				geo_play_flag=true;
				akt_titel=geoplayerlist_array[geo_id][0][1][0];
				audioplayer=new MediaPlayer().create(con, Uri.fromFile(new File(geoplayerlist_array[geo_id][0][1][0])));
				SharedPreferences info = PreferenceManager.getDefaultSharedPreferences(con);
				int volumen =Integer.valueOf(info.getString("sound_geoplayer", "0"));
				audioplayer.setVolume((float)volumen/100, (float)volumen/100);
				audioplayer.start();
				audioplayer.setOnCompletionListener(new OnCompletionListener() {
					
					public void onCompletion(MediaPlayer mp) {
						// TODO Auto-generated method stub
						audioplayer.stop();
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
		}//if (geo_verification==true && geo_play_flag==false) {
		

		return geo_verification;
	}//public boolean geo_listener(double x, double y) {
	
	public boolean geoplayer_isplay() {
		return geo_play_flag;
	}//public boolean geoplayer_isplay() {
	
	public void setVolumen(int vol) {
		if (audioplayer != null) {
			int MaxVolume = 100;
			float volume= (float) (Math.log(MaxVolume-(vol-1))/Math.log(MaxVolume));
			audioplayer.setVolume(1-volume, 1-volume);
			//audioplayer.setVolume((float)vol/100, (float)vol/100);
		}
	}//public void setVolumen(int vol) {
	
}
