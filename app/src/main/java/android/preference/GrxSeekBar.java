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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.prefssupport.info.PrefAttrsInfo;


public class GrxSeekBar extends Preference implements OnSeekBarChangeListener,
        GrxPreferenceScreen.CustomDependencyListener {

    private final String TAG = getClass().getName();
    private int mMax = 3;
    private int mMin = 0;
    private int mInterval= 1;
    private int mDefaultValue;
    private int mCurrValue;
    private String mUnits = "";
    private boolean mPopup;

    private SeekBar mSeekBar;


    TextView vTxtPopup;
    FrameLayout vPopup;

    TextView vTxtValue;
    TextView vTxtMax;
    TextView vTxtMin;

    private int mValorAux;

    private PrefAttrsInfo myPrefAttrsInfo;

    public GrxSeekBar(Context context) {
        super(context);
        setLayoutResource(R.layout.pref_seekbar_lay);
        setWidgetLayoutResource(R.layout.pref_seekbar_widget_lay);

    }

    public GrxSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini_param(context, attrs);
    }

    public GrxSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ini_param(context, attrs);
    }

    private void ini_param(Context context, AttributeSet attrs) {

        setLayoutResource(R.layout.pref_seekbar_lay);
        setWidgetLayoutResource(R.layout.pref_seekbar_widget_lay);
        Resources res = context.getResources();
        mMax = attrs.getAttributeIntValue(null, "grxMax", 3);
        mMin = attrs.getAttributeIntValue(null, "grxMin", 0);
        mUnits = getAttributeStringValue(attrs, null, "grxUni", "");
        mPopup = attrs.getAttributeBooleanValue(null,"grxPopup",true);
        try {
            String newInterval = attrs.getAttributeValue(null, "grxInter");
            if (newInterval != null)
                mInterval = Integer.parseInt(newInterval);
        } catch (Exception e) {
        }

        myPrefAttrsInfo = new PrefAttrsInfo(context, attrs, getTitle(), getSummary(),getKey(), mMin);

        String sVdef = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        if (sVdef==null) {
            mDefaultValue=mMin;
            setDefaultValue(mDefaultValue);
        }


  }

    private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
        String value = attrs.getAttributeValue(namespace, name);
        if (value == null)
            value = defaultValue;
        return value;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        View view = super.onCreateView(parent);
        LinearLayout layout = (LinearLayout) view;
        layout.setOrientation(LinearLayout.VERTICAL);
        vPopup = (FrameLayout) view.findViewById(R.id.popup);
        vTxtPopup =(TextView) vPopup.findViewById(R.id.vtxtpopup);
        vTxtValue =(TextView) view.findViewById(R.id.texto_valor_pref);
        View widget = view.findViewById(android.R.id.widget_frame);;
        widget.setPadding(0,0,0,0);
        vTxtMax = (TextView) widget.findViewById(R.id.seekbar_max_value);
        vTxtMin = (TextView) widget.findViewById(R.id.seekbar_min_value);
        if(myPrefAttrsInfo!=null) {
            if(myPrefAttrsInfo.get_my_summary().isEmpty()){
                FrameLayout.LayoutParams newpopupparams = (FrameLayout.LayoutParams) vPopup.getLayoutParams();
                newpopupparams.topMargin=0;
                vPopup.setPadding(30,6,30,6);
            }
        }

        return view;
    }

    @Override
    public void onBindView(View view) {
        vTxtMax.setText(String.valueOf(mMax));
        vTxtMin.setText(String.valueOf(mMin));
        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(mCurrValue - mMin);
        vTxtValue.setText(String.valueOf(mCurrValue)+ " "+mUnits);
        super.onBindView(view);
        if (view != null && !view.isEnabled()) {
            mSeekBar.setEnabled(false);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        int newValue = progress + mMin;
        if (newValue > mMax)
            newValue = mMax;
        else if (newValue < mMin)
            newValue = mMin;
        else if (mInterval != 1 && newValue % mInterval != 0)
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;
    /*  //if someone wants real time changes the following code should be executed

        if (!callChangeListener(newValue)) {
            seekBar.setProgress(mCurrValue - mMin);
            return;
        }*/
        mCurrValue = newValue;
        seekBar.setProgress(mCurrValue - mMin);
        if(vTxtValue!=null) vTxtValue.setText(String.valueOf(mCurrValue)+ " "+mUnits);
        if(mUnits.isEmpty()) vTxtValue.setText(String.valueOf(mCurrValue)+ " "+mUnits);
        if(mPopup) {
            if(mUnits.isEmpty()) vTxtPopup.setText(String.valueOf(mCurrValue));
            else vTxtPopup.setText(String.valueOf(mCurrValue)+ " "+mUnits);
        }
       //persistInt(newValue);  // un-comment for real time changes. BUT do not use customized dependencies if your seekbar´s values range is big and interval little or it will be laggy.
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mPopup) vPopup.setVisibility(View.VISIBLE);
        mValorAux = mCurrValue;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mPopup) vPopup.setVisibility(View.INVISIBLE);
        if(mCurrValue!=mValorAux){
            if(getKey()==null || getKey().isEmpty()) return;
            persistInt(mCurrValue);
            callChangeListener(mCurrValue);
            save_value_in_settings_system();
            notifyChanged();
            send_broadcasts_and_change_group_key();
        }
    }


    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        mDefaultValue = ta.getInt(index, 0);
        return mDefaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int real;

        if (restoreValue) {
            mCurrValue = getPersistedInt(mDefaultValue);
        } else {
            int temp = 0;
            try {
                temp = (Integer) defaultValue;
            } catch (Exception ex) {
            }

            mCurrValue = temp;
            if(getKey()==null || getKey().isEmpty()) return;
            persistInt(temp);
        }

        if(myPrefAttrsInfo.get_allowed_save_in_settings_db()){
            if(!myPrefAttrsInfo.is_valid_key()) return;
                  try {
                    real = Settings.System.getInt(getContext().getContentResolver(), this.getKey());
                    if(real!=mCurrValue){
                        Settings.System.putInt(getContext().getContentResolver(), this.getKey(), mCurrValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
          }
    }

    /**
     * make sure that the seekbar is disabled if the preference is disabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(mSeekBar!=null) mSeekBar.setEnabled(enabled);
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        if (mSeekBar != null) {
            mSeekBar.setEnabled(!disableDependent);
        }
    }

    private void save_value_in_settings_system(){
        if(!myPrefAttrsInfo.is_valid_key()) return;
        if(myPrefAttrsInfo.get_allowed_save_in_settings_db()) {
                int vtemp = Settings.System.getInt(getContext().getContentResolver(), this.getKey(),mDefaultValue);
                if (vtemp!=mCurrValue) {
                    Settings.System.putInt(getContext().getContentResolver(), this.getKey(), mCurrValue);
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
