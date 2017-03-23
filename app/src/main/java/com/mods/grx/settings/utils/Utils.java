package com.mods.grx.settings.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.mods.grx.settings.Common;
import com.mods.grx.settings.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

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

public class Utils {

    Utils(){}



    public static void delete_file(String filename){
        File f = new File(filename);
        if(f.exists()){
            f.setReadable(true, false);
            f.setWritable(true, false);
            f.delete();
        }
    }

   /*
    UTILIDADES GESTIÓN FICHEROS
     */



    public static void delete_files(String foler, String extension){
        File dir = new File(foler);
        if(dir.exists()&&dir.isDirectory()){
            File ficheros[]=dir.listFiles();
            if(ficheros.length!=0){
                for(int ind=0;ind<ficheros.length;ind++){
                    if(ficheros[ind].getName().contains(extension)) ficheros[ind].delete();
                }

            }
        }
    }


    public static void delte_grx_tmp_files(String folder){
        File dir = new File(folder);
        if(dir.exists()&&dir.isDirectory()){
            File ficheros[]=dir.listFiles();
            if(ficheros.length!=0){
                for(int ind=0;ind<ficheros.length;ind++){
                    if(ficheros[ind].getName().contains(Common.TMP_PREFIX)) ficheros[ind].delete();
                }

            }
        }
    }

    public static void copy_files(String from_folder, String to_folder, String extension){

        if(to_folder.equals(from_folder)) return;
        File dir_origen = new File(from_folder);
        if(dir_origen.exists() && dir_origen.isDirectory()){
            File ficheros_origen[]=dir_origen.listFiles();
            if(ficheros_origen.length!=0){
                for(int ind=0;ind<ficheros_origen.length;ind++){
                    if(ficheros_origen[ind].getName().contains(extension)){
                        File fichero_destino = new File(to_folder+File.separator+ficheros_origen[ind].getName());

                        file_copy(ficheros_origen[ind],fichero_destino);
                    }
                }
            }
        }
    }

    public static void file_copy(File ori_file, File dest_file){

        try {
            FileInputStream i_s = new FileInputStream(ori_file);
            FileOutputStream o_s = new FileOutputStream(dest_file);
            FileChannel inChannel = i_s.getChannel();
            FileChannel outChannel = o_s.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            i_s.close();
            o_s.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void show_toast(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    public static void file_copy(String ori_file_name, String dest_file_name){
        if(ori_file_name==null || dest_file_name == null) {
            return;
        }

        File file_in = new File(ori_file_name);
        File file_out = new File(dest_file_name);
        if(file_in.exists()) Utils.file_copy(file_in,file_out);
    }

    public static void delete_file(File f){
        if(f!=null) f.delete();
    }




    public static void delete_files_or_create_folder(String dest_folder, String extension){
        File f = new File(dest_folder);
        if(f.exists() && f.isDirectory()) {
            File ficheros_origen[]=f.listFiles();
            if(ficheros_origen.length!=0) {
                for (int ind = 0; ind < ficheros_origen.length; ind++) {
                    if (ficheros_origen[ind].getName().contains(extension))
                        delete_file(ficheros_origen[ind]);

                }
            }

        }else f.mkdirs();
    }

    public static void create_folder(String folder){
        File f = new File(folder);
        if(!f.exists()) f.mkdirs();
    }

    public static void fix_foler_permissions(String folder, String extension){
        File f = new File(folder);
        if(f.exists() && f.isDirectory()) {
            File ficheros_origen[]=f.listFiles();
            if(ficheros_origen.length!=0) {
                for (int ind = 0; ind < ficheros_origen.length; ind++) {
                    if (ficheros_origen[ind].getName().contains(extension))
                        ficheros_origen[ind].setWritable(true,false);
                    ficheros_origen[ind].setReadable(true,false);

                }
            }

        }
    }


    public static String get_formatted_string_from_array_res(Context context, int array_id, String separator){
        String array[] = context.getResources().getStringArray(array_id);
        String output = "";
        for(int i=0;i<array.length;i++){
            output+=array[i];
            output+=separator;
        }
        return output;
    }


    public static int find_pos_in_string_array(String[] values, String key){
        int pos = 0;
        for(int i=0; i<values.length;i++){
            if(values[i].equals(key)){
                pos=i;
                break;
            }
        }
        return pos;
    }


    public static String get_activity_label_from_intent(Context context, Intent intent){
        String string;
        string = intent.getStringExtra(Common.EXTRA_URI_LABEL);
        if(string==null) {
            try{
                ComponentName c_n = intent.getComponent();
                if(c_n!=null) {
                    ActivityInfo a_i = context.getPackageManager().getActivityInfo(c_n, 0);
                    if(a_i!=null) string = a_i.loadLabel(context.getPackageManager()).toString();
                }

            }catch (Exception e){}
        }

        if(string==null) string = "?";
        return string;
    }

    public static String get_label_from_packagename_activityname(Context context, String nombre_paquete, String nombre_actividad){

        String etiqueta="-";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(nombre_paquete,nombre_actividad ));

        ResolveInfo ri = context.getPackageManager().resolveActivity(intent,0);
        if(ri!=null){
            etiqueta=ri.loadLabel(context.getPackageManager()).toString();
        }else etiqueta="";


        ApplicationInfo aitemp;
        String eti_app="";
        try {
            aitemp=context.getPackageManager().getApplicationInfo(ri.activityInfo.packageName,0);
            eti_app=context.getPackageManager().getApplicationLabel(aitemp).toString();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(eti_app.equals(etiqueta)) etiqueta=nombre_actividad;
        else {
            etiqueta=etiqueta;
        }
        if(etiqueta.contains(".")) etiqueta=Utils.format_label(etiqueta);
        return etiqueta;

    }

    public static String format_label(String label){
        return ".." + label.substring(Math.max(0, label.length() - Common.ACTIVITY_LABEL_MAX_CHARS));
    }


    public static Drawable get_icon_from_resolveinfo(Context context, Intent intent){
        Drawable drawable = null;
        ResolveInfo ri = context.getPackageManager().resolveActivity(intent,0);
        if(ri!=null){
            try{
                drawable=ri.loadIcon(context.getPackageManager());
            }catch (Exception e){}
        }
        return drawable;
    }


    public static Drawable get_icon_from_intent(Context context, Intent intent){

        Drawable drawable=null;
        try {
            ComponentName c_n = intent.getComponent();
            if(c_n!=null) {
                ActivityInfo a_i = context.getPackageManager().getActivityInfo(c_n, 0);
                if(a_i!=null) drawable = a_i.loadIcon(context.getPackageManager());
            }
        }catch (Exception e){}
        if(drawable==null){
            ResolveInfo ri = context.getPackageManager().resolveActivity(intent,0);
            if(ri!=null){
                try{
                    drawable=ri.loadIcon(context.getPackageManager());
                }catch (Exception e){}
            }
        }

        //if(drawable==null) drawable=context.getDrawable(R.drawable.ic_no_encontrada);
        return drawable;
    }

    public static Drawable get_drawable_from_path(Context contex, String path){

        Drawable tmp=null;
        try {
            File f_i = new File(path);
            if(f_i.exists() && f_i.canRead() ){
                FileInputStream i_s = new FileInputStream(f_i);
                tmp = new BitmapDrawable(contex.getResources(), BitmapFactory.decodeStream(i_s));
                i_s.close();
            }
        }catch (Exception e){
            Toast.makeText(contex,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        return tmp;
    }

    public static void animate_textview_marquee(TextView textView){
         textView.setTextIsSelectable(true);
        textView.setSingleLine(true);
        textView.setHorizontallyScrolling(true);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setMarqueeRepeatLimit(1);
    }

    public static void animate_textview_marquee_forever(TextView textView){
        textView.setTextIsSelectable(true);
        textView.setSingleLine(true);
        textView.setHorizontallyScrolling(true);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);



    }



    public static String get_drawable_name_from_intent(Intent intent){

        return intent.getStringExtra(Common.EXTRA_URI_DRAWABLE_NAME);
    }


    public static Drawable get_drawable_from_intent(Context context, Intent intent){
        Drawable drawable = null;
        String icon_name = intent.getStringExtra(Common.EXTRA_URI_ICON);
        if(icon_name != null){
            drawable = Utils.get_drawable_from_path(context, icon_name);
        }
        if(drawable==null){
            String ndrw = intent.getStringExtra(Common.EXTRA_URI_DRAWABLE_NAME);
            if(ndrw!=null) {
                int idimg = context.getResources().getIdentifier(ndrw,"drawable",context.getPackageName());
                if(idimg!=0) drawable=context.getResources().getDrawable(idimg);
            }
        }
        if(drawable==null){
            drawable=Utils.get_icon_from_intent(context, intent);
        }
        return drawable;
    }


    public static void delete_grx_icon_file_from_intent(Intent intent){
        String file_name=null;
        if(intent!=null) file_name = intent.getStringExtra(Common.EXTRA_URI_ICON);
        if(file_name!=null){
            File f_ico = new File(file_name);
            if(f_ico!=null && f_ico.exists()) {
                f_ico.delete();
            }
        }
    }

    public static void delete_grx_icon_file_from_intent(Intent intent, String starts_with){
        String file_name=null;
        if(intent!=null) file_name = intent.getStringExtra(Common.EXTRA_URI_ICON);
        if(file_name!=null){
            File f_ico = new File(file_name);
            if(f_ico!=null && f_ico.exists() && f_ico.getName().startsWith(starts_with)) {
                f_ico.delete();
            }
        }
    }

    public static void delete_grx_icon_file_from_uri_string(String uri){
        String file_name=null;
        Intent intent = null;
        try {
            intent = Intent.parseUri(uri, 0);
        }catch (URISyntaxException e) {}
        if(intent!=null) file_name = intent.getStringExtra(Common.EXTRA_URI_ICON);
        if(file_name!=null){
            File f_ico = new File(file_name);
            if(f_ico!=null && f_ico.exists()) {
                f_ico.delete();
            }
        }
    }

    public static void delete_grx_icon_file_from_uri_string(String uri, String starts_with){
        String file_name=null;
        Intent intent = null;
        try {
            intent = Intent.parseUri(uri, 0);
        }catch (URISyntaxException e) {}
        if(intent!=null) file_name = intent.getStringExtra(Common.EXTRA_URI_ICON);
        if(file_name!=null){
            File f_ico = new File(file_name);
            if(f_ico!=null && f_ico.exists() && f_ico.getName().startsWith(starts_with)) {
                f_ico.delete();
            }
        }
    }

    public static void set_read_write_file_permissions(String file_name){
        if(file_name==null || file_name.isEmpty()) return;
        try {
            File file = new File(file_name);
            if(file.exists()){
                file.setReadable(true,false);
                file.setWritable(true,false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static int get_array_id(Context context,String array_name){
        if(array_name!=null && !array_name.isEmpty()){
            String aux[]=array_name.split("/");
            return context.getResources().getIdentifier(aux[1], "array", context.getPackageName() );
        }else return 0;
    }


    public static String get_file_name_from_intent(Intent intent){
        String file_name = null;
        if(intent!=null){
            file_name=intent.getStringExtra(Common.EXTRA_URI_ICON);
        }
        return file_name;

    }

    public static String get_file_name_from_uri_string(String uri){
        String file_name = null;
        Intent intent;
        try {
            intent = Intent.parseUri(uri, 0);
        }catch (URISyntaxException e) {
            return file_name;
        }
        if(intent!=null){
            file_name=intent.getStringExtra(Common.EXTRA_URI_ICON);
        }
        return file_name;
    }

    public static String get_short_file_name_from_uri_string(String uri){

        String file_name = null;
        Intent intent;
        try {
            intent = Intent.parseUri(uri, 0);
        }catch (URISyntaxException e) {
            return file_name;
        }
        if(intent!=null){
            file_name=intent.getStringExtra(Common.EXTRA_URI_ICON);
        }

        if(file_name!=null) {
            File file = new File(file_name);
            if(file.exists()) return file.getName();
        }
        return file_name;

    }

    public static String get_short_file_name_from_string(String filename){
        String file_name = null;
        if(filename!=null) {
            File file = new File(filename);
            if(file.exists()) return file.getName();
        }
        return file_name;

    }

    public static String change_extra_value_in_uri_string(String uri, String extra_key, String new_value){
        Intent intent;
        try {
            intent = Intent.parseUri(uri, 0);
        }catch (URISyntaxException e) {
            return null;
        }

        if(intent!=null) {
            intent.putExtra(extra_key, new_value);
            return intent.toUri(0);
        }
        return null;
    }

    public static boolean rename_grx_tmp_file(String tmp_name){

        String n_f=tmp_name.replace("grxtmp","");
        File f_t = new File(tmp_name);
        if(f_t.exists()){
            File f_f = new File(n_f);
            f_t.renameTo(f_f);
            f_f.setReadable(true,false);
            f_f.setWritable(true,false);
            return true;
        }return false;
    }

    public static int get_contrast_text_color(int bgcolor){
        int textcolor;
        double luminance = ColorUtils.calculateLuminance(bgcolor);
        if(luminance>(double) 0.5) textcolor = 0xff222222;
        else textcolor = 0xffffffff;
        return textcolor;
    }

    public static Drawable get_application_icon(Context context, String packagename){
        Drawable drw = null;
        try {
            drw= context.getPackageManager().getApplicationInfo(packagename,0).loadIcon(context.getPackageManager());
            }catch (Exception e){}
        return drw;
    }





    public static boolean is_app_installed(Context context, String packagename){
        boolean t = false;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(packagename,0);
            if(pi!= null) t= true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }


    public static boolean is_activity_installed(Context context, String packagename, String activityname){
        boolean t = false;
        try {
            ComponentName componentName = new ComponentName(packagename, activityname);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
            if(resolveInfo!=null) t=true;
        }catch (Exception e){}
        return t;
    }


    public static String get_activity_name_from_package_name(Context context, String packageName){
        String className=null;
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            className = launchIntent.getComponent().getClassName();
        }catch (Exception e){

        }
        return className;
    }



    public static String get_app_name(Context context, String packagename) {
        String name;
        if(packagename==null || packagename.isEmpty()) name ="";
        else {
            try {
                name = context.getPackageManager().getApplicationInfo(packagename, 0).loadLabel(context.getPackageManager()).toString();
            } catch (Exception e) {
                e.printStackTrace();
                name=context.getResources().getString(R.string.gs_no_instalada, packagename);
            }
        }
        return name;
    }

    public static void change_group_key_value(Context context, String group_key){
        int i = Settings.System.getInt(context.getContentResolver(),group_key,0);
        if (i<32) i++;
        else i=1;
        Settings.System.putInt(context.getContentResolver(),group_key,i);
    }

    public static void send_bc1(Context context){
        Intent intent = new Intent();
        try {
            intent.setAction(context.getResources().getString(R.string.gs_grxBc1));
            context.sendBroadcast(intent);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    public static void send_bc2(final Context context){
        Runnable BC = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                try{
                    intent.setAction(context.getResources().getString(R.string.gs_grxBc2));
                    context.sendBroadcast(intent);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        };

        Handler handler = new Handler();
        handler.removeCallbacks(BC);
        handler.postDelayed(BC,Long.valueOf(400));
    }





}
