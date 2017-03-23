package android.preference;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.Common;
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

public class GrxInfoText extends Preference implements GrxPreferenceScreen.CustomDependencyListener {

    private String mMyDependencyRule;
    private ImageView vAndroidIcon;
    private int mIconId=0;
    private ImageView vWidgetIcon=null;

    TextView textView;

    public GrxInfoText(Context c){
        super(c);
    }

    public GrxInfoText(Context c, AttributeSet a){
        super(c,a);
        ini_param(c,a);
    }

    public GrxInfoText(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        ini_param(context,attrs);
    }

    private void ini_param(Context context, AttributeSet attrs) {

        mMyDependencyRule=attrs.getAttributeValue(null, "grxDepRule");
        if(mMyDependencyRule!=null) {
            if (getKey() == null || getKey().isEmpty()) {
                setKey(getClass().getName() + "_" + getOrder());
            }
        }
        boolean showdividers = attrs.getAttributeBooleanValue(null,"grxShowDividers",false);
        setSelectable(showdividers);

        String dr_name = attrs.getAttributeValue(null,"grxIconRight");
        if(dr_name!=null){
            mIconId=getContext().getResources().getIdentifier(dr_name,"drawable",getContext().getPackageName());
        }
        if(mIconId!=0) setWidgetLayoutResource(R.layout.widget_arrow);


    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = (View) super.onCreateView(parent);
        TextView vTit =  (TextView) view.findViewById(android.R.id.title);
        vTit.setVisibility(View.GONE);
        if(mIconId!=0){
            ImageView arrow = (ImageView) view.findViewById(R.id.widget_arrow);
            if(arrow!=null) arrow.setVisibility(View.GONE);
            vWidgetIcon = (ImageView) view.findViewById(R.id.widget_icon);
            vWidgetIcon.setVisibility(View.VISIBLE);
            vWidgetIcon.setImageDrawable(getContext().getResources().getDrawable(mIconId));

        }

        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        String t = getSummary().toString();
        if(t!=null) setSummary(Html.fromHtml(getSummary().toString()));

        return view;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        if(vAndroidIcon==null) vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        if(vWidgetIcon!=null) vAndroidIcon.setAlpha(alpha);
        vAndroidIcon. setAlpha(alpha);
    }


    /**********  Onpreferencechangelistener - add custom dependency rule *********/

    @Override
    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener onPreferenceChangeListener){
        super.setOnPreferenceChangeListener(onPreferenceChangeListener);
        if(mMyDependencyRule!=null) {
            GrxPreferenceScreen grxPreferenceScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
            grxPreferenceScreen.add_custom_dependency(this, mMyDependencyRule, null);
        }
    }

    /************ custom dependencies ****************/

    public void OnCustomDependencyChange(boolean state){
            setEnabled(state);
    }


}
