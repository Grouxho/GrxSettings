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
package com.mods.grx.settings.dlgs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mods.grx.settings.R;


import java.io.File;
import java.io.FileInputStream;


public class DlgFrColorPalette extends DialogFragment implements View.OnClickListener {


    ImageView img_montaje;
    ImageView img_muestra_color;
    View v_contenedor;


    ImageView v_c;
    ImageView v_vb;
    ImageView v_m;
    ImageView v_lvb;
    ImageView v_dvb;
    ImageView v_lm;
    ImageView v_dm;
    int color_centra;
    int c_vb;
    int c_m;
    int c_lvb;
    int c_dvb;
    int c_dm;
    int c_lm;
    int color_actual;

    private onColorAutoListener ColorAutoListener;

    TextView v_texto_muestra;

    String mCallbackFragmentName;
    String mImgFile;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }


    public DlgFrColorPalette() {
    }

    public static DlgFrColorPalette newInstance(String callback_fragment, String img_file_name) {
        DlgFrColorPalette ret = new DlgFrColorPalette();
        ret.ini_palette(callback_fragment, img_file_name);

        return ret;

    }

    private void ini_palette(String callback_fragment, String img_file_name) {

        mCallbackFragmentName = callback_fragment;
        mImgFile = img_file_name;

    }

    private View palette_view() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.grx_palette_detected_color, null);
        img_montaje = (ImageView) view.findViewById(R.id.montaje_color);
        img_muestra_color = (ImageView) view.findViewById(R.id.muestra_color);
        v_contenedor = view.findViewById(R.id.contenedor_montaje);

        v_vb = (ImageView) view.findViewById(R.id.paleta_vibrant);
        v_vb.setOnClickListener(this);
        v_c = (ImageView) view.findViewById(R.id.paleta_calculado);
        v_c.setOnClickListener(this);
        v_m = (ImageView) view.findViewById(R.id.paleta_muted);
        v_m.setOnClickListener(this);
        v_lvb = (ImageView) view.findViewById(R.id.paleta_light_vibrant);
        v_lvb.setOnClickListener(this);
        v_dvb = (ImageView) view.findViewById(R.id.paleta_dark_vibrant);
        v_dvb.setOnClickListener(this);
        v_lm = (ImageView) view.findViewById(R.id.paleta_light_muted);
        v_lm.setOnClickListener(this);
        v_dm = (ImageView) view.findViewById(R.id.paleta_dark_muted);

        v_dm.setOnClickListener(this);
        v_texto_muestra = (TextView) view.findViewById(R.id.texto_muestra_color);




        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int[] colors = new int[]{color_actual, color_centra, c_m, c_dm, c_lm, c_vb, c_lvb, c_dvb};
        outState.putIntArray("colors", colors);
        outState.putString("target_fragment",mCallbackFragmentName);
        outState.putString("file_name",mImgFile);
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        if (state != null) {
            int[] colors = new int[8];
            colors = state.getIntArray("colors");
            color_actual = colors[0];
            color_centra = colors[1];
            c_m = colors[2];
            c_dm = colors[3];
            c_lm = colors[4];
            c_vb = colors[5];
            c_lvb = colors[6];
            c_dvb = colors[7];
            mCallbackFragmentName=state.getString("target_fragment");
            mImgFile=state.getString("file_name");

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gs_auto_color);
        builder.setView(palette_view());
        builder.setNegativeButton(R.string.gs_no, null);
        builder.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (ColorAutoListener == null) {
                    ColorAutoListener = (onColorAutoListener) getFragmentManager().findFragmentByTag(mCallbackFragmentName);
                }
                if (ColorAutoListener != null) ColorAutoListener.onColorAuto(color_actual, true);
                else dismiss();
            }
        });

        if(state==null) generate_palette();
        else draw_colors();

        AlertDialog d = builder.create();

        return d;
    }


    private Bitmap read_bmp() {
        Bitmap bitmap = null;
        if (mImgFile != null) {
            File f = new File(mImgFile);
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            } catch (Exception e) {
                bitmap = null;
            }

        }


        return bitmap;
    }




    private Drawable bmp_to_drawable() {
        BitmapDrawable drawable = null;
        if (mImgFile != null) {
            drawable = new BitmapDrawable(getActivity().getResources(), mImgFile);
        }

        return drawable;

    }

    public interface onColorAutoListener {
        public void onColorAuto(int color, boolean auto);
    }


    @Override
    public void onClick(View v) {

        String texto = "";
        switch (v.getId()) {

            case R.id.paleta_calculado:
                color_actual = color_centra;
                texto = "Central";
                break;
            case R.id.paleta_vibrant:
                color_actual = c_vb;
                texto = "Vibrant";
                break;
            case R.id.paleta_light_vibrant:
                color_actual = c_lvb;
                texto = "Light Vibrant";
                break;
            case R.id.paleta_dark_vibrant:
                color_actual = c_dvb;
                texto = "Dark Vibrant";
                break;

            case R.id.paleta_muted:
                color_actual = c_m;
                texto = "Muted";
                break;

            case R.id.paleta_light_muted:
                color_actual = c_lm;
                texto = "Light Muted";
                break;

            case R.id.paleta_dark_muted:
                color_actual = c_dm;
                texto = "Dark Muted";
                break;
        }
        img_muestra_color.setColorFilter(color_actual, PorterDuff.Mode.MULTIPLY);
        v_texto_muestra.setText(texto);
        v_contenedor.setBackgroundColor(color_actual);
    }



    private void draw_colors(){

        v_m.setColorFilter(c_m, PorterDuff.Mode.MULTIPLY);
        v_dm.setColorFilter(c_dm, PorterDuff.Mode.MULTIPLY);
        v_lm.setColorFilter(c_lm, PorterDuff.Mode.MULTIPLY);
        v_vb.setColorFilter(c_vb, PorterDuff.Mode.MULTIPLY);
        v_lvb.setColorFilter(c_lvb, PorterDuff.Mode.MULTIPLY);
        v_dvb.setColorFilter(c_dvb, PorterDuff.Mode.MULTIPLY);
        img_muestra_color.setColorFilter(color_centra,PorterDuff.Mode.MULTIPLY);
        v_c.setColorFilter(color_centra,PorterDuff.Mode.MULTIPLY);
        v_contenedor.setBackgroundColor(color_centra);
        img_montaje.setImageDrawable(bmp_to_drawable());
    }

    public void generate_palette() {

        Bitmap bm = read_bmp();

        if(bm==null) {
            dismiss();
            return;
        }

        final int color_central = bm.getPixel(bm.getWidth() / 2, bm.getHeight() / 2);
        color_centra = color_central;
        color_actual = color_central;
        Palette.from(bm).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {

                c_m = palette.getMutedColor(color_central);
                c_dm = palette.getDarkMutedColor(color_central);
                c_lm = palette.getLightMutedColor(color_central);
                c_vb = palette.getVibrantColor(color_central);
                c_lvb = palette.getLightVibrantColor(color_central);
                c_dvb = palette.getDarkVibrantColor(color_central);

                draw_colors();

            }
        });

    }




    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void set_listener(onColorAutoListener listener){
        ColorAutoListener = listener;
    }



}
