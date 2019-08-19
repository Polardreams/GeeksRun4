package de.polardreams.geeksrun;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Toast;

public class Options extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * preference sind im res/xml zu finden
		 */
		try {
		addPreferencesFromResource(R.xml.preference);
		} catch (Exception e) {
			Toast.makeText(this, "Konnte Einstellungen mit XML-File nicht laden.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}//Try
	}
}
