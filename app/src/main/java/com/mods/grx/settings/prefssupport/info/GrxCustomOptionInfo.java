package com.mods.grx.settings.prefssupport.info;

import android.graphics.drawable.Drawable;

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

public class GrxCustomOptionInfo {

    String mTitle;
    String mValue;
    Drawable mIcon;
    boolean mIsSelected;


    public GrxCustomOptionInfo(String tit, String val, Drawable icon){
        mTitle=tit;
        mIcon = icon;
        mValue=val;
        mIsSelected=false;

    }

    public Drawable get_icon(){return mIcon;}

    public String get_title(){return mTitle;}

    public String get_value(){return mValue;}

    public void set_selected(boolean est){mIsSelected=est;}

    public boolean is_selected(){return mIsSelected;}


}
