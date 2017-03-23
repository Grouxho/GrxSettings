package com.mods.grx.settings.dlgs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.GrxSettingsActivity;
import com.mods.grx.settings.R;
import com.mods.grx.settings.act.GrxImagePicker;
import com.mods.grx.settings.utils.GrxImageHelper;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.prefssupport.colorpicker.CircleColorDrawable;
import com.mods.grx.settings.prefssupport.colorpicker.ColorPickerView;
import com.mods.grx.settings.prefssupport.colorpicker.OnColorChangedListener;
import com.mods.grx.settings.prefssupport.colorpicker.Utils;
import com.mods.grx.settings.prefssupport.colorpicker.builder.ColorWheelRendererBuilder;
import com.mods.grx.settings.prefssupport.colorpicker.renderer.ColorWheelRenderer;
import com.mods.grx.settings.prefssupport.colorpicker.slider.AlphaSlider;
import com.mods.grx.settings.prefssupport.colorpicker.slider.LightnessSlider;

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

public class DlgFrGrxColorPicker extends DialogFragment implements DlgFrColorPalette.onColorAutoListener{

    private OnGrxColorPickerListener mCallBack;
    private String mTitle;
    private String mKey;
    private int mInitialColor;
    private boolean mWheelType;
    private boolean mAlphaSlider;
    private boolean mAuto;
    private Integer[] initialColor = new Integer[]{null, null, null, null, null};
    private ColorPickerView colorPickerView;
    private ImageView colorpreview;
    private String mHelperFragmentName;

    public DlgFrGrxColorPicker(){}

    public interface OnGrxColorPickerListener{
        void onGrxColorSet(int color);
    }

    public static DlgFrGrxColorPicker newInstance(OnGrxColorPickerListener callback, String helperfragment, String title, String key, int initialcolor, boolean wheeltype, boolean alphaslider, boolean auto){
        DlgFrGrxColorPicker ret = new DlgFrGrxColorPicker();
        ret.ini_picker(callback, helperfragment, title,key,initialcolor, wheeltype,alphaslider, auto);
        return ret;
    }

    private void ini_picker(OnGrxColorPickerListener callback, String helperfragment,  String title, String key, int initialcolor, boolean wheeltype, boolean alphaslider, boolean auto){

        mCallBack=callback;
        mHelperFragmentName=helperfragment;
        mTitle=title;
        mKey=key;
        mInitialColor=initialcolor;
        mWheelType=wheeltype;
        mAlphaSlider=alphaslider;
        mAuto=auto;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mInitialColor = colorPickerView.getSelectedColor();
        outState.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY, mHelperFragmentName);
        outState.putString("title",mTitle);
        outState.putString(Common.EXTRA_KEY, mKey);
        outState.putInt("value", mInitialColor);
        outState.putBoolean("type",mWheelType);
        outState.putBoolean("alpha", mAlphaSlider);
        outState.putBoolean("auto", mAuto);

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(mCallBack==null) {
            if(mHelperFragmentName.equals(Common.TAG_PREFSSCREEN_FRAGMENT)){
                GrxPreferenceScreen prefsScreen =(GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                mCallBack=(DlgFrGrxColorPicker.OnGrxColorPickerListener) prefsScreen.find_callback(mKey);
            }else mCallBack=(DlgFrGrxColorPicker.OnGrxColorPickerListener) getFragmentManager().findFragmentByTag(mHelperFragmentName);
        }
    }


    private View color_picker_view(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.grx_color_picker_layout,null);
        colorPickerView = (ColorPickerView) view.findViewById(R.id.colorpickerview);
        colorPickerView.setInitialColors(initialColor,getStartOffset(initialColor));

        ColorPickerView.WHEEL_TYPE wheelType;
        if(mWheelType) wheelType=ColorPickerView.WHEEL_TYPE.FLOWER;
        else wheelType= ColorPickerView.WHEEL_TYPE.CIRCLE;

        ColorWheelRenderer renderer = ColorWheelRendererBuilder.getRenderer(wheelType);
        colorPickerView.setRenderer(renderer);
        if(mAlphaSlider){
            AlphaSlider alphaSlider = (AlphaSlider) view.findViewById(R.id.alpha_slider);
            alphaSlider.setVisibility(View.VISIBLE);
            colorPickerView.setAlphaSlider(alphaSlider);
            alphaSlider.setColor(getStartColor(initialColor));
            TextView textView =(TextView) view.findViewById(R.id.v_txt_alfa);
            textView.setVisibility(View.VISIBLE);

        }

        LightnessSlider lightnessSlider = (LightnessSlider) view.findViewById(R.id.lightness_slider);
        colorPickerView.setLightnessSlider(lightnessSlider);
        lightnessSlider.setColor(getStartColor(initialColor));

        TypedArray a = getActivity().getTheme().obtainStyledAttributes( new int[] {R.attr.main_bg_color});
        int bgcolor = a.getColor(0,0);
        a.recycle();

        colorPickerView.setBackgroundDrawable(new CircleColorDrawable(bgcolor)) ;
        EditText colorEdit = (EditText) view.findViewById(R.id.edit_text);
        colorEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        colorEdit.setSingleLine();
        colorEdit.setVisibility(View.GONE);
        int maxLength = mAlphaSlider ? 9 : 7;
        colorEdit.setText(Utils.getHexString(getStartColor(initialColor), mAlphaSlider));
        colorEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        colorPickerView.setColorEdit(colorEdit);
        colorPickerView.setDensity(12);
        colorpreview = (ImageView) view.findViewById(R.id.color_preview);
        colorpreview.setImageDrawable(new CircleColorDrawable( initialColor[0] ));
       /* LinearLayout color_preview_container = (LinearLayout) view.findViewById(R.id.color_preview_container);
        colorPickerView.setColorPreview(color_preview_container, getStartOffset(initialColor));*/
        colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int selectedColor) {
                colorpreview.setImageDrawable(new CircleColorDrawable( selectedColor));
            }
        });


        return view;
    }

    private int getStartColor(Integer[] colors) {
        Integer startColor = getStartOffset(colors);
        return startColor == null ? Color.WHITE : colors[startColor];
    }

    private Integer getStartOffset(Integer[] colors) {
        Integer start = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == null) {
                return start;
            }
            start = (i + 1) / 2;
        }
        return start;
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        if(state!=null){
            mHelperFragmentName= state.getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
            mTitle=state.getString("title");
            mKey=state.getString(Common.EXTRA_KEY);
            mInitialColor= state.getInt("value");
            mWheelType=state.getBoolean("type");
            mAlphaSlider=state.getBoolean("alpha");
            mAuto=state.getBoolean("auto");
        }

        initialColor[0] = mInitialColor;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle)
               .setView(color_picker_view())
               .setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCallBack!=null) {
                            mCallBack.onGrxColorSet(colorPickerView.getSelectedColor());
                        }
                    }
                });

        if(mAuto) builder.setNeutralButton("Auto",null);

        final AlertDialog ad = builder.create();

        if(mAuto){

                ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button button = ad.getButton(DialogInterface.BUTTON_NEUTRAL);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if((getTag()!=null && !getTag().isEmpty())){
                                    GrxSettingsActivity activity = (GrxSettingsActivity) getActivity();
                                    Intent intent = new Intent(activity, GrxImagePicker.class);
                                    intent.putExtra(Common.TAG_DEST_FRAGMENT_NAME_EXTRA_KEY,getTag());
                                    intent = GrxImageHelper.intent_img_crop_circular(intent);
                                    activity.do_fragment_gallery_image_picker(intent);
                                }
                        }
                        });
                    }
                });
        }


        return ad;
    }

    public void setcolor(int color){
        this.colorPickerView.setColor(color,true);
        colorpreview.setImageDrawable(new CircleColorDrawable( color));
        mInitialColor=color;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        String sFile = data.getStringExtra(GrxImagePicker.S_DIR_IMG);

        if(sFile!=null) {
                DlgFrColorPalette dlg_palette = DlgFrColorPalette.newInstance(Common.TAG_DLGFRGRXCOLORPICKER, sFile);
                dlg_palette.set_listener(this);
                dlg_palette.show(getFragmentManager(),Common.TAG_DLGFRGRCOLORPALETTE);
           }

    }

    public void onColorAuto(int color, boolean auto){
        if(auto) setcolor(color);
    }

}
