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

package com.mods.grx.settings.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mods.grx.settings.R;

import java.io.File;


public class AdapterBackups extends BaseAdapter {
    Context context;
    File ficheros[];


    public void AdapterBackups(Context ctx, File arr[]){
        context=ctx;
        ficheros=arr;

    }

    @Override
    public int getCount() {
        return ficheros.length;
    }

    @Override
    public Object getItem(int position) {
        return ficheros[position];
    }

    @Override
    public long getItemId(int position) {
        long l = (long) position;
        return l;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View vista = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1,null);
        TextView tv = (TextView) vista.findViewById(android.R.id.text1);
        tv.setText(ficheros[position].getName().replace("."+context.getResources().getString(R.string.gs_backup_ext),""));
        return vista;
    }
}
