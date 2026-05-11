# Progress Implementasi Android Kabaddi Counter++

Tanggal update: 2026-05-11

Dokumen ini memetakan progres implementasi terhadap `spesifikasi_android_bagian_1_2_exclude_backend.md`.

---

## Progress Eksekusi Terbaru (Bagian 1)

Perubahan yang sudah diimplementasikan di codebase:

1. Migrasi Theme ke Preference Library
- Menambahkan dependency `androidx.preference:preference-ktx`.
- Menambahkan `SettingsActivity` + `SettingsFragment`.
- Menambahkan `preferences.xml` dengan `ListPreference`:
  - `system`
  - `light`
  - `dark`
- `MainActivity` sekarang membaca preference theme dan menerapkan `AppCompatDelegate` berdasarkan key `pref_theme_mode`.

2. State Match Lokal untuk Load/Update
- Entity `MatchRecord` diperluas dengan:
  - `status`
  - `source`
  - `remoteMatchId`
  - `createdAt`
  - `updatedAt`
- Menambahkan konstanta state lokal:
  - `LOCAL_DRAFT`
  - `LOCAL_FINISHED`
- Database version dinaikkan ke `2`.

3. Room Capability untuk Multiple Match Lanjutan
- DAO kini memiliki:
  - `insertMatch`
  - `updateMatch`
  - `getMatchById`
  - `getAllMatches` (urut `updatedAt DESC`)
  - `deleteMatch`

4. Alur Counter Saved Match
- `HistoryFragment` + `MatchAdapter` kini mendukung:
  - klik item untuk `load` match ke counter
  - delete match
- `ScoreViewModel` kini mendukung:
  - save baru vs update existing (berdasarkan `currentMatchId`)
  - `loadMatch(match)`
  - `finish/reopen` match
  - status label + lock/unlock editability

5. Lock Behavior Berdasarkan State
- Saat `LOCAL_DRAFT`: nama tim dan tombol skor editable.
- Saat `LOCAL_FINISHED`: nama tim dan tombol skor non-editable.
- Tombol finish bersifat toggle (`Finish Match` / `Reopen Match`).

6. Export JSON
- Export masih ke shared storage via MediaStore (tetap berjalan).
- Payload JSON diekspor dengan field yang lebih dekat ke spesifikasi:
  - `match_id`, `team_a_name`, `team_b_name`, `team_a_score`, `team_b_score`, `status`, `source`, `exported_at`.

---

## Status terhadap Checklist Bagian 1

1. Two-way binding Team A/B: **Selesai**
2. Theme via Preference Library + Settings screen: **Selesai**
3. Theme persistent setelah restart: **Selesai (berbasis preference key)**
4. Export JSON ke shared storage: **Selesai (MediaStore)**
5. Multiple match with Room: **Selesai inti (save/list/load/update/delete)**
6. Saved match bisa dibuka ke counter: **Selesai**
7. State match lokal draft/finished: **Selesai**

Catatan:
- Implementasi export saat ini memakai MediaStore, bukan SAF `ACTION_CREATE_DOCUMENT`.
- Ini masih sesuai poin shared storage, tetapi bisa ditingkatkan ke SAF jika ingin mengikuti rekomendasi spesifikasi secara ketat.

---

## Belum Dikerjakan (Bagian 2)

Belum diimplementasikan:
- Match list dari API backend
- Subscribe live match
- FCM receive/update
- Foreground service persistent notification
- Permission/service manifest untuk fitur live di atas

---

## Catatan Verifikasi Teknis

Build belum dapat diverifikasi di environment saat ini karena konfigurasi Java lokal belum siap:
- `JAVA_HOME is not set and no 'java' command could be found in your PATH`.

Selain itu, perintah `git status` gagal karena safe-directory ownership mismatch pada repo lokal.
