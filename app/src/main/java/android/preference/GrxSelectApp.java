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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.dlgs.DlgFrSelecApp;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;
import com.mods.grx.settings.utils.Utils;

public class GrxSelectApp extends Preference implements DlgFrSelecApp.OnGrxAppListener,
        GrxPreferenceScreen.CustomDependencyListener{


    private boolean sys;
    private boolean ord;


    private String mDefaultValue;
    private String mLabel;

    private String mValue;
    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;

    private PrefAttrsInfo myPrefAttrsInfo;
    private boolean mArrow=true;


    private ImageView vAndroidIcon;
    ImageView vWidgetArrow =null;
    ImageView vWidgetIcon =null;

    Drawable mAppIcon=null;

    public GrxSelectApp(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini_param(context, attrs);

    }

    public GrxSelectApp(Context context){super(context); }

    public GrxSelectApp(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        ini_param(context, attrs);
    }


    private void ini_param(Context context, AttributeSet attrs){
        clicks=0;
        myPrefAttrsInfo = new PrefAttrsInfo(context, attrs, getTitle(), getSummary(),getKey(), false);
        Resources res = context.getResources();
        sys=attrs.getAttributeBooleanValue(null,"grxAllApps",false);
        ord=attrs.getAttributeBooleanValue(null,"GrxOrd",true);



        //si no existe android:defaultValue -> establecemos un valor por defecto y así se ejecuta onSetinitial y podemos persistir y sincronizar con settings system
        //importante no hacer nada en ongetdefaultvalue, pues si en el xml existe android:defaultValue, será el primer método que se ejecute, antes que ini_param y no
        //estará inicializado nada de nada.

        mDefaultValue =myPrefAttrsInfo.get_my_string_def_value();
        setDefaultValue(mDefaultValue);
        mLabel =myPrefAttrsInfo.get_my_summary();
        if(getWidgetLayoutResource()==0) {
            setWidgetLayoutResource(R.layout.widget_arrow);
            mArrow=false;
        }

        /*
        TypedArray a = getContext().getTheme().obtainStyledAttributes( new int[] {R.attr.tint_arrow_prefs});
        color_tint = a.getColor(0,0);
        a.recycle();*/
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        mDefaultValue = a.getString(index);
        return mDefaultValue;
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        vWidgetArrow = (ImageView) view.findViewById(R.id.widget_arrow);
        vWidgetIcon = (ImageView) view.findViewById(R.id.widget_icon);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        return view;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        vWidgetIcon.setImageDrawable(mAppIcon);
        vWidgetArrow.setVisibility(View.GONE);
        if(mAppIcon==null){
            if(mArrow) vWidgetArrow.setVisibility(View.VISIBLE);
            vWidgetIcon.setVisibility(View.GONE);
        }else{
            vWidgetIcon.setVisibility(View.VISIBLE);
        }
        setSummary(mLabel);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        vWidgetIcon.setAlpha(alpha);
        vWidgetArrow.setAlpha(alpha);
        vAndroidIcon.setAlpha(alpha);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if(mDefaultValue ==null) mDefaultValue ="";
        if (restorePersistedValue) {
            mValue =getPersistedString(mDefaultValue);

        } else {
            mValue = mDefaultValue;
            persistString(mValue);

        }
        save_value_in_settings_db(mValue);
        config_preference(mValue);
     }


    /*

    Apk seleccionada

     */


    @Override
    public void onGrxAppSel(DlgFrSelecApp dialog, String packagename){
        if(packagename!=null) {
            mValue = packagename;
            persistString(mValue);
            config_preference(mValue);
            notifyChanged();
            callChangeListener(mValue);
            save_value_in_settings_db(mValue);
            send_broadcasts_and_change_group_key();
        }
    }


    /*

    Click, doble click...

     */

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

    private void accion_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        show_dialog();
    }

    private void accion_doble_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        show_reset_dialgo();
    }

    private void show_dialog(){
        DlgFrSelecApp dlg;
        GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(prefsScreen!=null){
            dlg = (DlgFrSelecApp) prefsScreen.getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRSELECTAPP);
            if(dlg==null){
                dlg = DlgFrSelecApp.newInstance(this, Common.TAG_PREFSSCREEN_FRAGMENT, myPrefAttrsInfo.get_my_key(),myPrefAttrsInfo.get_my_title(),sys,ord);
                dlg.show(prefsScreen.getFragmentManager(),Common.TAG_DLGFRGRSELECTAPP);
            }
        }


    }

    private void show_reset_dialgo(){

        if(mValue.isEmpty()) return;
        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
        dlg.setTitle(getContext().getResources().getString(R.string.gs_tit_reset));
        dlg.setMessage(getContext().getResources().getString(R.string.gs_mensaje_reset));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    reset_value();
            }
        });
        dlg.show();
    }

    private void reset_value(){
        mValue = mDefaultValue;
        persistString(mValue);
        save_value_in_settings_db(mValue);
        config_preference(mValue);
        notifyChanged();
        callChangeListener(mValue);
        send_broadcasts_and_change_group_key();
    }


    private void config_preference(String packagename){
           mValue =packagename;
           mAppIcon= Utils.get_application_icon(getContext(),mValue);
           mLabel = Utils.get_app_name(getContext(),mValue);
           if(mLabel.isEmpty()) mLabel=myPrefAttrsInfo.get_my_summary();
            setSummary(mLabel);

    }


    private void save_value_in_settings_db(String value){
        if(!myPrefAttrsInfo.is_valid_key()) return;
        if(myPrefAttrsInfo.get_allowed_save_in_settings_db()) {
            String real = Settings.System.getString(getContext().getContentResolver(), this.getKey());
            if(real==null) real="N/A";
            if (!real.equals(value)) {
                Settings.System.putString(getContext().getContentResolver(), this.getKey(), value);
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




