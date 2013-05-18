package com.cejensen.missedcalls;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
	}

	public static class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.prefs);
			for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++)
			{
				initSummary(getPreferenceScreen().getPreference(i));
			}

			unregister();
			register();
		}

		@Override
		public void onResume()
		{
			register();
			super.onResume();
		}

		@Override
		public void onPause()
		{
			unregister();
			super.onPause();
		}

		@Override
		public void onDestroy()
		{
			unregister();
			super.onDestroy();
		}

		public void register()
		{
			PreferenceManager.getDefaultSharedPreferences(this.getPreferenceScreen().getContext()).registerOnSharedPreferenceChangeListener(this);
		}

		public void unregister()
		{
			PreferenceManager.getDefaultSharedPreferences(this.getPreferenceScreen().getContext()).unregisterOnSharedPreferenceChangeListener(
					this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			Log.d("onSharedPreferenceChanged", "key=" + key);
			PrefsFragment.initSummary(getPreferenceManager().findPreference(key));
		}

		private static void initSummary(Preference p)
		{
			if (p instanceof PreferenceCategory)
			{
				PreferenceCategory pCat = (PreferenceCategory) p;
				for (int i = 0; i < pCat.getPreferenceCount(); i++)
				{
					initSummary(pCat.getPreference(i));
				}
			}
			else
			{
				updatePrefSummary(p);
			}

		}

		private static void updatePrefSummary(Preference p)
		{
			if (p instanceof ListPreference)
			{
				ListPreference listPref = (ListPreference) p;
				p.setSummary(listPref.getEntry());
			}
			if (p instanceof EditTextPreference)
			{
				EditTextPreference editTextPref = (EditTextPreference) p;
				if (editTextPref.getKey().equals("password"))
				{
					p.setSummary("**************");
				}
				else
				{
					p.setSummary(editTextPref.getText());
				}
			}

		}

	}
}
