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
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;

import java.util.Calendar;

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

public class GrxTimePicker extends Preference implements
        GrxPreferenceScreen.CustomDependencyListener{


    private int mDefaultValue;
    private int mValue;
    private String mFormattedValue="";

    private TextView vTxtValue;
    ImageView vAndroidIcon;

    private PrefAttrsInfo myPrefAttrsInfo;

    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;



    public GrxTimePicker(Context c) {
        super(c);
    }

    public GrxTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini_preference(context,attrs);

    }

    private void ini_preference(Context c, AttributeSet att) {
        setWidgetLayoutResource(R.layout.widget_text);
        clicks=0;
        myPrefAttrsInfo = new PrefAttrsInfo(c, att, getTitle(), getSummary(),getKey(), 0);
        String s_def=att.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        if(s_def==null){
            setDefaultValue(myPrefAttrsInfo.get_my_int_def_value());
        }else mDefaultValue=myPrefAttrsInfo.get_my_int_def_value();
    }

    public int get_int_from_string_time(String time){
        int h=0;
        int m=0;
        String arr[]=time.split(":");
        if (arr.length==2){
            h=Integer.valueOf(arr[0]);
            m=Integer.valueOf(arr[1]);
        }
        return ((h*60)+m);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        mDefaultValue = a.getInt(index, 0);
        return mDefaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int real;
        if (restoreValue) {
            mValue = getPersistedInt(mDefaultValue);
        } else {
            int temp = 0;
            try {
                temp = (Integer) defaultValue;
            } catch (Exception ex) {
            }
            mValue = temp;
            if (getKey() == null || getKey().isEmpty()) return;
            persistInt(temp);
        }

        set_formatted_value(mValue);
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
        vTxtValue.setText(mFormattedValue);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        vAndroidIcon.setAlpha(alpha);
        vTxtValue.setAlpha(alpha);
        vTxtValue.setText(mFormattedValue);
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



    private void accion_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        show_timepicker();
    }

    private void accion_doble_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        show_reset_dialog();
    }



    @Override
    protected void onClick(){

        if(handler==null) set_up_double_click();
        if(RDobleClick==null) show_timepicker();
        else{
            clicks++;
            if(!doble_clic_pendiente){
                handler.removeCallbacks(RDobleClick);
                doble_clic_pendiente=true;
                handler.postDelayed(RDobleClick,timeout);
            }
        }


    }


    private void show_timepicker(){
        GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(prefsScreen!=null) prefsScreen.ini_dlg_GrxTimePicker(getKey(), mValue);

    }


    private void show_reset_dialog(){
        if(mValue==mDefaultValue) return;
        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
        dlg.setTitle(getContext().getResources().getString(R.string.gs_tit_reset));
        dlg.setMessage(getContext().getResources().getString(R.string.gs_mensaje_reset));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset_preference_value();
            }
        });
        dlg.show();
    }


    public void set_new_value(int value){
        mValue=value;
        save_value();
        set_formatted_value(mValue);

    }


    private void save_value() {
        persistInt(mValue);
        set_formatted_value(mValue);
        notifyChanged();
        save_value_in_settings_system();
        if(getOnPreferenceChangeListener()!=null) getOnPreferenceChangeListener().onPreferenceChange(this,mValue);
        send_broadcasts_and_change_group_key();
    }

    private void reset_preference_value(){
        mValue=mDefaultValue;
        save_value();
    }


    private void set_formatted_value(int value){
        String hs;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,(int)value/60);
        cal.set(Calendar.MINUTE,(int)value%60);
        android.text.format.DateFormat df;
        hs = android.text.format.DateFormat.getTimeFormat(getContext()).format(cal.getTime());
        //if (value < (int) 600) hs = "0"+hs;
        mFormattedValue=hs;
    }



    private void save_value_in_settings_system(){
        if(!myPrefAttrsInfo.is_valid_key()) return;
        if(myPrefAttrsInfo.get_allowed_save_in_settings_db()) {
            int vtemp = Settings.System.getInt(getContext().getContentResolver(), this.getKey(),mDefaultValue);
            if (vtemp!=mValue) {
                Settings.System.putInt(getContext().getContentResolver(), this.getKey(), mValue);
            }
        }
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




