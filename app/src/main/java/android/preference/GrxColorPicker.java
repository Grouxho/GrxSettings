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
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.ClipboardManager;
import android.widget.Toast;


import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.R;
import com.mods.grx.settings.prefssupport.colorpicker.CircleColorDrawable;
import com.mods.grx.settings.dlgs.DlgFrGrxColorPicker;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;

public class GrxColorPicker extends Preference implements DlgFrGrxColorPicker.OnGrxColorPickerListener,
        GrxPreferenceScreen.CustomDependencyListener{

	protected boolean alphaSlider;
	protected boolean lightSlider;
	protected int selectedColor = 0;


    boolean mWheelType;
	protected int density;
	private String pickerTitle;


    private Runnable RDobleClick;
    private Handler handler;
    private boolean doble_clic_pendiente;
    private Long timeout;
    private int clicks;

    private int mDefaultValue;
    private boolean Auto;
    private PrefAttrsInfo myPrefAttrsInfo;

    private ImageView vAndroidIcon;

	protected ImageView colorIndicator;


	public GrxColorPicker(Context context) {
		super(context);
	}

	public GrxColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		ini_param(context, attrs);
	}

	public GrxColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		ini_param(context, attrs);
	}


	private void ini_param(Context context, AttributeSet attrs) {

        myPrefAttrsInfo = new PrefAttrsInfo(context, attrs, getTitle(), getSummary(),getKey(), getContext().getResources().getInteger(R.integer.def_grxDefColor));
        clicks=0;

        Resources res = context.getResources();
        Auto = attrs.getAttributeBooleanValue(null, "grxAuto", res.getBoolean(R.bool.def_grxColorAuto));
		lightSlider = true;
        alphaSlider=attrs.getAttributeBooleanValue(null, "grxAlpha", res.getBoolean(R.bool.def_grxAlpha));
        density=12;
        mWheelType = attrs.getAttributeBooleanValue(null, "grxFlower", res.getBoolean(R.bool.def_grxFlower));
        mDefaultValue =attrs.getAttributeIntValue(null, "grxDefColor", res.getInteger(R.integer.def_grxDefColor));
        String sVdef = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        if (sVdef==null) {
            setDefaultValue(mDefaultValue);
        }
        pickerTitle=getTitle().toString();
        if(pickerTitle==null) pickerTitle=res.getString(R.string.gs_titulo_def_color_picker);
		setWidgetLayoutResource(R.layout.pref_colorpicker_color_widget);
	}


    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        mDefaultValue = ta.getInt(index, getContext().getResources().getInteger(R.integer.def_grxDefColor));
        return mDefaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        if (restorePersistedValue) {
            selectedColor=getPersistedInt(mDefaultValue);
        } else {
            selectedColor= mDefaultValue;
            persistInt(selectedColor);
        }
        save_value_in_settings_system();
    }


    private void save_value_in_settings_system(){
        if(!myPrefAttrsInfo.is_valid_key()) return;
        if(myPrefAttrsInfo.get_allowed_save_in_settings_db()) {
            int vtemp = Settings.System.getInt(getContext().getContentResolver(), this.getKey(),mDefaultValue);
            if (vtemp!=selectedColor) {
                Settings.System.putInt(getContext().getContentResolver(), this.getKey(), selectedColor);
            }
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        colorIndicator = (ImageView) view.findViewById(R.id.color_indicator);
        colorIndicator.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cbm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("color", (Integer.toHexString(selectedColor).toUpperCase()));
                cbm.setPrimaryClip(clip);
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.gs_copiado_clipboard),Toast.LENGTH_LONG).show();
                return true;
            }
        });
        return view;
    }


	@Override
	protected void onBindView(@NonNull View view) {
		super.onBindView(view);
        CircleColorDrawable colorChoiceDrawable = null;
		Drawable currentDrawable = colorIndicator.getDrawable();
        if (currentDrawable!=null && currentDrawable instanceof CircleColorDrawable)
            colorChoiceDrawable = (CircleColorDrawable) currentDrawable;
        if (colorChoiceDrawable==null) {
            colorChoiceDrawable = new CircleColorDrawable(selectedColor);
        }
		int tmpColor = isEnabled()
			? selectedColor
			: darken(selectedColor, .5f);
		colorIndicator.setImageDrawable(colorChoiceDrawable);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        if (vAndroidIcon!=null) vAndroidIcon.setAlpha(alpha);
        if(colorIndicator!=null) colorIndicator.setAlpha(alpha);

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
        if(RDobleClick==null) cp_dialog();
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
        cp_dialog();
    }

    private void accion_doble_click(){
        clicks=0;
        doble_clic_pendiente=false;
        handler.removeCallbacks(RDobleClick);
        if(mDefaultValue==selectedColor) return;
        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
        dlg.setTitle(getContext().getResources().getString(R.string.gs_tit_reset));
        dlg.setMessage(getContext().getResources().getString(R.string.gs_mensaje_reset));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                save_value(mDefaultValue);

            }
        });
        dlg.show();

    }


	public void save_value(int valor) {
        if(selectedColor!=valor){
            selectedColor = valor;
            persistInt(valor);
            notifyChanged();
            save_value_in_settings_system();
            if(getOnPreferenceChangeListener()!=null) getOnPreferenceChangeListener().onPreferenceChange(this,selectedColor);
            send_broadcasts_and_change_group_key();
        }
	}


	public static int darken(int color, float factor) {

		int a = Color.alpha(color);

		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);

		return Color.argb(a,
			Math.max((int)(r * factor), 0),
			Math.max((int)(g * factor), 0),
			Math.max((int)(b * factor), 0));


	}



    private void cp_dialog(){

        GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
        if(prefsScreen!=null){
            DlgFrGrxColorPicker dlgFrGrxColorPicker =  (DlgFrGrxColorPicker) prefsScreen.getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRXCOLORPICKER);
            if (dlgFrGrxColorPicker==null){
                dlgFrGrxColorPicker= DlgFrGrxColorPicker.newInstance(this, Common.TAG_PREFSSCREEN_FRAGMENT, myPrefAttrsInfo.get_my_title(),myPrefAttrsInfo.get_my_key(),selectedColor,mWheelType,alphaSlider,Auto);
                dlgFrGrxColorPicker.show(prefsScreen.getFragmentManager(),Common.TAG_DLGFRGRXCOLORPICKER);
            }
        }



    }

    public void onGrxColorSet(int color){
        save_value(color);
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