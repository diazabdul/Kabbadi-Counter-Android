# Milestone Implementasi Bertahap Bagian 1

Tanggal update: 2026-05-11

Referensi rencana: `doc_spec/langkah_implementasi_bagian_1.md`

---

## Milestone Status

### Tahap 1 - Theme Preference Library
Status: **DONE**

### Tahap 2 - Rapikan Export JSON
Status: **DONE**

### Tahap 3 - Room update capability
Status: **DONE**

### Tahap 4 - State lokal (`LOCAL_DRAFT`/`LOCAL_FINISHED`)
Status: **DONE**

### Tahap 5 - Load saved match ke counter
Status: **DONE**

### Tahap 6 - SAF export (opsional)
Status: **DONE**

Yang sudah dikerjakan:
1. Export JSON dimigrasikan ke SAF dengan `ActivityResultContracts.CreateDocument("application/json")`.
2. `ViewModel` tidak lagi langsung menulis file; sekarang hanya menyiapkan JSON payload dan memicu event export.
3. `MainActivity` menerima event, membuka file picker, lalu menulis isi JSON ke URI hasil picker.
4. Menambahkan handling cancel picker (`uri == null`) dengan toast `Export canceled`.
5. Menambahkan handling write failure dengan toast error.
6. Setelah export berhasil, JSON viewer tetap bisa dibuka untuk preview file hasil.

File utama yang berubah:
- `app/src/main/java/com/example/kabaddikounter/viewModels/ScoreViewModel.kt`
- `app/src/main/java/com/example/kabaddikounter/MainActivity.kt`

Verification yang perlu dijalankan di Android Studio:
1. Klik Download -> pastikan dialog create document muncul.
2. Pilih lokasi + simpan -> pastikan toast sukses dan file JSON bisa dibuka.
3. Ulang Download lalu cancel -> app tidak crash, muncul toast cancel.
4. Verifikasi isi JSON tetap sesuai struktur Tahap 2.

---

## Ringkasan Bagian 1

Seluruh tahap pada roadmap Bagian 1 sudah diimplementasikan:
- Theme preference
- Export JSON terstruktur
- Room update capability
- Local state draft/finished
- Load/update saved match
- SAF export flow
## Update 2026-05-11 - Perbaikan Masalah 2

Perbaikan ini mengimplementasikan flow eksplisit agar `Save` tidak lagi override match secara ambigu.

Perubahan utama:
1. Flow command dipisah di ViewModel:
   - `createMatch()` -> insert match baru
   - `saveScore()` -> update skor match aktif by id
   - `finishMatch()` -> ubah status match aktif ke `LOCAL_FINISHED`
   - `startNewMatch()` -> keluar dari context match aktif dan kembali mode create
2. Context aktif disimpan jelas di `currentMatchId`.
3. Aturan editability sinkron dengan spesifikasi:
   - nama tim editable hanya saat `currentMatchId == null`
   - skor editable hanya saat match aktif berstatus `LOCAL_DRAFT`
4. Load dari history (`loadMatch`) kini mengikat context aktif + lock behavior by status.
5. UI tombol di Counter diubah agar semantik jelas:
   - `Create Match`
   - `Save Score`
   - `Finish Match`
   - `New Match`
6. Ditambahkan label mode (`Mode: New Match` / `Mode: Match #id (status)`).

File yang berubah:
- `app/src/main/java/com/example/kabaddikounter/viewModels/ScoreViewModel.kt`
- `app/src/main/java/com/example/kabaddikounter/data/MatchDao.kt`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/values/strings.xml`

Checklist verifikasi manual:
1. Buka app -> mode awal `New Match`, nama tim editable, tombol skor nonaktif.
2. Isi nama tim -> klik `Create Match` -> nama tim lock, tombol skor aktif.
3. Ubah skor -> klik `Save Score` -> skor tersimpan ke match aktif (bukan insert match baru).
4. Klik `Finish Match` -> tombol skor nonaktif.
5. Klik match history dengan status `LOCAL_DRAFT` -> skor aktif; `LOCAL_FINISHED` -> skor nonaktif.
6. Klik `New Match` -> context aktif ter-reset, nama tim editable lagi.

## Update 2026-05-11 - Stabilisasi Lanjutan Masalah 2

Perbaikan tambahan setelah implementasi flow utama:
1. Menambahkan state enabled eksplisit untuk tombol aksi:
   - `isCreateEnabled`
   - `isSaveScoreEnabled`
   - `isFinishEnabled`
2. Binding tombol di UI kini sinkron penuh dengan state:
   - `Create Match` hanya aktif saat mode create.
   - `Save Score` hanya aktif saat match aktif `LOCAL_DRAFT`.
   - `Finish Match` hanya aktif saat match aktif `LOCAL_DRAFT`.
3. Guard tambahan di ViewModel:
   - `finishMatch()` menolak aksi jika match sudah finished.

Dampak:
- User tidak perlu menebak state karena tombol yang tidak valid langsung nonaktif.
- Mengurangi kemungkinan salah klik yang menghasilkan toast error berulang.
