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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;


import java.util.ArrayList;
import java.util.regex.Pattern;


public class DlgFrMultiSelect extends DialogFragment  {  //single (ListPreference) and multiselectList preferences ..

    private TextView vSelectedTxt;
    private ListView vList;

    private ArrayList<GrxInfoItem> mItemList;

    private GrxMultiSelectListener mCallBack;

    private int mIdOptionsArr;
    private int mIdValuesArr;
    private int mIdIconsArray;
    private String mHelperFragment;
    private String mKey;
    private String mTitle;
    private String mValue;
    private String mOriValue;
    private String mSeparator;
    private int mMaxNumOfAccesses;

    private int mNumChecked;
    private String mSummAuxString;


    public DlgFrMultiSelect(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public interface GrxMultiSelectListener{
        void GrxSetMultiSelect(String value);
    }


    private void save_callback(DlgFrMultiSelect.GrxMultiSelectListener callback){
        mCallBack =callback;
    }

    public static DlgFrMultiSelect newInstance(DlgFrMultiSelect.GrxMultiSelectListener callback,String HelperFragment, String key, String title, String value,
                                                  int id_array_options, int id_array_values, int id_array_icons, String separtor, int maxitems
    ){


        DlgFrMultiSelect ret = new DlgFrMultiSelect();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,HelperFragment);
        bundle.putString("key",key);
        bundle.putString("tit",title);
        bundle.putString("val",value);
        bundle.putInt("opt_arr_id",id_array_options);
        bundle.putInt("val_array_id",id_array_values);
        bundle.putInt("icons_array_id", id_array_icons );
        bundle.putString("separator", separtor);
        bundle.putInt("max_items", maxitems);
        ret.setArguments(bundle);
        ret.save_callback(callback);
        return ret;

    }


    /************  DIALOG, VIEW, INSTANCE ************************/

    private void check_callback(){
        if(mCallBack==null) {
            if (mHelperFragment.equals(Common.TAG_PREFSSCREEN_FRAGMENT)) {
                GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                if (prefsScreen != null)
                    mCallBack = (DlgFrMultiSelect.GrxMultiSelectListener) prefsScreen.find_callback(mKey);
            }else mCallBack=(DlgFrMultiSelect.GrxMultiSelectListener) getFragmentManager().findFragmentByTag(mHelperFragment);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle state) {

        mHelperFragment=getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        mKey=getArguments().getString("key");
        mTitle=getArguments().getString("tit");
        mOriValue=getArguments().getString("val");
        mValue=mOriValue;
        mIdOptionsArr= getArguments().getInt("opt_arr_id");
        mIdValuesArr=getArguments().getInt("val_array_id");
        mIdIconsArray=getArguments().getInt("icons_array_id");
        mSeparator=getArguments().getString("separator");
        mMaxNumOfAccesses = getArguments().getInt("max_items");

        if (state != null) {
            mValue =  state.getString("curr_val");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(multiselect_view());

        if(mMaxNumOfAccesses!=1){ //is multivalue
            builder.setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });
            builder.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mValue=get_return_value();
                    set_result_and_exit();
                }
            });
        }
        mItemList = new ArrayList<>();
        mNumChecked=0;
        ini_items_list();
        if(mMaxNumOfAccesses!=0) mSummAuxString = "( " + String.valueOf(mMaxNumOfAccesses)+" max.)";
        else mSummAuxString=" ";
        update_summary();
        set_adapter();

        AlertDialog ad = builder.create();
        return ad;

    }

    private View multiselect_view(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.pref_multiselect_dlg_lay, null);
        vList = (ListView) view.findViewById(R.id.lista_opciones);
        if(mMaxNumOfAccesses==0){
            LinearLayout vCheck = (LinearLayout) view.findViewById(R.id.boton_check);
            vCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check_all(true);
                }
            });
            LinearLayout vUncheck = (LinearLayout) view.findViewById(R.id.boton_uncheck);
            vUncheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check_all(false);
                }
            });
        }else {
            LinearLayout vBotones = (LinearLayout) view.findViewById(R.id.botones);
            vBotones.setVisibility(View.GONE);
            LinearLayout vSeparador = (LinearLayout) view.findViewById(R.id.separador);
            vSeparador.setVisibility(View.GONE);
        }

        vSelectedTxt = (TextView) view.findViewById(R.id.txt_apps_seleccionadas);
        if(mMaxNumOfAccesses==1) vSelectedTxt.setVisibility(View.GONE);
        vList.setDividerHeight(Common.cDividerHeight);
        return view;

    }



    private void ini_items_list(){

        TypedArray icons_array=null;
        String vals_array[] = getResources().getStringArray(mIdValuesArr);
        String opt_array[] = getResources().getStringArray(mIdOptionsArr);
        if(mIdIconsArray!=0){
            icons_array = getResources().obtainTypedArray(mIdIconsArray);
        }
        String[] selected =null;
        if(mValue!=null && !mValue.isEmpty()) selected=mValue.split(Pattern.quote(mSeparator));

        for(int i=0;i<vals_array.length;i++){
            Drawable drwtmp = null;
            if(icons_array!=null) {
                drwtmp = icons_array.getDrawable(i);
            }

            int last_pos_for_single_choice=0;

            boolean isChecked = false;
            if(selected!=null){
                for(int ii=0;ii<selected.length;ii++){
                    if(selected[ii].equals(vals_array[i])){
                        isChecked=true;
                        mNumChecked++;
                        break;
                    }
                }
            }
            mItemList.add(new GrxInfoItem(opt_array[i], vals_array[i],drwtmp,isChecked));
        }
        if(icons_array!=null) icons_array.recycle();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("curr_val", get_return_value());
    }


    private int get_last_visible_pos_for_single_choice_mode(){
        int pos=0;
        for(int i=0;i<mItemList.size();i++) {
            if(mItemList.get(i).isChecked()) {
                pos=i;
                break;
            }
        }
        return pos;
    }

    private void set_adapter(){
        vList.setAdapter(mAdapter);
        if(mMaxNumOfAccesses==1) vList.setSelection(get_last_visible_pos_for_single_choice_mode());


        vList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(mMaxNumOfAccesses==1) {
                    mValue=mItemList.get(position).getValue();
                    set_result_and_exit();
                }else{
                    boolean tmp;
                    tmp=mItemList.get(position).isChecked();

                    if(tmp){
                        tmp=false;
                        mNumChecked--;
                        mItemList.get(position).setChecked(tmp);
                        mAdapter.notifyDataSetChanged();
                        update_summary();
                    }
                    else{
                        tmp = true;
                        if(mMaxNumOfAccesses==0 ){
                            mNumChecked++;
                            mItemList.get(position).setChecked(tmp);
                            mAdapter.notifyDataSetChanged();
                            update_summary();
                        }else{
                            if(mNumChecked<mMaxNumOfAccesses) {
                                mNumChecked++;
                                mItemList.get(position).setChecked(tmp);
                                mAdapter.notifyDataSetChanged();
                                update_summary();
                            }else Toast.makeText(getActivity(),R.string.gs_aviso_num_max_opciones,Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }


    private void set_result_and_exit(){
        check_callback();
        if (mCallBack == null) this.dismiss();
        mCallBack.GrxSetMultiSelect(mValue);
        mItemList.clear();
        this.dismiss();
    }


    public String get_return_value(){
        String resultado="";
        for(int i=0;i<mItemList.size();i++){
            if(mItemList.get(i).isChecked()){
                resultado+=mItemList.get(i).getValue();
                resultado+=mSeparator;
            }
        }
        return resultado;
    }


    private void check_all(boolean check){

        if(check) mNumChecked=mItemList.size();
        else mNumChecked=0;
        for(int i=0;i<mItemList.size();i++){
            mItemList.get(i).setChecked(check);
        }
        update_summary();
        mAdapter.notifyDataSetChanged();
    }



    private void update_summary(){
        vSelectedTxt.setText(getString(R.string.gs_multi_select_seleccionadas,mNumChecked)+" "+mSummAuxString);
    }


    /******************* Item Info and Adapter ***********/

    private class GrxInfoItem {

        private String mLabel;
        private String mValue;
        private Drawable mIcon;
        private boolean mIsChecked;

        public GrxInfoItem(String label, String value, Drawable icon, boolean checked){

            mLabel=label;
            mValue=value;
            mIcon=icon;
            mIsChecked=checked;

        }

        public String getLabel(){
            return mLabel;
        }

        public String getValue(){
            return mValue;
        }

        public boolean isChecked(){
            return mIsChecked;
        }

        public Drawable getIcon(){
            return mIcon;
        }

        public void setChecked(boolean checked){
            mIsChecked=checked;
        }

    }



    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomViewHolder cvh;
            if (convertView == null) {
                cvh = new CustomViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_list_multiselect_item, null);
                cvh.vIcon = (ImageView) convertView.findViewById(R.id.icono);
                cvh.vLabel = (TextView) convertView.findViewById(R.id.texto);
                cvh.vRadioButton = (RadioButton) convertView.findViewById(R.id.radiobutton);
                cvh.vCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }

            GrxInfoItem grxInfoItem = (GrxInfoItem) this.getItem(position);
            if(mMaxNumOfAccesses==1) {
                cvh.vRadioButton.setVisibility(View.VISIBLE);
                cvh.vRadioButton.setChecked(grxInfoItem.isChecked());
                cvh.vRadioButton.setClickable(false);
            }else {
                cvh.vCheckBox.setVisibility(View.VISIBLE);
                cvh.vCheckBox.setChecked(grxInfoItem.isChecked());
                cvh.vCheckBox.setClickable(false);
            }
            cvh.vLabel.setText(grxInfoItem.getLabel());
            if(mIdIconsArray==0) cvh.vIcon.setVisibility(View.GONE);
            else cvh.vIcon.setImageDrawable(grxInfoItem.getIcon());


           return convertView;
        }

        class CustomViewHolder {
            public ImageView vIcon;
            public TextView vLabel;
            public RadioButton vRadioButton;
            public CheckBox vCheckBox;
        }
    };


}
