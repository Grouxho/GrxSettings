package com.mods.grx.settings.dlgs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;

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

public class DlgFrEditText extends DialogFragment {

    private OnGrxEditTextListener mCallBack;
    private String mTitle;
    private String mkey;
    private String mHelperFragmentName;
    private String mValue;
    private EditText mEditText;

    public DlgFrEditText(){}

    public interface OnGrxEditTextListener{
        void onEditTextDone(String text);
    }


    public static DlgFrEditText newInstance(OnGrxEditTextListener callback, String help_frg, String key, String tit, String value){

        DlgFrEditText ret = new DlgFrEditText();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,help_frg);
        bundle.putString("key",key);
        bundle.putString("tit",tit);
        bundle.putString("val",value);
        ret.setArguments(bundle);
        ret.ini_fragment(callback);
        return ret;
    }


       private void ini_fragment(OnGrxEditTextListener callback){
        mCallBack=callback;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mValue = mEditText.getText().toString();
        outState.putString("val", mValue);

    }





    public View edit_text_view(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.pref_edittext_dlg_lay,null);
        mEditText = (EditText) view.findViewById(R.id.txt_edittext);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        mHelperFragmentName=getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        mkey=getArguments().getString("key");
        mTitle=getArguments().getString("tit");
        mValue=getArguments().getString("val");

        if(state!=null){
            mValue=state.getString("val");
        }
        if(mValue==null) mValue="";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle)
                .setView(edit_text_view())
                .setPositiveButton(R.string.gs_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCallBack==null) {
                            if(mHelperFragmentName.equals(Common.TAG_PREFSSCREEN_FRAGMENT)){
                                GrxPreferenceScreen prefsScreen =(GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                                mCallBack=(DlgFrEditText.OnGrxEditTextListener) prefsScreen.find_callback(mkey);
                            }else mCallBack=(DlgFrEditText.OnGrxEditTextListener) getFragmentManager().findFragmentByTag(mHelperFragmentName);
                        }
                        if(mCallBack!=null) mCallBack.onEditTextDone(mEditText.getText().toString());
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        mEditText.append(mValue);
        AlertDialog ad = builder.create();
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mEditText.setSelection(mEditText.getText().length());
            }
        });
        return ad;
    }


}
