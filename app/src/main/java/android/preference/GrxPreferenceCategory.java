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

package android.preference;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;


public class GrxPreferenceCategory extends PreferenceCategory implements GrxPreferenceScreen.CustomDependencyListener{


    private TextView vTit=null;

    private boolean dep=true;


    private String mTitColor;
    private String mBackgroundColor;
    private boolean mHidden;
    private String mMyDependencyRule;

    public GrxPreferenceCategory(Context context) {
        super(context);
        setLayoutResource(R.layout.pref_category_lay);
    }

    public GrxPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMyDependencyRule=attrs.getAttributeValue(null, "grxDepRule");
        if(mMyDependencyRule!=null) {
            if (getKey() == null || getKey().isEmpty()) {
                setKey(getClass().getName() + "_" + getOrder());
            }
        }
        setLayoutResource(R.layout.pref_category_lay);
        mTitColor = attrs.getAttributeValue(null, "grxTextColor");
        mBackgroundColor=attrs.getAttributeValue(null, "grxBackGroundColor");
        mHidden = attrs.getAttributeBooleanValue(null,"grxHide",false);

    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        dep = !disableDependent;
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        LinearLayout ll = (LinearLayout) super.onCreateView(parent);
        vTit =  (TextView) ll.findViewById(android.R.id.title);
        if(mTitColor!=null) vTit.setTextColor(Color.parseColor(mTitColor));
        if(mBackgroundColor!=null) vTit.setBackgroundColor(Color.parseColor(mBackgroundColor));
        if(mHidden) {
            ll.removeAllViews();
        }

        return ll;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        if(vTit!=null) {
            if(dep) vTit.setAlpha((float) 1.0 );
            else vTit.setAlpha((float) 0.4);
        }
    }

    /**********  Onpreferencechangelistener - add custom dependency rule *********/

    @Override
    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener onPreferenceChangeListener){
        if(!Common.SyncUpMode) {
            super.setOnPreferenceChangeListener(onPreferenceChangeListener);
            if(mMyDependencyRule!=null) {
            GrxPreferenceScreen grxPreferenceScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
            grxPreferenceScreen.add_custom_dependency(this, mMyDependencyRule, null);
        }
    }
    }

    /************ custom dependencies ****************/
    public void OnCustomDependencyChange(boolean state){
        setEnabled(state);
    }
}
