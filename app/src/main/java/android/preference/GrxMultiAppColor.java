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
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;



import com.mods.grx.settings.Common;
import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.dlgs.DlgFrGrxMultiAppColor;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;
import com.mods.grx.settings.utils.Utils;

import java.util.regex.Pattern;


public class GrxMultiAppColor extends Preference implements DlgFrGrxMultiAppColor.OnGrxMultiAppColorListener,
        GrxPreferenceScreen.CustomDependencyListener{

    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;



    private boolean mShowAllApps;
    private int grxMaxApps;
    private boolean mSaveActivityName;

    private boolean mShowColorSelection;
    private int mDefaultColor;
    private boolean mShowAuto;
    private boolean mFlowerStyle;
    private boolean mShowAlpha;


    int idwidgetlayout=0;
    int arrowvisibility;
    ImageView vWidgetArrow =null;
    ImageView vAndroidIcon;



    private PrefAttrsInfo myPrefAttrsInfo;
    private String mValue;
    private String mLabel;
    private String mDefaultValue;

    public GrxMultiAppColor (Context c){
        super(c);
    }

    public  GrxMultiAppColor(Context c, AttributeSet a){
        super(c,a);
        ini_param(c,a);
    }


    public GrxMultiAppColor(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        ini_param(context,attrs);
    }

    private void ini_param(Context c, AttributeSet attrs){
        clicks=0;
        myPrefAttrsInfo = new PrefAttrsInfo(c, attrs, getTitle(), getSummary(),getKey(), true);

        Resources res = getContext().getResources();

        mShowAllApps=attrs.getAttributeBooleanValue(null,"grxAllApps",false); //user and system apps
        mSaveActivityName = attrs.getAttributeBooleanValue(null, "grxSaveActName", false);
        grxMaxApps=myPrefAttrsInfo.get_my_max_items();

        mShowColorSelection=attrs.getAttributeBooleanValue(null,"grxColor",true);
        mDefaultColor=attrs.getAttributeIntValue(null,"grxDefcolor",getContext().getResources().getColor(R.color.defcolor_multiapp));
        mShowAuto=attrs.getAttributeBooleanValue(null, "grxAuto", res.getBoolean(R.bool.def_grxColorAuto));
        mFlowerStyle=attrs.getAttributeBooleanValue(null, "grxFlower", res.getBoolean(R.bool.def_grxAlpha));
        mShowAlpha=attrs.getAttributeBooleanValue(null, "grxAlpha", res.getBoolean(R.bool.def_grxAlpha));

        setDefaultValue(myPrefAttrsInfo.get_my_string_def_value());

        mValue = myPrefAttrsInfo.get_my_string_def_value();

    }




    @Override
    protected View onCreateView(ViewGroup parent) {
        idwidgetlayout = getLayoutResource();
        if(idwidgetlayout==0) setWidgetLayoutResource(R.layout.widget_arrow);
        arrowvisibility = (idwidgetlayout==0) ? View.GONE : View.VISIBLE;
        View view = (View) super.onCreateView(parent);
        vWidgetArrow = (ImageView) view.findViewById(R.id.widget_arrow);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        return view;
    }

    @Override
    public void onBindView(View view) {
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        if (vWidgetArrow != null) vWidgetArrow.setAlpha(alpha);
        if (vAndroidIcon != null) vAndroidIcon.setAlpha(alpha);
        setSummary(mLabel);
        super.onBindView(view);

    }



    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        mDefaultValue = a.getString(index);
        return mDefaultValue;
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


    private void config_preference(String valor){
        int napps=0;
        if(! (valor==null || valor.isEmpty())  ){
            String[] arr = valor.split(Pattern.quote(myPrefAttrsInfo.get_my_separator()));
            napps=arr.length;
        }
        if(napps==0) mLabel=myPrefAttrsInfo.get_my_summary();
        else mLabel= getContext().getString( R.string.gs_multi_apps_seleccionadas,napps ) ;
    }


    private void save_value(String value) {
        mValue=value;
        if(!myPrefAttrsInfo.is_valid_key()) return;
        persistString(mValue);
        notifyChanged();
        if(getOnPreferenceChangeListener()!=null) getOnPreferenceChangeListener().onPreferenceChange(this,mValue);
        save_value_in_settings_db(mValue);
        send_broadcasts_and_change_group_key();
    }


    @Override
    public void onGrxMultiAppColorSel(DlgFrGrxMultiAppColor dialog, int num, String apps){
        if(!mValue.equals(apps)){
            save_value(apps);
            config_preference(mValue);
        }
    }



 /*
    CLICK, DOUBLE CLICK..
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
        reset_value();
    }



    private void reset_value(){
        if(mValue.isEmpty()) return;
        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
        dlg.setTitle(getContext().getResources().getString(R.string.gs_tit_reset));
        dlg.setMessage(getContext().getResources().getString(R.string.gs_mensaje_reset));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mValue= myPrefAttrsInfo.get_my_string_def_value();
                config_preference(mValue);
                save_value(mValue);
                setSummary(mLabel);
            }
        });
        dlg.show();
    }


    private void show_dialog(){
        GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(prefsScreen!=null){
            DlgFrGrxMultiAppColor dlg = (DlgFrGrxMultiAppColor) prefsScreen.getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRMULTIPPCOLOR);
            if(dlg!=null) return;
            dlg = DlgFrGrxMultiAppColor.newInstance(this, Common.TAG_PREFSSCREEN_FRAGMENT, myPrefAttrsInfo.get_my_key(), myPrefAttrsInfo.get_my_title(),mValue,
                    mShowAllApps,mSaveActivityName,myPrefAttrsInfo.get_my_max_items(),
                    mShowColorSelection,mDefaultColor,mFlowerStyle,mShowAlpha,mShowAuto,
                    myPrefAttrsInfo.get_my_separator());
            dlg.show(prefsScreen.getFragmentManager(),Common.TAG_DLGFRGRMULTIPPCOLOR);

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
