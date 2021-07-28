package com.yusuffahrudin.masuyamobileapp.informasibarang;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.yusuffahrudin.masuyamobileapp.R;
import com.yusuffahrudin.masuyamobileapp.adapter.AdapterVPFotoProdukKecil;
import com.yusuffahrudin.masuyamobileapp.api.API;
import com.yusuffahrudin.masuyamobileapp.api.ApiService;
import com.yusuffahrudin.masuyamobileapp.controller.AppController;
import com.yusuffahrudin.masuyamobileapp.controller.DialogAlert;
import com.yusuffahrudin.masuyamobileapp.data.ArrayTampung;
import com.yusuffahrudin.masuyamobileapp.data.Result;
import com.yusuffahrudin.masuyamobileapp.data.UserAkses;
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.BrandResponse;
import com.yusuffahrudin.masuyamobileapp.util.Server;
import com.yusuffahrudin.masuyamobileapp.util.SessionManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by yusuf fahrudin on 17-01-2018.
 */

public class DetailBarangFragment extends Fragment {
    private String kdbrg, kdkota, password;
    private Double hargaJualExc = 0.0, hargaJualInc = 0.0;
    private String hrgjualmin_excppn, hrgjualmin_incppn, user;
    private boolean isCanViewPrice = false;
    private int success;
    private EditText edt_jenis, edt_nmtype, edt_packing3;
    private AutoCompleteTextView edt_brand;
    private ImageView selectBrand;
    private EditText edt_tax, edt_mton, edt_mkubik, edt_hrg_exc, edt_hrg_inc;
    private TextView tv_kdbrg, tv_nmbrg;
    private Button btn_harga, btn_save;
    //private BottomSheetDialog dialog;
    View layout_view_popup ;
    LayoutInflater layout_inflater_popup;
    AlertDialog.Builder ad_builder_popup_notif;
    AlertDialog alertDialog_popup_notif;
    private ArrayList<String> arrayBrand;
    private ArrayList<String> arrayFoto;
    private ArrayAdapter adapterBrand;
    private final List<UserAkses> listAkses = ArrayTampung.getListAkses();
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("in", "ID"));

    private static final String TAG = DetailBarangFragment.class.getSimpleName();

    public DetailBarangFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail_barang, container, false);
        SessionManager sessionManager = new SessionManager(Objects.requireNonNull(this.getActivity()).getApplicationContext());
        layout_inflater_popup = (LayoutInflater) DetailBarangFragment.this.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        HashMap<String, String> cache = sessionManager.getUserDetails();
        kdkota = cache.get(SessionManager.kdkota);
        user = cache.get(SessionManager.kunci_email);

        Intent i = this.getActivity().getIntent();
        kdbrg = Objects.requireNonNull(i.getExtras()).getString(ListBarangActivity.KDBRG);

        tv_kdbrg = view.findViewById(R.id.tv_kdbrg);
        tv_nmbrg = view.findViewById(R.id.tv_nmbrg);
        edt_jenis = view.findViewById(R.id.edt_jenis);
        edt_nmtype = view.findViewById(R.id.edt_nmtype);
        edt_packing3 = view.findViewById(R.id.edt_packing3);
        edt_brand = view.findViewById(R.id.edt_brand);
        edt_tax = view.findViewById(R.id.edt_tax);
        edt_mton = view.findViewById(R.id.edt_mton);
        edt_mkubik = view.findViewById(R.id.edt_mkubik);
        edt_hrg_exc = view.findViewById(R.id.edt_hrg_exc);
        edt_hrg_inc = view.findViewById(R.id.edt_hrg_inc);
        btn_harga = view.findViewById(R.id.btn_harga);
        btn_save = view.findViewById(R.id.btn_simpan);
        selectBrand = view.findViewById(R.id.select_brand);
        ViewPager vp_image = view.findViewById(R.id.vp_image);
        CircleIndicator indicator = view.findViewById(R.id.indicator);

        SetFoto();
        GetBrand();
        selectBrand.setOnClickListener(view12 -> edt_brand.showDropDown());

        AdapterVPFotoProdukKecil adapterVPFotoProdukKecil = new AdapterVPFotoProdukKecil(this.getActivity(), arrayFoto, kdbrg);
        vp_image.setAdapter(adapterVPFotoProdukKecil);
        indicator.setViewPager(vp_image);

        GetData();

        edt_hrg_exc.addTextChangedListener(new TextWatcher() {
               @Override
               public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

               @Override
               public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

               @Override
               public void afterTextChanged(Editable editable) {
                   if (edt_tax.getText().toString().equalsIgnoreCase("PPPN")){
                       if (!edt_hrg_exc.getText().toString().equals("")){
                           Double hrg = Double.parseDouble(UbahAngka(edt_hrg_exc.getText().toString())) * 1.1;
                           String hargaincppn = nf.format(hrg);
                           edt_hrg_inc.setText(hargaincppn);
                       }
                   }
               }
        });
        btn_harga.setOnClickListener(view1 -> DialogPassword());
        btn_save.setOnClickListener(view2 -> {
                    if (isCanViewPrice){
                        SaveData(kdbrg, edt_brand.getText().toString(),
                                Double.parseDouble(UbahAngka(edt_hrg_exc.getText().toString())),
                                Double.parseDouble(UbahAngka(edt_hrg_inc.getText().toString())),
                                Double.parseDouble(UbahAngka(edt_mton.getText().toString())),
                                Double.parseDouble(UbahAngka(edt_mkubik.getText().toString()))
                        );
                    } else {
                        SaveData(kdbrg, edt_brand.getText().toString(), hargaJualExc, hargaJualInc,
                                Double.parseDouble(UbahAngka(edt_mton.getText().toString())),
                                Double.parseDouble(UbahAngka(edt_mkubik.getText().toString()))
                        );
                    }

        });

        return view;
    }

    private void GetData() {
        Server a = new Server(kdkota);
        String url_select_detail_produk = a.URL() + "masterbrg/select_detail_barang.php";

        final ProgressDialog progressDialog = ProgressDialog.show(this.getActivity(), "", "Please Wait...");
        new Thread() {
            public void run() {
                try{
                    sleep(10000);
                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        }.start();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_select_detail_produk, response -> {
            Log.d(TAG, "Response : " + response);
            ShowData(response);
            CekAkses();
            progressDialog.dismiss();
        }, error -> {
            Log.e(TAG, "Error : "+ error.getMessage());
            FirebaseCrashlytics.getInstance().recordException(error);
            new DialogAlert(getString(R.string.error_pengambilan_data)+" "+error.getMessage(), "error", Objects.requireNonNull(getActivity()));
            progressDialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() {
                //posting parameter ke post url
                Map<String, String> params = new HashMap<>();
                params.put("kdbrg", kdbrg);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));
    }

    private void ShowData(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");

            JSONObject obj = result.getJSONObject(0);

            tv_kdbrg.setText(kdbrg);
            tv_nmbrg.setText(obj.getString("NmBrg"));
            edt_brand.setText(obj.getString("KdMerk"));
            edt_nmtype.setText(obj.getString("NmType").trim());
            edt_jenis.setText(obj.getString("Jenis"));
            edt_packing3.setText(obj.getString("Packing3"));
            edt_tax.setText(obj.getString("JnsPpn"));
            edt_hrg_exc.setText(nf.format(obj.getDouble("HrgExc")));
            if (edt_tax.getText().toString().equalsIgnoreCase("PPPN")){
                if (!edt_hrg_exc.getText().toString().equals("")){
                    Double hrg = Double.parseDouble(UbahAngka(edt_hrg_exc.getText().toString())) * 1.1;
                    String hargaincppn = nf.format(hrg);
                    edt_hrg_inc.setText(hargaincppn);
                }
            } else {
                edt_hrg_inc.setText(edt_hrg_exc.getText().toString());
            }
            edt_mton.setText(nf.format(obj.getDouble("MTon")));
            edt_mkubik.setText(nf.format(obj.getDouble("MKubik")));

            hrgjualmin_excppn = nf.format(obj.getDouble("HrgMinExc"));
            hrgjualmin_incppn = nf.format(obj.getDouble("HrgMinInc"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void CekAkses(){
        for (int i=0; i<listAkses.size(); i++){
            String str = listAkses.get(i).getModul();
            String modul = str.substring(str.indexOf("-") + 1);
            if (str.contains("Informasi Barang")){
                if (modul.equalsIgnoreCase("Harga Jual Min")){
                    btn_harga.setEnabled(listAkses.get(i).getAkses() == 1);
                }
                if (modul.equalsIgnoreCase("Detail Barang")){
                    if  (listAkses.get(i).getEdit() == 1) {
                        edt_brand.setEnabled(true);
                        selectBrand.setEnabled(true);
                        edt_mkubik.setEnabled(true);
                        edt_mton.setEnabled(true);
                        btn_save.setVisibility(View.VISIBLE);
                    } else {
                        edt_brand.setEnabled(false);
                        selectBrand.setEnabled(false);
                        edt_mkubik.setEnabled(false);
                        edt_mton.setEnabled(false);
                    }
                }
                if (modul.equalsIgnoreCase("Harga Jual")) {
                    if  (listAkses.get(i).getAkses() == 0) {
                        hargaJualExc = Double.parseDouble(UbahAngka(edt_hrg_exc.getText().toString()));
                        hargaJualInc = Double.parseDouble(UbahAngka(edt_hrg_inc.getText().toString()));
                        edt_hrg_inc.setText("0");
                        edt_hrg_exc.setText("0");
                    }
                    if (listAkses.get(i).getEdit() == 1) {
                        isCanViewPrice = true;
                        edt_hrg_exc.setEnabled(true);
                        btn_save.setVisibility(View.VISIBLE);
                    } else {
                        edt_hrg_exc.setEnabled(false);
                    }
                }
            }
        }
    }

    private void SetFoto(){
        Server a = new Server(kdkota);
        arrayFoto = new ArrayList<>();
        arrayFoto.add(a.URL_IMAGE()+kdbrg+".jpg");
        arrayFoto.add(a.URL_IMAGE()+kdbrg+"_1.jpg");
        arrayFoto.add(a.URL_IMAGE()+kdbrg+"_2.jpg");
    }

    private void GetBrand(){
        arrayBrand = new ArrayList<>();
        arrayBrand.add("");
        API.INSTANCE.instance().create(ApiService.class).getBrand()
                .enqueue(new Callback<BrandResponse>() {
                    @Override
                    public void onResponse(Call<BrandResponse> call, Response<BrandResponse> response) {
                        if(response.code() == 200) {
                            for (int i=0; i<response.body().getResult().size(); i++){
                                arrayBrand.add(response.body().getResult().get(i).getKdmerk());
                            }
                            adapterBrand = new ArrayAdapter(requireContext(), R.layout.spinner_black, arrayBrand);
                            edt_brand.setAdapter(adapterBrand);
                        }
                    }

                    @Override
                    public void onFailure(Call<BrandResponse> call, Throwable e) {
                        Log.d("Get Brand", "Error "+e.getMessage());
                        FirebaseCrashlytics.getInstance().recordException(e);
                        new DialogAlert(getString(R.string.error_pengambilan_data)+" "+e.getMessage(), "error", requireActivity());
                    }
                });
    }

    // untuk menampilkan dialog password
    @SuppressLint("InflateParams")
    private void DialogPassword() {


        layout_view_popup = layout_inflater_popup.inflate(R.layout.dialog_password, null);
        ad_builder_popup_notif = new AlertDialog.Builder(DetailBarangFragment.this.getContext());
        ad_builder_popup_notif.setView(layout_view_popup);
        ad_builder_popup_notif.setCancelable(false);
        alertDialog_popup_notif = ad_builder_popup_notif.create();
        Objects.requireNonNull(alertDialog_popup_notif.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog_popup_notif.show();

        /*dialog = new BottomSheetDialog(requireActivity(), R.style.SheetDialog);
        View dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_password, null);
        dialog.setContentView(dialogView);
        dialog.setTitle("Password");*/

        final EditText edt_password = layout_view_popup.findViewById(R.id.edt_password);
        Button btnOK = layout_view_popup.findViewById(R.id.btn_ok);
        Button btnCancel = layout_view_popup.findViewById(R.id.btn_cancel);

        btnOK.setOnClickListener(v -> {
            password = edt_password.getText().toString();
            System.out.println(1);
            CekPassword();
            alertDialog_popup_notif.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog_popup_notif.dismiss();
            edt_password.setText(null);
        });

        alertDialog_popup_notif.show();
    }

    // untuk menampilkan dialog harga
    @SuppressLint("InflateParams")
    private void DialogHarga() {
        layout_view_popup = layout_inflater_popup.inflate(R.layout.dialog_hargajualmin, null);
        ad_builder_popup_notif = new AlertDialog.Builder(DetailBarangFragment.this.getContext());
        ad_builder_popup_notif.setView(layout_view_popup);
        ad_builder_popup_notif.setCancelable(false);
        alertDialog_popup_notif = ad_builder_popup_notif.create();
        Objects.requireNonNull(alertDialog_popup_notif.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        alertDialog_popup_notif.show();

        /*dialog = new BottomSheetDialog(requireActivity(), R.style.SheetDialog);
        View dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_hargajualmin, null);
        dialog.setContentView(dialogView);
        dialog.setTitle("Harga Jual Minimum");*/

        EditText edt_hrgjualmin_excppn = layout_view_popup.findViewById(R.id.edt_hrgjualmin_excppn);
        EditText edt_hrgjualmin_incppn = layout_view_popup.findViewById(R.id.edt_hrgjualmin_incppn);
        Button btnOK = layout_view_popup.findViewById(R.id.btn_ok);
        edt_hrgjualmin_excppn.setText(hrgjualmin_excppn);
        edt_hrgjualmin_incppn.setText(hrgjualmin_incppn);

        btnOK.setOnClickListener(v -> alertDialog_popup_notif.dismiss());

        alertDialog_popup_notif.show();
    }

    private void CekPassword(){
        System.out.println(2);
        Server a = new Server(kdkota);
        String url_login = a.URL() + "tools/login.php";
        Log.v(TAG, "url activity_login : "+ url_login);

        System.out.println(3);
        final StringRequest strReq = new StringRequest(Request.Method.POST, url_login, response -> {
            Log.d(TAG, "Response : "+ response);

            try{
                System.out.println(4+" response = "+response);
                JSONObject jObj = new JSONObject(response);
                success = jObj.getInt("success");

                //cek error node pada JSON
                if (success == 1){
                    System.out.println(5);
                    DialogHarga();
                } else {
                    System.out.println(6);
                    Toast.makeText(getActivity(), "Password yang anda masukkan salah!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e){
                System.out.println(7);
                e.printStackTrace();
                new DialogAlert(e.toString(), "error", Objects.requireNonNull(getActivity()));
            }
        }, error -> {
            System.out.println(8);
            Log.e(TAG, "Error Volley : "+error.getMessage());
            new DialogAlert(error.getMessage(), "error", Objects.requireNonNull(getActivity()));
        }) {

            @Override
            protected Map<String, String> getParams() {
                //Posting parameter ke post url
                Map<String, String> params = new HashMap<>();
                params.put("user", user);
                params.put("pass", password);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, getResources().getString(R.string.tag_json_obj));
    }

    private void SaveData(String kdBrg, String brand, Double hrgExc, Double hrgInc, Double mTon, Double mKubik){
        API.INSTANCE.instance().create(ApiService.class)
                .updateProduct(kdBrg, brand,hrgExc,hrgInc,mTon,mKubik)
                .enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        if (response.isSuccessful()){
                            String message = response.body().getMessage();
                            if (response.body().getSuccess() == 1){
                                new DialogAlert(message, "success", requireActivity());
                            } else {
                                new DialogAlert(message, "error", requireActivity());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable e) {
                        new DialogAlert(e.getMessage(), "error", requireActivity());
                        Log.d("Update Product", "Error "+e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    private String UbahAngka(String angka){
        String buangRibuan = angka.replace(".","");
        return buangRibuan.replace(",",".");
    }
}
