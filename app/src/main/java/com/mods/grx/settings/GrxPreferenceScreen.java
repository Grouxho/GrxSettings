package com.mods.grx.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.GrxPickImage;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import android.preference.GrxCheckBoxPreference;
import android.preference.GrxDatePicker;
import android.preference.GrxMultiAppColor;
import android.preference.GrxPreferenceCategory;
import android.preference.GrxSwitchPreference;
import android.preference.GrxTimePicker;

import com.mods.grx.settings.act.GrxImagePicker;
import com.mods.grx.settings.dlgs.DlgFrGrxDatePicker;
import com.mods.grx.settings.dlgs.DlgFrGrxTimePicker;
import com.mods.grx.settings.prefssupport.CustomDependencyHelper;
import com.mods.grx.settings.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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

public class GrxPreferenceScreen extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        DlgFrGrxDatePicker.OnGrxDateSetListener,
        DlgFrGrxTimePicker.OnGrxTimeSetListener
    {

    String mCurrentScreen;
    String mCurrentSubScreen;
    String mCurrentKey;
    int mDividerHeight;


    PreferenceScreen mGrxScreen;
    public Map<String, String> mScreensTree;
    private ArrayList<String> mAuxScreenKeys = new ArrayList<String>();
    public LinkedHashMap<String, Integer> mScreenPositions;
    HashSet<String> mGroupKeyList;
    boolean mSyncMode=false;
    int mAutoIndexForKey=0;

    GrxSettingsActivity mGrxSettingsActivity =null;

    int mNumPrefs=0;

     Map<String, List<CustomDependencyHelper>> CustomDependencies = new HashMap<String, List<CustomDependencyHelper>>();

     PreferenceScreen mCurrentPreferenceScreen ;


    public GrxPreferenceScreen(){

    }

    static GrxPreferenceScreen newInstance (String screen, String subscreen, String key, int dividerheight){

        GrxPreferenceScreen prefsfragment = new GrxPreferenceScreen();
        Bundle bundle = new Bundle();
        bundle.putString(Common.EXTRA_SCREEN,screen);
        bundle.putString(Common.EXTRA_SUB_SCREEN,subscreen);
        bundle.putString(Common.EXTRA_KEY, key);
        bundle.putInt(Common.EXTRA_DIV_HEIGHT,dividerheight);
        prefsfragment.setArguments(bundle);
        return prefsfragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGrxScreen=null;
        mScreensTree = new HashMap<>();
        mScreenPositions = new LinkedHashMap<>();
        mGroupKeyList = new HashSet<>();


        if(savedInstanceState==null){
            Bundle bundle = getArguments();
            mCurrentScreen = getArguments().getString(Common.EXTRA_SCREEN);
            mCurrentSubScreen = getArguments().getString(Common.EXTRA_SUB_SCREEN);
            mCurrentKey = getArguments().getString(Common.EXTRA_KEY);
            mDividerHeight = getArguments().getInt(Common.EXTRA_DIV_HEIGHT,getResources().getInteger(R.integer.def_divider));
        }else{
            mCurrentScreen=savedInstanceState.getString(Common.EXTRA_SCREEN);
            mCurrentSubScreen=savedInstanceState.getString(Common.EXTRA_SUB_SCREEN);
            mCurrentKey=savedInstanceState.getString(Common.EXTRA_KEY);
            mDividerHeight=savedInstanceState.getInt(Common.EXTRA_DIV_HEIGHT);
        }


        if(mCurrentScreen!=null && !mCurrentScreen.isEmpty()){
            int i = getActivity().getResources().getIdentifier(mCurrentScreen, "xml", getActivity().getPackageName());
            addPreferencesFromResource(i);
            mGrxScreen = getPreferenceScreen();
            mNumPrefs=mGrxScreen.getPreferenceCount();
            String c = create_key_for_preferencescreen(mGrxScreen.getKey());
            getPreferenceScreen().setKey(c);
            mScreensTree.put(c,"");
            if(!Common.SyncUpMode && Common.GroupKeysList!=null) Common.GroupKeysList.clear();
            ini_preference_screen(mGrxScreen);

            if(mCurrentSubScreen!=null && !TextUtils.isEmpty(mCurrentSubScreen)) {
                change_screen((PreferenceScreen) getPreferenceScreen().findPreference(mCurrentSubScreen));
            }

        }

        update_all_custom_dependencies();




    }
        @Override
        public void onResume() {
            super.onResume();
            if(mGrxSettingsActivity !=null && Common.SyncUpMode) mGrxSettingsActivity.pref_screen_synchronized(mNumPrefs);
        }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Common.EXTRA_SCREEN,mCurrentScreen);
        outState.putString(Common.EXTRA_SUB_SCREEN,mCurrentSubScreen);
        outState.putString(Common.EXTRA_KEY,mCurrentKey);
        outState.putInt(Common.EXTRA_DIV_HEIGHT,mDividerHeight);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mGrxSettingsActivity = (GrxSettingsActivity) getActivity();

    }



    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference){
        if (preference.getClass().getSimpleName().equals("PreferenceScreen")) {
            PreferenceScreen preferenceScreen1 = (PreferenceScreen) preference;
            if(preferenceScreen1.getIntent()==null) change_screen((PreferenceScreen) preference);
        }
        return false;
    }

        private void change_screen(PreferenceScreen preferenceScreen){
            if(preferenceScreen!=null){
                String p_actual= getPreferenceScreen().getKey();
                mScreenPositions.put(p_actual,current_list_position());
                PreferenceScreen p = (PreferenceScreen) preferenceScreen;
                if(p.getDialog()!=null) p.getDialog().dismiss();
                setPreferenceScreen((PreferenceScreen)preferenceScreen);
                mGrxSettingsActivity.onBackKey(preferenceScreen.getTitle(), true);
                goto_last_list_position(mScreenPositions.get(getPreferenceScreen().getKey()));
                mCurrentSubScreen=getPreferenceScreen().getKey();
                mGrxSettingsActivity.onScreenChange(mCurrentSubScreen);
            }
            if (!(mCurrentKey==null || mCurrentKey.isEmpty())){
                Preference pref = getPreferenceScreen().findPreference(mCurrentKey);
                if(pref!=null) {
                    if(pref.isEnabled()) getPreferenceScreen().onItemClick(null,null,pref.getOrder(),0);
                }
                mCurrentKey=null;
            }

        }

    private void goto_last_list_position(final int pos){
        View rootView = getView();
        if(rootView!=null){
            final ListView list = (ListView) rootView.findViewById(android.R.id.list);
            if(list!=null) {
                list.clearFocus();
                list.post(new Runnable() {
                    @Override
                    public void run() {
                        list.setSelection(pos);
                    }
                });
            }
        }

    }

    private int current_list_position(){
        int pos=0;
        View rootView = getView();
        if(rootView!=null){
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            if(list!=null) pos=list.getFirstVisiblePosition();
        }
        return pos;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mGrxSettingsActivity = (GrxSettingsActivity) getActivity();
        View rootView = getView();
        ListView list = (ListView) rootView.findViewById(android.R.id.list);
        list.setDividerHeight(mDividerHeight);
        mGrxSettingsActivity.onListReady(list);
        String mScreenTemp = mScreensTree.get(getPreferenceScreen().getKey());
        if(mScreenTemp==null || mScreenTemp.isEmpty() ) mGrxSettingsActivity.onBackKey("", false);
        else {
            if(mCurrentSubScreen!=null && !mCurrentSubScreen.isEmpty()){
                mGrxSettingsActivity.onBackKey(getPreferenceScreen().getTitle(), true);
            }
        }

    }



    private void ini_preference_screen(PreferenceScreen ps){
        int nprefs = ps.getPreferenceCount();
        String nps = ps.getKey();
        mAuxScreenKeys.add(nps);
        for(int i=0;i<nprefs;i++){
		ini_preference(ps.getPreference(i), mAuxScreenKeys.get(mAuxScreenKeys.size()-1));
        }
        mAuxScreenKeys.remove(nps);
    }

    private void crea_categoria(GrxPreferenceCategory cat){
        for(int i=0;i<cat.getPreferenceCount();i++){
			ini_preference(cat.getPreference(i),mAuxScreenKeys.get(mAuxScreenKeys.size()-1));
        }
    }


    private void ini_preference(Preference pref, String pant){
        switch (pref.getClass().getSimpleName()) {

            case "PreferenceScreen":
                PreferenceScreen pst = (PreferenceScreen) pref;
                pst.setWidgetLayoutResource(R.layout.widget_accent_arrow);
                String c = create_key_for_preferencescreen(pst.getKey());
                pst.setKey(c);

                mScreensTree.put(c,pant);
                mScreenPositions.put(c,mScreensTree.size()-1);
                ini_preference_screen(pst);
                break;
            case "GrxPreferenceCategory":
                GrxPreferenceCategory pc = (GrxPreferenceCategory) pref;
                pc.setOnPreferenceChangeListener(this);
                crea_categoria(pc);
                break;
            case "GrxOpenIntent":
            case "GrxCheckBoxPreference":
            case "GrxSwitchPreference":
            case "GrxOpenActivity":
            case "GrxNumberPicker":
            case "GrxSeekBar":
            case "GrxEditText":
            case "GrxMultipleSelection":
            case "GrxSingleSelection":
            case "GrxSortList":
            case "GrxSelectSortItems":
            case "GrxDatePicker":
            case "GrxTimePicker":
            case "GrxColorPicker":
            case "GrxSelectApp":
            case "GrxAccess":
            case "GrxMultiAccess":
            case "GrxPickImage":
            case "GrxMultiAppColor":
                  pref.setOnPreferenceChangeListener(this);
            break;




            default:
                break;
        }
    }


    @Override
    public boolean onPreferenceChange(Preference pref,Object ob){
        //show_toast(pref.getClass().getSimpleName());
        switch (pref.getClass().getSimpleName()) {
            case "GrxSwitchPreference":
                GrxSwitchPreference swp = (GrxSwitchPreference) pref;
                swp.save_value_in_settings(!swp.isChecked());
                swp.send_broadcasts_and_change_group_key();
                break;
            case "GrxCheckBoxPreference":
                GrxCheckBoxPreference cbp = (GrxCheckBoxPreference) pref;
                cbp.save_value_in_settings(!cbp.isChecked());
                cbp.send_broadcasts_and_change_group_key();
                break;

    }
        update_custom_dependencies(pref.getKey() , ob);
        return true;
    }





    private String create_key_for_preferencescreen(String clave){
        String tmp = clave;
        if((clave==null)||clave.isEmpty()) {
            tmp = mCurrentScreen +   Integer.toString(mAutoIndexForKey);
            mAutoIndexForKey++;
        }
        return tmp;
    }

    private void update_group_key_list(String gcr){
        if(gcr!=null && !gcr.isEmpty()){
            if(!mGroupKeyList.contains(gcr)) mGroupKeyList.add(gcr);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            int i= lv.getListPaddingLeft();
            int t = lv.getListPaddingTop();
            int d = lv.getListPaddingRight();
            int a = lv.getListPaddingBottom();
            a+=getResources().getDimensionPixelSize(R.dimen.grx_padding_lista_preferencias);
            lv.setPadding(i,t,d,a);
        }
        return v;
    }


    private void show_toast(String msg){
        Toast.makeText(GrxSettingsApp.getContext(),msg,Toast.LENGTH_SHORT).show();
    }

    private void show_snack_msg(String msg){
        if(isAdded()) Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public boolean exec_back_pressed(){

        boolean vd = true;
        String pp=getPreferenceScreen().getKey();
        String tmp = mScreensTree.get(pp);
        mScreenPositions.put(getPreferenceScreen().getKey(),current_list_position());
        if(tmp!=null){
            if(tmp.isEmpty())vd=true;
            else {
                PreferenceScreen pstemp = (PreferenceScreen) mGrxScreen.findPreference(tmp);
                if (pstemp!=null){
                    String tmp1 = mScreensTree.get(getPreferenceScreen().getKey());
                    boolean ni;
                    if(pstemp.getKey().equals(mGrxScreen.getKey())) ni=false;
                    else ni = true;
                    mGrxSettingsActivity.onBackKey(pstemp.getTitle(), ni);
                    setPreferenceScreen(pstemp);
                    goto_last_list_position(mScreenPositions.get(getPreferenceScreen().getKey()));
                    mCurrentSubScreen=pstemp.getKey();
                    mGrxSettingsActivity.onScreenChange(mCurrentSubScreen);
                    vd=false;
                }
            }
        return vd;
        }
        return vd;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        return true;
    }

    public interface onListReady{
        public void onListReady(ListView listaprefs);
    }

    public interface onScreenChange{
        public void  onScreenChange(String ultima_pantalla);
    }

    public interface onBackKey{
        public void onBackKey(CharSequence Subtitle, boolean navicon);
    }

    public void update_divider_height(int DividerHeight) {
        mDividerHeight=DividerHeight;
        View rootView = getView();
        if (rootView != null) {
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            if (list != null) {
                list.setDividerHeight(DividerHeight);

            }
        }
    }

    /*********** image picker support - this way is easy to re-create the state..********************************/

    public void pick_image(Intent intent, int reqcode){

        getActivity().startActivityForResult(intent,reqcode);

    }

    /************  broadcast and group keys support *******************/

    public void send_broadcasts_and_change_group_key(String groupkey, boolean bc1, boolean bc2){
        if(Common.SyncUpMode || !isAdded()) return;
        if(groupkey!=null && !groupkey.isEmpty()) {
            Utils.change_group_key_value(getActivity(),groupkey);
        }
        if(bc1) Utils.send_bc1(getActivity());
        if(bc2) Utils.send_bc2(getActivity());
    }


    /*************** Dialog Fragments Listeners and support  *******************/

        public void ini_dlg_GrxDatePicker(String key, String value){
        DlgFrGrxDatePicker dlg = DlgFrGrxDatePicker.newInstance(key,value);
        dlg.setOnGrxDateSetListener(this);
        dlg.show(getFragmentManager(),Common.TAG_DLGFRGRDATEPICKER);
    }

    public void GrxDateSet(String value, String key){
        GrxDatePicker grxDatePicker= (GrxDatePicker) findPreference(key);
        if(grxDatePicker!=null) grxDatePicker.set_new_value(value);
    }

     public void ini_dlg_GrxTimePicker(String key, int value){
            DlgFrGrxTimePicker dlg = DlgFrGrxTimePicker.newInstance(key, value);
            dlg.setOnGrxTimeSetListener(this);
            dlg.show(getFragmentManager(),Common.TAG_DLGFRGRTIMEPICKER);
        }
    public void GrxTimeSet(int value, String key){
        GrxTimePicker pref = (GrxTimePicker) findPreference(key);
        if(pref!=null) pref.set_new_value(value);
    }

    public Preference find_callback(String key){
        return findPreference(key);
    }

        /********** SYNC UP SUPPORT  **************************************************/

        public void add_group_key_for_syncup(String groupkey){
            if(groupkey!=null && !groupkey.isEmpty()){
                if(!Common.GroupKeysList.contains(groupkey)) {
                    Common.GroupKeysList.add(groupkey);
                    if(isAdded()) show_toast(groupkey);
                }
            }
        }

       /***************** CUSTOM DEPENDENCIES *****************************/

    public interface CustomDependencyListener{

        void OnCustomDependencyChange(boolean state);

    }


    public void add_custom_dependency(GrxPreferenceScreen.CustomDependencyListener dependencyListener, String rule, String separator){
        if(rule == null || rule.isEmpty()) return;
        List<CustomDependencyHelper> dependencylisteners;
        CustomDependencyHelper customDependencyHelper = new CustomDependencyHelper(dependencyListener, rule, separator);
        String dependency_key = customDependencyHelper.get_custom_dependency_key();
        if(CustomDependencies.containsKey(dependency_key)){
             dependencylisteners = CustomDependencies.get(dependency_key);
        }else dependencylisteners = new ArrayList<CustomDependencyHelper>();
        dependencylisteners.add(customDependencyHelper);
        CustomDependencies.put(dependency_key,dependencylisteners);
    }


    private String get_string_value_for_dependency_key(int type, String key){

        switch (type){
            case 0: return String.valueOf(Common.sp.getInt(key,-1));
            case 1:
            case 2: return Common.sp.getString(key,"");
            case 3: return Common.sp.getBoolean(key,false) ? "true" : "false";
        }
        return null;
    }

    public void update_all_custom_dependencies(){

        for (Map.Entry<String, List<CustomDependencyHelper>> entry : CustomDependencies.entrySet()) {
            String dependency_key = entry.getKey();
            List<CustomDependencyHelper> dependencyHelpers = entry.getValue();
            String dependency_key_value = get_string_value_for_dependency_key(dependencyHelpers.get(0).get_dependency_type(), dependency_key);
            int num_listeners = dependencyHelpers.size();
            for(int i = 0; i< num_listeners;i++){
                CustomDependencyHelper customDependencyHelper = dependencyHelpers.get(i);
                customDependencyHelper.get_listener().OnCustomDependencyChange(customDependencyHelper.listener_should_be_enabled(dependency_key_value));
            }

        }

    }


      private String get_string_value_for_dependency_key(int type, Object value){
          switch (type){
              case 0: return String.valueOf((int) value);
              case 1:
              case 2: String tmp = (String) value;
                      if(tmp==null) tmp = "";
                      return tmp;
              case 3: return (boolean) value ? "true" : "false";
          }
          return null;

      }

     public void update_custom_dependencies(String key, Object ob){
         if(key==null || key.isEmpty()) return;
         List<CustomDependencyHelper> dependencyHelpers = CustomDependencies.get(key);
         if(dependencyHelpers==null) return;

         int type = dependencyHelpers.get(0).get_dependency_type();
         String dependency_key_value = get_string_value_for_dependency_key(type,ob);
         int num_listeners = dependencyHelpers.size();
         for(int i = 0; i< num_listeners;i++){
             CustomDependencyHelper customDependencyHelper = dependencyHelpers.get(i);
             customDependencyHelper.get_listener().OnCustomDependencyChange(customDependencyHelper.listener_should_be_enabled(dependency_key_value));
         }



     }

    /*************************************************************************/

     public void imager_picker_result(Intent data, int requestcode){

         String key = data.getStringExtra(Common.TAG_DEST_FRAGMENT_NAME_EXTRA_KEY);
         if(key==null || key.isEmpty()) return;

         GrxPickImage grxPickImage = (GrxPickImage) getPreferenceScreen().findPreference(key);
         if(grxPickImage==null) return;

         switch (requestcode){
             case Common.REQ_CODE_GALLERY_IMAGE_PICKER_JUST_URI:
                    grxPickImage.set_new_value(data.getData().toString());

                 break;
             case Common.REQ_CODE_GALLERY_IMAGE_PICKER_CROP_CIRCULAR:
                 String sFile = data.getStringExtra(GrxImagePicker.S_DIR_IMG);
                 File file = new File(sFile);
                 if(file!=null) {
                         Uri uri = Uri.fromFile(file);
                         grxPickImage.set_new_value(uri.toString());
                     }
                 break;
         }
     }



}
