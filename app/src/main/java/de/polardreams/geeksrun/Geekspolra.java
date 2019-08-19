package de.polardreams.geeksrun;

import java.io.File;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Geekspolra  {
	private Context c;
	private ListView lv = null;
	private File explor_focus = null;
	private boolean finfish_flag=false;
	private String [] entries=null;
	private String adress = "";
	Button btn_back, btn_close, btn_safe;
	private TextView kopfzeile;
	private boolean sd_card_test=false;

	Geekspolra(Context context, ListView listv){
		c=context;
		lv = listv;
		lv.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				lv.setVisibility(View.GONE);
			}
		});
		explor_focus = Environment.getExternalStorageDirectory();
		File ex_test=new File(Environment.getExternalStorageDirectory().toString());
		if (ex_test.exists()==true) {
			sd_card_test=true;
		}//if (Environment.getExternalStorageDirectory().toString()=="") {

	}//Geekspolra(Context context) {

	public void explore() {
		//while(finfish_flag) {
		try {
			String[] s = new String[explor_focus.list().length];
			s=explor_focus.list();
			entries =new String[explor_focus.list().length];
			for (int n =0; n<explor_focus.list().length;n++) {
				entries[n]=s[n];
			}//for (int n =0; n<explor_focus.list().length;n++) {

			ArrayAdapter aa = new ArrayAdapter<String>(c,R.layout.listview_white_option, R.id.listview_content_option, entries);

			btn_back = new Button(c);
			btn_back.setBackgroundResource(R.drawable.btn_dark);
			btn_back.setTextColor(Color.WHITE);
			btn_back.setText("zur�ck");
			btn_back.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					/*
					 * Also hier kommt der Fehler sobald ich beim LG G3 einmal zur�ckgehen
					 * liegt das daran, dass ich keine SD Karte habe?
					 *
					 * oder hat das eine andere Ursache?
					 */
					File backup_file=explor_focus;
					try {
						explor_focus = new File(explor_focus.toString().substring(0,explor_focus.toString().lastIndexOf("/")));
						String[] s = new String[explor_focus.list().length];
						s=explor_focus.list();
						entries =new String[explor_focus.list().length];
						for (int n =0; n<explor_focus.list().length;n++) {
							entries[n]=s[n];
						}//for (int n =0; n<explor_focus.list().length;n++) {

						ArrayAdapter aa = new ArrayAdapter<String>(c,R.layout.listview_white_option, R.id.listview_content_option, entries);
						lv.setAdapter(aa);
					} catch (Exception e1) {
						e1.printStackTrace();
						Toast.makeText(c, "oooops, soweit kannst du nicht zur�ck.", Toast.LENGTH_SHORT).show();
						explor_focus=backup_file;
					}
				}
			});
			kopfzeile = new TextView(c);
			kopfzeile.setText("GeeksPlora");
			kopfzeile.setTextColor(Color.WHITE);
			lv.addHeaderView(kopfzeile);
			lv.addHeaderView(btn_back);

			btn_close = new Button(c);
			btn_close.setText("abbrechen");
			btn_close.setBackgroundResource(R.drawable.btn_dark);
			btn_close.setTextColor(Color.WHITE);
			btn_close.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					lv.setVisibility(View.GONE);
					finfish_flag=true;
					adress=Environment.getExternalStorageDirectory().toString();
					Toast.makeText(c, "Pfad wurde nicht gespeichert, stattdessen wurde der Stammordner"+adress+" genommen.", Toast.LENGTH_SHORT).show();
					lv.removeHeaderView(btn_close);
					lv.removeHeaderView(btn_safe);
					lv.removeHeaderView(btn_back);
					lv.removeHeaderView(kopfzeile);
				}

			});
			lv.addHeaderView(btn_close);

			btn_safe = new Button(c);
			btn_safe.setText("speichern");
			btn_safe.setBackgroundResource(R.drawable.btn_dark);
			btn_safe.setTextColor(Color.WHITE);
			btn_safe.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					lv.setVisibility(View.GONE);
					finfish_flag=true;
					adress=explor_focus.toString();
					Toast.makeText(c, "Pfad: "+adress+", wurde gespeichert", Toast.LENGTH_SHORT).show();
					lv.removeHeaderView(btn_close);
					lv.removeHeaderView(btn_safe);
					lv.removeHeaderView(btn_back);
					lv.removeHeaderView(kopfzeile);

				}
			});
			lv.addHeaderView(btn_safe);

			lv.setAdapter(aa);
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					File backup_file=explor_focus;
					try {
						if (position-9<=explor_focus.list().length) {
							explor_focus = new File(explor_focus+"/"+entries[position-4]);
							if (!explor_focus.isFile()) {
								String[] s = new String[explor_focus.list().length];
								//s=explor_focus.list();
								s=explor_focus.list();
								entries =new String[explor_focus.list().length];
								for (int n =0; n<explor_focus.list().length;n++) {
									entries[n]=s[n];
								}//for (int n =0; n<explor_focus.list().length;n++) {
								ArrayAdapter aa = new ArrayAdapter<String>(c,R.layout.listview_white_option, R.id.listview_content_option, entries);
								lv.setAdapter(aa);
							} else {
								Toast.makeText(c, "Das ist leider kein Ordner gewesen.", Toast.LENGTH_SHORT).show();
								explor_focus = new File(explor_focus.toString().substring(0,explor_focus.toString().lastIndexOf("/")));
							}//if (!explor_focus.isFile()) {
						}//if (position<explor_focus.list().length+2) {
					} catch (Exception e1) {
						e1.printStackTrace();
						Toast.makeText(c, "oooops, das geht gar nicht. Zugriff verweigert f�r: "+entries[position-4], Toast.LENGTH_SHORT).show();
						explor_focus=backup_file;
					}//try
				}
			});
			lv.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lv.setVisibility(View.VISIBLE);
				}
			});//lv.post(new Runnable() {
		} catch(Exception e) {
			Toast.makeText(c, "Konnte Geeksplore nicht laden.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}//Try
		if (sd_card_test==true) {
			Toast.makeText(c, "Die SD-Karte befindet sich auf: "+Environment.getExternalStorageDirectory().toString(), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(c, "Du hast keine SD-Karte im Slot.", Toast.LENGTH_SHORT).show();
		}//if (sd_card_test==true) {

	}//expore

	public String get_adress() {
		return adress;
	}
	public boolean get_process() {
		return finfish_flag;
	}
}
