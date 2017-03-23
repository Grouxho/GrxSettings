package android.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.mods.grx.settings.act.GrxImagePicker;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;
import com.mods.grx.settings.utils.GrxImageHelper;

import java.io.File;


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

public class GrxPickImage extends Preference implements  GrxPreferenceScreen.CustomDependencyListener {

    private PrefAttrsInfo myPrefAttrsInfo;

    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;

    ImageView vWidgetIcon = null;
    ImageView vWidgetArrow =null;
    ImageView vAndroidIcon;

    private int mSizeX=0;
    private int mSizeY=0;
    private boolean mCircular=false;

    private boolean mJustUri=true;

    private Drawable mIcon=null;

    private String mValue="";

    int idwidgetlayout=0;
    int arrowvisibility;

    int mIconSize;


    public GrxPickImage(Context c){
        super(c);
    }

    public GrxPickImage(Context c, AttributeSet a){
        super(c,a);
        ini_param(c,a);
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        show_info();
    }

    public GrxPickImage(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        ini_param(context,attrs);
    }

    private void ini_param(Context context, AttributeSet attrs){
        clicks=0;
        myPrefAttrsInfo = new PrefAttrsInfo(context, attrs, getTitle(), getSummary(),getKey(), false);
        setDefaultValue(myPrefAttrsInfo.get_my_string_def_value());
        idwidgetlayout = getLayoutResource();
        if(idwidgetlayout==0) setWidgetLayoutResource(R.layout.widget_arrow);
        arrowvisibility = (idwidgetlayout==0) ? View.GONE : View.VISIBLE;

        mSizeX = attrs.getAttributeIntValue(null,"grxSizeX",0);
        mSizeY = attrs.getAttributeIntValue(null,"grxSizeY",0);
        mCircular = attrs.getAttributeBooleanValue(null,"grxCircular",false);

        mJustUri = (mSizeX!=0 && mSizeY!=0) ? false : true;

        Resources resources = getContext().getResources();

        mIconSize = resources.getDimensionPixelSize(R.dimen.icon_size_in_prefs);


    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = (View) super.onCreateView(parent);
        vWidgetArrow = (ImageView) view.findViewById(R.id.widget_arrow);
        vWidgetIcon = (ImageView) view.findViewById(R.id.widget_icon);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vWidgetIcon.setScaleType(ImageView.ScaleType.CENTER);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        return view;
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
        get_image();
    }


    private void get_image(){
        if(Common.SyncUpMode) return;
        if(mValue==null){
            mValue="";
            mIcon=null;
            show_info();
        }else {
            ImageLoader imageLoader = new ImageLoader();
            imageLoader.execute();
        }

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
        if(RDobleClick==null) pick_image();
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
        pick_image();
    }


    private void accion_doble_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        reset_value();
    }



    private void pick_image(){
        if(!myPrefAttrsInfo.is_valid_key()) return;
        GrxPreferenceScreen chl = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(chl!=null){

            if(mJustUri) {
                Intent intent = new Intent(chl.getActivity(), GrxImagePicker.class);
                intent.putExtra(Common.TAG_DEST_FRAGMENT_NAME_EXTRA_KEY,myPrefAttrsInfo.get_my_key());
                intent.putExtra(GrxImagePicker.S_URI_MODE,true);
                chl.pick_image(intent, Common.REQ_CODE_GALLERY_IMAGE_PICKER_JUST_URI);
            }
            else {
                Intent intent = new Intent(chl.getActivity(), GrxImagePicker.class);
                intent.putExtra(Common.TAG_DEST_FRAGMENT_NAME_EXTRA_KEY,myPrefAttrsInfo.get_my_key());
                intent = GrxImageHelper.intent_avatar_img(intent, mSizeX, mSizeY,mCircular);
                String output_file_name = Common.IconsDir + File.separator + String.valueOf(System.currentTimeMillis()+".jpg");
                intent.putExtra(GrxImagePicker.S_OUTPUT_FILE_NAME,output_file_name);
                chl.pick_image(intent, Common.REQ_CODE_GALLERY_IMAGE_PICKER_CROP_CIRCULAR);
            }
        }
    }

    private void reset_value(){
        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
        dlg.setTitle(getContext().getResources().getString(R.string.gs_tit_reset));
        dlg.setMessage(getContext().getResources().getString(R.string.gs_mensaje_reset));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                set_new_value(myPrefAttrsInfo.get_my_string_def_value());
            }
        });
        dlg.show();
    }



    private void show_info(){
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        if(vWidgetArrow !=null) {
            if(mIcon!=null ) {
                vWidgetIcon.setImageDrawable(mIcon);
                vWidgetIcon.setVisibility(View.VISIBLE);
                vWidgetArrow.setVisibility(View.GONE);
            }else{
                vWidgetArrow.setVisibility(arrowvisibility);
                vWidgetIcon.setVisibility(View.GONE);
            }
            vWidgetArrow.setAlpha(alpha);
            vWidgetIcon.setAlpha(alpha);
        }
        if(vAndroidIcon!=null) vAndroidIcon. setAlpha(alpha);
    }


    public void set_new_value(String new_value){
          boolean detele_current_img=false;
          if(mValue!=null){
              detele_current_img = mValue.contains( getContext().getString(R.string.grx_dir_datos_app)+File.separator+getContext().getString(R.string.grx_ico_sub_dir) );
          }
          if(detele_current_img) {
              File file = new File(Uri.parse(mValue).getPath());
              if(file!=null && file.exists()) file.delete();
          }

        mValue = new_value;
        set_value(mValue);

    }

    private void set_value(String uri){
        if(uri==null) mValue="";
        else mValue=uri;
        persistString(mValue);
        notifyChanged();
        if(getOnPreferenceChangeListener()!=null) getOnPreferenceChangeListener().onPreferenceChange(this,mValue);
        save_value_in_settings_db(mValue);
        get_image();

        send_broadcasts_and_change_group_key();
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



    private class ImageLoader extends AsyncTask<Void, Void,Void> {

        @Override
        protected Void doInBackground(Void...params) {
            if(mValue!=null && !mValue.isEmpty()){
                    mIcon = GrxImageHelper.get_scaled_drawable_from_uri_string_for_square_container(getContext(),mValue,mIconSize);
            }else mIcon=null;
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            show_info();
        }
    }
}
