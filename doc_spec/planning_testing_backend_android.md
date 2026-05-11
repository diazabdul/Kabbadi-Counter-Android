# Planning Testing Backend via Android (Retrofit)

Tanggal: 2026-05-11
Scope: Membuktikan backend berjalan dan dapat diakses dari Android app dengan konfigurasi URL fleksibel.

---

## 1. Tujuan

Tujuan utama:
1. Memastikan Android app bisa terhubung ke backend production/dev.
2. Memastikan endpoint backend merespons sesuai format yang diharapkan Android.
3. Menyediakan bukti teknis bahwa integrasi backend-ready sebelum fitur Bagian 2 penuh dikerjakan tim.

Bukan tujuan dokumen ini:
- Implementasi penuh live score/FCM/foreground service.
- Pengujian seluruh logic bisnis backend.

---

## 2. Strategi Konfigurasi URL

Konfigurasi base URL menggunakan `local.properties` agar fleksibel.

### 2.1 Key yang digunakan

Di `local.properties`:

```properties
BACKEND_BASE_URL=https://your-domain.com/api/
```

Catatan:
- URL **harus** diakhiri `/` untuk kompatibilitas Retrofit.
- File `local.properties` tidak di-commit, sehingga aman untuk setup lokal tiap developer.

### 2.2 Alur penggunaan

1. Gradle membaca `BACKEND_BASE_URL` dari `local.properties`.
2. Nilai diinject ke `BuildConfig.BASE_URL`.
3. Retrofit client menggunakan `BuildConfig.BASE_URL`.

### 2.3 Fallback

Jika key kosong/tidak ada, gunakan fallback:
- `http://10.0.2.2:8000/api/`

---

## 3. Vertical Slice Testing (Minimum Proof)

Agar cepat dan terukur, implementasi testing dibatasi ke vertical slice ini:

1. Endpoint `GET /match` via Retrofit.
2. Tampilkan hasil pada satu screen sederhana (RecyclerView/list atau text list).
3. State UI minimal:
- Loading
- Success
- Empty
- Error
4. Tombol Refresh untuk re-hit API.

Opsional (jika ada waktu):
5. Endpoint `POST /match/{id}/subscribe` dengan payload minimal (token dummy/manual) untuk bukti endpoint write.

---

## 4. Test Case Plan

## 4.1 Konfigurasi

TC-01: Base URL dari local.properties terbaca
- Langkah:
1. Isi `BACKEND_BASE_URL` di `local.properties`.
2. Build app.
3. Jalankan request endpoint.
- Expected:
- Request mengarah ke domain pada `BACKEND_BASE_URL`.

TC-02: Fallback URL aktif saat key tidak ada
- Langkah:
1. Hapus/comment `BACKEND_BASE_URL`.
2. Build app.
3. Jalankan request endpoint.
- Expected:
- Request mengarah ke fallback URL.

## 4.2 Endpoint GET /match

TC-03: Response sukses + data
- Prasyarat: backend up, punya data match.
- Langkah: buka screen match list.
- Expected:
- Loading tampil.
- Data list tampil.
- Status `LIVE/END` terlihat.

TC-04: Response sukses + data kosong
- Prasyarat: backend up, data kosong.
- Langkah: refresh list.
- Expected:
- Empty state muncul (bukan crash).

TC-05: Backend down / network error
- Prasyarat: backend dimatikan / URL salah.
- Langkah: refresh list.
- Expected:
- Error state muncul.
- App tidak freeze/crash.

TC-06: Response format tidak valid
- Prasyarat: backend kirim payload tidak sesuai DTO.
- Langkah: refresh list.
- Expected:
- Error ditangani aman.
- App tidak crash.

## 4.3 Opsional Subscribe

TC-07: Subscribe berhasil (LIVE)
- Prasyarat: ada match LIVE.
- Langkah: subscribe match LIVE.
- Expected:
- Request sukses.
- UI memberi feedback subscribe berhasil.

TC-08: Subscribe ditolak (END)
- Prasyarat: pilih match END.
- Langkah: subscribe.
- Expected:
- Error message backend tampil.
- App tidak crash.

---

## 5. Bukti yang Perlu Dikumpulkan

Untuk pelaporan/tunjuk bukti:
1. Screenshot `local.properties` (tanpa data sensitif selain URL).
2. Screenshot screen `Loading`, `Success`, `Empty`, `Error`.
3. Logcat ringkas yang menunjukkan request ke base URL aktif.
4. (Opsional) rekaman singkat: backend off -> error, backend on -> success.

---

## 6. Risiko dan Mitigasi

1. Risiko: URL tidak pakai trailing slash.
- Dampak: Retrofit error baseUrl.
- Mitigasi: validasi URL saat build/awal runtime, atau auto-append `/`.

2. Risiko: CORS/HTTPS certificate issue (jika endpoint tertentu).
- Dampak: request gagal.
- Mitigasi: gunakan endpoint HTTPS valid untuk production; untuk dev pakai endpoint lokal stabil.

3. Risiko: format response backend berubah.
- Dampak: parse DTO gagal.
- Mitigasi: buat error handling parse + logging jelas.

4. Risiko: ketergantungan jaringan tidak stabil.
- Dampak: false negative saat testing.
- Mitigasi: ulang test pada jaringan berbeda / backend health check.

---

## 7. Definisi Selesai (Definition of Done)

Testing backend dianggap cukup terbukti jika:
1. Android berhasil memanggil `GET /match` menggunakan `BuildConfig.BASE_URL` dari `local.properties`.
2. UI menampilkan state `loading/success/empty/error` dengan benar.
3. Perubahan URL di `local.properties` dapat mengubah target backend tanpa edit kode Kotlin.
4. Tidak ada crash pada skenario error umum.

---

## 8. Next Step Implementasi (Setelah Dokumen Disetujui)

1. Implement wiring `local.properties -> BuildConfig.BASE_URL`.
2. Buat Retrofit `ApiService` + DTO minimal `GET /match`.
3. Buat screen test sederhana untuk validasi endpoint.
4. Jalankan test case TC-01 s.d. TC-06.
5. Dokumentasikan hasil uji singkat di `doc_spec`.

## Progress Implementasi (2026-05-11)

Status: Implementasi vertical slice backend proof selesai (versi minimum).

Yang sudah diimplementasikan:
1. Konfigurasi URL fleksibel:
- `BACKEND_BASE_URL` dibaca dari `local.properties`.
- Diinject ke `BuildConfig.BASE_URL` via Gradle.
- Fallback default: `http://10.0.2.2:8000/api/`.

2. Integrasi Retrofit:
- `ApiClient` + logging interceptor.
- `MatchApiService` endpoint `GET /match`.
- DTO response dan item match.

3. Screen pembuktian backend:
- `BackendTestActivity`.
- State `Loading / Success / Empty / Error`.
- Tombol `Refresh`.
- Menampilkan Base URL aktif.

4. Navigasi:
- Tombol `Backend Test` ditambahkan di `MainActivity`.

5. Manifest:
- Permission `INTERNET` ditambahkan.

Catatan penggunaan:
- Pastikan `local.properties` memiliki key:
  `BACKEND_BASE_URL=https://domain-kamu.com/api/`
- URL harus diakhiri `/` (jika tidak, gradle akan auto-append).
