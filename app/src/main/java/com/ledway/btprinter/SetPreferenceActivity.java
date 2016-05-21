package com.ledway.btprinter;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;

/**
 * Created by togb on 2016/5/21.
 */
public class SetPreferenceActivity extends PreferenceActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    initSummary(getPreferenceScreen());
  }

  private void initSummary(Preference p) {
    if (p instanceof PreferenceGroup) {
      PreferenceGroup pGrp = (PreferenceGroup) p;
      for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
        initSummary(pGrp.getPreference(i));
      }
    } else {
      updatePrefSummary(p);
    }
  }

  private void updatePrefSummary(Preference p) {
    if (p instanceof ListPreference) {
      ListPreference listPref = (ListPreference) p;
      p.setSummary(listPref.getEntry());
    } else if (p instanceof EditTextPreference) {
      EditTextPreference editTextPref = (EditTextPreference) p;
      if (p.getTitle().toString().toLowerCase().contains("password")) {
        p.setSummary("******");
      } else {
        p.setSummary(editTextPref.getText());
      }
    }else {
      p.setSummary(p.getSharedPreferences().getString(p.getKey(),""));
    }
  }
}
