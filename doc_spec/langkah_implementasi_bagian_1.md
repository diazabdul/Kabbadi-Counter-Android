# Langkah Implementasi Bertahap Bagian 1 (Aman dari Crash)

Dokumen ini adalah rencana implementasi **Bagian 1** secara bertahap dengan pendekatan micro-step + verification gate.

Tujuan:
- Meminimalkan risiko crash.
- Setiap tahap bisa diverifikasi sebelum lanjut.
- Mudah rollback jika ada masalah.

Tanggal: 2026-05-11

---

## Prinsip Eksekusi

1. Satu tahap = satu scope kecil.
2. Setelah setiap tahap, wajib verifikasi build + smoke test.
3. Jangan ubah Room schema dan UI besar dalam commit yang sama.
4. Jika ada crash, perbaiki dulu tahap aktif sebelum lanjut.

---

## Baseline Sebelum Mulai

1. Pastikan project dalam kondisi bersih (setelah undo).
2. Buka project di Android Studio.
3. Sync Gradle.
4. Jalankan app baseline sekali untuk memastikan kondisi awal stabil.

Verification gate baseline:
- App bisa dibuka.
- Counter + save + history lama masih berjalan seperti sebelumnya.

---

## Tahap 1 - Theme Preference Library (tanpa ubah Room)

### Scope
- Tambah dependency `androidx.preference`.
- Tambah `SettingsActivity` dan `SettingsFragment`.
- Tambah `preferences.xml` dengan pilihan:
  - `Follow System`
  - `Light`
  - `Dark`
- Hubungkan `MainActivity` agar membaca preference dan apply theme via `AppCompatDelegate`.
- Tambah akses ke Settings dari UI (button/menu sederhana).

### Tidak termasuk
- Perubahan schema Room.
- Perubahan flow save/load match.

### Verification gate
1. `assembleDebug` sukses.
2. Buka Settings -> ubah theme ke Dark -> UI berubah.
3. Tutup app, buka lagi -> theme tetap.
4. Ubah ke Light dan System -> keduanya bekerja.

Exit criteria Tahap 1:
- Requirement 3.2 (Preference Library + persistence) terpenuhi.

---

## Tahap 2 - Rapikan Export JSON (tetap tanpa ubah Room schema)

### Scope
- Pertahankan mekanisme export yang sudah stabil (MediaStore) dulu.
- Rapikan struktur JSON output agar konsisten dan valid.
- Tambah handling error yang jelas pada kondisi gagal tulis.

### Tidak termasuk
- Migrasi ke SAF dulu (opsional tahap lanjutan).
- Perubahan entity database.

### Verification gate
1. Isi beberapa data match.
2. Export JSON sukses.
3. File terbuka dan format JSON valid.
4. Skenario data kosong -> tampil pesan, tidak crash.

Exit criteria Tahap 2:
- Requirement export shared storage stabil dan terverifikasi.

---

## Tahap 3 - Room: tambah kemampuan update (tanpa load dulu)

### Scope
- Ubah DAO untuk mendukung `updateMatch()`.
- Tambah timestamp `updatedAt` jika diperlukan.
- Tetap pertahankan flow lama agar risiko kecil.

### Strategi migrasi aman
- Jika ubah schema:
  - naikkan DB version,
  - gunakan migration eksplisit (lebih aman) atau fallback destruktif untuk dev-only.

### Verification gate
1. `assembleDebug` sukses.
2. Save match tetap jalan.
3. Update match lewat skenario internal (mis. update by id dari ViewModel) berhasil.
4. Tidak ada crash saat app start (cek Room migration path).

Exit criteria Tahap 3:
- Pondasi update data di Room tersedia dan stabil.

---

## Tahap 4 - State lokal match (`LOCAL_DRAFT` / `LOCAL_FINISHED`)

### Scope
- Tambah field status pada entity.
- Definisikan state minimal:
  - `LOCAL_DRAFT` (editable)
  - `LOCAL_FINISHED` (read-only)
- Tambah aksi `Finish` / `Reopen` di ViewModel (belum wajib ubah UI kompleks).

### Verification gate
1. Match baru default `LOCAL_DRAFT`.
2. Saat status `LOCAL_FINISHED`, increment score ditolak logic-nya.
3. `Reopen` mengembalikan editable.
4. Data status tersimpan benar di Room.

Exit criteria Tahap 4:
- Aturan state lokal berjalan konsisten di data layer + ViewModel.

---

## Tahap 5 - Load saved match ke counter

### Scope
- Di daftar history, klik item untuk load ke counter.
- Saat load:
  - isi team A/B dan score dari record.
  - jika `LOCAL_DRAFT` -> editable.
  - jika `LOCAL_FINISHED` -> read-only.
- Tombol Save pada record ter-load melakukan update by id (bukan insert baru).

### Verification gate
1. Buat 2 match berbeda.
2. Klik match pertama -> counter menampilkan data benar.
3. Edit dan save -> record pertama ter-update, bukan nambah record baru.
4. Load match finished -> kontrol skor terkunci.

Exit criteria Tahap 5:
- Requirement “load/update saved match” selesai.

---

## Tahap 6 (Opsional) - Migrasi export ke SAF

### Scope
- Ganti flow export ke `Intent.ACTION_CREATE_DOCUMENT`.
- User memilih nama/lokasi file secara eksplisit.

### Verification gate
1. Dialog create document muncul.
2. File berhasil dibuat di lokasi pilihan.
3. Cancel action tidak crash.

Exit criteria Tahap 6:
- Selaras penuh dengan rekomendasi SAF di spesifikasi.

---

## Checklist Verifikasi per Tahap

Untuk setiap tahap, jalankan:
1. Build: `assembleDebug`.
2. Smoke run di emulator/device.
3. Uji 3 skenario: normal, edge case, cancel/failure path.
4. Simpan catatan hasil (pass/fail + logcat jika fail).

---

## Protokol Jika Gagal / Crash

1. Stop implementasi tahap berikutnya.
2. Ambil stacktrace Logcat lengkap (baris exception utama + cause).
3. Perbaiki hanya akar masalah tahap aktif.
4. Ulang verification gate tahap aktif.
5. Lanjut tahap berikutnya hanya jika seluruh gate pass.

---

## Mapping ke Requirement Bagian 1

- 3.1 Two-way binding: sudah ada baseline, validasi ulang ringan tiap tahap.
- 3.2 Theme Preference Library: Tahap 1.
- 3.3 Export JSON shared storage: Tahap 2 (dan Tahap 6 opsional SAF).
- 3.4 Multiple match Room (save/list/load/update/delete): Tahap 3, 4, 5.

---

## Rekomendasi Urutan Eksekusi Nyata

1. Tahap 1
2. Tahap 2
3. Tahap 3
4. Tahap 4
5. Tahap 5
6. Tahap 6 (opsional)

Urutan ini memisahkan risiko tinggi (Room schema + state logic) setelah bagian UI preference stabil.
