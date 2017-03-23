package com.mods.grx.settings.dlgs;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;

import android.util.Log;
import android.widget.TimePicker;

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

public class DlgFrGrxTimePicker extends DialogFragment {


    private String mkey;
    private int mValue;

    private DlgFrGrxTimePicker.OnGrxTimeSetListener mCallback;

    public DlgFrGrxTimePicker(){}


    public static DlgFrGrxTimePicker newInstance(String key, int value){

        DlgFrGrxTimePicker ret = new DlgFrGrxTimePicker();
        Bundle bundle = new Bundle();
        bundle.putString("key",key);
        bundle.putInt("val",value);
        ret.setArguments(bundle);
        return ret;
    }

    public interface OnGrxTimeSetListener{
        void GrxTimeSet(int value, String key);
    }

    public void setOnGrxTimeSetListener(DlgFrGrxTimePicker.OnGrxTimeSetListener callback){
        mCallback=callback;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(mCallback==null) mCallback=(DlgFrGrxTimePicker.OnGrxTimeSetListener) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {

        mkey=getArguments().getString("key");
        mValue=getArguments().getInt("val",0);

        int hours = (mValue/60);
        int minutes = mValue - (hours*60);

        TimePickerDialog tpd = new TimePickerDialog(getActivity(),R.style.GrxDialogStyle, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(mCallback!=null) mCallback.GrxTimeSet((hourOfDay*60)+minute,mkey);
                else   Log.d("grxajustes", "null callback in timepicker");
            }
        },hours,minutes,true);

        /*if(getActivity().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            tpd.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            View view = getActivity().getLayoutInflater().inflate(R.layout.lay_view_titulo, null);
            TextView tit = (TextView) view.findViewById(R.id.txt_titulo);
            tit.setText("Prueba");
            tpd.setCustomTitle(view);
        }*/

        return tpd;
    }




}
