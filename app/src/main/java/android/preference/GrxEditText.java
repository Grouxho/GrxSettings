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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.dlgs.DlgFrEditText;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;


public class GrxEditText extends  Preference implements DlgFrEditText.OnGrxEditTextListener,
    GrxPreferenceScreen.CustomDependencyListener {

    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;
    private PrefAttrsInfo myPrefAttrsInfo;
    private String mDefaultValue;
    private String mCurrentValue;
    ImageView vAndroidIcon;

    public GrxEditText(Context c){
        super(c);
    }

    public GrxEditText(Context c, AttributeSet attr){
        super(c,attr);
        inicializa_preferencia(c,attr);
    }

    private void inicializa_preferencia(Context c, AttributeSet attrs){
        clicks=0;
        myPrefAttrsInfo = new PrefAttrsInfo(c, attrs, getTitle(), getSummary(),getKey(), true);
        mDefaultValue=myPrefAttrsInfo.get_my_string_def_value();
        String sd = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        setDefaultValue(mDefaultValue);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
    return view;
    }


    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        setSummary(mCurrentValue);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        vAndroidIcon.setAlpha(alpha);
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mCurrentValue=getPersistedString(mDefaultValue);
        } else {
            mCurrentValue=mDefaultValue;
            persistString(mCurrentValue);
        }
        save_value_in_settings(mCurrentValue);
    }

    private void save_value_in_settings(String valor){
        if(!myPrefAttrsInfo.is_valid_key()) return;
        if(myPrefAttrsInfo.get_allowed_save_in_settings_db()) {
                String real = Settings.System.getString(getContext().getContentResolver(), this.getKey());
                if(real==null) real="N/A";
                if (!real.equals(valor)) {
                    Settings.System.putString(getContext().getContentResolver(), this.getKey(), valor);
                }
            }
    }

    private void set_up_double_click(){
        handler = new Handler();
        timeout = Long.valueOf(ViewConfiguration.getDoubleTapTimeout());
        doble_clic_pendiente=false;

        RDobleClick = new Runnable() {
            @Override
            public void run() {
                if(!doble_clic_pendiente){
                    accion_click();
                }else {
                    if(clicks==0) accion_click();
                    else if(clicks!=2) {
                        accion_click();
                    }else {
                        accion_doble_click();
                    }

                }
            }
        };
    }


    private void accion_doble_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        reset_value();
    }

    private void reset_value(){
        if(mCurrentValue.equals(mDefaultValue)) return;
        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
        dlg.setTitle(getContext().getResources().getString(R.string.gs_tit_reset));
        dlg.setMessage(getContext().getResources().getString(R.string.gs_mensaje_reset));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                set_value(mDefaultValue);

            }
        });
        dlg.show();
    }



    @Override
    protected void onClick() {
        if(handler==null) set_up_double_click();
        if(RDobleClick==null) show_dialog();
        else{
            clicks++;
            if(!doble_clic_pendiente){
                handler.removeCallbacks(RDobleClick);
                doble_clic_pendiente=true;
                handler.postDelayed(RDobleClick,timeout);
            }
        }
    }

    private void accion_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        show_dialog();
    }




    private void show_dialog() {
        GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(prefsScreen!=null){
            DlgFrEditText dlg = (DlgFrEditText)prefsScreen.getFragmentManager().findFragmentByTag("Common.TAG_DLGFRGREDITTEXT");
            if(dlg==null) {
                dlg = DlgFrEditText.newInstance(this, Common.TAG_PREFSSCREEN_FRAGMENT,myPrefAttrsInfo.get_my_key(),myPrefAttrsInfo.get_my_title(),mCurrentValue);
                dlg.show(prefsScreen.getFragmentManager(),Common.TAG_DLGFRGREDITTEXT);
            }
        }
    }

    private void set_value(String value){
        if(!value.equals(mCurrentValue)){
            mCurrentValue=value;
            setSummary(mCurrentValue);
            persistString(value);
            notifyChanged();
            save_value_in_settings(value);
            if(getOnPreferenceChangeListener()!=null) getOnPreferenceChangeListener().onPreferenceChange(this,value);
            send_broadcasts_and_change_group_key();
        }
    }

    public void onEditTextDone(String text){
            set_value(text);
    }


    /********* broadcast , change group key value **********/

    private void send_broadcasts_and_change_group_key(){
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
