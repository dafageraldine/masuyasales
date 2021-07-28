package com.yusuffahrudin.masuyamobileapp.api

import com.yusuffahrudin.masuyamobileapp.data.CekVersiResponse
import com.yusuffahrudin.masuyamobileapp.data.GudangJualResponse
import com.yusuffahrudin.masuyamobileapp.data.Result
import com.yusuffahrudin.masuyamobileapp.data.customer.*
import com.yusuffahrudin.masuyamobileapp.data.evaluasi_barang.*
import com.yusuffahrudin.masuyamobileapp.data.history_pembelian.HistoryPembelianResponse
import com.yusuffahrudin.masuyamobileapp.data.history_penjualan.HistoryPenjualanResponse
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.BrandResponse
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.KartuStokResponse
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.ProductResponse
import com.yusuffahrudin.masuyamobileapp.data.informasi_barang.StokResponse
import com.yusuffahrudin.masuyamobileapp.data.sales_order.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File
import java.util.*

interface ApiService {
    @get:GET("tools/select_kota.php")
    val kota: Call<KotaResponse?>?

    @FormUrlEncoded
    @POST("tools/select_kdgd_jual.php")
    fun getGdgJual(@Field("user_kota") userKota: String?,
                   @Field("user") user: String?,
                   @Field("level") level: String?): Call<GudangJualResponse?>?

    @get:GET("salesorder/select_note_ar_accept.php")
    val noteARAccept: Call<NoteOtorisasiResponse?>?

    @get:GET("salesorder/select_note_ar_reject.php")
    val noteARReject: Call<NoteOtorisasiResponse?>?

    @get:GET("salesorder/select_note_sl_accept.php")
    val noteSLAccept: Call<NoteOtorisasiResponse?>?

    @get:GET("masterbrg/select_brand.php")
    val brand: Call<BrandResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/update_detail_product.php")
    fun updateProduct(@Field("kdBrg") kdBrg: String?,
                      @Field("brand") brand: String?,
                      @Field("hrgExc") hrgExc: Double?,
                      @Field("hrgInc") hrgInc: Double?,
                      @Field("mTon") mTon: Double?,
                      @Field("mKubik") mKubik: Double?): Call<Result?>?

    @FormUrlEncoded
    @POST("masterbrg/select_history_penjualan.php")
    fun getHistoryPenjBrg(@Field("kdbrg") kdbrg: String?,
                           @Field("sales") sales: String?,
                          @Field("level") level: String?,
                           @Field("kdkota") kdkota: String?): Call<HistoryPenjualanResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/select_history_pembelian.php")
    fun getHistoryPembBrg(@Field("kdbrg") kdbrg: String?,
                       @Field("kdkota") kdkota: String?): Call<HistoryPembelianResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/evaluasi_barang_total.php")
    fun getEvaluasiBrgBar(@Field("kdbrg") kdbrg: String?,
                    @Field("kota") kota: String?,
                    @Field("cbg") cbg: Int): Call<EvaluasiBarangResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/evaluasi_barang_linechart.php")
    fun getEvaluasiBrgLine(@Field("kdbrg") kdbrg: String?,
                    @Field("kota") kota: String?,
                    @Field("cbg") cbg: Int): Call<EvaluasiBrgLineChartResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/evaluasi_barang_pertahun.php")
    fun getEvaluasiBrgPerthn(@Field("kdbrg") kdbrg: String?,
                          @Field("kota") kota: String?,
                          @Field("cbg") cbg: Int,
                          @Field("tahun") tahun: String?): Call<EvaluasiBarangResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/evaluasi_barang_detail_table.php")
    fun getEvaluasiBrgDetail(@Field("kdbrg") kdbrg: String?,
                          @Field("kota") kota: String?,
                          @Field("cbg") cbg: Int,
                          @Field("tahun") tahun: String?,
                          @Field("bulan") bulan: String?): Call<EvaluasiBrgDetailResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/select_loss_customer.php")
    fun getEvaluasiLossCust(@Field("kdbrg") kdbrg: String?,
                          @Field("kota") kota: String?): Call<EvaluasiLossCustResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/evaluasi_select_history.php")
    fun getEvaluasiBrgHistory(@Field("kdbrg") kdbrg: String?,
                              @Field("kdcust") kdcust: String?,
                            @Field("kota") kota: String?): Call<EvaluasiBrgHistoryResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/select_kartu_stok.php")
    fun getKartuStok(@Field("kdbrg") kdbrg: String?,
                     @Field("kdgd") kota: String?): Call<KartuStokResponse>?

    @FormUrlEncoded
    @POST("masterbrg/select_stok_barang.php")
    fun getStokBarang(@Field("kdbrg") kdbrg: String?,
                      @Field("kota") kota: String,
                      @Field("user") user: String): Observable<StokResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/select_stok_barang_rusak.php")
    fun getStokBarangRusak(@Field("kdbrg") kdbrg: String?,
                      @Field("kota") kota: String,
                      @Field("user") user: String): Observable<StokResponse?>?

    @FormUrlEncoded
    @POST("salesorder/select_otorisasi_sales_order.php")
    fun getSOOtorisasi(@Field("user") user: String?,
                       @Field("nobukti") nobukti: String?,
                       @Field("cust") cust: String?,
                       @Field("autho") autho: String?,
                       @Field("status") status: String?,
                       @Field("item") item: String?): Observable<SalesOrderResponse?>?

    @FormUrlEncoded
    @POST("salesorder/select_sales_order_all.php")
    fun getSOAll(@Field("user") user: String?,
                 @Field("itemCount") itemCount: Int,
                 @Field("nobukti") nobukti: String?,
                 @Field("cust") cust: String?,
                 @Field("autho") autho: String?,
                 @Field("status") status: String?,
                 @Field("item") item: String?): Observable<SalesOrderResponse?>?

    @FormUrlEncoded
    @POST("salesorder/select_sales_order_open.php")
    fun getSOOpen(@Field("user") user: String?,
                  @Field("itemCount") itemCount: Int,
                  @Field("nobukti") nobukti: String?,
                  @Field("cust") cust: String?,
                  @Field("autho") autho: String?,
                  @Field("status") status: String?,
                  @Field("item") item: String?): Observable<SalesOrderResponse?>?

    @FormUrlEncoded
    @POST("salesorder/select_sales_order_pending.php")
    fun getSOPending(@Field("user") user: String?,
                     @Field("itemCount") itemCount: Int,
                     @Field("nobukti") nobukti: String?,
                     @Field("cust") cust: String?,
                     @Field("autho") autho: String?,
                     @Field("status") status: String?,
                     @Field("item") item: String?): Observable<SalesOrderResponse?>?

    @FormUrlEncoded
    @POST("salesorder/select_sales_order_closed.php")
    fun getSOClose(@Field("user") user: String?,
                   @Field("itemCount") itemCount: Int,
                   @Field("nobukti") nobukti: String?,
                   @Field("cust") cust: String?,
                   @Field("autho") autho: String?,
                   @Field("status") status: String?,
                   @Field("item") item: String?): Observable<SalesOrderResponse?>?

    @FormUrlEncoded
    @POST("salesorder/select_barang.php")
    fun getItemSOforSale(@Field("kdcust") kdcust: String?,
                         @Field("kdgd") kdgd: String?,
                         @Field("status_pajak") statusPajak: String?,
                         @Field("itemCount") itemCount: Int): Observable<ItemJualResponse?>?

    @FormUrlEncoded
    @POST("salesorder/search_barang.php")
    fun findItemSOforSale(@Field("kdcust") kdcust: String?,
                          @Field("kdgd") kdgd: String?,
                          @Field("status_pajak") statusPajak: String?,
                          @Field("brg") brg: String?): Observable<ItemJualResponse?>?

    @FormUrlEncoded
    @POST("salesorder/insert_sales_order.php")
    fun saveSO(@FieldMap param: HashMap<String?, String?>?): Call<SalesOrderResponse?>?

    @GET("salesorder/cek_customer.php")
    fun cekCustomerSO(@Query("kdcust") kdcust: String?,
                      @Query("kdkel") kdkel: String?): Call<String?>?

    @FormUrlEncoded
    @POST("tools/cek_versi.php")
    fun versioncheck(@FieldMap param: HashMap<String?, String?>?): Call<CekVersiResponse?>?

    @FormUrlEncoded
    @POST("salesorder/cek_double.php")
    fun cekDoubleSO(@FieldMap param: HashMap<String?, String?>?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/cek_special_price.php")
    fun cekSpecialPrice(@Field("kdcust") kdcust: String?,
                        @Field("kdbrg") kdbrg: String?,
                        @Field("qty") qty: Double?,
                        @Field("tgl") tgl: String?,
                        @Field("tipe") tipe: String?,
                        @Field("nobukti") nobukti: String?,
                        @Field("harga") harga: Double?,
                        @Field("disc1") disc1: Double?,
                        @Field("disc2") disc2: Double?,
                        @Field("disc3") disc3: Double?): Call<Result?>?

    /*@Multipart
    @POST("salesorder/upload_image_po.php")
    fun uploadImagePO(@Part file: MultipartBody.Part,
                      @Part("file") name: RequestBody): Call<Result?>?*/

    @FormUrlEncoded
    @POST("salesorder/upload_image_po.php")
    fun uploadImagePO(@Field("file") file: String?,
                      @Field("name") name: String?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/delete_image_po.php")
    fun deleteImagePO(@Field("filename") fileName: String): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/select_so_header.php")
    fun getHeaderSO(@Field("no_order") nobukti: String?): Observable<SalesOrderResponse?>?

    @FormUrlEncoded
    @POST("salesorder/update_autho_so.php")
    fun updateOto(@Field("check") check: Int?,
                  @Field("tipe") tipe: String?,
                  @Field("ket") ket: String?,
                  @Field("tgl") tgl: String?,
                  @Field("level") level: String?,
                  @Field("who") who: String?,
                  @Field("nobukti") nobukti: String?,
                  @Field("undercost") undercost: Boolean?,
                  @Field("underbottom") underbottom: Boolean?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/cancel_sales_order.php")
    fun cancelSO(@FieldMap param: HashMap<String?, String?>?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/update_sales_order.php")
    fun updateSO(@FieldMap param: HashMap<String?, String?>?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/select_so_item.php")
    fun getDetailSO(@Field("no_order") nobukti: String?): Observable<ItemOrderResponse?>?

    @FormUrlEncoded
    @POST("customer/select_hutang_customer.php")
    fun getHutangCust(@Field("kdcust") kdcust: String?): Call<HutangCustResponse?>?

    @FormUrlEncoded
    @POST("customer/select_history_penjualan.php")
    fun getHistoryPenjCust(@Field("customer") customer: String?,
                           @Field("sales") sales: String?,
                           @Field("level") level: String?,
                           @Field("kdkota") kdkota: String?): Call<HistoryPenjualanResponse?>?

    @FormUrlEncoded
    @POST("customer/evaluasi_customer_total.php")
    fun getEvaluasiCustBar(@Field("kdcust") kdcust: String?): Call<EvaluasiCustomerResponse?>?

    @FormUrlEncoded
    @POST("customer/evaluasi_customer_linechart.php")
    fun getEvaluasiCustLine(@Field("kdcust") kdcust: String?): Call<EvaluasiCustLineChartResponse?>?

    @FormUrlEncoded
    @POST("customer/evaluasi_customer_pertahun.php")
    fun getEvaluasiCustPerthn(@Field("kdcust") kdcust: String?,
                          @Field("tahun") tahun: String?): Call<EvaluasiCustomerResponse?>?

    @FormUrlEncoded
    @POST("customer/evaluasi_customer_detail_table.php")
    fun getEvaluasiCustDetail(@Field("kdcust") kdcust: String?,
                          @Field("tahun") tahun: String?,
                          @Field("bulan") bulan: String?): Call<EvaluasiCustDetailResponse?>?

    @FormUrlEncoded
    @POST("customer/select_loss_item.php")
    fun getEvaluasiLossItem(@Field("kdcust") kdcust: String?,
                            @Field("kota") kota: String?): Call<EvaluasiLossItemResponse?>?

    @FormUrlEncoded
    @POST("customer/evaluasi_select_history.php")
    fun getEvaluasiCustHistory(@Field("kdcust") kdcust: String?,
                               @Field("kdbrg") kdbrg: String?,
                                @Field("kota") kota: String?): Call<EvaluasiCustHistoryResponse?>?

    @FormUrlEncoded
    @POST("historypenj/select_history_penjualan.php")
    fun getHistoryPenj(@Field("namabrg") nmbrg: String?,
                            @Field("customer") customer: String?,
                            @Field("sales") sales: String?,
                            @Field("from_tgl") from_tgl: String?,
                            @Field("to_tgl") to_tgl: String?,
                            @Field("kdkota") kdkota: String?): Observable<HistoryPenjualanResponse?>?

    @FormUrlEncoded
    @POST("historypenj/select_history_penjualan_item.php")
    fun getHistoryPenjItem(@Field("nofak") nofak: String?,
                               @Field("kdkota") kdkota: String?): Call<HistoryPenjualanResponse?>?

    @FormUrlEncoded
    @POST("historypemb/select_history_pembelian.php")
    fun getHistoryPemb(@Field("namabrg") nmbrg: String?,
                       @Field("supplier") supplier: String?,
                       @Field("from_tgl") from_tgl: String?,
                       @Field("to_tgl") to_tgl: String?,
                       @Field("kdkota") kdkota: String?): Observable<HistoryPembelianResponse?>?

    @FormUrlEncoded
    @POST("updateprice/specialprice/select_customer.php")
    fun getCustomerSpecialPrice(@Field("tipe") tipe: String?,
                                @Field("itemCount") itemCount: Int): Observable<CustomerResponse?>?

    @FormUrlEncoded
    @POST("updateprice/specialprice/search_customer.php")
    fun searchCustomerSpecialPrice(@Field("tipe") tipe: String?,
                                   @Field("cust") cust: String?): Observable<CustomerResponse?>?

    @FormUrlEncoded
    @POST("masterbrg/select.php")
    fun getBarang(@Field("namabrg") kdbrg: String?,
                  @Field("itemCount") itemCount: Int): Observable<ProductResponse?>?

    @FormUrlEncoded
    @POST("updateprice/specialprice/select_barang.php")
    fun getBarangSpecialPrice(@Field("tipe") tipe: String?,
                              @Field("kdcust") kdcust: String?,
                              @Field("itemCount") itemCount: Int): Observable<ProductResponse?>?

    @FormUrlEncoded
    @POST("updateprice/customer/select_pricelist_cust.php")
    fun getBarangPriceCust(@Field("kdcust") kdcust: String?,
                           @Field("itemCount") itemCount: Int): Observable<ProductResponse?>?

    @FormUrlEncoded
    @POST("updateprice/select_satuan.php")
    fun getBarangHrgMin(@Field("kdbrg") kdbrg: String?): Observable<ProductResponse?>?

    @FormUrlEncoded
    @POST("updateprice/select_pricelist.php")
    fun getBarangPricelist(@Field("kdcust") kdcust: String?,
                           @Field("kdbrg") kdbrg: String?): Observable<ProductResponse?>?

    @FormUrlEncoded
    @POST("updateprice/specialprice/insert_special_price.php")
    fun saveSpecialPrice(@FieldMap param: HashMap<String?, String?>?): Call<Result?>?

    @FormUrlEncoded
    @POST("updateprice/specialprice/insert_special_price_customers.php")
    fun saveSpecialPriceCust(@FieldMap param: HashMap<String?, String?>?): Call<Result?>?

    @FormUrlEncoded
    @POST("customer/select_all_customer.php")
    fun getAllCustomer(@Field("user") user: String?): Observable<CustomerResponse?>?

    @FormUrlEncoded
    @POST("customer/search_customer.php")
    fun searchCustomer(@Field("user") user: String?,
                       @Field("cust") cust: String?): Observable<CustomerResponse?>?

    @FormUrlEncoded
    @POST("salesorder/update_oto_od_multi_so.php")
    fun updateOtoODMultiSO(@FieldMap param: HashMap<String?, String?>?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/update_oto_od_so.php")
    fun updateOtoODSO(@FieldMap param: HashMap<String?, String?>?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/update_oto_harga_so.php")
    fun updateOtoHargaSO(@Field("underbottomsp") underbottomsp: String?,
                         @Field("underbottomso") underbottomso: String?,
                         @Field("undercost") undercost: String?,
                         @Field("oto") oto: String?,
                         @Field("who") who: String?,
                         @Field("nobukti") nobukti: String?,
                         @Field("kdbrg") kdbrg: String?): Call<Result?>?

    @FormUrlEncoded
    @POST("salesorder/insert_pelunasan.php")
    fun insertPelunasan(@Field("nobukti") nobukti: String?,
                        @Field("lunas") lunas: String?,
                        @Field("who") who: String?): Call<Result?>?
}