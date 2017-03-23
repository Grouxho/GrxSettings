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
import android.content.Intent;
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

import com.mods.grx.settings.dlgs.DlgFrGrxAccess;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;
import com.mods.grx.settings.utils.Utils;

import java.io.File;
import java.net.URISyntaxException;



public class GrxAccess extends Preference implements
        DlgFrGrxAccess.GrxAccesListener,
        GrxPreferenceScreen.CustomDependencyListener{



    private String mValue="";

    private boolean mSaveCustomActionsIcons;

    private boolean mShowShortCuts;
    private boolean mShowUsuApps;
    private boolean mShowActivities;

    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;

    private int id_options_array;
    private int id_values_array;
    private int id_icons_array;

    private PrefAttrsInfo myPrefAttrsInfo;

    private Drawable mIcon;
    private String mLabel;

    private ImageView vAndroidIcon;
    ImageView vWidgetArrow =null;
    ImageView vWidgetIcon =null;

    private boolean mArrow=true;


    public GrxAccess(Context c){
        super(c);
    }

    public GrxAccess(Context c, AttributeSet a){
        super(c,a);
        ini_param(c,a);
    }


    public GrxAccess(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        ini_param(context,attrs);
    }



    /***** set up and initial values *************/

    private void ini_param(Context context, AttributeSet attrs){
        clicks=0;

        myPrefAttrsInfo = new PrefAttrsInfo(context, attrs, getTitle(), getSummary(),getKey(), false);

        Resources res = context.getResources();

        mLabel =myPrefAttrsInfo.get_my_summary();
        if(getWidgetLayoutResource()==0) {
            setWidgetLayoutResource(R.layout.widget_arrow);
            mArrow=false;
        }

        id_options_array = Utils.get_array_id(getContext(),attrs.getAttributeValue(null,"grxA_entries"));
        id_values_array = Utils.get_array_id(getContext(),attrs.getAttributeValue(null,"grxA_values"));
        id_icons_array = Utils.get_array_id(getContext(),attrs.getAttributeValue(null,"grxA_ics"));

        mSaveCustomActionsIcons = attrs.getAttributeBooleanValue(null, "grxSv_ics", res.getBoolean(R.bool.def_grxSv_ics) );

        mShowShortCuts = attrs.getAttributeBooleanValue(null, "grxShc", res.getBoolean(R.bool.def_grxShc) );
        mShowUsuApps = attrs.getAttributeBooleanValue(null, "grxApps", res.getBoolean(R.bool.def_grxApps) );
        mShowActivities=attrs.getAttributeBooleanValue(null, "grxAct", res.getBoolean(R.bool.def_grxAct) );

        //si no existe android:defaultValue -> establecemos un valor por defecto y así se ejecuta onSetinitial y podemos persistir y sincronizar con settings system
        //importante no hacer nada en ongetdefaultvalue, pues si en el xml existe android:defaultValue, será el primer método que se ejecute, antes que ini_param y no
        //estará inicializado nada de nada.

        setDefaultValue(myPrefAttrsInfo.get_my_string_def_value());

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return myPrefAttrsInfo.get_my_string_def_value();
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mValue=getPersistedString(myPrefAttrsInfo.get_my_string_def_value());
        } else {
            mValue= myPrefAttrsInfo.get_my_string_def_value();
            if(!myPrefAttrsInfo.is_valid_key()) return;
            persistString(mValue);
        }
        save_value_in_settings_db(mValue);
        config_preference(mValue);

    }

     /************** view & value management *******************************/

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
        vWidgetIcon.setImageDrawable(mIcon);
        vWidgetArrow.setVisibility(View.GONE);
        if(mIcon==null){
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

    private void save_value(String value) {
        mValue=value;
        if(mValue==null) mValue="";
        if(!myPrefAttrsInfo.is_valid_key()) return;
        persistString(mValue);
        notifyChanged();
        getOnPreferenceChangeListener().onPreferenceChange(this,mValue);
        save_value_in_settings_db(mValue);
        send_broadcasts_and_change_group_key();
    }


    private void config_preference(String value){
        Intent intent=null;
        mIcon=null;
        mLabel = myPrefAttrsInfo.get_my_summary();
        if(value==null || value.isEmpty()) {return;}
        try {
            intent = Intent.parseUri(value, 0);
        }catch (URISyntaxException e) {
            return;
        }
        mLabel = Utils.get_activity_label_from_intent(getContext(), intent);
        setSummary(mLabel);
        mIcon = Utils.get_drawable_from_intent(getContext(),intent);
        //show_icon(mIcon);

    }

    /******************* click - double click  *************************************/




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
        clear_value();

    }

    private void clear_value(){
        if(mValue==null || mValue.isEmpty()) return;
        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
        dlg.setTitle(getContext().getResources().getString(R.string.gs_tit_reset));
        dlg.setMessage(getContext().getResources().getString(R.string.gs_mensaje_reset));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String curr_file_name = Utils.get_file_name_from_uri_string(mValue);
                mValue= myPrefAttrsInfo.get_my_string_def_value();
                String def_file_name =  Utils.get_file_name_from_uri_string(mValue);
                if(curr_file_name != null  ) {
                    if(def_file_name!= null){
                        if(!def_file_name.equals(curr_file_name)) Utils.delete_file(curr_file_name);
                    }else Utils.delete_file(curr_file_name);
                }
                save_value(mValue);
                config_preference(mValue);
                setSummary(mLabel);

            }
        });
        dlg.show();
    }


    /***************  Show Acess dialog *****************/

    private void show_dialog(){
        GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(prefsScreen!=null){

            if (prefsScreen.getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRACCESS) != null) return;

            DlgFrGrxAccess dlg = DlgFrGrxAccess.newInstance(this, Common.TAG_PREFSSCREEN_FRAGMENT, myPrefAttrsInfo.get_my_key(), myPrefAttrsInfo.get_my_title(),mValue,
                    mShowShortCuts,mShowUsuApps,mShowActivities,
                    id_options_array, id_values_array, id_icons_array, mSaveCustomActionsIcons,
                    false);
            dlg.show(prefsScreen.getFragmentManager(),Common.TAG_DLGFRGRACCESS);
        }


    }




    public void GrxSetAccess(String uri){
        Utils.delete_grx_icon_file_from_uri_string(mValue);
        mValue=uri;
        String uri_file_name = Utils.get_short_file_name_from_uri_string(uri);
        if(uri_file_name!=null){
            String new_file_name=uri_file_name.replace(Common.TMP_PREFIX,"");
            String dest_file_name = Common.IconsDir + File.separator + new_file_name;
            uri_file_name = Utils.get_file_name_from_uri_string(uri);
            Utils.file_copy(uri_file_name,dest_file_name);
            Utils.delete_file(uri_file_name);
            mValue = Utils.change_extra_value_in_uri_string(uri,Common.EXTRA_URI_ICON,dest_file_name);
        }
        save_value(mValue);
        config_preference(mValue);
    }



    /***************** just in case, to remember if in the future I add some functionality linked to custom dependency.... */

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
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
