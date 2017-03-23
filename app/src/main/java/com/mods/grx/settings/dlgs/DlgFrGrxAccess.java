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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;
import com.mods.grx.settings.utils.GrxImageHelper;
import com.mods.grx.settings.utils.Utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DlgFrGrxAccess extends DialogFragment implements AdapterView.OnItemClickListener, ExpandableListView.OnChildClickListener {

    List<ResolveInfo> mShortCutsList=null;
    List<ResolveInfo> mUsuAppsList=null;
    List<GrxActivityInfo> mActivitiesList=null;
    List<GrxCustomActionInfo> mCustomActionsList=null;
    private ArrayList<ItemSpinner> mSpinnerList;

    AsyncTask<Void, Void, Void> loader;

    private Spinner mSpinner;
    private ProgressBar mProgressBar;
    private TextView vtxtprogressbar;
    private ListView vListView;
    private ExpandableListView vExpListView;
    private LinearLayout vSelectionContainer;
    private LinearLayout vButtonsContainer;
    private LinearLayout vExpandButton;
    private LinearLayout vCollapseButton;

    private int mIdOptionsArr;
    private int mIdValuesArr;
    private int mIdIconsArray;
    private boolean mShowShortCuts;
    private boolean mShowApplications;
    private boolean mShowActivities;
    private boolean mShowCustomActions;
    private String mHelperFragment;
    private String mKey;
    private String mTitle;
    private String mValue;
    private GrxAccesListener mCallBack;
    private boolean mMultiOptionMode;
    private boolean mSaveCustomActionsIcons;

    private Intent mCurrentSelectedIntent;
    private String mOriValue;

    private boolean mDeleteTmpFileOnDismiss=true;


    private String mAuxShortCutActivityLabel=null;



    public DlgFrGrxAccess() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        mDeleteTmpFileOnDismiss = true;
        super.onResume();
    }

    public interface GrxAccesListener {
        void GrxSetAccess(String value);
    }

    public static DlgFrGrxAccess newInstance(GrxAccesListener callback,String HelperFragment, String key, String title, String value,
                                             boolean show_shortcuts, boolean show_apps, boolean show_activities,
                                             int id_array_options, int id_array_values, int id_array_icons, boolean save_icons,
                                             boolean multi_mode){


        DlgFrGrxAccess ret = new DlgFrGrxAccess();
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
        bundle.putBoolean("mode_multi",multi_mode);
        ret.setArguments(bundle);
        ret.save_callback(callback);
        return ret;

    }

    private void save_callback(DlgFrGrxAccess.GrxAccesListener callback){
        mCallBack =callback;
    }


    public void modo_multi(boolean modo){
        mMultiOptionMode=modo;
    }

    /*********************** MAIN LOGIC **********************************************/


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        int op = mSpinner.getSelectedItemPosition();
        op=mSpinnerList.get(op).getId();

        switch (op){
            case Common.ID_ACCESS_SHORCUT:
                try_to_get_shortcut(position);
                break;
            case Common.ID_ACCESS_APPS:
                build_intent_from_usu_app(position);
                break;
            case Common.ID_ACCESS_CUSTOM:
                build_intent_from_custom_action(position);
                break;
            default:
                break;
        }
    }

    /**** logic for shortcuts *****/

    private void try_to_get_shortcut(int pos){
        ResolveInfo ri = (ResolveInfo) mShortCutsList.get(pos);
        mAuxShortCutActivityLabel=null;
        try {
            mAuxShortCutActivityLabel = ri.loadLabel(getActivity().getPackageManager()).toString();
        }catch (Exception e){

        }  //save app label name to a temp var, now lets try to get shortcut  now. This is f.e. Whatsapp chat:  - let´s try to get the contact - group - ... intent now

        Intent intent = new  Intent(Intent.ACTION_CREATE_SHORTCUT);
        ComponentName c_n = new ComponentName(ri.activityInfo.packageName,ri.activityInfo.name);
        intent.setComponent(c_n);
        startActivityForResult(intent,Common.REQ_CODE_GET_SHORTCUT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String tmp_name = null;

        if(requestCode == Common.REQ_CODE_GET_SHORTCUT){
            if(resultCode== Activity.RESULT_OK){
                Bitmap ico = null;
                Intent.ShortcutIconResource iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                if(iconResource!=null){
                    try {
                        final Context pkgContext = getActivity().createPackageContext(iconResource.packageName,Context.CONTEXT_IGNORE_SECURITY);
                        final Resources pkgRes = pkgContext.getResources();
                        final int id_dr = pkgRes.getIdentifier(iconResource.resourceName, "drawable", iconResource.packageName);
                        ico = BitmapFactory.decodeResource(pkgRes,id_dr);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(ico==null){
                    ico = (Bitmap) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
                }

                if(ico!=null){
                    tmp_name = get_currenttimemillis_file_icon_name();
                    if(!GrxImageHelper.save_png_from_bitmap(ico,tmp_name)) tmp_name = null;
                    else Utils.set_read_write_file_permissions(tmp_name);
                }
                build_intent_from_shortcut((Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT), data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),tmp_name);
            }
        }
    }



    private void build_intent_from_shortcut(Intent intent, String text, String tmp_name ){
        if(intent==null){
            Toast.makeText(getActivity(),getString(R.string.gs_error_acceso_directo), Toast.LENGTH_SHORT).show();
        }else{
            Utils.delete_grx_icon_file_from_intent(mCurrentSelectedIntent, Common.TMP_PREFIX);
            mCurrentSelectedIntent=intent;
            String act_label="";
            String label="";
            if(mAuxShortCutActivityLabel!=null) act_label=mAuxShortCutActivityLabel;
            if(text!=null) label=text;
            if(act_label.equals(label)) mCurrentSelectedIntent.putExtra(Common.EXTRA_URI_LABEL,act_label );
            else mCurrentSelectedIntent.putExtra(Common.EXTRA_URI_LABEL,act_label+" : "+label);
            if(tmp_name!=null){
                mCurrentSelectedIntent.putExtra(Common.EXTRA_URI_ICON,tmp_name);
            }
            mCurrentSelectedIntent.putExtra(Common.EXTRA_URI_TYPE,Common.ID_ACCESS_SHORCUT);
            show_current_user_selection();
        }
    }

    private void build_intent_from_usu_app(int pos){
        ResolveInfo ri_app = (ResolveInfo) mUsuAppsList.get(pos);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra(Common.EXTRA_URI_TYPE,Common.ID_ACCESS_APPS);
        ComponentName cn = new ComponentName(ri_app.activityInfo.packageName,ri_app.activityInfo.name);
        intent.setComponent(cn);
        Utils.delete_grx_icon_file_from_intent(mCurrentSelectedIntent, Common.TMP_PREFIX);
        mCurrentSelectedIntent=intent;
        show_current_user_selection();
    }


    private void build_intent_from_custom_action(int pos){

        Drawable dr=null;
        String icon_name=null;
        String label = null;
        String val = null;
        String drawable_name=null;

        GrxCustomActionInfo grxInfoAccion = mCustomActionsList.get(pos);


        Intent intent = new Intent();
        intent.putExtra(Common.EXTRA_URI_TYPE,Common.ID_ACCESS_CUSTOM);

        label = grxInfoAccion.getLabel();
        if(label!=null) intent.putExtra(Common.EXTRA_URI_LABEL,label);

        val = grxInfoAccion.getVal();
        if(val!=null) intent.putExtra(Common.EXTRA_URI_VALUE,val);

        drawable_name = grxInfoAccion.getDrawableName();
        if(drawable_name!=null) intent.putExtra(Common.EXTRA_URI_DRAWABLE_NAME,drawable_name);

        if(mSaveCustomActionsIcons){
            dr=grxInfoAccion.getIcon();
            if(dr!=null){
                icon_name=get_currenttimemillis_file_icon_name();
                if(GrxImageHelper.save_png_from_bitmap(GrxImageHelper.drawableToBitmap(dr),icon_name ))
                    Utils.set_read_write_file_permissions(icon_name);
            }
        }

        if(icon_name!=null) intent.putExtra(Common.EXTRA_URI_ICON,icon_name);

        Utils.delete_grx_icon_file_from_intent(mCurrentSelectedIntent, Common.TMP_PREFIX);
        mCurrentSelectedIntent=intent;
        show_current_user_selection();

    }


    public void show_current_user_selection(){

        Drawable icon = null;
        String label=null;
        if(mCurrentSelectedIntent!=null){
            vSelectionContainer.removeAllViews();
            int type = mCurrentSelectedIntent.getIntExtra(Common.EXTRA_URI_TYPE,-1);
            if(type == -1 ) {
                Utils.delete_grx_icon_file_from_intent(mCurrentSelectedIntent, Common.TMP_PREFIX);
                mCurrentSelectedIntent=null;
                add_views_from_selected_intent(null,getResources().getString(R.string.gs_no_access_selection),false);
                return;
            }

            switch (type){
                case Common.ID_ACCESS_SHORCUT:
                case Common.ID_ACCESS_APPS:
                case Common.ID_ACCESS_ACTIVITIES:

                    label = Utils.get_activity_label_from_intent(getActivity(), mCurrentSelectedIntent);
                    icon = Utils.get_drawable_from_intent(getActivity(),mCurrentSelectedIntent);
                    add_views_from_selected_intent(icon,label,true);
                    break;
                case Common.ID_ACCESS_CUSTOM:
                    label = Utils.get_activity_label_from_intent(getActivity(), mCurrentSelectedIntent);
                    icon = Utils.get_drawable_from_intent(getActivity(),mCurrentSelectedIntent);
                    add_views_from_selected_intent(icon,label,false);
                    break;
                default:
                    break;
            }
        }else {
            add_views_from_selected_intent(null,getResources().getString(R.string.gs_no_access_selection),false);
        }
    }

    private void add_views_from_selected_intent(Drawable drawable, String text, boolean clickable){

        vSelectionContainer.removeAllViews();
        LinearLayout ll = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.pref_access_dlg_item_lay,null);

        ImageView iv = (ImageView) ll.findViewById(R.id.grx_id_img_item_acceso);
        if(drawable==null) iv.setVisibility(View.GONE);
        else iv.setImageDrawable(drawable);

        TextView tv = (TextView) ll.findViewById(R.id.grx_texto_item_acceso);
        if(text!=null) tv.setText(text);
        Utils.animate_textview_marquee(tv);
        tv.setSelected(true);
        ImageView arrow = (ImageView) ll.findViewById(R.id.arrow_icon);
        if(clickable) arrow.setVisibility(View.VISIBLE);
       //ll.setClickable(clickable);
        vSelectionContainer.addView(ll);
        vSelectionContainer.setClickable(clickable);
    }



    private String get_currenttimemillis_file_icon_name(){
        return Common.CacheDir + File.separator + Common.TMP_PREFIX+String.valueOf(System.currentTimeMillis()) + ".png";
    }


    /******************* DIALOG ,  VIEW , SAVED INSTANCE, DISMISS *************************/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mDeleteTmpFileOnDismiss=false;
        super.onSaveInstanceState(outState);
        String current_uri=null;
        if(mCurrentSelectedIntent!=null) current_uri= mCurrentSelectedIntent.toUri(0);
        if(current_uri== null ) current_uri = "";
        outState.putString("curr_val", current_uri);
    }

    private View access_view(){

        View view = getActivity().getLayoutInflater().inflate(R.layout.pref_access_dlg_lay, null);
        vButtonsContainer= (LinearLayout) view.findViewById(R.id.botones);
        vExpandButton = (LinearLayout) vButtonsContainer.findViewById(R.id.boton_abrir_grupos);
        vCollapseButton = (LinearLayout) vButtonsContainer.findViewById(R.id.boton_cerrar_grupos);
        vButtonsContainer.setVisibility(View.GONE);
        vExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand_activities_groups();
            }
        });

        vCollapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse_activities_groups();
            }
        });
        vSelectionContainer = (LinearLayout) view.findViewById(R.id.grx_id_contenedor_info_aux);
        vSelectionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(mCurrentSelectedIntent!=null) startActivity(mCurrentSelectedIntent);
                }catch ( Exception e){
                    e.printStackTrace();
                    Toast.makeText(getActivity(),getString(R.string.gs_aviso_acceso_no_permitido),Toast.LENGTH_LONG).show();
                }
            }
        });

        mSpinner = (Spinner) view.findViewById(R.id.grx_id_spinner);

        vListView =(ListView) view.findViewById(R.id.grx_id_listview);
        vExpListView = (ExpandableListView) view.findViewById(R.id.grx_id_explistview);
        vExpListView.setDivider(getResources().getDrawable(R.drawable.list_divider));
        vExpListView.setChildDivider(getResources().getDrawable(R.drawable.list_divider));

        vListView.setOnItemClickListener(this);
        vExpListView.setOnChildClickListener(this);

        vtxtprogressbar =(TextView) view.findViewById(R.id.grx_id_txt_progressbar);
        vtxtprogressbar.setText(getString(R.string.gs_creando_ordenando_listado));
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        vListView.setDividerHeight(Common.cDividerHeight);
        vExpListView.setDividerHeight(Common.cDividerHeight);

        return view;
    }


    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        if((loader!=null) && (loader.getStatus()== AsyncTask.Status.RUNNING)){
            loader.cancel(true);
            loader=null;
        }

        if(mShortCutsList!=null) mShortCutsList.clear();
        if(mUsuAppsList!=null) mUsuAppsList.clear();
        if(mActivitiesList!=null) mActivitiesList.clear();
        if(mCustomActionsList!=null) mCustomActionsList.clear();

        if(mCurrentSelectedIntent!=null){
            String current_uri = mCurrentSelectedIntent.toUri(0);
            if(!current_uri.equals(mOriValue)){
                if(mDeleteTmpFileOnDismiss && isAdded()) Utils.delete_grx_icon_file_from_intent(mCurrentSelectedIntent);
            }

        }






    }


    private void check_callback(){
        if(mCallBack==null) {
            if (mHelperFragment.equals(Common.TAG_PREFSSCREEN_FRAGMENT)) {
                GrxPreferenceScreen prefsScreen = (GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                if (prefsScreen != null)
                    mCallBack = (DlgFrGrxAccess.GrxAccesListener) prefsScreen.find_callback(mKey);
            }else {
                mCallBack=(DlgFrGrxAccess.GrxAccesListener) getFragmentManager().findFragmentByTag(mHelperFragment);
            }
        }
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
        mMultiOptionMode=getArguments().getBoolean("mode_multi");

        if(mIdOptionsArr==0 || mIdValuesArr == 0 ) mShowCustomActions=false;
        else mShowCustomActions=true;

        if (state != null) mValue =  state.getString("curr_val");

        if(mValue!=null && !mValue.isEmpty()){
            try{
                mCurrentSelectedIntent=Intent.parseUri(mValue,0);
            }catch (URISyntaxException e){}

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setView(access_view());
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
                check_callback();
                mDeleteTmpFileOnDismiss=false;
                if(mCallBack!=null && mCurrentSelectedIntent!=null) {
                    String curr_uri = mCurrentSelectedIntent.toUri(0);
                    if(mOriValue.equals(curr_uri)) mDeleteTmpFileOnDismiss = true;
                    else mCallBack.GrxSetAccess(curr_uri);
                } else mDeleteTmpFileOnDismiss = true;
            }
        });

        ini_spinner();
        show_current_user_selection();
        return builder.create();

    }


    /***************** SPINNER *****************************/

    private void ini_spinner() {
        mSpinnerList = new ArrayList<>();
        if(mShowShortCuts) mSpinnerList.add(new ItemSpinner(getString(R.string.gs_accesos_directos), Common.ID_ACCESS_SHORCUT));
        if(mShowApplications) mSpinnerList.add(new ItemSpinner(getString(R.string.gs_apps_usu), Common.ID_ACCESS_APPS));
        if(mShowActivities)mSpinnerList.add(new ItemSpinner(getString(R.string.gs_actividades), Common.ID_ACCESS_ACTIVITIES));
        if(mShowCustomActions) mSpinnerList.add(new ItemSpinner(getString(R.string.gs_acciones), Common.ID_ACCESS_CUSTOM));

        if(mSpinnerList.size()<2) {
            mSpinner.setBackgroundDrawable(null);
            mSpinner.setPadding(0,0,0,0);
            mSpinner.setEnabled(false);

        }
        mSpinner.setAdapter(AdapterSpinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int op = mSpinnerList.get(position).getId();
                switch (op){
                    case Common.ID_ACCESS_SHORCUT:
                        ini_shortcuts_list();
                        break;
                    case Common.ID_ACCESS_APPS:
                        ini_apps_list();
                        break;
                    case Common.ID_ACCESS_ACTIVITIES:
                        ini_activities_list();
                        break;
                    case Common.ID_ACCESS_CUSTOM:
                        ini_custom_actions_list();
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    private class ItemSpinner {
        private String Texto;
        private int id;

        public ItemSpinner(String texto, int i) {
            Texto = texto;
            id = i;
        }

        public String getLabel() {
            return Texto;
        }

        public int getId() {
            return id;
        }
    }

    private BaseAdapter AdapterSpinner = new BaseAdapter() {
        @Override
        public int getCount() {
            return mSpinnerList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSpinnerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mSpinnerList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomViewHolder cvh;
            if (convertView == null) {
                cvh = new CustomViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_access_spinner_lay, null);
                cvh.vCtxt = (TextView) convertView.findViewById(R.id.texto);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            ItemSpinner itemSpinner = (ItemSpinner) this.getItem(position);
            cvh.vCtxt.setText(itemSpinner.getLabel());
            if(mSpinnerList.size()==1){
                //cvh.vCtxt.setGravity(Gravity.CENTER);
                cvh.vCtxt.setPadding(60,0,0,0);

            }
            return convertView;
        }

        class CustomViewHolder {
            public TextView vCtxt;
        }
    };



    /************************************************************************************/

    /**************************** ShortCuts **********************************************/


    private void build_shortcuts_list(){
        Intent intent = new Intent();
        List<PackageInfo> ListaPaquetes = getActivity().getPackageManager().getInstalledPackages(0);
        intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
        for (PackageInfo packinfo : ListaPaquetes){
            intent.setPackage(packinfo.packageName);
            List<ResolveInfo> mActivitiesList = getActivity().getPackageManager().queryIntentActivities(intent, 0);
            for(ResolveInfo resolveInfo : mActivitiesList) {
                mShortCutsList.add(resolveInfo);
            }
        }
        Collections.sort(mShortCutsList, new ResolveInfo.DisplayNameComparator(getActivity().getPackageManager()));
    }

    private void ini_shortcuts_list(){
        vExpListView.setVisibility(View.GONE);
        vButtonsContainer.setVisibility(View.GONE);
        vListView.setVisibility(View.GONE);
        loader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSpinner.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                vListView.setVisibility(View.GONE);
                vtxtprogressbar.setVisibility(View.VISIBLE);
                mProgressBar.refreshDrawableState();
            }

            @Override
            protected Void doInBackground(Void... params) {
                if(mShortCutsList==null){
                    mShortCutsList = new ArrayList<ResolveInfo>();
                    build_shortcuts_list();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                vtxtprogressbar.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                vListView.setAdapter(null);
                vListView.setAdapter(mShortCutsAdapter);
                vListView.setVisibility(View.VISIBLE);
                if(mSpinnerList.size()>1) mSpinner.setEnabled(true);;
            }
        }.execute();
    }



    private BaseAdapter mShortCutsAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mShortCutsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mShortCutsList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_access_dlg_item_lay, null);
                cvh.vImgIcono = (ImageView) convertView.findViewById(R.id.grx_id_img_item_acceso);
                cvh.vTxt = (TextView) convertView.findViewById(R.id.grx_texto_item_acceso);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            //ResolveInfo item = (ResolveInfo) this.getItem(position);
            ResolveInfo item = (ResolveInfo) mShortCutsList.get(position);
            cvh.vTxt.setText(item.loadLabel(getActivity().getPackageManager()));
            cvh.vImgIcono.setImageDrawable(item.loadIcon(getActivity().getPackageManager()));

            return convertView;
        }

        class CustomViewHolder {
            public ImageView vImgIcono;
            public TextView vTxt;
        }
    };


    /*****************************************************************************/
    /****************************** CUSTOM ACTIONS *******************************/
    /*****************************************************************************/



    private void build_custom_actions_list(){
        TypedArray icons_array=null;
        String vals_array[] = getResources().getStringArray(mIdValuesArr);
        String opt_array[] = getResources().getStringArray(mIdOptionsArr);
        if(mIdIconsArray!=0){
            icons_array = getResources().obtainTypedArray(mIdIconsArray);
        }
        mCustomActionsList = new ArrayList<GrxCustomActionInfo>();
        for(int i=0;i<vals_array.length;i++){
            Drawable drwtmp = null;
            String drawable_name = null;
            if(icons_array!=null) {
                drwtmp = icons_array.getDrawable(i);
                int id_drw = icons_array.getResourceId(i,0);
                if (id_drw!=0) {
                    drawable_name = getResources().getResourceEntryName(id_drw);
                }
            }

            mCustomActionsList.add(new GrxCustomActionInfo(opt_array[i],vals_array[i], drwtmp, drawable_name) );
        }
        if(icons_array!=null) icons_array.recycle();
    }


    private void ini_custom_actions_list(){
        vListView.setVisibility(View.GONE);
        vExpListView.setVisibility(View.GONE);
        vButtonsContainer.setVisibility(View.GONE);
        loader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSpinner.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                vtxtprogressbar.setVisibility(View.VISIBLE);
                mProgressBar.refreshDrawableState();
            }

            @Override
            protected Void doInBackground(Void... params) {
                if(mCustomActionsList==null){
                    build_custom_actions_list();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                vtxtprogressbar.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                vListView.setAdapter(mCustomActionAdapter);
                vListView.setVisibility(View.VISIBLE);
                if(mSpinnerList.size()>1) mSpinner.setEnabled(true);
            }
        }.execute();
    }



    private BaseAdapter mCustomActionAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mCustomActionsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mCustomActionsList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_access_dlg_item_lay, null);
                cvh.vImgIcono = (ImageView) convertView.findViewById(R.id.grx_id_img_item_acceso);
                cvh.vTxt = (TextView) convertView.findViewById(R.id.grx_texto_item_acceso);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            GrxCustomActionInfo item = (GrxCustomActionInfo) mCustomActionsList.get(position);
            cvh.vTxt.setText(item.getLabel());
            if(item.getIcon()!=null) cvh.vImgIcono.setImageDrawable( item.getIcon());
            else cvh.vImgIcono.setImageDrawable(getResources().getDrawable(R.drawable.circulo));
            return convertView;
        }

        class CustomViewHolder {
            public ImageView vImgIcono;
            public TextView vTxt;
        }
    };



    private class GrxCustomActionInfo{
        private String mLabel;
        private String mValue;
        private Drawable mIcon;
        private String mDrawableName;

        public GrxCustomActionInfo(String label, String val, Drawable icon, String drawable_name) {
            mValue  = val;
            mLabel = label;
            mIcon = icon;
            mDrawableName = drawable_name;
        }

        public String getLabel() {
            return mLabel;

        }

        public String getVal() {
            return mValue;

        }

        public String getDrawableName(){
            return mDrawableName;
        }

        public Drawable getIcon(){
            return mIcon;
        }
    }



    /*****************************************************************************/
    /******************    Activities ********************************************/
    /*****************************************************************************/


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        String label;

        Drawable dr=null;
        boolean error=false;
        Intent intent;

        ActivityInfo ai[]=mActivitiesList.get(groupPosition).getPackageInfo().activities;
        if(ai!=null) {
            intent = new Intent();
            intent.setComponent(new ComponentName(ai[childPosition].packageName,ai[childPosition].name ));
            intent.putExtra(Common.EXTRA_URI_TYPE,Common.ID_ACCESS_ACTIVITIES);
            label=Utils.get_label_from_packagename_activityname(getActivity(), ai[childPosition].packageName,ai[childPosition].name);
            intent.putExtra(Common.EXTRA_URI_LABEL,label);
            Utils.delete_grx_icon_file_from_intent(mCurrentSelectedIntent, Common.TMP_PREFIX);
            mCurrentSelectedIntent = intent;
            show_current_user_selection();
        }

        return true;
    }


    private void expand_activities_groups(){
        for(int i= 0; i<mActivitiesList.size();i++){
            vExpListView.expandGroup(i);
        }
    }

    private void collapse_activities_groups(){
        for(int i= 0; i<mActivitiesList.size();i++){
            vExpListView.collapseGroup(i);
        }
    }


    private void build_activities_list(){
        List<PackageInfo> ListaPaquetes = getActivity().getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
        PackageManager pm = getActivity().getPackageManager();
        int i=0;
        mActivitiesList = new ArrayList<GrxActivityInfo>();
        for(i=0;i<ListaPaquetes.size();i++){
            PackageInfo pi = ListaPaquetes.get(i);
            ActivityInfo ai[] = pi.activities;

            if (ai!=null && ai.length!=0){
                mActivitiesList.add(new GrxActivityInfo(pi, pi.applicationInfo.loadLabel(pm).toString()));
            }

        }

        try {
            Collections.sort(mActivitiesList, new Comparator<GrxActivityInfo>() {
                @Override
                public int compare(GrxActivityInfo A_actinfo, GrxActivityInfo actinfo) {
                    try {
                        return String.CASE_INSENSITIVE_ORDER.compare(A_actinfo.getLabel(), actinfo.getLabel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private class GrxActivityInfo{
        private PackageInfo pinfo;
        private String label;

        public GrxActivityInfo(PackageInfo pi, String etiqueta) {
            pinfo=pi;
            label=etiqueta;
        }

        public String getLabel() {
            return label;

        }

        public PackageInfo getPackageInfo() {
            return pinfo;
        }

    }


    private void ini_activities_list(){
        vListView.setVisibility(View.GONE);
        vExpListView.setVisibility(View.GONE);
        loader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSpinner.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                vExpListView.setVisibility(View.GONE);
                vtxtprogressbar.setVisibility(View.VISIBLE);
                mProgressBar.refreshDrawableState();
            }

            @Override
            protected Void doInBackground(Void... params) {
                if(mActivitiesList==null){
                    build_activities_list();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                vtxtprogressbar.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                //vListView.setAdapter(mShortCutsAdapter);
                vExpListView.setAdapter(mActivityGroupAdapter);
                vExpListView.setVisibility(View.VISIBLE);
                vButtonsContainer.setVisibility(View.VISIBLE);
                if(mSpinnerList.size()>1) mSpinner.setEnabled(true);
            }
        }.execute();
    }



    BaseExpandableListAdapter mActivityGroupAdapter = new BaseExpandableListAdapter(){

        @Override
        public Object getChild(int groupPosition, int childPosition) {

            String nombre_actividad=mActivitiesList.get(groupPosition).getPackageInfo().activities[childPosition].name;
            String nombre_paquete=mActivitiesList.get(groupPosition).getPackageInfo().packageName;
            return Utils.get_label_from_packagename_activityname(getActivity(), nombre_paquete, nombre_actividad);
            //return mActivitiesList.get(groupPosition).getPackageInfo().activities[childPosition].name;//string - label
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mActivitiesList.get(groupPosition).getPackageInfo().activities.length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mActivitiesList.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mActivitiesList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private Drawable get_activity_icon(ActivityInfo ai, PackageManager pm){
            boolean error=false;
            Drawable dr=null;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(ai.packageName, ai.name));
            ResolveInfo ri = pm.resolveActivity(intent,0);
            if(ri==null) error=true;
            if(!error){
                try{
                    dr=ri.loadIcon(pm);
                }catch (Exception e){
                    error=true;

                }
            }

            if(error) dr = getActivity().getResources().getDrawable(R.drawable.circulo);
            return dr;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            CustomViewHolder cvh;
            PackageManager pm;
            pm = getActivity().getPackageManager();
            if (convertView == null) {
                cvh = new CustomViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_access_child_activity_item_lay, null);
                cvh.vTxt = (TextView) convertView.findViewById(R.id.grx_texto_item_acceso);
                cvh.vImgIcono = (ImageView) convertView.findViewById(R.id.grx_id_img_item_acceso);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            GrxActivityInfo item = (GrxActivityInfo) mActivitiesList.get(groupPosition);
            String txt_act = (String) getChild(groupPosition,childPosition);

            cvh.vImgIcono.setImageDrawable(get_activity_icon(item.getPackageInfo().activities[childPosition],pm));
            cvh.vTxt.setText(txt_act);
            return convertView;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            CustomViewHolder cvh;
            if (convertView == null) {
                cvh = new CustomViewHolder();
                //convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, null);
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_access_dlg_item_lay, null);
                //cvh.vTxt = (TextView) convertView.findViewById(android.R.id.text1);
                cvh.vTxt = (TextView) convertView.findViewById(R.id.grx_texto_item_acceso);
                cvh.vImgIcono = (ImageView) convertView.findViewById(R.id.grx_id_img_item_acceso);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            GrxActivityInfo item = (GrxActivityInfo) mActivitiesList.get(groupPosition);
            cvh.vTxt.setTypeface(Typeface.DEFAULT_BOLD);
            cvh.vTxt.setText(item.getLabel());
            cvh.vImgIcono.setImageDrawable(item.getPackageInfo().applicationInfo.loadIcon(getActivity().getPackageManager()));
                        return convertView;
        }

        class CustomViewHolder {
            public TextView vTxt;
            public ImageView vImgIcono;
        }

    };


    /*****************************************************************************************/
    /**********************  APPS ************************************************************/
    /*****************************************************************************************/

    private void build_apps_list(){
        List<PackageInfo> ListaPaquetes = getActivity().getPackageManager().getInstalledPackages(0);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (PackageInfo packinfo : ListaPaquetes){
            intent.setPackage(packinfo.packageName);
            List<ResolveInfo> mActivitiesList = getActivity().getPackageManager().queryIntentActivities(intent, 0);
            for(ResolveInfo resolveInfo : mActivitiesList) {
                mUsuAppsList.add(resolveInfo);
            }
        }

        Collections.sort(mUsuAppsList, new ResolveInfo.DisplayNameComparator(getActivity().getPackageManager()));
        // Toast.makeText(getActivity(),String.valueOf(mUsuAppsList.size()),Toast.LENGTH_SHORT).show();
    }


    private void ini_apps_list(){
        vExpListView.setVisibility(View.GONE);
        vButtonsContainer.setVisibility(View.GONE);
        loader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSpinner.setEnabled(false);
                vListView.setVisibility(View.GONE);
                vtxtprogressbar.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.refreshDrawableState();
            }

            @Override
            protected Void doInBackground(Void... params) {
                if(mUsuAppsList==null) {
                    mUsuAppsList = new ArrayList<ResolveInfo>();
                    build_apps_list();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                vtxtprogressbar.setVisibility(View.GONE);
                vListView.setAdapter(null);
                vListView.setAdapter(mUserAppAdapter);
                mProgressBar.setVisibility(View.GONE);
                vListView.setVisibility(View.VISIBLE);
                if(mSpinnerList.size()>1) mSpinner.setEnabled(true);;
            }
        }.execute();
    }




    private BaseAdapter mUserAppAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mUsuAppsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUsuAppsList.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_access_dlg_item_lay, null);
                cvh.vImgIcono = (ImageView) convertView.findViewById(R.id.grx_id_img_item_acceso);
                cvh.vTxt = (TextView) convertView.findViewById(R.id.grx_texto_item_acceso);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            //ResolveInfo item = (ResolveInfo) this.getItem(position);
            ResolveInfo item = (ResolveInfo) mUsuAppsList.get(position);
            cvh.vTxt.setText(item.loadLabel(getActivity().getPackageManager()));
            cvh.vImgIcono.setImageDrawable(item.loadIcon(getActivity().getPackageManager()));

            return convertView;
        }

        class CustomViewHolder {
            public ImageView vImgIcono;
            public TextView vTxt;
        }
    };

    /*****************************************************************************************/



    private void cambia_icono_accion(){
        Toast.makeText(getActivity(),"SIP",Toast.LENGTH_SHORT).show();
    }


}
