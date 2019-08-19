package de.polardreams.geeksrun;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Geekspokal extends TabActivity {
	private TabHost geekspokale;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geekspokal);
		View activity_view = getWindow().getDecorView();
		activity_view.setBackgroundColor(Color.BLACK);
		try {
		geekspokale=(TabHost)findViewById(android.R.id.tabhost);
		TabSpec tab3 = geekspokale.newTabSpec("tab3");
		TabSpec tab4 = geekspokale.newTabSpec("tab4");
		TabSpec tab5 = geekspokale.newTabSpec("tab5");
		TabSpec tab6 = geekspokale.newTabSpec("tab6");
		
		tab3.setIndicator("Strecke").setContent(new Intent (this, Pokal_km.class));
		tab4.setIndicator("Zeit").setContent(new Intent (this, Pokal_zeit.class));
		tab5.setIndicator("Leistung").setContent(new Intent (this, Pokal_Kalorien.class));
		tab6.setIndicator("Punkte").setContent(new Intent (this, Pokal_Punkte.class));
		
		geekspokale.addTab(tab3);
		geekspokale.addTab(tab4);
		geekspokale.addTab(tab5);
		geekspokale.addTab(tab6);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
