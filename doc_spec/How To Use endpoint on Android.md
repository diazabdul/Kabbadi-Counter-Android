# How To Use Endpoint on Android

## 1. Tujuan Dokumen

Dokumen ini ditujukan untuk tim Android agar bisa integrasi ke backend Kabaddi Live Score dengan cepat dan konsisten.

## 2. Base URL

Pilih sesuai environment:

- Android Emulator:
  - `http://10.0.2.2:8000/api`
- Physical device (1 Wi-Fi dengan laptop backend):
  - `http://IP_LAPTOP:8000/api`

Contoh:

- `http://10.0.2.2:8000/api/match`

## 3. Endpoint yang Dipakai Android

### 3.1 Get Match List

- Method: `GET`
- URL: `/match`
- Fungsi: ambil daftar pertandingan

Contoh response:

```json
{
  "message": "Matches retrieved successfully",
  "data": [
    {
      "id": 1,
      "team_a_name": "Garuda",
      "team_b_name": "Rajawali",
      "team_a_score": 0,
      "team_b_score": 0,
      "status": "LIVE"
    }
  ]
}
```

### 3.2 Get Match Detail

- Method: `GET`
- URL: `/match/{kabaddiMatch}`
- Fungsi: ambil detail match tertentu

### 3.3 Subscribe Device ke Match LIVE

- Method: `POST`
- URL: `/match/{kabaddiMatch}/subscribe`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "fcm_token": "ANDROID_FCM_TOKEN",
  "device_name": "Optional Device Name"
}
```

Catatan:

- Panggil ini setelah user memilih match LIVE.
- Jika token sama subscribe ke match lain, backend otomatis memindahkan subscription.

Response sukses:

```json
{
  "message": "Subscribed successfully",
  "data": {
    "match": {
      "id": 1,
      "team_a_name": "Garuda",
      "team_b_name": "Rajawali",
      "team_a_score": 0,
      "team_b_score": 0,
      "status": "LIVE"
    },
    "subscription": {
      "kabaddi_match_id": 1,
      "device_name": "Optional Device Name"
    }
  }
}
```

## 4. Event FCM yang Diterima Android

Android menerima data payload (string values) dari backend.

### 4.1 Saat Skor Berubah (`SCORE_UPDATED`)

```json
{
  "type": "SCORE_UPDATED",
  "match_id": "1",
  "team_a_name": "Garuda",
  "team_b_name": "Rajawali",
  "team_a_score": "13",
  "team_b_score": "9",
  "scoring_team": "A",
  "scoring_team_name": "Garuda",
  "point": "1",
  "status": "LIVE"
}
```

### 4.2 Saat Match Selesai (`MATCH_ENDED`)

```json
{
  "type": "MATCH_ENDED",
  "match_id": "1",
  "team_a_name": "Garuda",
  "team_b_name": "Rajawali",
  "team_a_score": "13",
  "team_b_score": "9",
  "status": "END"
}
```

## 5. Alur Integrasi Android (Disarankan)

1. App start -> call `GET /match`.
2. User pilih match status `LIVE`.
3. Ambil FCM token dari Firebase Messaging.
4. Kirim token ke `POST /match/{id}/subscribe`.
5. Simpan `match_id` aktif di local state.
6. Saat menerima FCM:
- Jika `type=SCORE_UPDATED`, update counter + notification.
- Jika `type=MATCH_ENDED`, tampilkan status selesai + skor akhir.

## 6. Skenario Test Minimum untuk Tim Android

1. Buka daftar match:
- `GET /match` harus sukses dan menampilkan data.
2. Subscribe token:
- `POST /match/{id}/subscribe` harus return `Subscribed successfully`.
3. Trigger dari backend (oleh tim backend/dashboard):
- `POST /match/{id}/score`
- Android harus menerima payload `SCORE_UPDATED`.
4. Trigger stop:
- `POST /match/{id}/stop`
- Android harus menerima payload `MATCH_ENDED`.

## 7. Error yang Perlu Ditangani Android

1. Match sudah END saat subscribe:
- message: `Cannot subscribe because match has ended`
2. Validation error:
- status `422`
- parse field `errors`
3. Match tidak ditemukan:
- status `404`

## 8. Catatan Penting

- Semua nilai pada payload `data` FCM diperlakukan sebagai string.
- Token FCM bisa berubah, jadi refresh token harus dikirim lagi ke endpoint subscribe.
- Backend simulator score/stop dilakukan dari dashboard backend, bukan dari Android endpoint publik.
