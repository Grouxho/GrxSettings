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

import com.mods.grx.settings.Common;
import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mods.grx.settings.prefssupport.info.GrxAccessInfo;
import com.mods.grx.settings.sldv.*;
import com.mods.grx.settings.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class DlgFrGrxMultiAccess extends DialogFragment implements SlideAndDragListView.OnListItemLongClickListener,
        SlideAndDragListView.OnDragListener, SlideAndDragListView.OnSlideListener,
        SlideAndDragListView.OnListItemClickListener, SlideAndDragListView.OnMenuItemClickListener,
        SlideAndDragListView.OnItemDeleteListener, DlgFrGrxAccess.GrxAccesListener {


    private ArrayList<GrxAccessInfo> mItemsList;
    private SlideAndDragListView ListDragView;

    private LinearLayout vDeleteButton;

    private LinearLayout vOpenAccessDialogButton;



    private int mIdOptionsArr;
    private int mIdValuesArr;
    private int mIdIconsArray;
    private boolean mShowShortCuts;
    private boolean mShowApplications;
    private boolean mShowActivities;
    private String mHelperFragment;
    private String mKey;
    private String mTitle;
    private String mValue;
    private DlgFrGrxMultiAccess.GrxMultiAccessListener mCallBack;
    private boolean mSaveCustomActionsIcons;
    private String mOriValue;
    private String mSeparator;
    private int mMaxNumOfAccesses;
    private LinearLayout vHelpButton;

    private boolean mDeleteTmpFileOnDismiss=true;
    private int mIdItemClicked = -1;


    TextView vTxtSelectedItems;


    public interface GrxMultiAccessListener{
        void GrxSetMultiAccess(String value);
    }

    public DlgFrGrxMultiAccess(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        mDeleteTmpFileOnDismiss = true;
        super.onResume();
    }


    private void save_callback(DlgFrGrxMultiAccess.GrxMultiAccessListener callback){
        mCallBack =callback;
    }

    public static DlgFrGrxMultiAccess newInstance(DlgFrGrxMultiAccess.GrxMultiAccessListener callback,String HelperFragment, String key, String title, String value,
                                                  boolean show_shortcuts, boolean show_apps, boolean show_activities,
                                                  int id_array_options, int id_array_values, int id_array_icons, boolean save_icons, String separtor, int maxitems
    ){


        DlgFrGrxMultiAccess ret = new DlgFrGrxMultiAccess();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,HelperFragment);
        bundle.putString("key",key);
        bundle.putString("tit",title);
        bundle.putString("val",value);
        bundle.putBoolean("show_shortcuts",show_shortcuts);
        bundle.putBoolean("show_aps",show_apps);
        bundle.putBoolean("show_activities",show_activities);
        bundle.putInt("opt_arr_id",id_array_options);
        bundle.putInt("val_array_id",id_array_values);
        bundle.putInt("icons_array_id", id_array_icons );
        bundle.putBoolean("save_icons",save_icons);
        bundle.putString("separator", separtor);
        bundle.putInt("max_items", maxitems);
        ret.setArguments(bundle);
        ret.save_callback(callback);
        return ret;

    }


    /************  DIALOG, VIEW, INSTANCE ************************/

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        DlgFrGrxAccess dlgFrGrxAccess = (DlgFrGrxAccess) getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRACCESS);
        if(dlgFrGrxAccess!=null){
            getFragmentManager().beginTransaction().show(dlgFrGrxAccess);
        }
    }
    private void check_callback(){
        if(mCallBack==null) {
            if (mHelperFragment.equals(Common.TAG_PREFSSCREEN_FRAGMENT)) {
                GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                if (prefsScreen != null)
                    mCallBack = (DlgFrGrxMultiAccess.GrxMultiAccessListener) prefsScreen.find_callback(mKey);
            }else mCallBack=(DlgFrGrxMultiAccess.GrxMultiAccessListener) getFragmentManager().findFragmentByTag(mHelperFragment);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        mDeleteTmpFileOnDismiss=false;
        super.onSaveInstanceState(outState);
        mValue=get_result_from_item_list();
        outState.putString("curr_val", mValue);
        outState.putInt("clicked_id",mIdItemClicked);
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        mHelperFragment=getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        mKey=getArguments().getString("key");
        mTitle=getArguments().getString("tit");
        mOriValue=getArguments().getString("val");
        mValue=mOriValue;
        mShowShortCuts = getArguments().getBoolean("show_shortcuts");
        mShowApplications=getArguments().getBoolean("show_aps");
        mShowActivities=getArguments().getBoolean("show_activities");
        mIdOptionsArr= getArguments().getInt("opt_arr_id");
        mIdValuesArr=getArguments().getInt("val_array_id");
        mIdIconsArray=getArguments().getInt("icons_array_id");
        mSaveCustomActionsIcons=getArguments().getBoolean("save_icons");
        mSeparator=getArguments().getString("separator");
        mMaxNumOfAccesses = getArguments().getInt("max_items");


        if (state != null) {
            mValue =  state.getString("curr_val");
            mIdItemClicked = state.getInt("clicked_id");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(multi_access_view());
        builder.setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeleteTmpFileOnDismiss=true;
                dismiss();
            }
        });
        builder.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                set_result_and_do_callback();
                mDeleteTmpFileOnDismiss=true;
            }
        });

        mItemsList = new ArrayList<>();
        ini_accesses_list(mValue);
        show_summary();
        ini_drag_and_drop_list();
        check_add_items_button_state();
        AlertDialog ad = builder.create();

        return ad;

    }

    private void show_help(){

        AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
        ad.setTitle(getString(R.string.gs_ay_ayuda));
        ad.setMessage(getString(R.string.gs_select_sort_help));
        ad.show();
    }


    private void check_add_items_button_state(){
        if(mMaxNumOfAccesses==0) {
            vOpenAccessDialogButton.setClickable(true);
            vOpenAccessDialogButton.setAlpha((float) 1.0);
            return;
        }

        if(mItemsList!=null){
            if(mItemsList.size()>=mMaxNumOfAccesses) {
                vOpenAccessDialogButton.setClickable(false);
                vOpenAccessDialogButton.setAlpha((float) 0.3);
            }else{
                vOpenAccessDialogButton.setClickable(true);
                vOpenAccessDialogButton.setAlpha((float) 1.0);
            }
        }
    }

    private  View multi_access_view(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.pref_multiaccess_dlg_lay, null);
        vHelpButton = (LinearLayout) view.findViewById(R.id.help_button);
        vHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_help();
            }
        });
        ListDragView = (SlideAndDragListView) view.findViewById(R.id.lv_edit);

        vTxtSelectedItems = (TextView) view.findViewById(R.id.txt_apps_seleccionadas);
        vOpenAccessDialogButton = (LinearLayout) view.findViewById(R.id.boton_accesos);

        vOpenAccessDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_select_access_dialog();
            }
        });

        vDeleteButton = (LinearLayout) view.findViewById(R.id.boton_borrar);
        vDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_all_items();
            }
        });

        ListDragView.setDividerHeight(Common.cDividerHeight);

        return view;
    }



    private void open_select_access_dialog(){
        if (getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRACCESS) != null) return;
        mIdItemClicked = -1;
        DlgFrGrxAccess dlg = DlgFrGrxAccess.newInstance(this, Common.TAG_DLGFRGRMULTIACCESS, mKey,getString(R.string.add_new_item),"",
                mShowShortCuts,mShowApplications,mShowActivities,
                mIdOptionsArr, mIdValuesArr, mIdIconsArray, mSaveCustomActionsIcons,
                true);
        dlg.show(getFragmentManager(),Common.TAG_DLGFRGRACCESS);
    }



    private void show_summary(){

        if(mMaxNumOfAccesses!=0) vTxtSelectedItems.setText( getString(R.string.gs_num_apps_seleccionadas,mItemsList.size())+ "  "+ getString(R.string.gs_max_apps,mMaxNumOfAccesses));
        else vTxtSelectedItems.setText( getString(R.string.gs_num_apps_seleccionadas,mItemsList.size()));
    }

    /************************* LIST ***************************/


    private String get_result_from_item_list(){
        String result ="";
        for(int ind = 0; ind< mItemsList.size();ind ++){
            result+=mItemsList.get(ind).get_uri();
            result+=mSeparator;
        }

        return result;
    }


    private void ini_accesses_list(String value){
        mItemsList.clear();
        if(mValue==null || mValue.isEmpty()) {
            return;
        }
        String[] array = mValue.split(Pattern.quote(mSeparator));
        if(array!=null) {
            for (int i= 0; i<array.length; i++){
                mItemsList.add(new GrxAccessInfo(array[i],this.getActivity()));
            }
        }
    }

    private void ini_drag_and_drop_list(){


        TypedArray a = getActivity().getTheme().obtainStyledAttributes( new int[] {R.attr.complemnt_accent_color});
        int bgcolor = a.getColor(0,0);
        a.recycle();

        Menu menu = new Menu(true,false);
        menu.addItem(new MenuItem.Builder().setWidth( (int) getResources().getDimension(R.dimen.slv_item_bg_btn_width)*2   )
                .setBackground(new ColorDrawable(bgcolor))
                .setText(getString(R.string.gs_remove))
                .setTextColor(Utils.get_contrast_text_color(bgcolor))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .build());

        ListDragView.setMenu(menu);
        ListDragView.setAdapter(mAdapter);
        ListDragView.setOnListItemLongClickListener(this);
        ListDragView.setOnDragListener(this,mItemsList);
        ListDragView.setOnListItemClickListener(this);
        ListDragView.setOnSlideListener(this);
        ListDragView.setOnMenuItemClickListener(this);
        ListDragView.setOnItemDeleteListener(this);

        ListDragView.setDividerHeight(Common.cDividerHeight);
    }



    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mItemsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemsList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_multiaccess_multioption_item_lay, null);
                cvh.vImgGrabber= (ImageView) convertView.findViewById(R.id.icono);
                cvh.vTxt = (TextView) convertView.findViewById(R.id.texto);
                cvh.vIcono = (ImageView) convertView.findViewById(R.id.icono2);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }

            GrxAccessInfo item = (GrxAccessInfo) this.getItem(position);
            cvh.vTxt.setText(item.get_label());
            cvh.vIcono.setImageDrawable(item.get_icon_drawable());
            cvh.vImgGrabber.setImageDrawable(getResources().getDrawable(R.drawable.ic_grabber));
            //if(mItemsList.size()<2) cvh.vImgGrabber.setVisibility(View.INVISIBLE);

            return convertView;
        }

        class CustomViewHolder {
            public ImageView vImgGrabber;
            public TextView vTxt;
            public ImageView vIcono;
        }
    };


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
    public void onListItemClick(View v, final int position) {
        if (getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRACCESS) != null) return;
        mIdItemClicked = position;
        DlgFrGrxAccess dlg = DlgFrGrxAccess.newInstance(this, Common.TAG_DLGFRGRMULTIACCESS, mKey,getString(R.string.update_item),mItemsList.get(position).get_uri(),
                mShowShortCuts,mShowApplications,mShowActivities,
                mIdOptionsArr, mIdValuesArr, mIdIconsArray, mSaveCustomActionsIcons,
                true);
        dlg.show(getFragmentManager(),Common.TAG_DLGFRGRACCESS);
    }

    @Override
    public void onSlideOpen(View view, View parentView, int position, int direction) {
    }

    @Override
    public void onSlideClose(View view, View parentView, int position, int direction) {
    }

    @Override
    public void onListItemLongClick(View view, int position) {
    }


    @Override
    public void onItemDelete(View view, int position) {
        mItemsList.remove(position);
        update_changes();

    }

    @Override
    public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {

        switch (direction) {
            case MenuItem.DIRECTION_LEFT:

                return Menu.ITEM_SCROLL_BACK;

            case MenuItem.DIRECTION_RIGHT:
                switch (buttonPosition) {
                    case 0:
                        return Menu.ITEM_DELETE_FROM_BOTTOM_TO_TOP;
                    default:
                        return Menu.ITEM_NOTHING;


                }
        }
        return Menu.ITEM_NOTHING;
    }

    private void update_changes(){
        mAdapter.notifyDataSetChanged();
        show_summary();
        check_add_items_button_state();

    }



    private void delete_all_items(){
        if(mItemsList.size()>0){
            AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
            ad.setTitle(getString(R.string.gs_tit_borrar_todas_apps));
            ad.setMessage(getString(R.string.gs_ay_borrar_todas_apps));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.gs_si), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mItemsList.clear();
                    update_changes();
                }
            });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.gs_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            ad.show();
        }
    }

    private  void set_result_and_do_callback(){

        check_callback();
        if(mCallBack==null ) return;

        mValue=get_result_from_item_list();
        if(mOriValue==null) mOriValue="";
        if(mValue.equals(mOriValue)) return;


        List<String> ori_icon_files_to_delete = new ArrayList<String>();


        String[] arr_ori_uris = mOriValue.split(Pattern.quote(mSeparator));

        /* list of original icons file names ***/

        if(arr_ori_uris!=null) {
            for(int i=0; i<arr_ori_uris.length;i++) {
                String tmp = Utils.get_file_name_from_uri_string(arr_ori_uris[i]);
                if(tmp!=null) ori_icon_files_to_delete.add(tmp);
            }
        }



        /*** detect original icons to delete. Copy valid tmp icons. Replace tmp icon names in uris for the callback result ***/

        int size = mItemsList.size();
        for(int ind =0;ind<size;ind++){
            String icon_name = mItemsList.get(ind).get_icon_path();
            if(icon_name!=null){
                if(ori_icon_files_to_delete.contains(icon_name)) ori_icon_files_to_delete.remove(icon_name); //keep this file
                if(icon_name.contains(Common.TMP_PREFIX)) {
                    String shortname = Utils.get_short_file_name_from_string(icon_name);
                    if(shortname!=null) { //just in case the dialog access makes some mistake..
                        String new_icon_name = Common.IconsDir + File.separator + Utils.get_short_file_name_from_string(icon_name).replace(Common.TMP_PREFIX,"");
                        Utils.file_copy(icon_name, new_icon_name); // copy tmp to final icon
                        String new_uri = Utils.change_extra_value_in_uri_string(mItemsList.get(ind).get_uri(),Common.EXTRA_URI_ICON,new_icon_name); //new uri value with final icon name
                        mItemsList.get(ind).update_uri(new_uri); //update list value
                    }

                }
            }
        }

        mValue=get_result_from_item_list(); //updated value with valid icon names

        /*********** delete icons not more used  **/

        for(int i3 = 0;i3<ori_icon_files_to_delete.size();i3++) {
            Utils.delete_file(ori_icon_files_to_delete.get(i3));
        }

        mCallBack.GrxSetMultiAccess(mValue);

    }


    @Override
    public void GrxSetAccess(String value){

        if(mIdItemClicked==-1) mItemsList.add(new GrxAccessInfo(value,getActivity()));
        else mItemsList.get(mIdItemClicked).ini_access_info(value,getActivity());
        update_changes();
    }


    private void delete_tmp_files(){
        File dir = new File(Common.CacheDir);
        if(dir.exists()&&dir.isDirectory()){
            File ficheros[]=dir.listFiles();
            if(ficheros.length!=0){
                for(int ind=0;ind<ficheros.length;ind++){
                    if(ficheros[ind].getName().contains(Common.TMP_PREFIX)) ficheros[ind].delete();
                    }

            }
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        if(mDeleteTmpFileOnDismiss && isAdded()) {
            delete_tmp_files();
        }
    }

}
