package com.mods.grx.settings.prefssupport.info;

import android.content.Context;
import android.util.AttributeSet;

import com.mods.grx.settings.R;

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

public class PrefAttrsInfo {


    public String mMyKey;
    public String mMyTitle;
    public String mMySummary;

    private boolean mIsMultiValue;
    private String mMyStringDefaultValue;
    private String mMyStringSeparator="";
    private int mMyMaxItemsNum= 0;

    private int mMyIntDefaultValue;

    private boolean mMyBooleanDefaultValue;

    private boolean mSendBC1;
    private boolean mSendBC2;
    private String mMyGroupKey;
    private boolean mSaveInSettingsAllowed;

    private String mMyDependencyRule;
    private String mMyDependencySeparator;



    public PrefAttrsInfo(Context context, AttributeSet attrs, CharSequence title, CharSequence summary, String key) { //checkbox, switches, preferencecategory

        mMyBooleanDefaultValue = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "defaultValue",false);
        ini_parameters(context, attrs, title, summary, key);

    }


    public PrefAttrsInfo(Context context, AttributeSet attrs, CharSequence title, CharSequence summary, String key, int defvalue) { //int

        String s_def = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        if(s_def==null) mMyIntDefaultValue= defvalue;
        else mMyIntDefaultValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "defaultValue",defvalue);

        ini_parameters(context, attrs, title, summary, key);

    }


    public PrefAttrsInfo(Context context, AttributeSet attrs, CharSequence title, CharSequence summary, String key, boolean isMultivalue) {  //strings

        mIsMultiValue = isMultivalue;

        String s_def = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        if(s_def==null) mMyStringDefaultValue= "";
        else mMyStringDefaultValue = s_def;

        if(mIsMultiValue){
            mMyStringSeparator = attrs.getAttributeValue(null, "grxSep");
            if(mMyStringSeparator== null) mMyStringSeparator = context.getResources().getString(R.string.gs_def_sep);

            mMyMaxItemsNum=attrs.getAttributeIntValue(null,"grxMax",0);

        }

        ini_parameters(context, attrs, title, summary, key);

    }

    private void ini_parameters(Context context, AttributeSet attrs, CharSequence title, CharSequence summary, String key){



        if(title!=null) mMyTitle = title.toString();
        else mMyTitle = "";

        if(summary!=null) mMySummary = summary.toString();
        else mMySummary = "";

        mMyKey = key;

        mMyGroupKey = attrs.getAttributeValue(null, "grxGkey");
        mSendBC1 = attrs.getAttributeBooleanValue(null, "grxBc1", context.getResources().getBoolean(R.bool.def_grxBc1));
        mSendBC2 = attrs.getAttributeBooleanValue(null, "grxBc2", context.getResources().getBoolean(R.bool.def_grxBc2));
        if(context.getResources().getBoolean(R.bool.enable_settingsdb)) {
            mSaveInSettingsAllowed=attrs.getAttributeBooleanValue(null, "grxCr", context.getResources().getBoolean(R.bool.def_grxCr) );
        }else mSaveInSettingsAllowed=false;

        mMyDependencyRule = attrs.getAttributeValue(null, "grxDepRule");
        mMyDependencySeparator = attrs.getAttributeValue(null, "grxDepSeparator");


    }


    public String get_my_title(){
        return mMyTitle;
    }

    public String get_my_summary(){
        return mMySummary;
    }

    public String get_my_key(){
        return mMyKey;
    }

    public String get_my_separator(){
        return mMyStringSeparator;
    }

    public int get_my_int_def_value(){
        return mMyIntDefaultValue;
    }

    public String get_my_string_def_value(){
        return mMyStringDefaultValue;
    }

    public boolean get_my_boolean_def_value(){
        return mMyBooleanDefaultValue;
    }

    public String get_my_dependency_rule(){
        return mMyDependencyRule;
    }

    public String get_my_dependency_separator(){
        return mMyDependencySeparator;
    }

    public String get_my_group_key(){
        return mMyGroupKey;
    }

    public boolean get_send_bc1(){
        return mSendBC1;
    }

    public boolean get_send_bc2(){
        return mSendBC2;
    }

    public boolean get_allowed_save_in_settings_db(){
        return mSaveInSettingsAllowed;
    }

    public boolean is_valid_key(){
        return !(mMyKey==null || mMyKey.isEmpty() );
    }

    public int get_my_max_items(){return mMyMaxItemsNum;}

}
