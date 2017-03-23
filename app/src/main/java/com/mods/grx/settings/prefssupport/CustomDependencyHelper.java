package com.mods.grx.settings.prefssupport;

import com.mods.grx.settings.GrxPreferenceScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

public class CustomDependencyHelper {

    GrxPreferenceScreen.CustomDependencyListener mListener;

    private String mDependencyRule;

    private String mSeparator;

    private String mDependencyKey;
    private int mDependencyType;
    private boolean mEnableDependent = false;
    private String mValuesToCheck;
    private List<String> mValuesToContain = new ArrayList<String>();

    public CustomDependencyHelper(GrxPreferenceScreen.CustomDependencyListener listener, String rule, String separator){

        mListener=listener;
        mDependencyRule = rule;
        mSeparator=separator;


        String arr[] = rule.split(Pattern.quote("#"));

        if(arr[0].toUpperCase().equals("ENABLE")) mEnableDependent = true;

        mDependencyKey = arr[1];

        switch (arr[2].toUpperCase()){
            case "INT":
                mDependencyType = 0;
                break;
            case "STRING":
                mDependencyType = 1;
                break;
            case "MULTIVALUE":
                mDependencyType = 2;
                break;
            case "BOOLEAN":
                mDependencyType = 3;
                break;
        }

        if(mDependencyType==3) {
            if(arr[3].toUpperCase().equals("TRUE")) mValuesToCheck ="TRUE";
            else mValuesToCheck  = "FALSE";

        }else {

            String[] values = arr[3].split(Pattern.quote(","));
            mValuesToCheck=",";
            for(int i = 0; i<values.length; i++){
                String value = values[i];
                if(value.startsWith("(") && value.endsWith(")")) {
                    String substring = value.substring(1,value.length()-1);
                    if(substring.contains("NULL"))substring=substring.replace("NULL","");
                    mValuesToContain.add(substring);
                }else mValuesToCheck+=value+",";
            }
            if(!mValuesToCheck.endsWith(",")) mValuesToCheck += ",";
            if(mValuesToCheck.contains("NULL")) {
                mValuesToCheck=mValuesToCheck.replace("NULL","");
            }else if(mValuesToCheck.equals(",,")) mValuesToCheck=null;
        }

    }

    public String get_custom_dependency_key(){
        return mDependencyKey;
    }

   public int get_dependency_type(){
       return mDependencyType;
   }

   public boolean listener_should_be_enabled(String value){

       switch (mDependencyType){
           case 0:
           case 1:
           case 2:
               boolean matched = false;
               if(mValuesToCheck!=null) {
                   String pattern = "," + value + ",";
                   matched = mValuesToCheck.contains(pattern);
               }
               if(matched) return (matched==true) ? mEnableDependent : !mEnableDependent;
               for (int i = 0; i<mValuesToContain.size();i++){
                   String tmp = mValuesToContain.get(i);
                   if(value.contains(tmp)){
                       return mEnableDependent;
                   }
               }
               return !mEnableDependent;


           case 3:
               boolean dependency_value = (value.toUpperCase().equals("TRUE")) ? true: false;
               boolean rule_value = (mValuesToCheck.toUpperCase().equals("TRUE")) ? true: false;
               return (dependency_value == rule_value) ? mEnableDependent : !mEnableDependent;
       }
       return true;
   }

   public  GrxPreferenceScreen.CustomDependencyListener get_listener(){
       return mListener;
   }

}
