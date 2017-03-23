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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.sldv.Menu;
import com.mods.grx.settings.sldv.MenuItem;
import com.mods.grx.settings.sldv.SlideAndDragListView;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.utils.Utils;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class DlgFrGrxSortList extends DialogFragment implements SlideAndDragListView.OnDragListener, SlideAndDragListView.OnListItemLongClickListener {

    private OnGrxOrdenarListaListener mCallback;
    private String mTitle;
    private String mValue;
    private String mSeparator;
    private int mIdOptionsArr;
    private int mIdValuesArr;
    private int mIdIconsArray;
    private boolean mShowSortIcon;


    private ArrayList<GrxInfoItem> mItemList;

    private SlideAndDragListView mDragList;


    private float tam_txt;
    private int minheight;
    private int num_filas_max;


    private String mHelperFragment;
    private String mKey;

    public DlgFrGrxSortList(){}


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

    public interface OnGrxOrdenarListaListener{
        void onGrxOrdenarLista(String mValue);
    }

    public static DlgFrGrxSortList newInstance(OnGrxOrdenarListaListener callback, String HelperFragment, String key, String title, String value, String separator,
                                               int id_array_options, int id_array_values, int id_array_icons, boolean show_icon_sort ){
        DlgFrGrxSortList ret = new DlgFrGrxSortList();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,HelperFragment);
        bundle.putString("key",key);
        bundle.putString("tit",title);
        bundle.putString("val",value);
        bundle.putString("sep",separator);
        bundle.putInt("opt_arr_id",id_array_options);
        bundle.putInt("val_array_id",id_array_values);
        bundle.putInt("icons_array_id", id_array_icons );
        bundle.putBoolean("show_sort_icon",show_icon_sort);
        ret.setArguments(bundle);
        ret.save_callback(callback);
        return ret;

    }


    private void save_callback(OnGrxOrdenarListaListener callback){
        mCallback=callback;
    }


    private View sort_list_view(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.pref_sortlist_dlg_lay, null);

        mDragList = (SlideAndDragListView) view.findViewById(R.id.lv_edit);

        TextView vTxtAyuda = (TextView) view.findViewById(R.id.ayuda_ordenar);
        vTxtAyuda.setText(R.string.gs_ayuda_pref_ordenar);

        tam_txt = getResources().getDimension(R.dimen.textsize_listas_opciones);
        minheight = getResources().getDimensionPixelSize(R.dimen.view_minheight_listas_opciones);
        num_filas_max = getResources().getInteger(R.integer.max_items_vistos_listados_drag);

        mDragList.setVerticalScrollBarEnabled(true);
        mDragList.setDividerHeight(Common.cDividerHeight);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mValue=do_return_value();
        outState.putString("val", mValue);
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {

        mHelperFragment=getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        mKey=getArguments().getString("key");
        mTitle=getArguments().getString("tit");
        mValue=getArguments().getString("val");
        mSeparator = getArguments().getString("sep");
        mIdOptionsArr = getArguments().getInt("opt_arr_id");
        mIdValuesArr = getArguments().getInt("val_array_id");
        mIdIconsArray = getArguments().getInt("icons_array_id");
        mShowSortIcon = getArguments().getBoolean("show_sort_icon");

        if (state != null) {
            mValue=state.getString("val");
            if(mCallback==null){
                if(mHelperFragment.equals(Common.TAG_PREFSSCREEN_FRAGMENT)){
                    GrxPreferenceScreen prefsScreen =(GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                    mCallback=(DlgFrGrxSortList.OnGrxOrdenarListaListener) prefsScreen.find_callback(mKey);
                }else mCallback=(DlgFrGrxSortList.OnGrxOrdenarListaListener) getFragmentManager().findFragmentByTag(mHelperFragment);
            }
        }

        if(mItemList!=null) mItemList.clear();
        else mItemList = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(sort_list_view());
        builder.setNegativeButton(R.string.gs_no,null);
        builder.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCallback != null) mCallback.onGrxOrdenarLista(do_return_value());
                mItemList.clear();
                dismiss();
            }
        });

        ini_items_list();
        ini_drag_list();
        return builder.create();
    }


    private void ini_items_list(){
        TypedArray icons_array=null;
        String vals_array[] = getResources().getStringArray(mIdValuesArr);
        String opt_array[] = getResources().getStringArray(mIdOptionsArr);
        if(mIdIconsArray!=0){
            icons_array = getResources().obtainTypedArray(mIdIconsArray);
        }

        String values[] = mValue.split(Pattern.quote(mSeparator));
        mItemList.clear();
        for(int i=0;i<values.length;i++){
            int pos = Utils.find_pos_in_string_array(vals_array,values[i]);
            Drawable drawable=null;
            if(icons_array!=null) drawable = icons_array.getDrawable(pos);
            mItemList.add(
                    new GrxInfoItem(opt_array[pos], vals_array[pos],drawable)
            );
        }

        if(icons_array!=null) icons_array.recycle();
    }



    @Override
    public void onDragViewStart(int position) {
    }

    @Override
    public void onDragViewMoving(int position) {
    }

    @Override
    public void onDragViewDown(int position) {
    }

    @Override
    public void onListItemLongClick(View view, int position) {
    }

    private void ini_drag_list(){

        Menu menu = new Menu(false,false);

        menu.addItem(new MenuItem.Builder().setWidth( 0 )
                .setBackground(getResources().getDrawable(R.drawable.ic_delete))
                .setText(" ")
                .setTextColor(Color.GRAY)
                .setTextSize(1)
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .build());

        mDragList.setMenu(menu);
        mDragList.setDividerHeight(Common.cDividerHeight);
        mDragList.setAdapter(mAdapter);
        mDragList.setOnDragListener(this,mItemList);
        mDragList.setOnListItemLongClickListener(this);
        mAdapter.notifyDataSetChanged();
    }





    private String do_return_value(){
        String tmp="";
        for(int i=0;i<mItemList.size();i++){
            tmp+=mItemList.get(i).getValor();
            tmp+=mSeparator;
        }
        return tmp;
    }


    private class GrxInfoItem {

        private String Texto;
        private String Valor;
        private Drawable Icono;

        public GrxInfoItem(String texto, String valor, Drawable icono){
            Texto=texto;
            Valor=valor;
            Icono=icono;
        }
        public String getTexto(){
            return Texto;
        }

        public String getValor(){
            return Valor;
        }

        public Drawable getIcono(){
            return Icono;
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_sortlist_item_lay, null);
                cvh.vCtxt = (TextView) convertView.findViewById(R.id.texto);
                cvh.vIcono=(ImageView)   convertView.findViewById(R.id.icono);
                cvh.vIcono2=(ImageView)   convertView.findViewById(R.id.icono2);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }

            GrxInfoItem grxInfoItem = (GrxInfoItem) this.getItem(position);
            cvh.vCtxt.setText(grxInfoItem.getTexto());
            cvh.vCtxt.setTextSize(TypedValue.COMPLEX_UNIT_PX,tam_txt);
            Drawable ic = grxInfoItem.getIcono();
            if(ic!=null){
                cvh.vIcono2.setImageDrawable(ic);
                if(mShowSortIcon) cvh.vIcono.setImageResource(R.drawable.ic_grabber);
                else cvh.vIcono.setImageDrawable(ic);
            }else{
                if(mShowSortIcon){
                    cvh.vIcono.setImageResource(R.drawable.ic_grabber);
                    cvh.vIcono2.setImageResource(R.drawable.ic_grabber);

                }else{
                    cvh.vIcono.setVisibility(View.GONE);
                    cvh.vIcono2.setVisibility(View.GONE);
                }
            }

            convertView.setMinimumHeight(minheight);
            return convertView;
        }

        class CustomViewHolder {
            public TextView vCtxt;
            public ImageView vIcono;
            public ImageView vIcono2;
        }
    };

}
