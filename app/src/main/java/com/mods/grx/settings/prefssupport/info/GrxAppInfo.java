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

import android.graphics.drawable.Drawable;

public class GrxAppInfo {

    private String mPackageName;
    private String mLabel;
    private Drawable mIcoApp;
    private String mActividad;
    private int mColor;

    public GrxAppInfo(String packageName, String Actividad, String label, Drawable ico_app, int color){

        mPackageName=packageName;
        mActividad=Actividad;
        mLabel=label;
        mIcoApp=ico_app;
        mColor=color;
    }

    public String nombre_app(){
        return mPackageName;
    }

    public String nombre_actividad(){
        return mActividad;
    }

    public String etiqueta_app(){
        return mLabel;
    }

    public int color_app(){
        return mColor;
    }

    public Drawable icono_app(){
        return mIcoApp;
    }

    public void pon_color_app(int color){
        mColor = color;
    }
}
