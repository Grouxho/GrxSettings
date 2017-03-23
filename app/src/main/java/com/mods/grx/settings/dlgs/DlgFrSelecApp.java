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

package com.mods.grx.settings.dlgs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mods.grx.settings.GrxPreferenceScreen;
import com.mods.grx.settings.R;
import com.mods.grx.settings.Common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DlgFrSelecApp extends DialogFragment implements AdapterView.OnItemClickListener {

    private ListView vLista;
    private OnGrxAppListener mCallBack;
    private List<ApplicationInfo> AppsInstaladas;
    private ProgressBar pb;
    private TextView vtxtprogressbar;

    AsyncTask<Void, Void, Void> loader;
    private boolean bOrdenar;
    private boolean bSistema;
    private String Title;
    private String key;
    private String mHelperFragmentName;

    private int lastposition=0;


    public interface OnGrxAppListener{
        void onGrxAppSel(DlgFrSelecApp dialog, String packagename);

    }

    public DlgFrSelecApp(){}


    public static DlgFrSelecApp newInstance(OnGrxAppListener callback, String help_frg, String key, String tit, boolean sysapps, boolean sort){

        DlgFrSelecApp ret = new DlgFrSelecApp();
        Bundle bundle = new Bundle();
        bundle.putString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY,help_frg);
        bundle.putBoolean("appsys", sysapps);
        bundle.putBoolean("sort", sort);
        bundle.putString("key",key);
        bundle.putString("tit",tit);
        ret.setArguments(bundle);
        ret.inicializa(callback);
        return ret;
    }
    private void inicializa(OnGrxAppListener callback){
        mCallBack=callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        lastposition=vLista.getFirstVisiblePosition();
        outState.putInt("lastpos", lastposition);

    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        mHelperFragmentName=getArguments().getString(Common.TAG_FRAGMENTHELPER_NAME_EXTRA_KEY);
        bSistema=getArguments().getBoolean("appsys");
        bOrdenar=getArguments().getBoolean("sort");
        key=getArguments().getString("key");
        Title=getArguments().getString("tit");

        if(state!=null){
            lastposition=state.getInt("lastpos");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(Title)
                .setView(select_app_view())
                .setNegativeButton(R.string.gs_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }


    public View select_app_view(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.pref_selectapp_dlg_lay,null);
        vLista = (ListView) view.findViewById(R.id.listaapps);

        vLista.setOnItemClickListener(this);
        vtxtprogressbar =(TextView) view.findViewById(R.id.txt_progressbar);
        vtxtprogressbar.setVisibility(View.VISIBLE);
        if(bOrdenar) vtxtprogressbar.setText(getString(R.string.gs_creando_ordenando_listado));
        else vtxtprogressbar.setText(getString(R.string.gs_creando_listado));
        pb = (ProgressBar) view.findViewById(R.id.progressBar);
        loader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb.setVisibility(View.VISIBLE);
                pb.refreshDrawableState();
            }

            @Override
            protected Void doInBackground(Void... params) {
                AppsInstaladas = crea_lista_aplicaciones();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pb.setVisibility(View.GONE);
                vtxtprogressbar.setVisibility(View.GONE);
                vLista.setAdapter(mAdapter);
                vLista.setSelection(lastposition);
            }
        }.execute();

        vLista.setFadingEdgeLength(2);
        vLista.setDividerHeight(Common.cDividerHeight);
        return view;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ApplicationInfo appinfo = (ApplicationInfo) parent.getItemAtPosition(position);
        if(mCallBack==null) {
            if(mHelperFragmentName.equals(Common.TAG_PREFSSCREEN_FRAGMENT)){
                GrxPreferenceScreen prefsScreen =(GrxPreferenceScreen) getFragmentManager().findFragmentByTag(Common.TAG_PREFSSCREEN_FRAGMENT);
                mCallBack=(DlgFrSelecApp.OnGrxAppListener) prefsScreen.find_callback(key);
            }else mCallBack=(DlgFrSelecApp.OnGrxAppListener) getFragmentManager().findFragmentByTag(mHelperFragmentName);
        }
        if(mCallBack!=null) mCallBack.onGrxAppSel(this, appinfo.packageName);
        dismiss();
    }

    private ArrayList<ApplicationInfo> crea_lista_aplicaciones(){
        List<ApplicationInfo> AppsTmp;
        ArrayList<ApplicationInfo> ListaApps;
        ListaApps = new ArrayList<ApplicationInfo>();
        AppsTmp = getActivity().getPackageManager().getInstalledApplications(0);
        for(int ind=0;ind<AppsTmp.size();ind++) {
            try {
                if(bSistema) ListaApps.add(AppsTmp.get(ind));
                else {
                    if (getActivity().getPackageManager().getLaunchIntentForPackage(AppsTmp.get(ind).packageName) != null) {
                        ListaApps.add(AppsTmp.get(ind));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

                if(bOrdenar) {
                    try{
                    Collections.sort(ListaApps, new Comparator<ApplicationInfo>() {
                        @Override
                        public int compare(ApplicationInfo A_appinfo, ApplicationInfo appinfo) {
                            try{
                                return String.CASE_INSENSITIVE_ORDER.compare(A_appinfo.loadLabel(getActivity().getPackageManager()).toString(), appinfo.loadLabel(getActivity().getPackageManager()).toString());
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }


       return ListaApps;
    }


    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        if((loader!=null) && (loader.getStatus()== AsyncTask.Status.RUNNING)){
            loader.cancel(true);
            loader=null;
            if(AppsInstaladas!=null) AppsInstaladas.clear();

        }

    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return AppsInstaladas.size();
        }

        @Override
        public Object getItem(int position) {
             return AppsInstaladas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CustomViewHolder cvh;
            if (convertView == null) {
                cvh = new CustomViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.pref_app_select_item, null);
                cvh.vimgLogo = (ImageView) convertView.findViewById(R.id.img_item_edit);
                cvh.vtxtName = (TextView) convertView.findViewById(R.id.txt_item_edit);
                cvh.vtxtpaquete = (TextView) convertView.findViewById(R.id.nombre_paquete);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            ApplicationInfo item = (ApplicationInfo) this.getItem(position);
            cvh.vtxtName.setText(item.loadLabel(getActivity().getPackageManager()));
            cvh.vimgLogo.setImageDrawable(item.loadIcon(getActivity().getPackageManager()));
            cvh.vtxtpaquete.setText(item.packageName);
            return convertView;
        }

        class CustomViewHolder {
            public ImageView vimgLogo;
            public TextView vtxtName;
            public TextView vtxtpaquete;

        }
    };

}
