# Prompt Singkat untuk Codex — Modifikasi Android Kabaddi Counter++

Tolong modifikasi project Android Kabaddi Counter existing agar memenuhi requirement tugas besar IF5230 bagian 1 dan bagian 2 Android. Jangan membuat backend dan jangan membuat project baru.

Fokus implementasi:

1. Bagian 1 — Kabaddi Counter++
   - Team A dan Team B harus menggunakan two-way data binding ke ViewModel.
   - Tambahkan setting dark/light mode menggunakan AndroidX Preference Library.
   - Setting theme harus persistent.
   - Tambahkan fitur export match ke JSON di shared storage.
   - Tambahkan penyimpanan multiple match menggunakan Room Library.
   - Gunakan pola MVVM.

2. Bagian 2 — Android Live Score
   - Tambahkan screen daftar pertandingan dari API backend.
   - Setiap item match menampilkan nama Team A, nama Team B, skor, dan status LIVE/END.
   - Match LIVE bisa di-subscribe.
   - Saat subscribe, ambil FCM token dan kirim ke backend.
   - Setelah subscribe, counter utama diganti dengan data match live.
   - Saat subscribe, nama tim dan skor tidak boleh diedit manual.
   - App harus menerima FCM notification untuk update skor.
   - Notification harus menampilkan tim yang mencetak skor dan skor terbaru.
   - Jika match berakhir, counter tetap menampilkan skor akhir sampai user reset atau subscribe match lain.
   - Tambahkan foreground service untuk persistent notification skor live.

3. Batasan
   - Jangan implementasi backend.
   - Jangan implementasi auth.
   - Jangan implementasi WebSocket.
   - Jangan rewrite total project.
   - Ikuti bahasa dan struktur project existing.
   - Base URL backend cukup dibuat configurable di satu tempat.
   - App harus tetap bisa berjalan sebagai local counter walaupun backend tidak tersedia.

Gunakan dokumen `spesifikasi_android_bagian_1_2_exclude_backend.md` sebagai requirement lengkap.
