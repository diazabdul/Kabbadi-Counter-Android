# Spesifikasi Modifikasi Android Kabaddi Counter++
## Bagian 1 dan Bagian 2 — Android App Only, Backend Excluded

Dokumen ini berisi requirement untuk memodifikasi project Android Kabaddi Counter agar memenuhi spesifikasi tugas besar IF5230 bagian 1 dan bagian 2 dari sisi aplikasi Android.

Dokumen ini **tidak meminta implementasi backend**. Backend diasumsikan sudah tersedia sebagai API eksternal. Tugas Codex hanya memodifikasi project Android agar dapat:

1. memenuhi requirement Kabaddi Counter++,
2. menampilkan daftar pertandingan dari API,
3. subscribe pertandingan LIVE,
4. menerima FCM notification,
5. menampilkan persistent notification melalui foreground service.

---

# 1. Scope Pekerjaan

## 1.1 Yang harus dikerjakan

Codex harus memodifikasi aplikasi Android Kabaddi Counter yang sudah ada agar mendukung:

### Bagian 1 — Kabaddi Counter++

- Two-way data binding untuk nama Team A dan Team B melalui ViewModel.
- Dark/light mode yang tersimpan menggunakan Preference Library.
- Export data pertandingan ke JSON dan menyimpannya sebagai file di shared storage.
- Menyimpan multiple match menggunakan Room Library.
- Struktur aplikasi mengikuti pola MVVM.

### Bagian 2 — Live Score Android

- Menampilkan daftar pertandingan dari API backend.
- Subscribe pertandingan yang statusnya LIVE.
- Mengirim FCM token ke backend saat subscribe.
- Mengunci counter saat sedang subscribe pertandingan LIVE.
- Menerima FCM notification saat skor berubah.
- Menampilkan notification Android.
- Menjalankan foreground service untuk persistent notification skor live.

## 1.2 Yang tidak boleh dikerjakan

Codex **tidak perlu** membuat:

- backend Laravel,
- database backend,
- endpoint backend,
- Firebase Admin SDK,
- WebSocket server,
- panel admin backend,
- autentikasi backend.

Android hanya perlu mengonsumsi API backend yang sudah disediakan.

---

# 2. Prinsip Implementasi

## 2.1 Gunakan struktur project existing

Codex harus memodifikasi project Android yang sudah ada, bukan membuat project baru dari nol.

Aturan:

- Pertahankan fitur counter existing.
- Pertahankan alur utama aplikasi selama masih relevan.
- Jangan rewrite total aplikasi jika tidak diperlukan.
- Ikuti bahasa pemrograman yang sudah digunakan di project existing.
- Jika project existing menggunakan Java, lanjutkan dengan Java.
- Jika project existing menggunakan Kotlin, lanjutkan dengan Kotlin.
- Hindari mencampur Java dan Kotlin kecuali memang sudah ada di project.

## 2.2 Arsitektur yang diharapkan

Gunakan pola MVVM:

```txt
View / Activity / Fragment
        ↓ observes
ViewModel
        ↓ calls
Repository
        ↓ accesses
Local Data Source / Remote Data Source
        ↓
Room / Preference / API / Firebase
```

Minimal layer yang disarankan:

```txt
ui/
  counter/
  matches/
  settings/
  savedmatches/

viewmodel/

data/
  local/
    room/
    preference/
  remote/
    api/
    dto/
  repository/

service/
  fcm/
  foreground/
```

Penamaan folder boleh disesuaikan dengan struktur project existing.

---

# 3. Bagian 1 — Kabaddi Counter++

## 3.1 Two-way Data Binding untuk Team A dan Team B

### Requirement

Nama Team A dan Team B harus disimpan di ViewModel dan terhubung ke layout menggunakan two-way data binding.

### Perilaku yang diharapkan

- Ketika user mengubah nama Team A di input field, nilai di ViewModel ikut berubah.
- Ketika user mengubah nama Team B di input field, nilai di ViewModel ikut berubah.
- Jika ViewModel mengubah nama tim, tampilan UI ikut berubah.
- Nama tim tidak boleh hanya disimpan langsung di Activity/Fragment.
- Nama tim tidak boleh hanya diambil dari EditText secara manual saat tombol ditekan.

### Contoh konsep binding

Jika menggunakan XML Data Binding, input field sebaiknya menggunakan pola seperti:

```xml
android:text="@={viewModel.teamAName}"
```

Dan:

```xml
android:text="@={viewModel.teamBName}"
```

### State minimal di ViewModel

ViewModel minimal menyimpan:

```txt
teamAName
teamBName
teamAScore
teamBScore
isSubscribedToLiveMatch
currentMatchId
currentMatchStatus
```

Tipe boleh menggunakan:

- LiveData,
- MutableLiveData,
- StateFlow,
- ObservableField,

sesuai pendekatan project existing.

### Acceptance Criteria

- Mengubah nama Team A di UI langsung memperbarui ViewModel.
- Mengubah nama Team B di UI langsung memperbarui ViewModel.
- Setelah rotate screen, nilai nama tim tetap aman selama ViewModel masih hidup.
- Activity/Fragment tidak menjadi tempat utama penyimpanan state nama tim.

---

## 3.2 Dark/Light Mode dengan Preference Library

### Requirement

Aplikasi harus memiliki pengaturan dark/light mode dan pengaturan tersebut wajib diimplementasikan menggunakan AndroidX Preference Library.

### Pengaturan minimal

Sediakan setting tema:

```txt
Light Mode
Dark Mode
System Default / Follow System
```

Minimal sebenarnya cukup dark/light, tetapi opsi follow system boleh ditambahkan agar lebih fleksibel.

### Lokasi setting

Buat screen settings, misalnya:

```txt
Settings Screen
└── Theme Preference
    ├── Light
    ├── Dark
    └── Follow System
```

### Library yang digunakan

Gunakan:

```gradle
androidx.preference:preference
```

Gunakan `PreferenceFragmentCompat` atau pendekatan lain yang tetap memakai Preference Library.

### Penyimpanan

Theme preference harus tersimpan secara persistent.

Contoh key:

```txt
pref_theme_mode
```

Contoh value:

```txt
light
dark
system
```

### Perilaku aplikasi

- Saat user memilih Light Mode, aplikasi langsung atau setelah restart menggunakan light mode.
- Saat user memilih Dark Mode, aplikasi langsung atau setelah restart menggunakan dark mode.
- Saat user memilih Follow System, aplikasi mengikuti setting sistem Android.
- Setting tetap tersimpan setelah aplikasi ditutup dan dibuka kembali.

### Acceptance Criteria

- Setting theme muncul di Settings Screen.
- Setting disimpan menggunakan Preference Library.
- AppCompatDelegate atau mekanisme setara digunakan untuk menerapkan theme.
- Setelah aplikasi direstart, theme terakhir tetap digunakan.

---

## 3.3 Export Match ke JSON di Shared Storage

### Requirement

Aplikasi harus dapat mengekspor data pertandingan ke file JSON dan menyimpannya ke shared storage.

### Cara penyimpanan yang disarankan

Gunakan Storage Access Framework:

```txt
Intent.ACTION_CREATE_DOCUMENT
```

Dengan MIME type:

```txt
application/json
```

Alternatif lain boleh menggunakan MediaStore Downloads, tetapi Storage Access Framework lebih aman dan kompatibel untuk Android modern.

### Data yang perlu diekspor

Minimal JSON berisi:

```json
{
  "match_id": "local-or-remote-id",
  "team_a_name": "Team A",
  "team_b_name": "Team B",
  "team_a_score": 10,
  "team_b_score": 8,
  "status": "LOCAL",
  "source": "LOCAL_COUNTER",
  "exported_at": "2026-06-01T10:00:00Z"
}
```

Jika match berasal dari live subscription, boleh berisi:

```json
{
  "match_id": 1,
  "team_a_name": "Garuda",
  "team_b_name": "Rajawali",
  "team_a_score": 15,
  "team_b_score": 14,
  "status": "END",
  "source": "LIVE_MATCH",
  "exported_at": "2026-06-01T10:00:00Z"
}
```

### Tombol/UI

Tambahkan aksi export, misalnya:

- tombol `Export JSON`,
- menu item `Export`,
- atau action di detail match.

### Perilaku

- Saat user memilih export, aplikasi membuka file picker untuk membuat file JSON.
- User dapat memilih lokasi penyimpanan file.
- File JSON berhasil dibuat.
- Jika gagal, tampilkan error yang jelas.
- Jika user membatalkan, aplikasi tidak crash.

### Acceptance Criteria

- Export menghasilkan file `.json`.
- File dapat dibuka dari file manager.
- Isi JSON valid.
- Aplikasi tidak meminta permission storage lama yang tidak perlu pada Android modern.
- Aplikasi menangani error dan cancel action.

---

## 3.4 Multiple Match dengan Room Library

### Requirement

Aplikasi harus dapat menyimpan lebih dari satu pertandingan menggunakan Room Library.

### Fungsi minimal

Aplikasi harus mendukung:

```txt
Create / Save Match
Read Match List
Load Match
Update Match
Delete Match
```

Delete boleh opsional jika waktu terbatas, tetapi sangat disarankan.

### Entity yang disarankan

Buat entity untuk match lokal:

```txt
MatchEntity
- id: Long
- teamAName: String
- teamBName: String
- teamAScore: Int
- teamBScore: Int
- status: String
- source: String
- remoteMatchId: Long?
- createdAt: Long
- updatedAt: Long
```

Contoh status:

```txt
LOCAL
LIVE
END
```

Contoh source:

```txt
LOCAL_COUNTER
LIVE_MATCH
```

### DAO minimal

DAO minimal menyediakan:

```txt
insertMatch(match)
updateMatch(match)
deleteMatch(match)
getMatchById(id)
getAllMatches()
```

`getAllMatches()` sebaiknya mengurutkan berdasarkan `updatedAt DESC`.

### Database

Buat Room database, misalnya:

```txt
KabaddiDatabase
```

Berisi:

```txt
MatchEntity
```

Jika ingin lebih rapi, boleh tambahkan entity log skor:

```txt
ScoreLogEntity
- id
- matchId
- team
- point
- teamAScoreAfter
- teamBScoreAfter
- createdAt
```

Namun `ScoreLogEntity` tidak wajib.

### Screen Saved Matches

Tambahkan screen untuk melihat match yang sudah disimpan.

Setiap item menampilkan:

- nama Team A,
- nama Team B,
- skor,
- status,
- waktu update terakhir.

Saat item diklik:

- match tersebut dimuat ke counter screen,
- skor dan nama tim muncul sesuai data tersimpan.

### Perilaku save

Aplikasi boleh menyediakan:

- tombol `Save Match`,
- auto-save saat skor berubah,
- atau save saat user menekan tombol tertentu.

Yang penting, user bisa memiliki multiple match yang tersimpan di Room.

### Acceptance Criteria

- User dapat menyimpan lebih dari satu match.
- Data match tetap ada setelah aplikasi ditutup.
- User dapat membuka kembali match yang tersimpan.
- Room digunakan sebagai storage utama multiple match, bukan SharedPreferences.

---

# 4. Bagian 2 — Live Score Android

Bagian ini hanya mencakup implementasi Android. Backend diasumsikan sudah tersedia.

---

## 4.1 Melihat Daftar Pertandingan dari API Backend

### Requirement

Aplikasi harus menyediakan screen daftar pertandingan yang datanya didapatkan dari API backend.

### Data yang wajib tampil per item

Setiap item match harus menampilkan:

```txt
Nama Team A
Nama Team B
Skor Team A
Skor Team B
Status pertandingan: LIVE / END
```

### Perilaku screen

Screen daftar pertandingan harus memiliki:

```txt
Loading state
Success state
Empty state
Error state
Refresh action
```

Refresh bisa berupa:

- pull to refresh,
- tombol refresh,
- atau reload saat screen dibuka.

### Model data remote

Gunakan DTO/model seperti:

```txt
RemoteMatchDto
- id
- team_a_name
- team_b_name
- team_a_score
- team_b_score
- status
```

### Contoh response yang diasumsikan

Android boleh mengasumsikan response API berbentuk:

```json
{
  "data": [
    {
      "id": 1,
      "team_a_name": "Garuda",
      "team_b_name": "Rajawali",
      "team_a_score": 10,
      "team_b_score": 8,
      "status": "LIVE"
    }
  ]
}
```

Jika backend aktual berbeda, sesuaikan DTO tanpa mengubah konsep UI.

### API client

Gunakan Retrofit/OkHttp atau library HTTP yang sudah ada di project.

Base URL jangan hardcode di banyak tempat.

Simpan base URL di salah satu tempat berikut:

- `BuildConfig`,
- `local.properties`,
- resource config,
- atau satu object constants.

### Acceptance Criteria

- Aplikasi dapat mengambil daftar match dari backend.
- Match LIVE dan END terlihat berbeda secara visual.
- Jika API gagal, user melihat pesan error.
- Jika data kosong, user melihat empty state.
- UI tidak freeze saat loading.

---

## 4.2 Subscribe Pertandingan LIVE

### Requirement

User dapat subscribe ke pertandingan yang statusnya `LIVE`.

Saat subscribe:

- Android mengambil FCM token device.
- Android mengirim token ke backend.
- Counter utama diganti dengan data pertandingan yang disubscribe.
- Nama tim dan skor tidak dapat diedit selama subscribe aktif.

### Aturan subscribe

- Hanya match dengan status `LIVE` yang bisa di-subscribe.
- Match dengan status `END` tidak boleh di-subscribe.
- Jika user subscribe match baru, counter berpindah ke match baru.
- Jika sebelumnya ada match live yang sedang disubscribe, subscription lama dianggap tergantikan oleh subscription baru.
- Setelah subscribe berhasil, simpan `subscribedMatchId` secara lokal.

### Data yang dikirim ke backend

Request subscribe mengirim:

```json
{
  "fcm_token": "DEVICE_FCM_TOKEN"
}
```

### State setelah subscribe

Counter screen harus masuk mode:

```txt
LIVE_SUBSCRIBED_MODE
```

Pada mode ini:

- Nama Team A read-only.
- Nama Team B read-only.
- Skor read-only.
- Tombol increment score lokal disabled/hidden.
- Tombol reset tetap tersedia.
- Tombol export tetap boleh tersedia.
- Tombol save ke Room boleh tetap tersedia.

### Reset dari live mode

Jika user menekan reset:

- keluar dari mode live subscription secara lokal,
- kembali ke counter default,
- nama dan skor dapat diedit lagi,
- foreground service jika aktif harus dihentikan atau dikembalikan ke state non-live.

Catatan:

- Jika backend punya endpoint unsubscribe, Android boleh memanggilnya.
- Jika backend tidak punya endpoint unsubscribe, reset cukup menghapus state lokal subscription.

### Acceptance Criteria

- Subscribe hanya tersedia untuk match LIVE.
- FCM token berhasil diambil dari Firebase Messaging.
- FCM token dikirim ke backend.
- Setelah subscribe, counter utama menampilkan match yang dipilih.
- Counter terkunci selama subscribe aktif.
- Reset mengembalikan aplikasi ke mode counter lokal.

---

## 4.3 Handling Pertandingan END

### Requirement

Jika pertandingan yang sedang disubscribe berakhir, counter tetap menampilkan skor akhir.

### Perilaku yang diharapkan

Ketika Android menerima informasi bahwa match berakhir:

- status counter berubah menjadi `END`,
- skor akhir tetap tampil,
- nama tim tetap tampil,
- skor tidak bisa diedit,
- user dapat memilih reset untuk kembali ke counter default,
- user dapat subscribe pertandingan lain.

### Sumber status END

Status END bisa diterima melalui:

- FCM data message,
- refresh API,
- response endpoint tertentu,
- atau payload notification stop match.

### Acceptance Criteria

- Saat status match menjadi END, UI tidak kembali otomatis ke default.
- Skor akhir tetap terlihat.
- User punya kontrol untuk reset atau subscribe match lain.
- Foreground notification ikut menampilkan status selesai atau dihentikan.

---

## 4.4 FCM Notification untuk Update Skor

### Requirement

Aplikasi harus menerima push notification dari Firebase Cloud Messaging setiap terjadi perubahan skor pada match yang disubscribe.

### Firebase Messaging Service

Buat atau modifikasi service:

```txt
FirebaseMessagingService
```

Fungsi minimal:

- menerima message dari FCM,
- membaca payload,
- menampilkan notifikasi Android,
- memperbarui state lokal jika payload berasal dari match yang sedang disubscribe,
- menangani token refresh.

### Payload yang diharapkan

Backend dapat mengirim payload seperti:

```json
{
  "type": "SCORE_UPDATE",
  "match_id": "1",
  "scoring_team": "A",
  "scoring_team_name": "Garuda",
  "team_a_name": "Garuda",
  "team_b_name": "Rajawali",
  "team_a_score": "11",
  "team_b_score": "8",
  "status": "LIVE"
}
```

Untuk match selesai:

```json
{
  "type": "MATCH_ENDED",
  "match_id": "1",
  "team_a_name": "Garuda",
  "team_b_name": "Rajawali",
  "team_a_score": "15",
  "team_b_score": "12",
  "status": "END"
}
```

### Isi notification

Notification update skor wajib menampilkan:

- tim yang mencetak skor,
- skor baru.

Contoh body:

```txt
Garuda mencetak skor. Skor sekarang Garuda 11 - 8 Rajawali.
```

Untuk match selesai:

```txt
Pertandingan selesai. Skor akhir Garuda 15 - 12 Rajawali.
```

### Notification Channel

Untuk Android 8 ke atas, buat notification channel, misalnya:

```txt
Channel ID: live_score_channel
Channel Name: Live Score Updates
Importance: DEFAULT / HIGH
```

### Android 13+

Jika target SDK Android 13 atau lebih tinggi, aplikasi perlu menangani permission:

```txt
POST_NOTIFICATIONS
```

User harus diminta izin notifikasi sebelum aplikasi menampilkan notification.

### Token refresh

Saat FCM token berubah:

- simpan token terbaru secara lokal,
- jika sedang subscribe match, kirim ulang token ke backend untuk match tersebut.

### Acceptance Criteria

- Aplikasi dapat menerima FCM message.
- Notification muncul saat skor berubah.
- Notification body menyebut tim pencetak skor dan skor baru.
- Payload match END ditangani.
- Token refresh tidak menyebabkan subscription rusak.
- App tidak crash jika payload tidak lengkap.

---

## 4.5 Foreground Service untuk Persistent Live Score Notification

### Requirement

Aplikasi harus menyediakan mode foreground service agar user dapat melihat skor live dari persistent notification tanpa membuka aplikasi.

### Kapan foreground service aktif

Foreground service aktif jika:

- user sudah subscribe match LIVE,
- user mengaktifkan mode live notification / foreground mode.

Foreground service tidak sebaiknya aktif saat:

- belum subscribe match,
- match sudah di-reset ke counter default,
- user mematikan mode foreground,
- tidak ada match live yang sedang dipantau.

### Isi persistent notification

Persistent notification harus menampilkan:

- nama Team A,
- skor Team A,
- nama Team B,
- skor Team B,
- status LIVE atau END.

Contoh:

```txt
Garuda 11 - 8 Rajawali
Status: LIVE
```

Saat match selesai:

```txt
Garuda 15 - 12 Rajawali
Status: END
```

### Update notification

Saat FCM update skor diterima:

- update state match lokal,
- update persistent notification foreground service.

### Service class

Buat service, misalnya:

```txt
LiveScoreForegroundService
```

Service minimal mendukung action:

- START,
- UPDATE,
- STOP.

### Notification channel

Foreground service menggunakan channel terpisah atau sama dengan live score:

```txt
Channel ID: live_score_foreground_channel
Channel Name: Live Score Foreground Service
Importance: LOW
```

### Android modern

Pastikan:

- `startForeground()` dipanggil dengan benar.
- Permission notification ditangani jika diperlukan.
- Deklarasi service ada di `AndroidManifest.xml`.
- Jika target SDK memerlukan foreground service permission tertentu, tambahkan sesuai kebutuhan.

### Acceptance Criteria

- User dapat mengaktifkan foreground mode saat subscribe match LIVE.
- Persistent notification muncul.
- Persistent notification menampilkan skor live.
- Saat skor berubah, persistent notification ikut berubah.
- Saat reset, service berhenti.
- Saat match END, notification menampilkan skor akhir/status END.

---

# 5. Mode Counter dan State Aplikasi

Aplikasi sebaiknya memiliki state yang jelas.

## 5.1 Local Counter Mode

Mode default.

Ciri:

- user bisa mengubah nama tim,
- user bisa mengubah skor,
- user bisa reset,
- user bisa save ke Room,
- user bisa export JSON.

State:

```txt
mode = LOCAL_COUNTER
status = LOCAL
```

## 5.2 Live Subscribed Mode

Mode setelah user subscribe match LIVE.

Ciri:

- nama tim read-only,
- skor read-only,
- data berasal dari backend/FCM,
- foreground service bisa diaktifkan,
- update skor berasal dari FCM,
- user bisa reset untuk keluar.

State:

```txt
mode = LIVE_SUBSCRIBED
status = LIVE
subscribedMatchId = remote match id
```

## 5.3 Live Ended Mode

Mode ketika match yang disubscribe sudah selesai.

Ciri:

- skor akhir tetap tampil,
- nama tim tetap tampil,
- skor tidak bisa diedit,
- user bisa reset,
- user bisa subscribe match lain,
- export JSON tetap bisa dilakukan.

State:

```txt
mode = LIVE_ENDED
status = END
subscribedMatchId = remote match id
```

---

# 6. Navigasi dan Screen yang Disarankan

Minimal screen yang dibutuhkan:

## 6.1 Counter Screen

Fungsi:

- menampilkan counter utama,
- menampilkan nama tim,
- menampilkan skor,
- increment/decrement/reset lokal jika mode lokal,
- menampilkan status mode,
- tombol save match,
- tombol export JSON,
- tombol foreground mode jika subscribe LIVE.

## 6.2 Match List Screen

Fungsi:

- menampilkan daftar pertandingan dari API,
- tombol refresh,
- tombol subscribe untuk match LIVE,
- status LIVE/END terlihat jelas.

## 6.3 Saved Matches Screen

Fungsi:

- menampilkan match yang tersimpan di Room,
- load match ke counter,
- opsional delete match.

## 6.4 Settings Screen

Fungsi:

- pengaturan dark/light/system theme,
- opsional pengaturan base URL backend jika dibutuhkan,
- opsional pengaturan foreground notification.

Catatan:

- Jika menggunakan bottom navigation, screen yang masuk akal:
  - Counter
  - Matches
  - Saved
  - Settings

---

# 7. Integrasi Dependency

Tambahkan dependency sesuai kebutuhan project.

## 7.1 Data Binding

Pastikan data binding aktif di Gradle.

Contoh konsep:

```gradle
android {
    buildFeatures {
        dataBinding true
    }
}
```

Jika project sudah memakai View Binding, Data Binding tetap perlu diaktifkan untuk requirement two-way binding.

## 7.2 Preference Library

Gunakan AndroidX Preference.

```gradle
implementation "androidx.preference:preference:<latest-compatible-version>"
```

## 7.3 Room

Gunakan Room.

Dependency menyesuaikan Java/Kotlin project:

- Java: gunakan annotationProcessor.
- Kotlin: gunakan kapt atau ksp sesuai project.

## 7.4 Retrofit/OkHttp

Untuk konsumsi API backend.

Jika project sudah punya HTTP client lain, boleh lanjutkan yang sudah ada.

## 7.5 Firebase Messaging

Gunakan Firebase Cloud Messaging.

Pastikan:

- `google-services.json` tersedia di app module,
- Gradle plugin Google Services terpasang,
- Firebase Messaging dependency ditambahkan,
- service dideklarasikan di manifest.

---

# 8. Requirement Manifest

Pastikan manifest mendukung:

## 8.1 Internet

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 8.2 Notification Android 13+

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 8.3 Foreground Service

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

Service foreground harus dideklarasikan.

## 8.4 Firebase Messaging Service

Service FCM harus dideklarasikan dengan intent filter yang sesuai.

---

# 9. Error Handling

Aplikasi harus menangani error berikut:

## 9.1 API error

Contoh:

- backend tidak aktif,
- timeout,
- 500 server error,
- response tidak valid.

UI harus menampilkan pesan error dan memberi opsi retry.

## 9.2 FCM token gagal diambil

Jika token gagal:

- tampilkan pesan gagal subscribe,
- jangan ubah counter ke live mode sebelum subscribe berhasil.

## 9.3 Subscribe gagal

Jika subscribe gagal:

- counter tetap di mode sebelumnya,
- tampilkan pesan error.

## 9.4 Payload FCM tidak lengkap

Jika payload tidak lengkap:

- jangan crash,
- abaikan field yang tidak valid,
- log error.

## 9.5 Export gagal

Jika export gagal:

- tampilkan pesan gagal,
- jangan crash.

## 9.6 Room error

Jika save/load gagal:

- tampilkan error,
- jangan kehilangan state UI saat ini.

---

# 10. Requirement UX

## 10.1 Status badge

Tampilkan status dengan jelas:

```txt
LOCAL
LIVE
END
```

## 10.2 Lock indicator

Saat subscribe LIVE, tampilkan informasi bahwa counter terkunci karena mengikuti live match.

Contoh:

```txt
Sedang menonton pertandingan LIVE. Skor diperbarui otomatis.
```

## 10.3 Empty state

Untuk daftar match kosong:

```txt
Belum ada pertandingan dari server.
```

Untuk saved match kosong:

```txt
Belum ada pertandingan yang disimpan.
```

## 10.4 Loading state

Saat fetch match list:

```txt
Memuat pertandingan...
```

## 10.5 Error state

Jika gagal fetch:

```txt
Gagal memuat pertandingan. Coba lagi.
```

---

# 11. Acceptance Checklist Lengkap

## Bagian 1

- [ ] Team A menggunakan two-way data binding ke ViewModel.
- [ ] Team B menggunakan two-way data binding ke ViewModel.
- [ ] Nama tim tidak disimpan langsung sebagai state utama di Activity/Fragment.
- [ ] Dark/light mode tersedia di Settings.
- [ ] Dark/light mode menggunakan Preference Library.
- [ ] Setting theme tersimpan setelah app restart.
- [ ] Export JSON tersedia.
- [ ] Export JSON menyimpan file ke shared storage.
- [ ] File JSON valid.
- [ ] Multiple match disimpan menggunakan Room.
- [ ] User dapat melihat daftar match tersimpan.
- [ ] User dapat membuka match tersimpan.
- [ ] User dapat menyimpan lebih dari satu match.

## Bagian 2 Android

- [ ] App memiliki screen daftar pertandingan dari API backend.
- [ ] Setiap match menampilkan nama tim, skor, dan status LIVE/END.
- [ ] Match LIVE bisa di-subscribe.
- [ ] Match END tidak bisa di-subscribe.
- [ ] Saat subscribe, app mengambil FCM token.
- [ ] Saat subscribe, app mengirim FCM token ke backend.
- [ ] Setelah subscribe, counter utama menampilkan match yang dipilih.
- [ ] Saat subscribe LIVE, nama tim dan skor tidak bisa diedit.
- [ ] App menerima FCM notification saat skor berubah.
- [ ] Notification menampilkan tim pencetak skor dan skor baru.
- [ ] Payload MATCH_ENDED ditangani.
- [ ] Saat match selesai, counter tetap menampilkan skor akhir.
- [ ] User bisa reset untuk kembali ke counter default.
- [ ] Foreground service dapat diaktifkan.
- [ ] Persistent notification menampilkan skor live.
- [ ] Persistent notification berubah saat skor berubah.
- [ ] Foreground service berhenti saat reset atau dimatikan user.

---

# 12. Skenario Manual Testing

## 12.1 Test two-way binding

1. Buka counter screen.
2. Ubah nama Team A.
3. Ubah nama Team B.
4. Tekan tombol skor.
5. Pastikan nama dan skor tetap sinkron dengan ViewModel.
6. Rotate screen jika memungkinkan.
7. Pastikan nama tim tidak hilang selama state masih dikelola ViewModel.

## 12.2 Test theme preference

1. Buka Settings.
2. Pilih Dark Mode.
3. Pastikan aplikasi berubah ke dark mode.
4. Tutup aplikasi.
5. Buka kembali.
6. Pastikan dark mode tetap aktif.
7. Ubah ke Light Mode.
8. Pastikan aplikasi berubah ke light mode.

## 12.3 Test export JSON

1. Isi nama tim dan skor.
2. Tekan Export JSON.
3. Pilih lokasi file.
4. Simpan file.
5. Buka file dari file manager.
6. Pastikan JSON valid dan berisi data pertandingan.

## 12.4 Test Room multiple match

1. Buat match pertama.
2. Simpan match.
3. Reset counter.
4. Buat match kedua.
5. Simpan match.
6. Buka Saved Matches.
7. Pastikan ada dua match.
8. Pilih salah satu match.
9. Pastikan data match muncul di counter.

## 12.5 Test daftar match API

1. Buka Match List.
2. Pastikan loading muncul.
3. Pastikan data dari backend tampil.
4. Matikan koneksi/backend.
5. Refresh.
6. Pastikan error state muncul.

## 12.6 Test subscribe live match

1. Buka Match List.
2. Pilih match LIVE.
3. Tekan Subscribe.
4. Pastikan app mengambil FCM token.
5. Pastikan request subscribe berhasil.
6. Pastikan counter menampilkan match tersebut.
7. Pastikan input nama dan tombol skor terkunci.

## 12.7 Test FCM update score

1. Subscribe match LIVE.
2. Trigger update skor dari backend.
3. Pastikan notification muncul.
4. Pastikan notification menyebut tim pencetak skor dan skor baru.
5. Jika app terbuka, pastikan UI ikut berubah.

## 12.8 Test match ended

1. Subscribe match LIVE.
2. Trigger stop match dari backend.
3. Pastikan notification match ended muncul.
4. Pastikan counter menampilkan skor akhir.
5. Pastikan status menjadi END.
6. Tekan reset.
7. Pastikan counter kembali ke mode lokal.

## 12.9 Test foreground service

1. Subscribe match LIVE.
2. Aktifkan foreground mode.
3. Pastikan persistent notification muncul.
4. Trigger update skor dari backend.
5. Pastikan persistent notification berubah.
6. Reset counter.
7. Pastikan foreground service berhenti.

---

# 13. Catatan Implementasi untuk Codex

Ikuti instruksi ini saat memodifikasi project:

1. Jangan membuat backend.
2. Jangan membuat project Android baru.
3. Modifikasi project existing.
4. Gunakan MVVM.
5. Pastikan Team A dan Team B menggunakan two-way data binding.
6. Gunakan Preference Library untuk theme.
7. Gunakan Room untuk multiple match.
8. Gunakan shared storage untuk export JSON.
9. Gunakan Firebase Cloud Messaging untuk push notification.
10. Gunakan foreground service untuk persistent live score notification.
11. Buat error handling yang aman.
12. Jangan hardcode base URL di banyak file.
13. Jangan simpan secret Firebase/backend di source code.
14. Pastikan aplikasi tetap bisa berjalan sebagai counter lokal walaupun backend mati.
15. Prioritaskan fitur sesuai spesifikasi tugas, bukan fitur tambahan yang tidak diminta.

---

# 14. Non-Goals

Fitur berikut tidak perlu dibuat kecuali diminta terpisah:

- Login/register user.
- Role admin.
- Sistem turnamen lengkap.
- WebSocket.
- Realtime database.
- Payment.
- Statistik pemain.
- Multi-device account sync.
- Backend Laravel.
- Dashboard backend.
- Firebase Admin SDK.
- CI/CD.
- Deployment server.

---

# 15. Prioritas Implementasi

Jika waktu terbatas, kerjakan dengan urutan berikut:

## Prioritas 1 — Wajib Bagian 1

1. Two-way data binding Team A/Team B.
2. Preference Library untuk dark/light mode.
3. Room untuk multiple match.
4. Export JSON ke shared storage.

## Prioritas 2 — Wajib Bagian 2 Android

1. Match List dari API.
2. Subscribe LIVE match.
3. Lock counter saat subscribe.
4. FCM receive notification.
5. Match END handling.

## Prioritas 3 — Pelengkap Bagian 2

1. Foreground service.
2. Persistent notification live score.
3. Token refresh handling.
4. UI polish dan empty/error state.

---

# 16. Definisi Selesai

Implementasi dianggap selesai jika:

- Semua requirement Bagian 1 terpenuhi.
- Semua requirement Android Bagian 2 terpenuhi.
- Aplikasi bisa digunakan sebagai counter lokal.
- Aplikasi bisa menampilkan match dari backend.
- Aplikasi bisa subscribe match LIVE.
- Aplikasi bisa menerima FCM notification.
- Aplikasi bisa menampilkan persistent notification live score.
- Aplikasi tidak crash pada kondisi error umum.
- Code tersusun rapi dan masih mengikuti struktur project existing.
