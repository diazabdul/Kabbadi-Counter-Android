# UI Color Architecture

Tanggal: 2026-06-02

## Tujuan

Menetapkan single source of truth untuk warna UI tanpa merusak dark mode yang sudah final.

## Prinsip

Arsitektur warna dibagi menjadi 4 lapisan:

1. Palette mentah
- file: `ui/theme/Color.kt`
- hanya berisi raw color seperti `Jet`, `Cream`, `Raid`, `Neg`
- tidak dipakai langsung oleh screen

2. App semantic theme
- file: `ui/theme/Theme.kt`
- memetakan palette ke `ColorScheme`
- dark mode saat ini dianggap paten dan tidak diubah sembarangan

3. Global semantic UI tokens
- file: `ui/theme/tokens/AppUiTokens.kt`
- ini adalah single source of truth utama untuk fungsi visual lintas aplikasi
- contoh: input, card, action, badge, muted text

4. Screen/feature tokens tipis
- folder: `ui/theme/tokens`
- contoh: `MatchScreenTokens.kt`
- screen token hanya merakit kebutuhan unik screen dari `AppUiTokens`
- screen token tidak boleh menjadi sumber utama yang liar

## Aturan Implementasi

1. Jangan pakai `Color(...)` langsung di screen kecuali kasus sangat khusus.
2. Jangan meracik banyak `MaterialTheme.colorScheme.*` langsung di composable screen untuk keputusan warna inti.
3. Gunakan `AppUiTokens` sebagai sumber utama untuk warna lintas aplikasi.
4. Screen token hanya dipakai jika komponen screen punya kebutuhan khusus.
5. Jika light mode perlu penyesuaian, prioritaskan perbaikan di `AppUiTokens`, lalu mapping tipis di token screen bila perlu.
6. Jangan ubah mapping dark mode kecuali memang ada bug nyata.

## Struktur Folder

Direktori aktif:

- `app/src/main/java/com/example/kabaddikounter/ui/theme/Color.kt`
- `app/src/main/java/com/example/kabaddikounter/ui/theme/Theme.kt`
- `app/src/main/java/com/example/kabaddikounter/ui/theme/tokens/AppUiTokens.kt`
- `app/src/main/java/com/example/kabaddikounter/ui/theme/tokens/UiColorMode.kt`
- `app/src/main/java/com/example/kabaddikounter/ui/theme/tokens/MatchScreenTokens.kt`

Rencana screen berikutnya:
- `HistoryScreenTokens.kt`
- `BackendScreenTokens.kt`
- `SettingsScreenTokens.kt` jika dibutuhkan

## Mode Detection

Token global dan token screen menggunakan helper `UiColorMode` di `ui/theme/tokens/UiColorMode.kt`.

Tujuannya:
- token tidak perlu menebak mode sendiri
- dark mode dan light mode dibedakan di satu tempat

## Status Saat Ini

Sudah dimigrasikan:
- `MatchScreen` ke `MatchScreenTokens`
- `MatchScreenTokens` kini berbasis `AppUiTokens`

Belum dimigrasikan:
- `HistoryScreen`
- `BackendTestScreen`
- `SettingsScreen`

## Guardrail Dark Mode

Aturan utama:
- visual dark mode saat ini adalah baseline
- migrasi arsitektur tidak boleh mengubah colorway dark mode
- setiap refactor token harus mempertahankan mapping dark mode yang lama
