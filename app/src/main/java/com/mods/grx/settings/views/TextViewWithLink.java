package com.mods.grx.settings.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.mods.grx.settings.Common;
import com.mods.grx.settings.utils.Utils;

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
public class TextViewWithLink extends TextView implements View.OnClickListener{

    private String mUrl;
    private boolean mAnimateText;

    public TextViewWithLink(Context context){
        super(context);
    }

    public TextViewWithLink(Context context, AttributeSet attrs){
        super(context, attrs);
        ini_params(context,attrs);
    }

    private void ini_params(Context context, AttributeSet attributeSet) {
        mUrl = attributeSet.getAttributeValue(null, Common.INFO_ATTR_ULR);
        mAnimateText = attributeSet.getAttributeBooleanValue(null, Common.INFO_ATTR_ANIMATE_TEXT,false);
        if(mUrl!=null) {
            setClickable(true);
            setOnClickListener(this);
        }
        if(mAnimateText){
            setSingleLine();
            Utils.animate_textview_marquee_forever(this);
            setSelected(true);
        }
    }

    @Override
    public void onClick(View view){
        if(mUrl!=null){
            Intent myintent=new Intent(Intent.ACTION_VIEW);
            myintent.setData(Uri.parse(mUrl));
            getContext().startActivity(myintent);
        }
    }

}
