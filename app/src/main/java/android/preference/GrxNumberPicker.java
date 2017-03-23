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
import android.content.res.TypedArray;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.dlgs.DlgFrGrxNumberPicker;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;


public class GrxNumberPicker extends Preference implements DlgFrGrxNumberPicker.OnGrxNumberPickerSetListener,
        GrxPreferenceScreen.CustomDependencyListener {


    private String mUnits;
    private int min=0;
    private int max=5;
    private int mValue;
    private int mDefaultValue;
    private TextView vTxtValue;
    ImageView vAndroidIcon;
    private PrefAttrsInfo myPrefAttrsInfo;

    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;


    public GrxNumberPicker(Context c) {
        super(c);
    }

    public GrxNumberPicker(Context c, AttributeSet a) {
        super(c, a);
        ini_preference(c, a);


    }

    private void ini_preference(Context c, AttributeSet att) {
        setWidgetLayoutResource(R.layout.widget_text);
        clicks=0;
        myPrefAttrsInfo = new PrefAttrsInfo(c, att, getTitle(), getSummary(),getKey(), 0);
        min = att.getAttributeIntValue(null, "grxMin", 0);
        max = att.getAttributeIntValue(null,"grxMax", 5);
        mUnits = att.getAttributeValue(null, "grxUni");
        String s_def=att.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        if(s_def==null){
            mDefaultValue=min;
            mValue=mDefaultValue;
            setDefaultValue(min);
        }
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        mDefaultValue= a.getInt(index,min );
        return mDefaultValue;
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mValue=getPersistedInt(mDefaultValue);
        } else {
            mValue=mDefaultValue;
            if(getKey()==null || getKey().isEmpty()) return;
            persistInt(mValue);
        }
        save_value_in_settings(mValue);


    }

    private void save_value_in_settings(int valor){
        if(!myPrefAttrsInfo.is_valid_key()) return;
            if(myPrefAttrsInfo.get_allowed_save_in_settings_db()){
                int real = Settings.System.getInt(getContext().getContentResolver(), this.getKey(), -1);
                if (real!=valor) {
                    Settings.System.putInt(getContext().getContentResolver(), this.getKey(), valor);
                }
            }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        vTxtValue = (TextView) view.findViewById(R.id.pref_text_value);
        return view;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        set_value_text(mValue);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        vAndroidIcon.setAlpha(alpha);
        vTxtValue.setAlpha(alpha);
    }


    public void set_value_text(int v) {
        mValue = v;
        if ((mUnits != null) && (!mUnits.isEmpty()))
            vTxtValue.setText(Integer.toString(v).concat(" ").concat(mUnits));
        else vTxtValue.setText(Integer.toString(v));
    }


    private void set_value(int v) {

        if(mValue!=v){
            set_value_text(v);
            if(!myPrefAttrsInfo.is_valid_key()) return;
            persistInt(mValue);
            notifyChanged();
            save_value_in_settings(mValue);
            if(getOnPreferenceChangeListener()!=null) getOnPreferenceChangeListener().onPreferenceChange(this,mValue);
            send_broadcasts_and_change_group_key();
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

    private void accion_doble_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        reset_value();
    }




    private void reset_value(){
        if(mValue==mDefaultValue) return;
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


    private void accion_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        show_dialog();
    }

    private void show_dialog(){
        GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(prefsScreen!=null){
            DlgFrGrxNumberPicker dlgFrGrxNumberPicker = (DlgFrGrxNumberPicker) prefsScreen.getFragmentManager().findFragmentByTag("DlgFrGrxNumberPicker");
            if (dlgFrGrxNumberPicker==null){
                dlgFrGrxNumberPicker = DlgFrGrxNumberPicker.newInstance(this,getKey(), myPrefAttrsInfo.get_my_title(), mValue,min,max,mUnits, Common.TAG_PREFSSCREEN_FRAGMENT);
                dlgFrGrxNumberPicker.show(prefsScreen.getFragmentManager(), "DlgFrGrxNumberPicker");
            }
        }
    }



    public void onGrxNumberPickerSet(int value, String key){
        set_value(value);
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

