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

package com.mods.grx.settings;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;

import com.appeaser.sublimenavigationviewlibrary.OnNavigationMenuEventListener;
import com.appeaser.sublimenavigationviewlibrary.SublimeBaseMenuItem;
import com.appeaser.sublimenavigationviewlibrary.SublimeGroup;
import com.appeaser.sublimenavigationviewlibrary.SublimeMenu;
import com.appeaser.sublimenavigationviewlibrary.SublimeNavMenuView;
import com.appeaser.sublimenavigationviewlibrary.SublimeNavigationView;

import com.appeaser.sublimenavigationviewlibrary.SublimeTextWithBadgeMenuItem;
import com.mods.grx.settings.act.GrxImagePicker;
import com.mods.grx.settings.adapters.AdapterBackups;
import com.mods.grx.settings.fab.ObservableScrollView;
import com.mods.grx.settings.fragments.GrxInfoFragment;
import com.mods.grx.settings.utils.GrxImageHelper;
import com.mods.grx.settings.dlgs.DlgFrGrxAjustes;
import com.mods.grx.settings.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



/*constantes*/



public class GrxSettingsActivity extends AppCompatActivity implements
        DlgFrGrxAjustes.OnDlgFrGrxAjustesListener,
        GrxPreferenceScreen.onListReady,
        GrxPreferenceScreen.onBackKey,
        GrxPreferenceScreen.onScreenChange,
        GrxInfoFragment.onSlidingTabChanged
        {


    /*svn*/
    final String SS_KEY_MENU_1 = "ss.key.menu.1";
    final String SS_KEY_MENU_2 = "ss.key.menu.2";
    final String SS_KEY_LAST_ITEM = "ss.key.lastitem";
    final String SS_KEY_CURRENT_MENU = "ss.key.current.menu";

    private SublimeNavigationView mSVN;  //SublimeNavigationView
    private SublimeNavMenuView vSvnMenu; //Sublime Menu View
    FloatingActionButton mFabSvn; //Standard Floating action Buttom in SNV

    //Padding for Expand - Collapse Buttoms - SNV main menu
    private int mPaddinOnVGrButtons;
    private int mPaddinOffVGrButtons;
    private LinearLayout vExpandCollapseButtons;
    private LinearLayout vExpandButton;
    private LinearLayout vCollapseButton;


    private int mCurrentMenu;
    private SublimeMenu mOptionsMenu, mConfigMenu;

    private DrawerLayout mDrawer;
    private boolean mDrawerRight;
    private com.mods.grx.settings.fab.FloatingActionButton fab;

    public Toolbar mToolbar;


    GrxPreferenceScreen PrefScreenFragment;

    private boolean mExpandCollapseVisible;

    private SublimeBaseMenuItem mCurrentMenuItem;


    private boolean mShowFab;
    private int mFabPosition;
    private boolean mRememberScreen;
    private boolean mGroupsExpanded;
    private boolean mShowExpandCollapseButtons;
    private int mDividerHeight;

    private android.support.v4.widget.DrawerLayout.LayoutParams posicion;

    private String mCurrentScreen;
    private String mCurrentSubScreen;

    private int mSnackBarBgColor;
    private int mNumberOfGroups=0;


    private boolean mExitConfirmation = Common.DEF_VAL_EXIT_CONFIRM;

    private HashSet<String> ListOfgcr;

    // sync

    public Map<Integer,String> ResXML;
    public int mNumSyncPrefs=0;
    public int mNumSyncScreens=0;


    //backup restore

    EditText mEditText;
    private Dialog mRestoreDialog = null;

    private  int mTheme;
    private String mSubScreenIntent=null;
    private String mGrxKeyIntent=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ini_sharedpreferences_dirs_and_aux_utils();
        set_theme();
        set_task_description();
        setContentView(R.layout.grx_nav_layout);

        mOptionsMenu=null;
        mCurrentMenuItem = null;
        mConfigMenu=null;
        mCurrentMenu=-1;

        //check sync mode if(synmod





        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(SS_KEY_MENU_1)) {
                mOptionsMenu = savedInstanceState.getParcelable(SS_KEY_MENU_1);
            }

            if (savedInstanceState.containsKey(SS_KEY_MENU_2)) {
                mConfigMenu = savedInstanceState.getParcelable(SS_KEY_MENU_2);

            }

            if (savedInstanceState.containsKey(SS_KEY_LAST_ITEM)) {
                mCurrentMenuItem = savedInstanceState.getParcelable(SS_KEY_LAST_ITEM);
            }

            if (savedInstanceState.containsKey(SS_KEY_CURRENT_MENU)) {
                mCurrentMenu = savedInstanceState.getInt(SS_KEY_CURRENT_MENU,-1);
            }

        }
        mCurrentScreen="";
        mCurrentSubScreen="";
        int mode = Common.INI_MODE_NORMAL;
        if(savedInstanceState!=null) mode= Common.INI_MODE_INSTANCE;
        else {
            if(getIntent()!=null){
                mCurrentScreen =getIntent().getStringExtra(Common.EXTRA_SCREEN);
                mCurrentSubScreen=getIntent().getStringExtra(Common.EXTRA_SUB_SCREEN);
                if(mCurrentSubScreen==null) mCurrentSubScreen="";
                mSubScreenIntent=mCurrentSubScreen;
                if(mCurrentScreen!=null) mode = Common.INI_MODE_INTENT;
                else mCurrentScreen="";
                mGrxKeyIntent=getIntent().getStringExtra(Common.EXTRA_KEY);

            }
        }

        mDrawerRight=Common.sp.getBoolean(Common.S_APPOPT_DRAWER_POS, Common.DEF_VAL_DRAWER_POS);
        ini_toolbar();

        ini_svn();
        ini_menus_svn();
        ini_fab_svn();
        ini_nav_svn();

        ini_main_fab();

        read_values_congif_menu_svn();
        update_main_fab_visibility();
        update_main_fab_position();
        update_menu_groups_svn();
        update_text_fab_position();
        update_text_selected_theme();
        update_text_divider_height();
        update_nav_header_bg();


        select_current_menu_and_screen(mode);
        update_svn_groups_buttons();
        if(mCurrentMenuItem!=null && !mCurrentScreen.isEmpty()) {
            mCurrentMenuItem.setChecked(true);
            mCurrentSubScreen="";
            change_screen_title(mCurrentMenuItem,mCurrentScreen);
            if(savedInstanceState==null) change_screen(mCurrentMenuItem,mCurrentScreen);
            else PrefScreenFragment = (GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
            if(mode!= Common.INI_MODE_INSTANCE) show_snack_message(mCurrentMenuItem.getTitle().toString());
        }
        if(savedInstanceState==null) Utils.delte_grx_tmp_files(Common.CacheDir); //let´s clean existing tmp files

        sync_preferences();

        if(mCurrentScreen.isEmpty()) show_info_fragment();
    }

    private void show_info_fragment(){
        GrxInfoFragment info_fragment= new GrxInfoFragment();
        getFragmentManager().beginTransaction().replace(R.id.content,info_fragment, Common.TAG_INFOFRAGMENT).commit();
        String title = getResources().getString(R.string.gs_rom_name);
        if(getSupportActionBar()!=null) getSupportActionBar().setTitle(title==null ? "?":title);

    }

    private void set_task_description(){
        //lets make nicer how the app in showed in recents.. this code shows recents_icon.png in recents and fix the bg color
        TypedArray a = this.getTheme().obtainStyledAttributes( new int[] {R.attr.colorPrimary});
        int bgcolor =a.getColor(0,0);
        a.recycle();
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name),
                GrxImageHelper.drawableToBitmap(getDrawable(R.drawable.recents_icon)),bgcolor);
        setTaskDescription(taskDescription);

    }

    private void set_theme(){

        mTheme = Common.sp.getInt(Common.S_APPOPT_USER_SELECTED_THEME,getResources().getInteger(R.integer.def_theme));

            switch (mTheme){
                case Common.INT_ID_THEME_BASE_LIGHT:
                     setTheme(R.style.AppTheme);
                        break;
                case Common.INT_ID_THEME_BASE_DARK:
                     setTheme(R.style.Theme_Base_Dark);
                    break;
                case Common.INT_ID_THEME_GREEN_LIGHT:
                    setTheme(R.style.Theme_Green_Light);
                    break;
                case Common.INT_ID_THEME_GREEN_DARK:
                    setTheme(R.style.Theme_Green_Dark);
                    break;
                case Common.INT_ID_THEME_RED_LIGHT:
                    setTheme(R.style.Theme_Red_Light);
                    break;
                case Common.INT_ID_THEME_RED_DARK:
                    setTheme(R.style.Theme_Red_Dark);
                    break;
                case Common.INT_ID_THEME_ORANGE_LIGHT:
                    setTheme(R.style.Theme_Orange_Light);
                    break;
                case Common.INT_ID_THEME_ORANGE_DARK:
                    setTheme(R.style.Theme_Orange_Dark);
                    break;
                case Common.INT_ID_THEME_PURPLE_LIGHT:
                    setTheme(R.style.Theme_Purple_Light);
                    break;
                case Common.INT_ID_THEME_PURPLE_DARK:
                    setTheme(R.style.Theme_Purple_Dark);
                    break;
                case Common.INT_ID_THEME_BROWN_LIGHT:
                    setTheme(R.style.Theme_Brown_Light);
                    break;
                case Common.INT_ID_THEME_YELLOW_DARK:
                    setTheme(R.style.Theme_Yellow_Dark);
                    break;
                case Common.INT_ID_THEME_GREEN_ORANGE_LIGHT:
                    setTheme(R.style.Theme_GreenOrange_Light);
                    break;

                default:
                    setTheme(R.style.AppTheme);
                    break;
            }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_KEY_MENU_1, mOptionsMenu);
        outState.putParcelable(SS_KEY_MENU_2, mConfigMenu);
        outState.putParcelable(SS_KEY_LAST_ITEM, mCurrentMenuItem);
        outState.putInt(SS_KEY_CURRENT_MENU, mCurrentMenu);
        if(mCurrentSubScreen==null) mCurrentSubScreen="";
        if(mGrxKeyIntent==null) mGrxKeyIntent="";
        getIntent().removeExtra(Common.EXTRA_SCREEN);
        getIntent().removeExtra(Common.EXTRA_SUB_SCREEN);
        getIntent().removeExtra(Common.EXTRA_KEY);

    }


    @Override
    public void onResume() {
        super.onResume();
        if(mCurrentMenuItem!=null) change_screen_title(mCurrentMenuItem, mCurrentScreen);


    }

    private void toolbar_subtitle(CharSequence Subtitle){
        if(getSupportActionBar()!=null){
            if(Subtitle!=null) getSupportActionBar().setSubtitle(Subtitle);
            else getSupportActionBar().setSubtitle("");
        }
    }


    @Override
    public void onBackKey(CharSequence Subtitle, boolean navicon){
        toolbar_subtitle(Subtitle);
     }

    @Override
    public void onScreenChange(String last_sub_screen){
        mCurrentSubScreen = last_sub_screen;
    }

    @Override
    public void SetObservableScrollView(ObservableScrollView observableScrollView){
        if(fab!=null && observableScrollView!=null){
            fab.attachToScrollView(observableScrollView);
            fab.show(true);
        }
    }

    @Override
    public void onListReady(ListView listaprefs){
        if(listaprefs!=null && fab!=null) {
            fab.attachToListView(listaprefs);
            fab.show(true);
        }
    }


    private void change_screen(SublimeBaseMenuItem menuItem, String screen_name) {
            toolbar_subtitle("");
            String subscreen=(mSubScreenIntent==null) ? mCurrentSubScreen : mSubScreenIntent;
            String key = (mGrxKeyIntent==null) ? "" : mGrxKeyIntent;
            mSubScreenIntent=null;
            mGrxKeyIntent=null;
            PrefScreenFragment = GrxPreferenceScreen.newInstance(mCurrentScreen,subscreen,key,mDividerHeight);
            getFragmentManager().beginTransaction().replace(R.id.content,PrefScreenFragment, Common.TAG_PREFSSCREEN_FRAGMENT).commit();
    }



    //ini menu item and screen
    private void select_current_menu_and_screen(int mode){

        if(mCurrentMenu==0) mSVN.switchMenuTo(mOptionsMenu);
        else mSVN.switchMenuTo(mConfigMenu);

        switch (mode){
            case Common.INI_MODE_INSTANCE:
                if(mCurrentMenuItem!=null) {
                    int tmp = mCurrentMenuItem.getItemId();
                    mCurrentMenuItem = mOptionsMenu.getMenuItem(tmp);
                    mCurrentScreen = getResources().getResourceEntryName(mCurrentMenuItem.getItemId());
                }
                break;
            case Common.INI_MODE_INTENT:
                    mCurrentMenuItem = find_menu_item(mCurrentScreen);
                    if(mCurrentMenuItem == null) mCurrentScreen="";
                break;
            case Common.INI_MODE_NORMAL:
                if(mRememberScreen) {
                    mCurrentScreen=read_last_screen();
                    if(!mCurrentScreen.isEmpty()) mCurrentMenuItem = find_menu_item(mCurrentScreen);
                }
                break;
        }

        SublimeGroup group;
        ArrayList<SublimeGroup> g = mOptionsMenu.grupos_menu();
        if(g!=null) mNumberOfGroups = g.size();
        if(mCurrentMenuItem!=null) {
            SublimeGroup svng = mOptionsMenu.getGroup(mCurrentMenuItem.getGroupId());
            if(svng!=null){
                if(svng.isCollapsed()) svng.setStateCollapsed(false);
            }
        }

    }




    private void change_screen_title(SublimeBaseMenuItem menuItem, String screen){
        String tmp = "-";
        String title=null;
        if(menuItem!=null) title=menuItem.getTitle().toString();
        if(title!=null && !title.isEmpty()) tmp=title;
        getSupportActionBar().setTitle(tmp);

    }


    private SublimeBaseMenuItem find_menu_item(String screen){
        int i = getResources().getIdentifier(mCurrentScreen, "id", getApplicationContext().getPackageName());
        if(i!=0) return mOptionsMenu.getMenuItem(i);
        return null;
    }

    private void ini_menus_svn(){
        //there is a problem in sublime not saving correctly svn state in some circumstances (f.e. changing fonts..) -> unmarshalling problems because of class not found
        //so the menus state is saved in the activity.
        if(mOptionsMenu==null){ //no saved instance state
            mCurrentMenu=0;
            if(!getResources().getBoolean(R.bool.DEMO)) {  //if not demo
                mOptionsMenu=mSVN.getMenu(); // clear list
                if(mOptionsMenu!=null) mOptionsMenu.clear();
                mSVN.switchMenuTo(R.menu.menu_grx_nav); //create rom options menu
            }
            mOptionsMenu = mSVN.getMenu();
            mSVN.switchMenuTo(R.menu.menu_grx_conf_nav); //create config menu
            mConfigMenu = mSVN.getMenu();
            delete_not_auth_options();
        }else {  //saved instance state -> clear default xml sublime menu
            SublimeMenu tmp = mSVN.getMenu();
            if(tmp!=null) tmp.clear();
        }
    }

    private void delete_not_auth_options(){
        if(!getResources().getBoolean(R.bool.allow_user_panel_header_bg)){
            mConfigMenu.removeItem(R.id.grx_header_svn_back);
        }

        if(!getResources().getBoolean(R.bool.allow_user_select_theme)){
            mConfigMenu.removeItem(R.id.grx_mid_theme);
        }

        String mTabsLayouts[]=getResources().getStringArray(R.array.tabs_layouts);
        if(mTabsLayouts==null){
            mConfigMenu.removeItem(R.id.grx_mid_rom_info);
        }

    }


    private void ini_sharedpreferences_dirs_and_aux_utils(){
        try {
            Common.sp = getBaseContext().createPackageContext(getPackageName(),CONTEXT_IGNORE_SECURITY).getSharedPreferences(getPackageName()+"_preferences",MODE_PRIVATE);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            finish();
        }
        Common.IconsDir= Environment.getExternalStorageDirectory().toString() + File.separator + getString(R.string.grx_dir_datos_app)+File.separator+getString(R.string.grx_ico_sub_dir);
        Common.CacheDir = getCacheDir().getAbsolutePath();
        Utils.create_folder(Common.IconsDir);

        Common.BackupsDir = Environment.getExternalStorageDirectory().toString() + File.separator + getString(R.string.grx_dir_datos_app)+File.separator+"backups";
        Utils.create_folder(Common.BackupsDir);

        int iconsize = getResources().getDimensionPixelSize(R.dimen.icon_size_in_prefs);
        Common.AndroidIconParams = new LinearLayout.LayoutParams(iconsize, iconsize);
        Common.GroupKeysList=new HashSet<>();


    }

    private void show_snack_message(String mensaje){
        Snackbar snackbar = Snackbar.make(mToolbar, mensaje, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        snackbar.getView().setBackgroundColor(mSnackBarBgColor);
        snackbar.show();

    }

    private void save_last_screen(){
        if(mCurrentScreen==null) mCurrentScreen="";
        Common.sp.edit().putString(Common.S_AUX_LAST_SCREEN,mCurrentScreen).commit();
    }

    private String read_last_screen(){
        return Common.sp.getString(Common.S_AUX_LAST_SCREEN,"");
    }

    private void ini_toolbar(){
        if(mDrawerRight)    posicion = new android.support.v4.widget.DrawerLayout.LayoutParams ((int) getResources().getDimension(R.dimen.ancho_panel), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT);
        else posicion = new android.support.v4.widget.DrawerLayout.LayoutParams ((int) getResources().getDimension(R.dimen.ancho_panel), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mDrawer.isDrawerVisible(mSVN) ) {
                    if(mDrawerRight) mDrawer.closeDrawer(Gravity.RIGHT);
                    else mDrawer.closeDrawer(Gravity.LEFT);
                }else {
                    if(mDrawerRight) mDrawer.openDrawer(Gravity.RIGHT);
                    else mDrawer.openDrawer(Gravity.LEFT);
                }
            }

        });


        TypedArray a = this.getTheme().obtainStyledAttributes( new int[] {R.attr.snackbar_bg});
        mSnackBarBgColor=a.getColor(0,0);
        a.recycle();

    }

    private void ini_main_fab(){

        fab = (com.mods.grx.settings.fab.FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.openDrawer(mSVN);
            }
        });

    }

    private void read_values_congif_menu_svn(){

        mSVN.setLayoutParams(posicion);

        // drawer.setLayoutParams(posicion);

        mConfigMenu.getMenuItem(R.id.grx_mid_drawer_dcha).setChecked(Common.sp.getBoolean(Common.S_APPOPT_DRAWER_POS, Common.DEF_VAL_DRAWER_POS));

        mShowFab = Common.sp.getBoolean(Common.S_APPOPT_SHOW_FAV, Common.DEF_VAL_SHOW_FAB);
        mConfigMenu.getMenuItem(R.id.grx_mid_mostrar_boton).setChecked(mShowFab);

        mRememberScreen = Common.sp.getBoolean(Common.S_APPOPT_REMEMBER_SCREEN, Common.DEF_VAL_REMENBER_SCREEN);
        mConfigMenu.getMenuItem(R.id.grx_mid_recordar_pantalla).setChecked(mRememberScreen);

        mGroupsExpanded=Common.sp.getBoolean(Common.S_APPOPT_MENU_GROUPS_ALWAYS_OPEN, Common.DEF_VAL_GROUPS_ALWAYS_OPEN);
        mConfigMenu.getMenuItem(R.id.grx_mid_grupos_abiertos).setChecked(mGroupsExpanded);

        mShowExpandCollapseButtons=Common.sp.getBoolean(Common.S_APPOPT_SHOW_COLLAPSE_EXPAND_BUTTONS, Common.DEF_VAL_SHOW_COL_EXP_BUTTONS );
        mConfigMenu.getMenuItem(R.id.grx_mid_botones_grupos).setChecked(mShowExpandCollapseButtons);

        if(mGroupsExpanded){
            mConfigMenu.getMenuItem(R.id.grx_mid_botones_grupos).setEnabled(false);
        }else mConfigMenu.getMenuItem(R.id.grx_mid_botones_grupos).setEnabled(true);

        mExitConfirmation = Common.sp.getBoolean(Common.S_APPOPT_EXIT_CONFIRM, Common.DEF_VAL_EXIT_CONFIRM);

        mConfigMenu.getMenuItem(R.id.grx_mid_confirmar_salir).setChecked(mExitConfirmation);

        mFabPosition = Common.sp.getInt(Common.S_APPOPT_FAB_POS, Common.DEF_VAL_FAB_POS);

        mDividerHeight =  Common.sp.getInt(Common.S_APPOPT_DIV_HEIGHT, getResources().getInteger(R.integer.def_divider));

        Common.cDividerHeight=mDividerHeight;

    }


    private void ini_nav_svn(){

        mSVN.setNavigationMenuEventListener(new OnNavigationMenuEventListener() {

            @Override
            public boolean onNavigationMenuEvent(Event event, SublimeBaseMenuItem menuItem) {
                String opcion;
                boolean estado;
                if (mCurrentMenu==0){
                    switch (event) {
                        case GROUP_EXPANDED:
                            break;
                        case GROUP_COLLAPSED:
                            break;
                        default:
                            String tmp_screen_name = getResources().getResourceEntryName(menuItem.getItemId());
                            if(mCurrentScreen.equals(tmp_screen_name)== false){
                                mCurrentScreen=tmp_screen_name;
                                if(mCurrentMenuItem!=null) mCurrentMenuItem.setChecked(false);
                                menuItem.setChecked(true);
                                mCurrentMenuItem = menuItem;
                                mDrawer.closeDrawers();
                                save_last_screen();
                                change_screen_title(mCurrentMenuItem,mCurrentScreen);
                                change_screen(mCurrentMenuItem,mCurrentScreen);
                                show_snack_message(mCurrentMenuItem.getTitle().toString());
                            }
                            break;
                    }
                }
                else {

                    switch (event){

                        case CHECKED:
                            opcion = getResources().getResourceEntryName(menuItem.getItemId());
                            estado = true;
                            update_config_menu_checkbox(opcion, estado);
                            break;
                        case UNCHECKED:
                            opcion = getResources().getResourceEntryName(menuItem.getItemId());
                            estado = false;
                            update_config_menu_checkbox(opcion, estado);
                            break;
                        default:
                            opcion = getResources().getResourceEntryName(menuItem.getItemId());
                            switch (opcion){
                                case "grx_mid_theme":
                                    dlg_set_theme();;
                                    break;

                                case "grx_header_svn_back":
                                    dlg_set_svn_header_bg();
                                    break;
                                case "grx_mid_rom_info":
                                    show_info_fragment();
                                    if(mDrawer.isDrawerVisible(mSVN)) mDrawer.closeDrawer(mSVN);
                                    mCurrentScreen="";
                                    save_last_screen();
                                    break;
                                default:break;
                            }


                            if (opcion.equals("grx_mid_posicion_boton")) {

                                dlg_fav_pos();
                            }
                            else {
                                if (opcion.equals("grx_mid_ancho_divider")) {
                                    dlg_divider_height();
                                }
                            }
                            break;
                    }
                }

                return true;
            }
        });
    }


    private void update_divider_height(){
        if(PrefScreenFragment!=null) PrefScreenFragment.update_divider_height(mDividerHeight);
    }

    private void dlg_fav_pos(){
        DlgFrGrxAjustes dlg = DlgFrGrxAjustes.newInstance(Common.INT_ID_APPDLG_FAV_POS);
        getFragmentManager().beginTransaction().add(dlg, Common.S_APPDLG_FAV_POS).commit();
    }


    private void dlg_exit(){
        DlgFrGrxAjustes dlg = DlgFrGrxAjustes.newInstance(Common.INT_ID_APPDLG_EXIT_CONFIRM);
        getFragmentManager().beginTransaction().add(dlg, Common.S_APPDLG_EXIT_CONFIRM).commit();
    }

    private void dlg_divider_height(){
        DlgFrGrxAjustes dlg = DlgFrGrxAjustes.newInstance(Common.INT_ID_APPDLG_DIV_HEIGHT);
        getFragmentManager().beginTransaction().add(dlg, Common.S_APPDLG_DIV_HEIGHT).commit();
    }


    private void dlg_set_theme(){
        DlgFrGrxAjustes dlg = DlgFrGrxAjustes.newInstance(Common.INT_ID_APPDLG_SET_THEME);
        getFragmentManager().beginTransaction().add(dlg, Common.S_APPDLG_SET_THEME).commit();
    }

    private void dlg_choose_panel_header_bg(){
        DlgFrGrxAjustes dlg = DlgFrGrxAjustes.newInstance(Common.INT_ID_APPDLG_SET_BG_PANEL_HEADER);
        getFragmentManager().beginTransaction().add(dlg, Common.S_APPDLG_SET_BG_PANEL_HEADER).commit();
    }




    private void restart_app() {
        finish();  //hay que reiniciar para que en ambas orientaciones pille bien el cambio de posición y las preferencias restauradas
        this.overridePendingTransition(0,R.animator.fadeout);
        startActivity(new Intent(this, GrxSettingsActivity.class));
        this.overridePendingTransition(R.animator.fadein, 0);
    }

    private void update_config_menu_checkbox(String opcion, boolean estado){

        switch(opcion){
            case "grx_mid_drawer_dcha":
                Common.sp.edit().putBoolean(Common.S_APPOPT_DRAWER_POS,estado).commit();
                restart_app();
                break;

            case "grx_mid_mostrar_boton":
                Common.sp.edit().putBoolean(Common.S_APPOPT_SHOW_FAV,estado).commit();
                mShowFab=estado;
                update_main_fab_visibility();
                break;

            case "grx_mid_recordar_pantalla":
                Common.sp.edit().putBoolean(Common.S_APPOPT_REMEMBER_SCREEN,estado).commit();
                mRememberScreen=estado;
                break;
            case "grx_mid_grupos_abiertos":
                Common.sp.edit().putBoolean(Common.S_APPOPT_MENU_GROUPS_ALWAYS_OPEN,estado).commit();
                mGroupsExpanded=estado;
                enable_disable_groups_buttons_option();
                update_svn_groups_buttons();
                break;
            case "grx_mid_botones_grupos":
                mShowExpandCollapseButtons=estado;
                Common.sp.edit().putBoolean(Common.S_APPOPT_SHOW_COLLAPSE_EXPAND_BUTTONS,estado).commit();
                update_svn_groups_buttons();
                break;
            case "grx_mid_confirmar_salir":
                Common.sp.edit().putBoolean(Common.S_APPOPT_EXIT_CONFIRM,estado).commit();
                mExitConfirmation=estado;
                break;

            default: break;
        }
    }


    public void dlg_set_svn_header_bg(){
        if(mDrawer.isDrawerVisible(mSVN)) mDrawer.closeDrawer(mSVN);
        String nav_header_file = Common.IconsDir + File.separator+getString(R.string.gs_nav_header_bg_image_name);
        File f = new File(nav_header_file);
        if(!f.exists()){
            init_panel_header_bg_picker();
        }else {
            dlg_choose_panel_header_bg();
        }
    }

    public void do_fragment_gallery_image_picker(Intent intent){

        intent.createChooser(intent, getResources().getString(R.string.gs_selecc_image_usando));
        startActivityForResult(intent,Common.REQ_CODE_GALLERY_IMAGE_PICKER_FROM_FRAGMENT);

    }

    private void init_panel_header_bg_picker(){
        Intent intent = new Intent(this, GrxImagePicker.class);
        int ancho = getResources().getDimensionPixelSize(R.dimen.snv_navigation_max_width);
        int alto = getResources().getDimensionPixelSize(R.dimen.svn_nav_header_height);

        intent = GrxImageHelper.intent_avatar_img(intent, ancho, alto);
        intent.createChooser(intent, getResources().getString(R.string.gs_selecc_image_usando));
        startActivityForResult(intent,Common.REQ_CODE_GALLERY_IMAGE_PICKER_FROM_GRXAJUSTES);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        boolean error=false;
        if(resultCode== Activity.RESULT_OK) {
            switch (requestCode){
                case Common.REQ_CODE_GALLERY_IMAGE_PICKER_FROM_FRAGMENT:
                    String dest_fragment_tag = data.getStringExtra(Common.TAG_DEST_FRAGMENT_NAME_EXTRA_KEY);
                    DialogFragment dialogFragment = null;
                    if(dest_fragment_tag!=null) {
                        dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag(dest_fragment_tag);
                        if(dialogFragment!=null) dialogFragment.onActivityResult(requestCode,resultCode,data);
                    }
                    break;
                case Common.REQ_CODE_GALLERY_IMAGE_PICKER_FROM_GRXAJUSTES:
                    String sFile = data.getStringExtra(GrxImagePicker.S_DIR_IMG);
                    if(sFile!=null) {
                        save_and_configure_header_image(sFile);
                    }else show_snack_message("IMG ERROR!!");
                    break;
                case Common.REQ_CODE_GALLERY_IMAGE_PICKER_JUST_URI:
                case Common.REQ_CODE_GALLERY_IMAGE_PICKER_CROP_CIRCULAR:
                    if(PrefScreenFragment!=null){
                        PrefScreenFragment.imager_picker_result(data,requestCode);
                    }

                    break;
                default:
                    break;
            }
        }else super.onActivityResult(requestCode,resultCode,data);
    }


    private void save_and_configure_header_image(String header_img){

        Bitmap bitmap = GrxImageHelper.load_bmp_image(header_img);
        String nav_header_file = Common.IconsDir + File.separator+getString(R.string.gs_nav_header_bg_image_name);
        GrxImageHelper.save_png_from_bitmap(bitmap,nav_header_file);
        Utils.delete_file(header_img);

        update_nav_header_bg();
    }


    private void update_nav_header_bg(){
        boolean color_bg = true;
        if(getResources().getBoolean(R.bool.allow_user_panel_header_bg)){
            String nav_header_file = Common.IconsDir + File.separator+getString(R.string.gs_nav_header_bg_image_name);
            File f = new File(nav_header_file);
            if(f.exists()){
                update_text_panel_header_bg(getString(R.string.gs_image));
                Bitmap bitmap = GrxImageHelper.load_bmp_image(nav_header_file);
                if(bitmap!=null){
                    FrameLayout header = (FrameLayout) mSVN.getHeaderView().findViewById(R.id.navigation_header_container);
                    if(header!=null) {
                        header.setBackground(new BitmapDrawable(bitmap));
                        color_bg = false;
                    }
                }
            }else update_text_panel_header_bg(getString(R.string.gs_default));
        }
        if(color_bg){
            FrameLayout header = (FrameLayout) mSVN.getHeaderView().findViewById(R.id.navigation_header_container);
            if(header!=null){
                TypedArray a = this.getTheme().obtainStyledAttributes( new int[] {R.attr.svn_nav_header_bg});
                if(header!=null) header.setBackgroundColor(a.getColor(0,0));
                a.recycle();
            }
        }
    }

    private void ini_svn(){
        mSVN = (SublimeNavigationView) findViewById(R.id.navigation_view);
        vSvnMenu = mSVN.getMenuView(); //hay que aplicar el padding en SublimeNavMenuView para poder activar o no los botones de grupo y que no se monte
        vExpandCollapseButtons = (LinearLayout) findViewById(R.id.botones);
        mPaddinOnVGrButtons=mSVN.getPaddingBottom(); //truco para no convertir he dejado inicialmente el padding que me interesa y lo consigo con el método getpadding
        vSvnMenu = mSVN.getMenuView(); //hay que aplicar el padding en SublimeNavMenuView para poder activar o no los botones de grupo y que no se monte
        mPaddinOffVGrButtons = vSvnMenu.getPaddingBottom(); //padding inicial a aplicar al menuview del sublime para cuando se desactivan los botones de grupo
        mSVN.setPadding(0,0,0,0); //dejamos la view como debe, con padding 0. En el xml dejé lo que quería.. por vaguería, je je..
        vExpandCollapseButtons.setPadding(0,0,0,0);
        vExpandButton = (LinearLayout) vExpandCollapseButtons.findViewById(R.id.boton_abrir_grupos);
        vCollapseButton = (LinearLayout) vExpandCollapseButtons.findViewById(R.id.boton_cerrar_grupos);
        vCollapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse_menu_groups();
            }
        });
        vExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand_menu_groups();
            }
        });


    }


    private void ini_fab_svn(){

        mFabSvn = (FloatingActionButton) mSVN.getHeaderView().findViewById(R.id.fab_h);

        mFabSvn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mCurrentMenu) {
                    case 0:
                        if(mExpandCollapseVisible) hide_expand_collapse_nav_buttons();
                        mSVN.switchMenuTo(mConfigMenu);
                        mCurrentMenu=1;

                        break;
                    case 1:
                        if(mExpandCollapseVisible) show_expand_collapse_nav_buttons();
                        mSVN.switchMenuTo(mOptionsMenu);
                        mCurrentMenu=0;
                        update_menu_groups_svn();


                        if (mCurrentMenuItem!=null){
                            if(mOptionsMenu.getGroup(mCurrentMenuItem.getGroupId())!=null) mOptionsMenu.getGroup(mCurrentMenuItem.getGroupId()).setStateCollapsed(false);
                        }

                        break;
                }
            }

        });
    }




    private void update_menu_groups_svn(){

        ArrayList<SublimeGroup> g = mOptionsMenu.grupos_menu();
        for (int i = 0; i < g.size(); i++) {
            if(g.get(i)!=null){
                g.get(i).setIsCollapsible(!mGroupsExpanded);
            }
        }


    }

    private void update_main_fab_position(){

        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        switch (mFabPosition){
            case 0:
                p.gravity = Gravity.BOTTOM|Gravity.CENTER;
                break;
            case 1:
                p.gravity = Gravity.BOTTOM|Gravity.LEFT;
                break;
            case 2:
                p.gravity = Gravity.BOTTOM|Gravity.RIGHT;
                break;

        }

        fab.setLayoutParams(p);
    }

    private void update_main_fab_visibility(){
        if(mShowFab) fab.setVisibility(View.VISIBLE);
        else fab.setVisibility(View.INVISIBLE);
        mConfigMenu.getMenuItem(R.id.grx_mid_posicion_boton).setEnabled(mShowFab);


    }




    private void update_text_fab_position(){
        String[] arr= getResources().getStringArray(R.array.gsa_posicion_boton);
        String tmp = mConfigMenu.getMenuItem(R.id.grx_mid_posicion_boton).getClass().getSimpleName();
        if(tmp.equals("SublimeTextWithBadgeMenuItem")) {
            SublimeTextWithBadgeMenuItem item = (SublimeTextWithBadgeMenuItem) mConfigMenu.getMenuItem(R.id.grx_mid_posicion_boton);
            item.setBadgeText(arr[mFabPosition]);
        }else mConfigMenu.getMenuItem(R.id.grx_mid_posicion_boton).setHint(arr[mFabPosition]);
    }

    private void update_text_panel_header_bg(String text){
        String tmp = mConfigMenu.getMenuItem(R.id.grx_header_svn_back).getClass().getSimpleName();
        if(tmp.equals("SublimeTextWithBadgeMenuItem")) {
            SublimeTextWithBadgeMenuItem item = (SublimeTextWithBadgeMenuItem) mConfigMenu.getMenuItem(R.id.grx_header_svn_back);
            item.setBadgeText(text);
        }else mConfigMenu.getMenuItem(R.id.grx_header_svn_back).setHint(text);
    }

    private void update_text_selected_theme(){
        if(!getResources().getBoolean(R.bool.allow_user_select_theme)) return;

        String[] arr= getResources().getStringArray(R.array.gsa_theme_list);
        int theme= Common.sp.getInt(Common.S_APPOPT_USER_SELECTED_THEME,getResources().getInteger(R.integer.def_theme));
        String tmp = mConfigMenu.getMenuItem(R.id.grx_mid_theme).getClass().getSimpleName();
        if(tmp.equals("SublimeTextWithBadgeMenuItem")) {
            SublimeTextWithBadgeMenuItem item = (SublimeTextWithBadgeMenuItem) mConfigMenu.getMenuItem(R.id.grx_mid_theme);
            item.setBadgeText(arr[theme]);
        }else mConfigMenu.getMenuItem(R.id.grx_mid_theme).setHint(arr[theme]);

    }

    private void update_text_divider_height(){

        String[] arr= getResources().getStringArray(R.array.gsa_ancho_divider);
        String tmp = mConfigMenu.getMenuItem(R.id.grx_mid_ancho_divider).getClass().getSimpleName();
        if(tmp.equals("SublimeTextWithBadgeMenuItem")) {
            SublimeTextWithBadgeMenuItem item = (SublimeTextWithBadgeMenuItem) mConfigMenu.getMenuItem(R.id.grx_mid_ancho_divider);
            item.setBadgeText(arr[mDividerHeight]);
        }else mConfigMenu.getMenuItem(R.id.grx_mid_ancho_divider).setHint(arr[mDividerHeight]);
    }



    private void hide_expand_collapse_nav_buttons(){
        vExpandCollapseButtons.setVisibility(View.INVISIBLE);
        vSvnMenu.setPadding(0,0,0,mPaddinOffVGrButtons);

    }

    private void show_expand_collapse_nav_buttons(){
        vExpandCollapseButtons.setVisibility(View.VISIBLE);
        vSvnMenu.setPadding(0,0,0,mPaddinOnVGrButtons);
    }

    private void enable_disable_groups_buttons_option(){
        if(mGroupsExpanded) mConfigMenu.getMenuItem(R.id.grx_mid_botones_grupos).setEnabled(false);
        else mConfigMenu.getMenuItem(R.id.grx_mid_botones_grupos).setEnabled(true);
    }

    private void update_svn_groups_buttons(){
        mExpandCollapseVisible=false;
        if(mNumberOfGroups!=0) {
            if(!mGroupsExpanded && mShowExpandCollapseButtons) mExpandCollapseVisible=true;
            if(mExpandCollapseVisible){
                if(mCurrentMenu==1) hide_expand_collapse_nav_buttons();
                else show_expand_collapse_nav_buttons();
            }else hide_expand_collapse_nav_buttons();
        }else hide_expand_collapse_nav_buttons();
    }



    private void expand_menu_groups(){
        SublimeGroup group;
        ArrayList<SublimeGroup> g = mOptionsMenu.grupos_menu();
        for (int i = 0; i < g.size(); i++) {
            if(g.get(i)!=null){
                group=g.get(i);
                if(group.isCollapsed()) g.get(i).setStateCollapsed(false);
            }
        }
    }

    private void collapse_menu_groups(){
        SublimeGroup group;
        ArrayList<SublimeGroup> g = mOptionsMenu.grupos_menu();
        for (int i = 0; i < g.size(); i++) {
            if(g.get(i)!=null){
                group=g.get(i);
                if(!group.isCollapsed()) g.get(i).setStateCollapsed(true);
            }
        }
    }


    @Override
    public void onDlgFrGrxAjustesSel(int tdialog, int opt){

        switch (tdialog){
            case Common.INT_ID_APPDLG_FAV_POS:
                mFabPosition=opt;
                Common.sp.edit().putInt(Common.S_APPOPT_FAB_POS,opt).commit();
                update_text_fab_position();
                update_main_fab_position();

                break;
            case Common.INT_ID_APPDLG_DIV_HEIGHT:
                mDividerHeight=opt;
                Common.sp.edit().putInt(Common.S_APPOPT_DIV_HEIGHT,opt).commit();
                update_text_divider_height();
                Common.cDividerHeight=mDividerHeight;
                update_divider_height();

                break;

            case  Common.INT_ID_APPDLG_EXIT_CONFIRM:
                if(opt!=0) this.finish();
                break;

            case Common.INT_ID_APPDLG_SET_THEME:
                Common.sp.edit().putInt(Common.S_APPOPT_USER_SELECTED_THEME,opt).commit();
                restart_app();
                break;
            case Common.INT_ID_APPDLG_SET_BG_PANEL_HEADER:
                 switch (opt){
                     case 0:
                         String nav_header_file = Common.IconsDir + File.separator+getString(R.string.gs_nav_header_bg_image_name);
                         Utils.delete_file(nav_header_file);
                         update_nav_header_bg();
                         break;
                     case 1:  init_panel_header_bg_picker();
                         break;
                 }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        boolean control;
        if (mDrawer.isDrawerOpen(mSVN)) {
            mDrawer.closeDrawers();
        }else{
            if(PrefScreenFragment==null) control=true;
            else control =PrefScreenFragment.exec_back_pressed();
            if(control){
                if(!mExitConfirmation) super.onBackPressed();
                else dlg_exit();
            }
        }
    }


    /****************** BACKUP - RESTORE MENU ************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grx_ajustes, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_backup) {
            show_backup_dialog();
            return true;
        }
        if (id == R.id.menu_restaurar){
            show_restore_dialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /***************** BACKUP ***********************************************/

    private void show_backup_dialog(){
        mEditText = null;
        AlertDialog.Builder adb= new AlertDialog.Builder(this);
        adb.setTitle(R.string.gs_tit_backup);
        View view = getLayoutInflater().inflate(R.layout.backup_dlg,null);
        mEditText = (EditText) view.findViewById(R.id.nombre_backup);
        TextView info= (TextView) view.findViewById(R.id.info_backup);
        info.setText(getString(R.string.gs_info_backup));
        mEditText.append("backup_");
        adb.setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String backup_name="";
                if(mEditText!=null) backup_name= mEditText.getText().toString();
                dialog.dismiss();
                show_confirm_backup_dialog(backup_name);
            }
        });
        adb.setView(view);
        adb.create().show();
    }

    private void show_confirm_backup_dialog(String backup_name){

        if(backup_name==null || backup_name.isEmpty()) show_snack_message(getString(R.string.gs_no_valid_name));
        else{
            File f = new File(Common.BackupsDir+File.separator+backup_name+"."+ getString(R.string.gs_backup_ext));
            if(f.exists()) show_overwrite_dialgo(backup_name);
            else show_backup_result(do_backup(backup_name));
        }
    }
    private void show_backup_result(String result){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.gs_tit_backup);
        ab.setMessage(result);
        ab.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ab.create().show();
    }


    private int app_version(){
        int app_version= -1;
        try{
            app_version = getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return app_version;
    }

    private String do_backup(String backup_name){
        Common.sp.edit().putBoolean(Common.S_CTRL_SYNC_NEEDED,true).commit();
        Common.sp.edit().putInt(Common.S_CTRL_APP_VER,app_version()).commit();
        boolean error=false;
        String serror="";
        ObjectOutputStream oos = null;
        FileOutputStream fos;
        File f = new File(Common.BackupsDir+File.separator+backup_name+"."+getString(R.string.gs_backup_ext));
        try{
            fos = new FileOutputStream(f);
            oos = new ObjectOutputStream(fos);
        }catch (Exception e){
            serror=e.toString();
            error=true;
        }
        if(!error){
            try{
                Map<String,?> prefes = Common.sp.getAll();
                if(oos!=null) oos.writeObject(prefes);
            }catch (Exception e){
                error = true;
                serror=e.toString();
            }
        }
        if(!error){
            try{
                if(oos!=null) {
                    oos.flush();
                    oos.close();
                }
            }catch (Exception e){
                error = true;
                serror=e.toString();
            }
        }

        String res;
        if(error) res = "Error: "+serror;
        else res = getString(R.string.gs_backup_ok)+" :  "+backup_name+"."+getString(R.string.gs_backup_ext);


        if(!error) {

            String ori_icons_dir = Common.IconsDir + File.separator;
            String dest_icons_dir = Common.BackupsDir + File.separator + backup_name + File.separator + getString(R.string.grx_ico_sub_dir) + File.separator;
            Utils.delete_files_or_create_folder(dest_icons_dir, ".png");
            Utils.copy_files(ori_icons_dir, dest_icons_dir, ".png");
            Utils.fix_foler_permissions(dest_icons_dir, ".png");
        }

        return res;

    }

    private void show_overwrite_dialgo(final String backup_name){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.gs_tit_sobreescribir_backup);
        ab.setMessage(getString(R.string.gs_mens_sobreescribir_backup, backup_name+"."+getString(R.string.gs_backup_ext)));
        ab.setPositiveButton(getString(R.string.gs_si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                show_backup_result(do_backup(backup_name));
            }
        });
        ab.setNegativeButton(getString(R.string.gs_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ab.create().show();

    }


    /******************** RESTORE ****************************************************/



    private void show_restore_dialog(){
        ListView lv = new ListView(this);
        File ficheros = new File(Common.BackupsDir+File.separator);
        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String ruta;
                if(pathname.isFile()){
                    ruta = pathname.getAbsolutePath().toLowerCase();
                    if(ruta.contains("."+getString(R.string.gs_backup_ext) )){
                        return true;
                    }
                }
                return false;
            }
        };
        File fa[]=ficheros.listFiles(ff);
        if(fa.length==0) show_snack_message(getString(R.string.gs_no_backups));
        else{

            AdapterBackups ab = new AdapterBackups();
            ab.AdapterBackups(this,fa);
            ListView lista = new ListView(this);
            lista.setAdapter(ab);
            lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    String file_name = tv.getText().toString();
                    mRestoreDialog.dismiss();
                    mRestoreDialog =null;
                    File f = new File(Common.BackupsDir+File.separator+file_name+"."+getString(R.string.gs_backup_ext));
                    if(f.exists()) show_restore_confirmation_dialog(file_name);
                    else show_snack_message(getString(R.string.gs_err_desconocido_restaurar));
                }
            });
            AlertDialog.Builder abd = new AlertDialog.Builder(this);
            abd.setTitle(R.string.gs_tit_restaurar);
            abd.setMessage(R.string.gs_mensaje_restaurar);
            abd.setView(lista);
            mRestoreDialog = abd.create();
            mRestoreDialog.show();
        }

    }

    private void show_restore_confirmation_dialog(final String backup){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.gs_tit_restaurar);
        adb.setMessage(getString(R.string.gs_mens_confirmar_restaurar, backup+"."+getString(R.string.gs_backup_ext)));
        adb.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                show_restore_result(do_restore(backup));
                Common.sp.edit().putInt(Common.S_APPOPT_USER_SELECTED_THEME,mTheme).commit(); //lets keep current theme
            }
        });
        adb.setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();;
            }
        });
        adb.create().show();
    }

    private String do_restore(String arch){
        int contador=0;
        boolean error=false;
        String serror="";
        ObjectInputStream ois = null;
        FileInputStream fis;
        SharedPreferences sp=Common.sp;

        File f = new File(Common.BackupsDir+File.separator+arch+"."+getString(R.string.gs_backup_ext));
        try{
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
        }catch (Exception e){
            serror=e.toString();
            error=true;
        }
        if(!error){

            sp.edit().clear().commit();
            try{

                Map map= (Map) ois.readObject();
                Set set = map.entrySet();
                Iterator iterator = set.iterator();

                while(iterator.hasNext()){
                    contador++;
                    Map.Entry entrada = (Map.Entry) iterator.next();
                    String clave = (String) entrada.getKey();
                    if( entrada.getValue() instanceof Boolean ){
                        Boolean b = (Boolean) entrada.getValue();
                        sp.edit().putBoolean(clave,b.booleanValue()).commit();
                    }else if(entrada.getValue() instanceof Float){
                        Float flo = (Float) entrada.getValue();
                        sp.edit().putFloat(clave,flo.floatValue()).commit();

                    }else if (entrada.getValue() instanceof Integer){
                        Integer ent = (Integer) entrada.getValue();
                        sp.edit().putInt(clave,ent.intValue()).commit();
                    }else if (entrada.getValue() instanceof Long){
                        Long lo = (Long) entrada.getValue();
                        sp.edit().putLong(clave,lo.longValue()).commit();
                    }else if (entrada.getValue() instanceof String){
                        String str = (String) entrada.getValue();
                        sp.edit().putString(clave,str).commit();
                    }else if (entrada.getValue() instanceof Set){
                        Set s = (Set) entrada.getValue();
                        sp.edit().putStringSet(clave,s).commit();
                    }
                }
            }catch (Exception e){
                serror=e.toString();
                error=true;
            }

        }
        if(ois!=null)
            try{
            ois.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        String ret;
        if(error) ret = "ERROR: "+serror;
        else ret = getString(R.string.gs_mens_resultado_restaurar,arch+"."+getString(R.string.gs_backup_ext),contador);


        if(!error){
            if(!error){

                String  ori_icon_folder=Common.BackupsDir+File.separator+arch+File.separator+getString(R.string.grx_ico_sub_dir)+File.separator;
                String dest_icon_folder=Common.IconsDir+File.separator;
                Utils.delete_files_or_create_folder(dest_icon_folder, ".png");
                Utils.copy_files(ori_icon_folder, dest_icon_folder,".png");
                Utils.fix_foler_permissions(dest_icon_folder, ".png");

            }
        }

        return ret;
    }

    private void show_restore_result(String resultado){
        sync_preferences();
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.gs_tit_restaurar);
        ab.setCancelable(false);
        ab.setMessage(resultado);
        ab.setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ab.create().show();
    }



    ///////////////////******************** sync suppor *****************************////

    public void finish_sync(){
        if(!mCurrentScreen.isEmpty() && mCurrentMenuItem!=null) change_screen(mCurrentMenuItem,mCurrentScreen);
        Common.SyncUpMode = false;
        if(this.getResources().getBoolean(R.bool.enable_settingsdb)) {
            for (String groupkey : Common.GroupKeysList) {
                Utils.change_group_key_value(this,groupkey);
            }
        }
        Common.GroupKeysList.clear();
        show_toast(getString(R.string.gs_sync_end));
    }


    public void chage_to_screen_to_sync(){
        if(mNumSyncScreens<ResXML.size()){
            android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            GrxPreferenceScreen prefsScreen = GrxPreferenceScreen.newInstance(ResXML.get(mNumSyncScreens),"","",0);
            fragmentTransaction.replace(R.id.content,prefsScreen, Common.TAG_PREFSSCREEN_FRAGMENT_SYNC).commit();
        }else finish_sync();
    }


    public void pref_screen_synchronized(int num_prefs){
        mNumSyncPrefs+=num_prefs;
        mNumSyncScreens++;
        chage_to_screen_to_sync();
    }


    private void sync_preferences(){
        if(Common.GroupKeysList!=null) Common.GroupKeysList.clear();
        Common.SyncUpMode=false;
        if(Common.sp.getBoolean(Common.S_CTRL_SYNC_NEEDED,true)){
            check_available_screens();
            if(ResXML.size()!=0){
                mNumSyncPrefs=0;
                mNumSyncScreens=0;
                Common.SyncUpMode = true;
                show_toast(getString(R.string.gs_sync_start));
                Common.sp.edit().putBoolean(Common.S_CTRL_SYNC_NEEDED,false).commit();
                chage_to_screen_to_sync();
                }
            }
        }




    private void check_available_screens(){
        SublimeBaseMenuItem menuItem;
        int id;
        String screen_name;
        int id_xml;
        int num_screens=0;

        ArrayList<SublimeBaseMenuItem> options_menu=mOptionsMenu.items_menu();
        ResXML = new HashMap<>();

        for(int i=0;i<options_menu.size();i++){
            id=0;
            id_xml=0;
            menuItem = options_menu.get(i);
            if(menuItem!=null) id=menuItem.getItemId();
            if(id!=0){
                try{
                    screen_name = getResources().getResourceEntryName(id);
                    if(screen_name!=null && ( !screen_name.isEmpty() ) ) id_xml = getResources().getIdentifier(screen_name, "xml", getApplicationContext().getPackageName());
                    if(id_xml!=0) {
                        ResXML.put(num_screens,getResources().getResourceEntryName(id_xml));
                        num_screens++;
                    }
                }catch (Exception e){}
            }
        }
      //  show_toast(String.valueOf(ResXML.size()));

    }

    private void show_toast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

}
