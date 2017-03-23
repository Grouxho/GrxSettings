package com.mods.grx.settings.dlgs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import com.mods.grx.settings.Common;

import java.util.Calendar;

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

public class DlgFrGrxDatePicker extends DialogFragment {

    private String mkey;
    private String mValue;
    private DlgFrGrxDatePicker.OnGrxDateSetListener mCallBack;

    private OnGrxDateSetListener mCallback;

    public DlgFrGrxDatePicker(){}


    public static DlgFrGrxDatePicker newInstance(String key, String value){

        DlgFrGrxDatePicker ret = new DlgFrGrxDatePicker();
        Bundle bundle = new Bundle();
        bundle.putString("key",key);
        bundle.putString("val",value);
        ret.setArguments(bundle);
        return ret;
    }

    public void setOnGrxDateSetListener(OnGrxDateSetListener callback){
        mCallback=callback;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public interface OnGrxDateSetListener{
        void GrxDateSet(String value, String key);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(mCallback==null) mCallback=(DlgFrGrxDatePicker.OnGrxDateSetListener) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
            mkey=getArguments().getString("key");
            mValue=getArguments().getString("val");

        int year = 0;
        int month = 0;
        int day = 0;
        Calendar now = Calendar.getInstance();
        if(mValue.isEmpty()){
            year = now.get(Calendar.YEAR);
            month = now.get(Calendar.MONTH);
            day = now.get(Calendar.DAY_OF_MONTH);
        }else{
            String[] arr= mValue.split("/");
            try{
                day = Integer.valueOf(arr[0]);
                month = Integer.valueOf(arr[1])-1;
                year = Integer.valueOf(arr[2]);
            }catch (NumberFormatException casque){
                System.out.println("Wrong date format (grxajustes)" + casque);
            }
        }
            DatePickerDialog dpd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    if(mCallback!=null) mCallback.GrxDateSet(String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year),mkey);
                    else {
                        Log.d("grxajustes", "null callback in datepicker");
                    }
                }
            }, year, month, day);
        return dpd;
    }

}
