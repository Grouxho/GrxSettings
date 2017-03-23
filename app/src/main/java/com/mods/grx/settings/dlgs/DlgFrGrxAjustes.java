package com.mods.grx.settings.dlgs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.AlertDialog;

import com.mods.grx.settings.GrxSettingsActivity;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static android.content.Context.MODE_PRIVATE;


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

public class DlgFrGrxAjustes extends DialogFragment{

    private OnDlgFrGrxAjustesListener mCallback;
    private int mTdialog;

    public interface OnDlgFrGrxAjustesListener {
        void onDlgFrGrxAjustesSel(int tdialog, int opt);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (GrxSettingsActivity) getActivity();
    }




    public static DlgFrGrxAjustes newInstance(int t_dialog){

        DlgFrGrxAjustes ret = new DlgFrGrxAjustes();
        Bundle save = new Bundle();
        save.putInt(Common.S_DLG_T_KEY, t_dialog);
        ret.setArguments(save);
        ret.ini_dlg_fr(t_dialog);
        return ret;
    }

    private void ini_dlg_fr(int tdialog){
        mTdialog=tdialog;
    }


    private int sp_val(String key, int defv){
        int ret = defv;

        try{
            SharedPreferences sp = getActivity().createPackageContext(getActivity().getPackageName(),CONTEXT_IGNORE_SECURITY).getSharedPreferences(getActivity().getPackageName()+"_preferences",MODE_PRIVATE);
            ret=sp.getInt(key, defv);

        }catch (PackageManager.NameNotFoundException e){

        }

        return ret;
    }


    private Dialog dlg_fab_pos(){

        final int fab_pos = sp_val(Common.S_APPOPT_FAB_POS, 0);

        AlertDialog adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.gs_tit_dlg_pos_boton)
                .setSingleChoiceItems(R.array.gsa_posicion_boton, fab_pos,null)
                .setNegativeButton(R.string.gs_no,null)
                .setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int sel = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if(mCallback!=null && sel!=fab_pos ){
                            mCallback.onDlgFrGrxAjustesSel(mTdialog,sel);
                        }
                    }
                }).create();

        return adb;
    }


    private Dialog dlg_exit_confirm(){
        AlertDialog adb = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.gs_titulo_salir)
            .setMessage(R.string.gs_mensaje_salir)
                .setNegativeButton(R.string.gs_no,null)
                .setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCallback!=null ){
                            mCallback.onDlgFrGrxAjustesSel(mTdialog,1);
                        }
                    }
        }).create();

        return adb;
    }

    private Dialog dlg_div_height(){

        final int div_height = sp_val( Common.S_APPOPT_DIV_HEIGHT, getResources().getInteger(R.integer.def_divider));

        AlertDialog adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.gs_tit_dlg_ancho_divider)
                .setSingleChoiceItems(R.array.gsa_ancho_divider, div_height,null)
                .setNegativeButton(R.string.gs_no,null)
                .setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int sel = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if(mCallback!=null && sel!=div_height ){
                            mCallback.onDlgFrGrxAjustesSel(mTdialog,sel);
                        }
                    }
                }).create();

        return adb;
    }


    private Dialog dlg_choose_panel_header_bg(){

        AlertDialog adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.gs_nav_header_bg_title)
                .setSingleChoiceItems(R.array.gsa_panel_header_options,1,null)
                .setNegativeButton(R.string.gs_no,null)
                .setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int sel = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if(mCallback!=null){
                            mCallback.onDlgFrGrxAjustesSel(mTdialog,sel);
                        }
                    }
                }).create();

        return adb;
    }


    private Dialog dlg_set_theme(){

        final int curr_theme = sp_val(Common.S_APPOPT_USER_SELECTED_THEME, getResources().getInteger(R.integer.def_theme));

        AlertDialog adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.gs_select_theme)
                .setSingleChoiceItems(R.array.gsa_theme_list, curr_theme,null)
                .setNegativeButton(R.string.gs_no,null)
                .setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int sel = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if(mCallback!=null && sel!=curr_theme ){
                            mCallback.onDlgFrGrxAjustesSel(mTdialog,sel);
                        }
                    }
                }).create();


        return adb;

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mTdialog=getArguments().getInt(Common.S_DLG_T_KEY);
        switch (mTdialog){
            case Common.INT_ID_APPDLG_FAV_POS: return dlg_fab_pos();
            case Common.INT_ID_APPDLG_DIV_HEIGHT: return dlg_div_height();
            case Common.INT_ID_APPDLG_EXIT_CONFIRM: return dlg_exit_confirm();
            case Common.INT_ID_APPDLG_SET_THEME: return dlg_set_theme();
            case Common.INT_ID_APPDLG_SET_BG_PANEL_HEADER: return dlg_choose_panel_header_bg();
        }
        return null;
    }


}
