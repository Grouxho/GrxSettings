package com.mods.grx.settings;

import android.content.SharedPreferences;
import android.widget.LinearLayout;

import java.util.HashSet;

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

public class Common {

    Common(){}


    //config menu

    public static final String S_APPOPT_DRAWER_POS= "grx_drawer_pos";
    public static final boolean DEF_VAL_DRAWER_POS = false;
    public static final String S_APPOPT_FAB_POS= "grx_fab_pos";
    public static final int DEF_VAL_FAB_POS = 0; //CENTER
    public static final String S_APPOPT_DIV_HEIGHT= "grx_div_height";
    public static final String S_APPOPT_SHOW_FAV= "grx_show_fab";
    public static final boolean DEF_VAL_SHOW_FAB = true;
    public static final String S_APPOPT_REMEMBER_SCREEN= "grx_remenber_screen";
    public static final boolean DEF_VAL_REMENBER_SCREEN = false;
    public static final String S_APPOPT_MENU_GROUPS_ALWAYS_OPEN= "grx_men_groups_open";
    public static final boolean DEF_VAL_GROUPS_ALWAYS_OPEN = true;
    public static final String S_APPOPT_SHOW_COLLAPSE_EXPAND_BUTTONS= "grx_expand_collapser_buttons";
    public static final boolean DEF_VAL_SHOW_COL_EXP_BUTTONS = false;
    public static final String S_APPOPT_EXIT_CONFIRM= "grx_exit_confirm";
    public static final boolean DEF_VAL_EXIT_CONFIRM = true;
    public static final String S_APPOPT_USER_SELECTED_THEME = "grx_user_selected_theme";

    //sync control key

    public static final String S_CTRL_SYNC_NEEDED = "grx_sync_needed";
    public static final String S_CTRL_APP_VER= "grx_app_version";

    //Themes

    public static final int INT_ID_THEME_BASE_LIGHT = 0;
    public static final int INT_ID_THEME_BASE_DARK = 1;
    public static final int INT_ID_THEME_GREEN_LIGHT = 2;
    public static final int INT_ID_THEME_GREEN_DARK = 3;
    public static final int INT_ID_THEME_RED_LIGHT = 4;
    public static final int INT_ID_THEME_RED_DARK = 5;
    public static final int INT_ID_THEME_ORANGE_LIGHT = 6;
    public static final int INT_ID_THEME_ORANGE_DARK = 7;
    public static final int INT_ID_THEME_PURPLE_LIGHT = 8;
    public static final int INT_ID_THEME_PURPLE_DARK = 9;
    public static final int INT_ID_THEME_BROWN_LIGHT = 10;
    public static final int INT_ID_THEME_YELLOW_DARK = 11;
    public static final int INT_ID_THEME_GREEN_ORANGE_LIGHT = 12;

    //aux

    public static final String S_AUX_LAST_SCREEN= "grx_last_screen";


    //dlgs

    public static final String S_DLG_T_KEY= "t_dialog";
    public static final String S_APPDLG_FAV_POS= "dlg_fav_pos";
    public static final int INT_ID_APPDLG_FAV_POS = 0;
    public static final String S_APPDLG_DIV_HEIGHT= "dlg_div_height";
    public static final int INT_ID_APPDLG_DIV_HEIGHT = 1;
    public static final String S_APPDLG_EXIT_CONFIRM = "dlg_exit_confirm";
    public static final int INT_ID_APPDLG_EXIT_CONFIRM = 2;
    public static final String S_APPDLG_SET_THEME= "dlg_set_theme";
    public static final int INT_ID_APPDLG_SET_THEME = 3;
    public static final String S_APPDLG_SET_BG_PANEL_HEADER= "dlg_set_panel_header_bg";
    public static final int INT_ID_APPDLG_SET_BG_PANEL_HEADER = 4;




    //App start modes

    public static final int INI_MODE_NORMAL = 0;
    public static final int INI_MODE_INSTANCE = 1;
    public static final int INI_MODE_INTENT = 2;


    //Intent Extras



    public static final String EXTRA_SCREEN = "GrxScreen";
    public static final String EXTRA_SUB_SCREEN = "GrxSubScreen";
    public static final String EXTRA_KEY = "GrxKey";
    public static final String EXTRA_MODE = "GrxMode";
    public static final String EXTRA_DIV_HEIGHT = "GrxDividerHeight";

    public static final String EXTRA_SUB_SCREEN_PREFSSCREEN = "GrxSubScreenPrefsScreen";

    public static final String TAG_PREFSSCREEN_FRAGMENT= "GrxPreferenceScreen";
    public static final String TAG_PREFSSCREEN_FRAGMENT_SYNC= "GrxPrefsScreen_sync";
    public static final String TAG_FRAGMENTHELPER_NAME_EXTRA_KEY= "GrxHelperFragment";

    public static final int REQ_CODE_GALLERY_IMAGE_PICKER_JUST_URI = 97;
    public static final int REQ_CODE_GALLERY_IMAGE_PICKER_CROP_CIRCULAR = 98;
    public static final int REQ_CODE_GALLERY_IMAGE_PICKER_FROM_FRAGMENT = 99;
    public static final int REQ_CODE_GALLERY_IMAGE_PICKER_FROM_GRXAJUSTES = 100;

    public static final int REQ_CODE_GET_SHORTCUT = 101;

    public static final String TAG_DEST_FRAGMENT_NAME_EXTRA_KEY= "GrxDestFragment";
    public static final String TAG_DLGFRGRXCOLORPICKER = "DlgFrGrxColorPicker";
    public static final String TAG_DLGFRGRCOLORPALETTE = "DlgFrColorPalette";
    public static final String TAG_DLGFRGRSELECTAPP = "DlgFrSelecApp";
    public static final String TAG_DLGFRGREDITTEXT = "DlgFrEditText";
    public static final String TAG_DLGFRGRACCESS = "DlgFrGrxAccess";
    public static final String TAG_DLGFRGRMULTIACCESS = "DlgFrGrxMultiAccess";
    public static final String TAG_DLGFRGRMULTIPPCOLOR = "DlgFrGrxMultiAppColor";
    public static final String TAG_DLGFRGRMULTIVALUES = "DlgFrSelectSortItems";
    public static final String TAG_DLGFRGRMULTISELECT = "DlgFrMultiSelect";
    public static final String TAG_DLGFRGRDATEPICKER = "DlgFrDatePicker";
    public static final String TAG_DLGFRGRTIMEPICKER = "DlgFrTimePicker";
    public static final String TAG_INFOFRAGMENT = "GrxInfoFragment";



    public static final int ID_ACCESS_SHORCUT = 0;
    public static final int ID_ACCESS_APPS = 1;
    public static final int ID_ACCESS_ACTIVITIES = 2;
    public static final int ID_ACCESS_CUSTOM = 3;

    public static final String EXTRA_URI_ICON = "grx_icon";
    public static final String EXTRA_URI_TYPE = "grx_type";
    public static final String EXTRA_URI_LABEL = "grx_label";
    public static final String EXTRA_URI_DRAWABLE_NAME = "grx_drawable";
    public static final String EXTRA_URI_VALUE = "grx_value";

    public static final String TMP_PREFIX = "grxTMP_";

    public static final int ACTIVITY_LABEL_MAX_CHARS = 25;


    //customizable info tabs

    public static String INFO_ATTR_ULR = "grxURL";
    public static String INFO_ATTR_ROUND_ICON = "grxCircular";
    public static String INFO_ATTR_ANIMATE_TEXT = "grxAnimateText";


    //shared global values

    public static int cDividerHeight;
    public static SharedPreferences sp;
    public static String IconsDir;
    public static String CacheDir;
    public static String BackupsDir;

    public static LinearLayout.LayoutParams AndroidIconParams;

    public static boolean SyncUpMode=false;
    public static HashSet<String> GroupKeysList=null;






}