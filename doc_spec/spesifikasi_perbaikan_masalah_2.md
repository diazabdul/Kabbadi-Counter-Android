# Spesifikasi Perbaikan Masalah 2 - Flow Match Lokal (Bagian 1)

Tanggal: 2026-05-11
Status: Draft disepakati untuk implementasi bertahap

Dokumen ini mendefinisikan flow baru untuk mencegah `Save` meng-override match yang tidak diinginkan.

---

## 1. Latar Belakang Masalah

Masalah saat ini:
1. Tombol `Save` berperilaku campuran (insert/update) berdasarkan context internal yang tidak terlihat jelas oleh user.
2. User kesulitan membedakan kapan sedang membuat match baru vs mengedit match lama.
3. Nama tim dan skor belum punya aturan edit yang tegas berdasarkan lifecycle match.

Tujuan perbaikan:
- Menjadikan alur match eksplisit dan dapat diprediksi.
- Mencegah override data yang tidak disengaja.
- Menetapkan aturan state match lokal yang konsisten.

---

## 2. Definisi State Match Lokal

State yang digunakan:
1. `LOCAL_DRAFT`
- Match sedang berlangsung.
- Skor boleh diubah.

2. `LOCAL_FINISHED`
- Match sudah diakhiri user.
- Skor tidak boleh diubah.

Catatan:
- State ini hanya untuk Bagian 1 (lokal), bukan state live backend.

---

## 3. Aturan Editability

### 3.1 Nama Tim

- Nama tim (`teamAName`, `teamBName`) editable hanya saat belum ada match aktif (`currentMatchId == null`).
- Setelah match dibuat (`Create Match`), nama tim terkunci.
- Saat load match dari history, nama tim tetap terkunci.

### 3.2 Skor

- Skor editable hanya jika:
  - ada match aktif, dan
  - status match aktif adalah `LOCAL_DRAFT`.
- Jika status `LOCAL_FINISHED`, tombol increment score tidak aktif.

---

## 4. Aksi Utama dan Semantik Tombol

Aksi dipisah agar tidak ambigu:

1. `Create Match`
- Fungsi: insert match baru.
- Precondition:
  - `currentMatchId == null`
  - nama tim valid (tidak kosong)
- Setelah sukses:
  - `currentMatchId` terisi id match baru
  - status `LOCAL_DRAFT`
  - nama tim terkunci

2. `Save Score`
- Fungsi: update skor match aktif.
- Precondition:
  - `currentMatchId != null`
  - status `LOCAL_DRAFT`
- Tidak boleh insert match baru.

3. `Finish Match`
- Fungsi: ubah status match aktif menjadi `LOCAL_FINISHED`.
- Setelah sukses:
  - skor terkunci
  - nama tim tetap terkunci

4. `New Match`
- Fungsi: keluar dari context match aktif dan mulai sesi baru.
- Efek:
  - `currentMatchId = null`
  - status kembali default draft session
  - nama tim editable lagi
  - skor reset 0-0

---

## 5. Flow Load dari History

Saat user klik item history:
1. Load data match ke counter.
2. Set sebagai match aktif (`currentMatchId = selected.id`).
3. Terapkan aturan editability berdasarkan status:
- `LOCAL_DRAFT` -> skor editable
- `LOCAL_FINISHED` -> skor locked
4. Nama tim tetap locked (karena bukan sesi create baru).

---

## 6. Guard dan Validasi Wajib

1. `Save Score` ditekan saat belum ada match aktif:
- tampilkan pesan: `Create match dulu`.
- tidak ada operasi DB.

2. `Finish Match` ditekan saat belum ada match aktif:
- tampilkan pesan informatif.
- tidak ada operasi DB.

3. `Create Match` dengan nama tim kosong:
- tampilkan validasi input.
- batalkan insert.

4. Semua operasi update hanya boleh menyentuh `currentMatchId` aktif.

---

## 7. Kontrak State di ViewModel

State minimum:
- `currentMatchId: Int?`
- `currentStatus: String` (`LOCAL_DRAFT` / `LOCAL_FINISHED`)
- `isNameEditable: Boolean`
- `isScoreEditable: Boolean`
- `teamA`, `teamB`, `scoreA`, `scoreB`

Derived behavior:
- `isNameEditable = (currentMatchId == null)`
- `isScoreEditable = (currentMatchId != null && currentStatus == LOCAL_DRAFT)`

---

## 8. Dampak UI yang Diperlukan

Minimal update UI:
1. Tombol terpisah:
- `Create Match`
- `Save Score`
- `Finish Match`
- `New Match`

2. Indikator context aktif:
- tampilkan `Match ID` aktif atau label `New Match Mode`.
- tampilkan status badge `LOCAL_DRAFT` / `LOCAL_FINISHED`.

3. Disable/enable control sesuai state.

---

## 9. Acceptance Criteria Perbaikan Masalah 2

Perbaikan dianggap selesai jika:
1. User dapat membuat match baru tanpa meng-override match lama.
2. `Save Score` hanya update match aktif.
3. Nama tim tidak bisa diubah setelah match dibuat.
4. Match `LOCAL_DRAFT` bisa diubah skornya.
5. Match `LOCAL_FINISHED` tidak bisa diubah skornya.
6. `New Match` selalu mengembalikan mode create dengan nama tim editable.
7. Load match dari history memuat data dengan aturan lock yang benar.

---

## 10. Rencana Implementasi Bertahap (Ringkas)

1. Tahap A: Refactor state ViewModel (`currentMatchId`, `isNameEditable`, `isScoreEditable`).
2. Tahap B: Pisahkan command DB (`createMatch`, `saveScore`, `finishMatch`).
3. Tahap C: Ubah UI tombol dan binding enable/disable.
4. Tahap D: Ubah flow history load + guard message.
5. Tahap E: Uji manual skenario utama + edge case.
