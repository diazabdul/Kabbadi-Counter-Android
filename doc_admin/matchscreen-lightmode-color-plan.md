# MatchScreen Light Mode Color Plan

Tanggal: 2026-06-02

## Tujuan

Dokumen ini menjadi acuan fix warna light mode untuk `MatchScreen` tanpa merusak dark mode yang saat ini sudah dianggap final.

Target file implementasi utama nanti:
- `app/src/main/java/com/example/kabaddikounter/ui/screens/MatchScreen.kt`
- `app/src/main/java/com/example/kabaddikounter/ui/theme/Theme.kt`
- jika perlu, helper token baru di folder `ui/theme`

## Constraint Utama

1. Dark mode tidak boleh berubah perilaku visualnya secara signifikan.
2. Layout tidak boleh dirombak:
- spacing
- sizing
- shapes
- hierarchy composable
3. Fix fokus pada mapping warna, bukan redesign screen.
4. Hindari hardcoded color decision di level screen.

## Masalah Saat Ini

`MatchScreen` masih menentukan banyak warna secara langsung memakai kombinasi `MaterialTheme.colorScheme.*` yang tidak konsisten antar konteks komponen.

Contoh masalah:
- kartu skor pertama memakai `primary` sebagai background, yang di light mode menjadi lime terang
- isi kartu pertama memakai campuran `onPrimary`, `surface`, `onSecondary`, `primaryContainer`
- kartu kedua bergantung pada default `CardDefaults.cardColors()`, sehingga hasil akhirnya mengikuti `surface`/`surfaceContainer` light theme yang saat ini masih cenderung gelap

Akibatnya:
- dark mode masih terasa cocok
- light mode tampak terlalu kontras, tidak seragam, dan beberapa token dipakai di konteks yang bukan peruntukannya

## Prinsip Fix

Fix harus dilakukan dengan pendekatan token semantik per-screen.

Artinya, `MatchScreen` tidak langsung memutuskan warna dari `colorScheme` untuk setiap elemen, tetapi memakai token bernama jelas sesuai fungsi UI.

## Token Semantik yang Dibutuhkan

Disarankan membuat model seperti `MatchScreenColors` atau helper sejenis.

Token minimum:

### Screen-level
- `screenBackground`
- `teamInputContainer` jika nanti dibutuhkan override halus untuk field nama tim

### Team score card - primary card (kartu atas / team A)
- `primaryCardContainer`
- `primaryCardTitle`
- `primaryCardScore`
- `primaryCardOutlineButtonBorder`
- `primaryCardOutlineButtonContent`
- `primaryCardFilledButtonContainer`
- `primaryCardFilledButtonContent`

### Team score card - secondary card (kartu bawah / team B)
- `secondaryCardContainer`
- `secondaryCardTitle`
- `secondaryCardScore`
- `secondaryCardOutlineButtonBorder`
- `secondaryCardOutlineButtonContent`
- `secondaryCardFilledButtonContainer`
- `secondaryCardFilledButtonContent`

### Match state badge
- `liveBadgeContainer`
- `liveBadgeContent`
- `finalBadgeContainer`
- `finalBadgeContent`

### Bottom actions
- `primaryActionContainer`
- `primaryActionContent`
- `dangerActionContainer`
- `dangerActionContent`

## Strategi Mapping yang Aman

### Dark mode

Dark mode harus dipertahankan sedekat mungkin dengan hasil sekarang.

Artinya:
- token dark mode diisi berdasarkan warna yang saat ini sudah terlihat bagus
- jangan ubah palette dark global kecuali benar-benar diperlukan
- lebih aman menambah helper token screen dibanding mengubah `DarkColorScheme`

### Light mode

Light mode diperbaiki lewat mapping token khusus:
- tentukan kombinasi warna yang tetap konsisten dengan palette aplikasi
- hindari lime terang penuh sebagai area dominan besar jika membuat kontras terlalu keras
- gunakan surface yang lebih terang atau container yang lebih lembut untuk kartu, sambil menyisakan accent hanya untuk elemen yang memang butuh penekanan

## Rekomendasi Implementasi Bertahap

1. Tambah helper token untuk `MatchScreen`.
2. Map helper itu untuk dua mode:
- dark mode mengikuti tampilan sekarang
- light mode memakai kombinasi yang lebih lembut dan konsisten
3. Refactor `MatchScreen` agar semua warna membaca dari helper token tersebut.
4. Jangan ubah layout dan state logic.
5. Verifikasi visual:
- team name fields
- kartu atas
- kartu bawah
- tombol `+1`
- tombol `+2`
- badge `LIVE`
- badge `FINAL`
- tombol bawah

## Area yang Tidak Perlu Diubah Dulu

Untuk iterasi pertama, hindari menyentuh:
- typography
- shape
- spacing
- state logic `isRemoteSubscribed`, `isScoreEditable`, dll.
- struktur composable `TeamScoreLayoutCard`

## Kriteria Selesai

Fix dianggap benar jika:

1. Light mode terlihat konsisten dan terbaca jelas.
2. Tidak ada warna tombol/teks yang tampak "asal ambil" dari token Material lain.
3. Dark mode tetap sama atau sangat dekat dengan tampilan saat ini.
4. Tidak ada perubahan layout.

## Catatan Eksekusi

Saat mulai implementasi:
- prioritaskan ekstraksi token dulu
- baru sesuaikan mapping light mode
- jangan mulai dari edit acak inline di `MatchScreen`

