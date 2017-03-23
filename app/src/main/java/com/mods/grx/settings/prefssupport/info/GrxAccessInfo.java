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

package com.mods.grx.settings.prefssupport.info;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.mods.grx.settings.Common;
import com.mods.grx.settings.utils.Utils;

import java.net.URISyntaxException;

public class GrxAccessInfo {

    private String mUri;

    private String mLabel = null;
    private String mGrxDrawableName = null;
    private String mGrxValue = null;
    private String mGrxIconPath = null;
    private int mGrxTypeOfAccess = -1;

    Drawable mDrawableIcon;


    public GrxAccessInfo(String uri, Context c){
        ini_access_info(uri, c);

    }


    public void ini_access_info(String uri, Context context){
        mUri=uri;

        Intent intent;
        try {
            intent = Intent.parseUri(uri, 0);
        }catch (URISyntaxException e) {
            return;
        }

        mLabel = Utils.get_activity_label_from_intent(context, intent);
        mGrxIconPath = Utils.get_file_name_from_intent(intent);
        mGrxDrawableName = intent.getStringExtra(Common.EXTRA_URI_DRAWABLE_NAME);
        mGrxValue = intent.getStringExtra(Common.EXTRA_URI_VALUE);
        mGrxTypeOfAccess = intent.getIntExtra(Common.EXTRA_URI_TYPE,-1);
        mDrawableIcon = Utils.get_drawable_from_intent(context,intent);

      }


    public void update_uri(String uri){mUri=uri;}

    public int get_access_type(){return mGrxTypeOfAccess;}

    public Drawable get_icon_drawable(){return mDrawableIcon;}

    public String get_uri(){return mUri;}

    public String get_label(){return (mLabel == null) ? "?" : mLabel;}

    public String get_icon_path(){return mGrxIconPath;}

    public String get_drawable_name(){return mGrxDrawableName;}

}
