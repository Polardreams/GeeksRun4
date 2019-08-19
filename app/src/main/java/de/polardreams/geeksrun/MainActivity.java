package de.polardreams.geeksrun;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {
			private Button btn1, btn2, btn3, btn4, btn5;
			private Intent i;
			
			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_main);
				/*
				Calendar c = new GregorianCalendar();
				c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
				Date today = c.getTime(); 
				Date deadline = new Date(116,06,30);
				
				if (today.after(deadline)) {
					Toast.makeText(this, "Die Lizenz ist abgelaufen.", Toast.LENGTH_LONG).show();
					btn1=(Button)findViewById(R.id.main_button3);
					btn1.setOnClickListener(this);
					btn1.setBackgroundColor(Color.BLACK);
					btn1.setTextColor(Color.WHITE);
					btn2=(Button)findViewById(R.id.main_button5);
					btn2.setOnClickListener(this);
					btn2.setBackgroundColor(Color.BLACK);
					btn2.setTextColor(Color.WHITE);
					btn3=(Button)findViewById(R.id.main_button2);
					btn3.setOnClickListener(this);
					btn3.setBackgroundColor(Color.BLACK);
					btn3.setTextColor(Color.WHITE);
					btn4=(Button)findViewById(R.id.main_button1);
					btn4.setOnClickListener(this);
					btn4.setBackgroundColor(Color.BLACK);
					btn4.setTextColor(Color.WHITE);
					btn5=(Button)findViewById(R.id.main_button4);
					btn5.setOnClickListener(this);
					btn5.setBackgroundColor(Color.BLACK);
					btn5.setTextColor(Color.WHITE);
					
					btn1.setEnabled(false);
					btn2.setEnabled(false);
				} else {
				*/
				
				btn1=(Button)findViewById(R.id.main_button3);
				btn1.setOnClickListener(this);
				btn1.setBackgroundColor(Color.BLACK);
				btn1.setTextColor(Color.WHITE);
				btn2=(Button)findViewById(R.id.main_button5);
				btn2.setOnClickListener(this);
				btn2.setBackgroundColor(Color.BLACK);
				btn2.setTextColor(Color.WHITE);
				btn3=(Button)findViewById(R.id.main_button2);
				btn3.setOnClickListener(this);
				btn3.setBackgroundColor(Color.BLACK);
				btn3.setTextColor(Color.WHITE);
				btn4=(Button)findViewById(R.id.main_button1);
				btn4.setOnClickListener(this);
				btn4.setBackgroundColor(Color.BLACK);
				btn4.setTextColor(Color.WHITE);
				btn5=(Button)findViewById(R.id.main_button4);
				btn5.setOnClickListener(this);
				btn5.setBackgroundColor(Color.BLACK);
				btn5.setTextColor(Color.WHITE);
				
				btn1.setBackgroundResource(R.drawable.btn_dark);
				btn2.setBackgroundResource(R.drawable.btn_dark);
				btn3.setBackgroundResource(R.drawable.btn_dark);
				btn4.setBackgroundResource(R.drawable.btn_dark);
				btn5.setBackgroundResource(R.drawable.btn_dark);
				
				View activity_view = getWindow().getDecorView();
				activity_view.setBackgroundResource(R.drawable.backround_main);
				//activity_view.setBackgroundColor(Color.BLACK);
				//}
			}
			
			
				@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					if (v == btn1) {
						i = new Intent(this, HUD.class);
						startActivity(i);
					}//btn1
					
					if (v == btn2) {
						i = new Intent(this, MainOption.class);
						startActivity(i);
					}//btn2
					
					if (v == btn3) {
						i = new Intent(this, History.class);
						startActivity(i);
						//finish();
					}//btn3
					
					if (v == btn4) {
						finish();
					}//btn4
					
					if (v == btn5) {
						i = new Intent(this, Performance.class);
						startActivity(i);
					}//btn4
					
			}
}
