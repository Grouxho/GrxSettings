/*      2017 Grouxho (esp-desarrolladores.com)

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package android.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;


public class GrxSwitchPreference extends SwitchPreference implements GrxPreferenceScreen.CustomDependencyListener {


    ImageView vAndroidIcon;
    private String mMyDependencyRule;
    private PrefAttrsInfo myPrefAttrsInfo;

    public GrxSwitchPreference(Context context, AttributeSet attrs) {
        super(context,attrs);
        ini_preference(context, attrs);
    }

    public GrxSwitchPreference(Context context) {
        super(context);
    }

    public GrxSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ini_preference(context, attrs);
    }

    private void ini_preference(Context c, AttributeSet att){
        myPrefAttrsInfo = new PrefAttrsInfo(c, att, getTitle(), getSummary(),getKey());

     }



    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = (View) super.onCreateView(parent);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        return view;
    }


    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        vAndroidIcon.setAlpha(alpha);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        boolean defvalue= a.getBoolean(index,false);
        return defvalue;
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            setChecked(getPersistedBoolean(myPrefAttrsInfo.get_my_boolean_def_value()));
        } else {
            setChecked(myPrefAttrsInfo.get_my_boolean_def_value());
            if(!myPrefAttrsInfo.is_valid_key()) return;;
            persistBoolean(isChecked());
        }
        save_value_in_settings(isChecked());
    }

    public void save_value_in_settings(boolean checked){
        if(!myPrefAttrsInfo.is_valid_key()) return;
        if(myPrefAttrsInfo.get_allowed_save_in_settings_db()){
                int value = (checked) ? 1:0;
                int real;
                try {
                    real = Settings.System.getInt(getContext().getContentResolver(), this.getKey());
                    if(real!=value){
                        Settings.System.putInt(getContext().getContentResolver(), this.getKey(), value);

                    }
                } catch (Settings.SettingNotFoundException e) {
                    Settings.System.putInt(getContext().getContentResolver(), this.getKey(), value);
                }
        }
    }



    /**** send broadcasts, change group keys value ************/

    public void send_broadcasts_and_change_group_key(){
        if(Common.SyncUpMode) return;
        GrxPreferenceScreen chl = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(chl!=null){
            chl.send_broadcasts_and_change_group_key(myPrefAttrsInfo.get_my_group_key(),myPrefAttrsInfo.get_send_bc1(),myPrefAttrsInfo.get_send_bc2());
        }
    }



    /**********  Onpreferencechangelistener - add custom dependency rule *********/

    @Override
    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener onPreferenceChangeListener){
        if(!Common.SyncUpMode){
            super.setOnPreferenceChangeListener(onPreferenceChangeListener);
            String mydeprule = myPrefAttrsInfo.get_my_dependency_rule();
            if(mydeprule!=null){
                GrxPreferenceScreen grxPreferenceScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
                grxPreferenceScreen.add_custom_dependency(this,mydeprule,null);
            }

        }else {
            GrxPreferenceScreen grxPreferenceScreen = (GrxPreferenceScreen) onPreferenceChangeListener;
            grxPreferenceScreen.add_group_key_for_syncup(myPrefAttrsInfo.get_my_group_key());
        }

    }

    /************ custom dependencies ****************/
    public void OnCustomDependencyChange(boolean state){
        setEnabled(state);
    }


}
