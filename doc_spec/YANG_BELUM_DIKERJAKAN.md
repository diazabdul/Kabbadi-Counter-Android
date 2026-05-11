# YANG_BELUM_DIKERJAKAN

Tanggal: 2026-05-11
Tujuan dokumen: handover untuk programmer berikutnya.

---

## Ringkasan Status Saat Ini

### Bagian 1 (Android Counter++)
Status: mayoritas sudah diimplementasikan.

Yang sudah ada:
1. Theme settings dengan Preference Library (`Light/Dark/System`).
2. Export JSON terstruktur.
3. Room update capability.
4. State lokal `LOCAL_DRAFT` / `LOCAL_FINISHED`.
5. Load saved match ke counter.
6. Flow eksplisit match:
   - `Create Match`
   - `Save Score`
   - `Finish Match`
   - `New Match`
7. Lock behavior:
   - nama tim editable hanya di mode create/new match,
   - skor editable hanya saat match aktif status `LOCAL_DRAFT`.
8. Backend proof screen (Retrofit `GET /match`) + URL dari `local.properties`.

Catatan:
- Untuk emulator gunakan `10.0.2.2`, bukan `127.0.0.1`.

---

## Yang Belum Dikerjakan

## A. Bagian 2 - Live Score Android (Utama)

Belum diimplementasikan penuh:
1. Screen daftar match production-ready (saat ini baru test screen sederhana).
2. Subscribe endpoint `POST /match/{id}/subscribe` dari UI utama.
3. Pengambilan FCM token aktual dan kirim token ke backend.
4. Integrasi `FirebaseMessagingService` untuk menerima event:
   - `SCORE_UPDATED`
   - `MATCH_ENDED`
5. Update state counter berdasarkan payload FCM match aktif.
6. Notification channel + notifikasi update skor.
7. Foreground service persistent live score notification.
8. Handling status `END` sesuai spesifikasi UX.
9. Permission flow Android 13+ untuk notifikasi (`POST_NOTIFICATIONS`) bila diperlukan.

## B. Hardening Bagian 1

Masih bisa ditingkatkan:
1. Buat migrasi Room yang proper (replace fallback destructive migration) jika data lama harus dipertahankan lintas versi.
2. Tambahkan test unit/instrumentation untuk flow `Create/Save/Finish/New/Load`.
3. Rapikan copywriting pesan toast agar konsisten bahasa.
4. Tambahkan empty-state khusus untuk saved match list jika dibutuhkan UX lebih jelas.

## C. Backend Integration Hardening

Saat ini backend test masih vertical-slice minimum. Perlu peningkatan:
1. Pisahkan konfigurasi `dev/staging/prod` (flavor/build type) bila project berkembang.
2. Tambah error parser yang lebih informatif untuk 422/404/500.
3. Tambah retry policy/timeout strategy yang lebih matang.
4. Tambah logging yang bisa dimatikan di release build.

---

## Prioritas Pengerjaan Selanjutnya (Saran)

1. Implement `subscribe` flow (UI + repository + API call).
2. Integrasikan FCM token retrieval & backend submit.
3. Implement `FirebaseMessagingService` + update counter state.
4. Implement notification channel + score update notification.
5. Implement foreground service persistent notification.
6. Tambahkan test skenario manual end-to-end untuk Bagian 2.

---

## File Penting Untuk Titik Mulai

### Core counter / local flow
- `app/src/main/java/com/example/kabaddikounter/viewModels/ScoreViewModel.kt`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/java/com/example/kabaddikounter/HistoryFragment.kt`
- `app/src/main/java/com/example/kabaddikounter/MatchAdapter.kt`

### Room
- `app/src/main/java/com/example/kabaddikounter/data/MatchRecord.kt`
- `app/src/main/java/com/example/kabaddikounter/data/MatchDao.kt`
- `app/src/main/java/com/example/kabaddikounter/data/AppDatabase.kt`

### Settings/theme
- `app/src/main/java/com/example/kabaddikounter/SettingsActivity.kt`
- `app/src/main/java/com/example/kabaddikounter/SettingsFragment.kt`
- `app/src/main/res/xml/preferences.xml`

### Backend test slice
- `app/src/main/java/com/example/kabaddikounter/ui/backend/BackendTestActivity.kt`
- `app/src/main/java/com/example/kabaddikounter/ui/backend/BackendTestViewModel.kt`
- `app/src/main/java/com/example/kabaddikounter/data/remote/api/ApiClient.kt`
- `app/src/main/java/com/example/kabaddikounter/data/remote/api/MatchApiService.kt`

---

## Catatan Operasional

1. Base URL backend dibaca dari `local.properties` via `BuildConfig.BASE_URL`.
2. Untuk dev emulator:
- `BACKEND_BASE_URL=http://10.0.2.2:8002/api/`
3. Hindari menyimpan secret/token sensitif di source code.
4. Pastikan file teks tetap UTF-8 tanpa BOM untuk menghindari error Gradle parser.
