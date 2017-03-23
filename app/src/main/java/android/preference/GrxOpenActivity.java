package android.preference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mods.grx.settings.Common;
import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.utils.Utils;

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

public class GrxOpenActivity extends Preference implements GrxPreferenceScreen.CustomDependencyListener {


    private String mActivity;
    private Intent mIntent;
    private String mLabel;
    private Drawable mIcon=null;

    private boolean mIsInstalled=false;

    ImageView vWidgetIcon;
    ImageView vAndroidIcon;
    private String mMyDependencyRule;

    public GrxOpenActivity(Context c){
        super(c);
    }

    public GrxOpenActivity(Context c, AttributeSet a){
        super(c,a);
        ini_param(c,a);
    }

    public GrxOpenActivity(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        ini_param(context,attrs);
    }

    private void ini_param(Context context, AttributeSet attrs){
        mMyDependencyRule=attrs.getAttributeValue(null, "grxDepRule");
        if(mMyDependencyRule!=null){
            if(getKey()==null || getKey().isEmpty()) {
                setKey(getClass().getName()+"_"+getOrder());
            }
        }
        mActivity = attrs.getAttributeValue(null,"grxActivity");
        String aux[]=mActivity.split(Pattern.quote("/"));
        ComponentName componentName = new ComponentName(aux[0], aux[1] );
        mIntent = new Intent();
        mIntent.setComponent(componentName);
        mIcon = Utils.get_icon_from_intent(getContext(),mIntent);
        mLabel = Utils.get_activity_label_from_intent(getContext(),mIntent);
        if(mLabel==null) mLabel="";
        ResolveInfo resolveInfo = getContext().getPackageManager().resolveActivity(mIntent, 0);
        if(resolveInfo!=null) mIsInstalled = true;
        setSummary(mIsInstalled ? mLabel : getContext().getResources().getString(R.string.gs_app_no_instalada));
        setEnabled(mIsInstalled);
    }

    @Override
    protected void onClick() {
        super.onClick();
        if (mIsInstalled) {
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(mIntent);
        }else Toast.makeText(getContext(),getContext().getResources().getString(R.string.gs_app_no_instalada),Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        if(mIcon!=null) setIcon(mIcon);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = (View) super.onCreateView(parent);
        vWidgetIcon= (ImageView) view.findViewById(R.id.widget_arrow);
        vAndroidIcon = (ImageView) view.findViewById(android.R.id.icon);
        vAndroidIcon.setLayoutParams(Common.AndroidIconParams);
        return view;
    }


    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        float alpha = (isEnabled() ? (float) 1.0 : (float) 0.4);
        if(vWidgetIcon!=null) vWidgetIcon.setAlpha(alpha);
        if(vAndroidIcon!=null) vAndroidIcon. setAlpha(alpha);
    }


    /**********  Onpreferencechangelistener - add custom dependency rule *********/

    @Override
    public void setOnPreferenceChangeListener(Preference.OnPreferenceChangeListener onPreferenceChangeListener){
        if(Common.SyncUpMode) return;
        super.setOnPreferenceChangeListener(onPreferenceChangeListener);
        if(mMyDependencyRule!=null) {
            GrxPreferenceScreen grxPreferenceScreen = (GrxPreferenceScreen) getOnPreferenceChangeListener();
            grxPreferenceScreen.add_custom_dependency(this, mMyDependencyRule, null);
        }
    }

    /************ custom dependencies ****************/

    public void OnCustomDependencyChange(boolean state){
        if(mIsInstalled) {
            setEnabled(state);
        }else setEnabled(false);
    }

}
