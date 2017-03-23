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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mods.grx.settings.prefssupport.colorpicker.CircleColorDrawable;
import com.mods.grx.settings.prefssupport.info.GrxAppInfo;
import com.mods.grx.settings.sldv.*;
import com.mods.grx.settings.utils.*;
import com.mods.grx.settings.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;


public class DlgFrGrxMultiAppColor extends DialogFragment implements
        DlgFrSelecApp.OnGrxAppListener, DlgFrGrxColorPicker.OnGrxColorPickerListener,
        SlideAndDragListView.OnListItemLongClickListener, SlideAndDragListView.OnDragListener, SlideAndDragListView.OnSlideListener,
        SlideAndDragListView.OnListItemClickListener, SlideAndDragListView.OnMenuItemClickListener,  SlideAndDragListView.OnItemDeleteListener{




    private LinearLayout boton_ayuda;
    private LinearLayout boton_apps_usu;
    private LinearLayout boton_apps_sistemas;
    private LinearLayout boton_borrar;
    private LinearLayout boton_ordenar;
    private LinearLayout boton_ok;
    private LinearLayout boton_cancelar;




    private Drawable icono_borrar;


    private boolean mFormatoExt;

    private String separador;
    private String pat_separador;


    private OnGrxMultiAppColorListener mCallBack;
    private String mHelperFragment;
    private String mKey;
    private String mTitle;
    private String mOriValue;

    private boolean mShowAllApps;
    private boolean mSaveActName;
    private int mMaxNumOfApps;

    private boolean mShowcolor;
    private int mDefColor;
    private boolean mFlower;
    private boolean mAlpha;
    private boolean mAuto;

    private String mSeparator;

    private String mValue;
    private int mIdItemClicked;


    private LinearLayout vHelpButton;
    private LinearLayout vDeleteButton;
    private LinearLayout vAddButton;
    private LinearLayout vSortItemsButton;
    private TextView vTxtSelectedItems;
    private SlideAndDragListView ListDragView;

    private ArrayList<GrxAppInfo> mAppList;


    public interface OnGrxMultiAppColorListener{
       void onGrxMultiAppColorSel(DlgFrGrxMultiAppColor dialog, int num, String apps);
    }

    public DlgFrGrxMultiAppColor(){}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    private View multiapp_view(){

        View view = getActivity().getLayoutInflater().inflate(R.layout.pref_multiappcolor_dlg_lay, null);
        vHelpButton = (LinearLayout) view.findViewById(R.id.help_button);
        vHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_help();
            }
        });

        vTxtSelectedItems = (TextView) view.findViewById(R.id.txt_apps_seleccionadas);

        vAddButton = (LinearLayout) view.findViewById(R.id.app_buttom);
        vAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_select_app_dialog();
            }
        });

        vDeleteButton = (LinearLayout) view.findViewById(R.id.boton_borrar);
        vDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_delete_items_dialog();
            }
        });

        vSortItemsButton = (LinearLayout) view.findViewById(R.id.boton_ordenar);
        vSortItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort_all_itmems();
            }
        });

        ListDragView= (SlideAndDragListView) view.findViewById(R.id.lv_edit);
        ListDragView.setDividerHeight(Common.cDividerHeight);

        return view;
    }



    private void save_callback(DlgFrGrxMultiAppColor.OnGrxMultiAppColorListener callback){
        mCallBack =callback;
    }

    public static DlgFrGrxMultiAppColor newInstance(DlgFrGrxMultiAppColor.OnGrxMultiAppColorListener callback,String HelperFragment,
                                                    String key, String title, String value,
                                                    boolean show_allapps, boolean save_actname, int max_apps,
                                                    boolean show_color, int defcolor, boolean showflower, boolean showalpha, boolean showauto,
                                                    String separator){


        DlgFrGrxMultiAppColor ret = new DlgFrGrxMultiAppColor();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,HelperFragment);
        bundle.putString("key",key);
        bundle.putString("tit",title);
        bundle.putString("val",value);
        bundle.putBoolean("show_allapps", show_allapps);
        bundle.putBoolean("save_actname",save_actname);
        bundle.putInt("max_items", max_apps);
        bundle.putBoolean("show_color",show_color);
        bundle.putInt("defcolor", defcolor);
        bundle.putBoolean("showflower",show_allapps);
        bundle.putBoolean("showalpha",showalpha);
        bundle.putBoolean("showauto",showauto);
        bundle.putString("separator", separator);
        ret.setArguments(bundle);
        ret.save_callback(callback);
        return ret;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
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
        mShowAllApps = getArguments().getBoolean("show_allapps");
        mSaveActName = getArguments().getBoolean("save_actname");
        mMaxNumOfApps = getArguments().getInt("max_items",0);
        mShowcolor = getArguments().getBoolean("show_color");
        mDefColor = getArguments().getInt("defcolor");
        mFlower = getArguments().getBoolean("showflower");
        mAlpha = getArguments().getBoolean("showalpha");
        mAuto = getArguments().getBoolean("showauto");
        mSeparator=getArguments().getString("separator");

        mIdItemClicked=-1;

        if (state != null) {
            mValue =  state.getString("curr_val");
            mIdItemClicked = state.getInt("clicked_id");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(multiapp_view());
        builder.setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        builder.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                set_result_and_do_callback();
            }
        });

        AlertDialog ad = builder.create();

        mAppList = new ArrayList<>();
        ini_app_list(mValue);
        ini_drag_list_view();
        update_changes();
        check_add_items_button_state();
        return ad;

    }


    private int get_color_from_array_element(String element){
        if(!mShowcolor) return mDefColor;
        String[] array = element.split(Pattern.quote("="));
        if(array==null) return mDefColor;
        else return Integer.valueOf(array[1]);
    }


    private String get_package_or_activity_name_from_array_element(String element, int index){
        String[] array;
        String[] array_package_activity;
        String package_activity="";
        if(mShowcolor){
            array = element.split(Pattern.quote("="));
            package_activity = array[0];
        }else package_activity=element;

        if(mSaveActName){
            array_package_activity=package_activity.split(Pattern.quote("/"));
            return array_package_activity[index];
        }else return package_activity;
    }


    private void ini_app_list(String value) {
        mAppList.clear();
        if (!value.isEmpty()) {
            String[] arr = value.split(Pattern.quote(mSeparator));
            for (int ind = 0; ind < arr.length; ind++) {

                int color = get_color_from_array_element(arr[ind]);
                String package_name = get_package_or_activity_name_from_array_element(arr[ind], 0);
                String activity_name = null;
                if (mSaveActName)
                    activity_name = get_package_or_activity_name_from_array_element(arr[ind], 1);

                boolean isInstalled = false;
                if (package_name != null)
                    isInstalled = Utils.is_app_installed(getActivity(), package_name);

                if (isInstalled) {
                    String label = Utils.get_app_name(getActivity(), package_name);
                    Drawable icon = Utils.get_application_icon(getActivity(), package_name);
                    mAppList.add(new GrxAppInfo(package_name, activity_name, label, icon, color));
                }
            }
        }
    }


    private String get_result_from_item_list(){
        String resultado ="";
        for(int ind=0;ind<mAppList.size();ind++){
            GrxAppInfo item = (GrxAppInfo) mAppList.get(ind);
            resultado+=item.nombre_app();
            String act = item.nombre_actividad();
            if(mSaveActName) if(act!=null) resultado+="/"+act;
            if(mShowcolor) {
                resultado+="=";
                resultado+=String.valueOf(mAppList.get(ind).color_app());
            }
            resultado+=mSeparator;
        }
        return resultado;
    }



    private void ini_drag_list_view(){
        TypedArray a = getActivity().getTheme().obtainStyledAttributes( new int[] {R.attr.complemnt_accent_color});
        int bgcolor = a.getColor(0,0);
        a.recycle();

        Menu menu = new Menu(true,false);
        menu.addItem(new MenuItem.Builder().setWidth( (int) getResources().getDimension(R.dimen.slv_item_bg_btn_width)*2   )
                .setBackground(new ColorDrawable(bgcolor))
                .setText(getString(R.string.gs_copy_color))
                .setTextColor(Utils.get_contrast_text_color(bgcolor))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .build());

        if(mShowcolor){
                 menu.addItem(new MenuItem.Builder().setWidth( (int) getResources().getDimension(R.dimen.slv_item_bg_btn_width) *2   )
                      .setBackground(new ColorDrawable(bgcolor))
                      .setText(getString(R.string.gs_remove))
                      .setTextColor(Utils.get_contrast_text_color(bgcolor))
                      .setDirection(MenuItem.DIRECTION_LEFT)
                      .build());
        }

        ListDragView.setMenu(menu);
        ListDragView.setAdapter(mAdapter);
        ListDragView.setOnListItemLongClickListener(this);
        ListDragView.setOnDragListener(this,mAppList);
        ListDragView.setOnListItemClickListener(this);
        ListDragView.setOnSlideListener(this);
        ListDragView.setOnMenuItemClickListener(this);
        ListDragView.setOnItemDeleteListener(this);
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
        mAppList.remove(position);
        update_changes();
    }

    @Override
    public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {

        switch (direction) {
            case MenuItem.DIRECTION_LEFT:
                switch (buttonPosition) {
                    case 0:
                        return Menu.ITEM_DELETE_FROM_BOTTOM_TO_TOP;
                    default:
                        return Menu.ITEM_NOTHING;
                }



            case MenuItem.DIRECTION_RIGHT:
                save_color_value_to_clipboard(itemPosition);
                return Menu.ITEM_SCROLL_BACK;
        }
        return Menu.ITEM_NOTHING;
    }

    @Override
    public void onListItemClick(View v, final int position) {
        if(mShowcolor) open_select_color_dialog(position);
    }


    private boolean app_already_selected(String packagename){
        boolean exists=false;
        for(int i = 0; i<mAppList.size(); i++){
            if(packagename.equals(mAppList.get(i).nombre_app())) {
                exists=true;
                break;
            }
        }
        return exists;
    }

    private void check_add_items_button_state() {
        if (mMaxNumOfApps == 0) {
            vAddButton.setClickable(true);
            vAddButton.setAlpha((float) 1.0);
            return;
        }

        if(mAppList!=null){
            if(mAppList.size()>=mMaxNumOfApps) {
                vAddButton.setClickable(false);
                vAddButton.setAlpha((float) 0.5);
            }else{
                vAddButton.setClickable(true);
                vAddButton.setAlpha((float) 1.0);
            }
        }
    }

    @Override
    public void onGrxAppSel(DlgFrSelecApp dialog, String packagename){
        if(app_already_selected(packagename)) {
            Toast.makeText(getActivity(), getString(R.string.gs_app_ya_seleccionada, ""), Toast.LENGTH_LONG).show();
            return;
        }
        else if (!Utils.is_app_installed(getActivity(),packagename)) return;

        String activity_name = Utils.get_activity_name_from_package_name(getActivity(),packagename); if (!Utils.is_app_installed(getActivity(),packagename)) return;
        String label = Utils.get_app_name(getActivity(),packagename); if (!Utils.is_app_installed(getActivity(),packagename)) return;
        Drawable icon = Utils.get_application_icon(getActivity(),packagename);
        mAppList.add(new GrxAppInfo(packagename,activity_name,label,icon,mDefColor));
        update_changes();
        check_add_items_button_state();
    }

    @Override
    public void onGrxColorSet(int color){
        if(mIdItemClicked==-1) return;
        mAppList.get(mIdItemClicked).pon_color_app(color);
        update_changes();
    }

    private void open_select_app_dialog(){
       DlgFrSelecApp dlg = (DlgFrSelecApp) getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRSELECTAPP);
        if(dlg==null){
            dlg = DlgFrSelecApp.newInstance(this, Common.TAG_DLGFRGRMULTIPPCOLOR, mKey,mTitle,mShowAllApps,true);
            dlg.show(getFragmentManager(),Common.TAG_DLGFRGRSELECTAPP);
        }
    }

    private void open_select_color_dialog(int position){
        if(!mShowcolor) return;
        mIdItemClicked = position;
        DlgFrGrxColorPicker dlgFrGrxColorPicker =  (DlgFrGrxColorPicker) getFragmentManager().findFragmentByTag(Common.TAG_DLGFRGRXCOLORPICKER);
        if (dlgFrGrxColorPicker==null){
            int color = mDefColor;
            if(mIdItemClicked!=-1){
                color = mAppList.get(position).color_app();
            }
            dlgFrGrxColorPicker= DlgFrGrxColorPicker.newInstance(this, Common.TAG_DLGFRGRMULTIPPCOLOR, mTitle,mKey,color,mFlower,mAlpha,mAuto);
            dlgFrGrxColorPicker.show(getFragmentManager(),Common.TAG_DLGFRGRXCOLORPICKER);
        }
    }


    private void sort_all_itmems(){
        if(mAppList.size()>1 ){
            try {
                Collections.sort(mAppList, new Comparator<GrxAppInfo>() {
                    @Override
                    public int compare(GrxAppInfo A_appinfo, GrxAppInfo appinfo) {
                        try {
                            return String.CASE_INSENSITIVE_ORDER.compare(A_appinfo.etiqueta_app(), appinfo.etiqueta_app());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
            }catch(Exception e) {
                e.printStackTrace();
            }
            update_changes();
        }
    }

    private void show_delete_items_dialog(){
        if(mAppList.size()>0){
            AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
            ad.setTitle(getString(R.string.gs_tit_borrar_todas_apps));
            ad.setMessage(getString(R.string.gs_ay_borrar_todas_apps));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.gs_si), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAppList.clear();
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

    private void show_help(){
        AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
        ad.setTitle(getString(R.string.gs_ay_ayuda));
        if(mShowcolor) ad.setMessage(getString(R.string.gs_multiappcolor_help_color));
        else ad.setMessage(getString(R.string.gs_multiappcolor_help));
        ad.show();
    }

    private void check_callback(){
        if(mCallBack==null) {
            if (mHelperFragment.equals(Common.TAG_PREFSSCREEN_FRAGMENT)) {
                GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                if (prefsScreen != null)
                    mCallBack = (DlgFrGrxMultiAppColor.OnGrxMultiAppColorListener) prefsScreen.find_callback(mKey);
            }else mCallBack=(DlgFrGrxMultiAppColor.OnGrxMultiAppColorListener) getFragmentManager().findFragmentByTag(mHelperFragment);
        }
    }

    private void set_result_and_do_callback(){
        check_callback();
        if (mCallBack != null) mCallBack.onGrxMultiAppColorSel(this,mAppList.size(),get_result_from_item_list());
        mAppList.clear();
        this.dismiss();
    }

    private void set_info_text(){
            if(mMaxNumOfApps!=0) vTxtSelectedItems.setText( getString(R.string.gs_num_apps_seleccionadas,mAppList.size())+ "  "+ getString(R.string.gs_max_apps,mMaxNumOfApps));
            else vTxtSelectedItems.setText( getString(R.string.gs_num_apps_seleccionadas,mAppList.size()));
    }


    private void update_changes(){
            mAdapter.notifyDataSetChanged();
            set_info_text();
    }

    private void save_color_value_to_clipboard(int pos){
        int col = mAppList.get(pos).color_app();
        ClipboardManager cbm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("color", ("#" + Integer.toHexString(col).toUpperCase())            );
        cbm.setPrimaryClip(clip);
        Toast.makeText(getActivity(),getString(R.string.gs_copiado_clipboard),Toast.LENGTH_LONG).show();
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mAppList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAppList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_info_app_multi, null);
                cvh.vimgLogo = (ImageView) convertView.findViewById(R.id.img_item_edit);
                cvh.vtxtName = (TextView) convertView.findViewById(R.id.txt_item_edit);
                cvh.vtxtpaquete = (TextView) convertView.findViewById(R.id.nombre_paquete);
                cvh.vcolorapp = (ImageView) convertView.findViewById(R.id.color_app);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }

            GrxAppInfo grxInfoApp = (GrxAppInfo) this.getItem(position);
            cvh.vtxtName.setText(grxInfoApp.etiqueta_app());
            cvh.vimgLogo.setImageDrawable(grxInfoApp.icono_app());
            cvh.vtxtpaquete.setText(grxInfoApp.nombre_app());
            if(mShowcolor){
                cvh.vcolorapp.setImageDrawable(new CircleColorDrawable(grxInfoApp.color_app()));
            }else cvh.vcolorapp.setVisibility(View.GONE);

            return convertView;
        }

        class CustomViewHolder {
            public ImageView vimgLogo;
            public TextView vtxtName;
            public TextView vtxtpaquete;
            public ImageView vcolorapp;
        }
    };




}
