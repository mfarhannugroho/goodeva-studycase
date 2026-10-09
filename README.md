# BLE Proximity Tracker & Radar (Native Android)

Aplikasi mobile pelacak kedekatan perangkat **Bluetooth Low Energy (BLE)** berbasis **Native Android (Kotlin)** yang memindai sinyal *advertising* perangkat di sekitar secara *real-time*. Dilengkapi dengan **Radar View interaktif berbasis zona jarak**, filter & pengurutan sinyal otomatis, serta persistensi riwayat penemuan perangkat ke **Room Database**.

---

## Build Artifact (APK)
File APK dapat di-generate mandiri melalui Android Studio atau Gradle command line dengan output di:
- **Lokasi Output**: `app/build/outputs/apk/debug/app-debug.apk`

---

## Fitur Utama

### 1. Dashboard Pemindaian (Screen 1: Scanner Dashboard)
- **Pemindaian Real-Time**: Mendeteksi perangkat BLE aktif di sekitar dengan menangkap Nama Perangkat, MAC Address/Hardware Identifier, Raw RSSI (dBm), dan Estimasi Jarak (Meter).
- **Kontrol Manual**: Tombol *Start* dan *Stop* scan dengan indikator status berdenyut (*pulsing animation*).
- **Auto-Sort Sinyal Terkuat**: Terurut otomatis secara dinamis dari nilai RSSI tertinggi (paling mendekati 0 dBm).
- **Filter Pencarian & Threshold**:
  - Kolom pencarian teks instan berdasarkan Nama Perangkat atau MAC Address.
  - Slider ambang batas sinyal minimum (misal: $\ge -80$ dBm, $\ge -70$ dBm, dll).
  - Opsi *Filter Chip* untuk menyembunyikan perangkat tanpa nama (*Hide Unnamed*).
- **Demo / Simulation Mode Toggle**: Fitur bawaan untuk menguji seluruh fitur scanner dan radar di Android Emulator atau saat tidak ada fisik beacon BLE di sekitar.

### 2. Pelacakan Target Intensif (Screen 2: Radar View)
- Memilih 1 target perangkat dari daftar scanner untuk masuk ke layar pelacakan terfokus.
- **Visualisasi Dinamis Custom Canvas**:
  - 5 Cincin Konsentris mewakili zona jarak fisik.
  - Sapuan jarum radar berputar 360° (*CRT sweep phosphor effect*).
  - Titik target (*pulsing blip*) yang bergerak radial mendekat/menjauh secara halus (*smooth animated transition*) mengikuti nilai RSSI.
- **Pemetaan Kategori Jarak**:
  | Nilai RSSI (dBm) | Kategori Sinyal | Perkiraan Jarak (Meter) | Aksen Warna |
  |---|---|---|---|
  | **-10 s/d -30 dBm** | Sangat Kuat (Sangat Dekat) | < 1 meter | Emerald Green `#00E676` |
  | **-30 s/d -50 dBm** | Kuat (Dekat) | 1 – 3 meter | Electric Cyan `#00E5FF` |
  | **-50 s/d -70 dBm** | Cukup / Baik | 3 – 10 meter | Vibrant Amber `#FFD600` |
  | **-70 s/d -80 dBm** | Lemah | 10 – 20 meter | Deep Orange `#FF9100` |
  | **-80 s/d -90 dBm** | Sangat Lemah / Putus-putus | > 20 meter (Batas jangkauan) | Crimson Red `#FF3D00` |
  | **< -90 dBm** | Sinyal Hilang (*Lost*) | Terputus / Di luar jangkauan | Muted Gray `#757575` |
- **Filter Penghalus Sinyal (EMA)**: Menggunakan *Exponential Moving Average* ($\alpha = 0.35$) untuk meredam lompatan liar akibat fluktuasi *multipath RF* tanpa mengorbankan responsivitas.
- **Grafik Tren Stabilitas Real-time (Sparkline)**: Plot mini kurva 30 sampel sinyal terakhir untuk memantau fluktuasi transmisi perangkat target.
- **Pendeteksi Sinyal Terputus**: Banner peringatan otomatis jika perangkat tidak memancarkan sinyal $> 10$ detik.

### 3. Riwayat Perangkat (Screen 3: History Log)
- **Persistensi Database Lokal (Room DB)**: Menyimpan setiap perangkat yang pernah terdeteksi secara otomatis (*auto-upsert*), mencakup MAC Address, Nama, RSSI Terakhir, RSSI Tertinggi (Peak), dan Timestamp Terakhir Terlihat.
- Data riwayat tetap tersimpan utuh meskipun aplikasi ditutup atau perangkat direstart.
- Fitur pencarian riwayat dan tombol *Clear All History* dengan dialog konfirmasi pencegahan data hilang.

---

## Arsitektur & Teknologi

### Pola Arsitektur: **Clean Architecture + MVVM**
Mengikuti pedoman resmi *Modern Android Development* (MAD) dari Google:

```
app/src/main/java/com/goodeva/bletracker/
├── core / di/                # Dependency Injection (Hilt Modules)
├── data/
│   ├── ble/                  # BleScannerDataSource (Native BLE) & MockBleScannerDataSource (Simulator)
│   ├── local/                # Room Database, DAO (DeviceHistoryDao), & Entity (DeviceHistoryEntity)
│   └── repository/           # BleRepositoryImpl & HistoryRepositoryImpl
├── domain/
│   ├── model/                # BleDevice, ProximityZone (Enum & mapper tabel spesifikasi)
│   ├── repository/           # BleRepository & HistoryRepository (Interfaces)
│   └── util/                 # DistanceCalculator (Log-distance path loss formula & formatters)
└── presentation/
    ├── history/              # HistoryScreen & HistoryViewModel
    ├── navigation/           # BleTrackerNavGraph & Screen routes
    ├── radar/                # RadarScreen, RadarViewModel, RadarCanvas, & RssiSparkline
    ├── scanner/              # ScannerScreen, ScannerViewModel, & DeviceItemCard
    └── theme/                # Color, Theme, & Type (Dark Glassmorphic Cyberpunk)
```

### Library & Alasan Pemilihan:
1. **Kotlin 2.0.21 & Coroutines/Flow**: Menyediakan pemrograman reaktif asinkronus berperforma tinggi untuk streaming paket BLE berkecepatan tinggi.
2. **Jetpack Compose (Material 3)**: Deklaratif UI toolkit modern. Memudahkan pembuatan animasi grafis kustom pada Canvas Radar tanpa overhead view hierarki XML.
3. **Dagger Hilt**: Standar de-facto *Dependency Injection* di Android untuk mengelola dependensi modular, memudahkan decoupling antar layer, dan mendukung *unit testability*.
4. **AndroidX Room (SQLite ORM)**: Abstraksi database lokal resmi yang cepat, aman terhadap compile-time query check, dan mendukung reaktif Kotlin `Flow`.
5. **Native Android Bluetooth LE API (`android.bluetooth.le.*`)**: Memberikan kontrol granular pada mode pemindaian (`SCAN_MODE_LOW_LATENCY`), filtering, dan callback hardware langsung.

---

## Rumus Estimasi Jarak (Log-Distance Path Loss Model)

Hubungan antara kekuatan sinyal yang diterima (RSSI dalam dBm) dan estimasi jarak fisik dihitung menggunakan formula propagasi gelombang elektromagnetik:

$$\text{Distance} = 10^{\frac{\text{TxPower} - \text{RSSI}}{10 \times n}}$$

- **TxPower**: Kekuatan sinyal yang terkalibrasi pada jarak 1 meter (default: $-59\text{ dBm}$ jika tidak disertakan dalam *advertising payload*).
- **RSSI**: Kekuatan sinyal mentah yang diterima antena perangkat Android saat ini (dBm).
- **$n$**: Eksponen propagasi sinyal lingkungan (indoor: $2.0 - 2.5$).

---

## Error Handling, Resiliensi & Lifecycle

1. **Android 12+ (API 31+) Runtime Permissions**:
   - Menangani izin modern `BLUETOOTH_SCAN` dan `BLUETOOTH_CONNECT` secara otomatis saat aplikasi dibuka.
   - Backward-compatible dengan Android 8 s/d 11 menggunakan `ACCESS_FINE_LOCATION`.
2. **Bluetooth Adapter State Resilience**:
   - Mendeteksi ketersediaan hardware Bluetooth dan status adapter (ON/OFF).
   - Menyediakan fallback halus dan beralih otomatis ke *Simulation Mode* jika hardware BLE tidak tersedia (misal di emulator).
3. **Hemat Daya & Lifecycle Awareness**:
   - Melalui `LifecycleEventObserver`, pemindaian BLE otomatis dihentikan saat aplikasi berpindah ke latar belakang (`ON_STOP`) guna menghemat konsumsi daya baterai, dan dapat dilanjutkan saat kembali ke *foreground*.
   - Bertahan saat rotasi layar atau perubahan konfigurasi berkat integrasi Jetpack `ViewModel`.

---

## Panduan Setup & Menjalankan Aplikasi

### Kebutuhan Sistem:
- JDK 17 atau JDK 21 (sudah terintegrasi dengan Android Studio)
- Android SDK Platform 35 / 36
- Android Studio Ladybug / Meerkat / versi terbaru
- Perangkat fisik Android dengan Bluetooth LE aktif (Android 8.0+) atau Android Emulator

### Cara Menjalankan Melalui Android Studio:
1. Buka folder `goodeva-project` di Android Studio via menu **File -> Open**.
2. Biarkan Gradle melakukan sinkronisasi dependensi secara otomatis.
3. Hubungkan perangkat fisik Android melalui USB Debugging atau jalankan Android Emulator.
4. Klik tombol **Run 'app' (Shift + F10)**.

### Cara Menjalankan & Build via Command Line:
```bash
# Menjalankan seluruh unit test
./gradlew testDebugUnitTest

# Membuat build APK debug
./gradlew assembleDebug

# Output file APK akan berada di:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## Panduan Pengujian (Testing Guide)

### 1. Menguji di Android Emulator (Tanpa Perangkat Fisik BLE):
1. Buka aplikasi di Emulator.
2. Di pojok kanan atas kartu scanner, pastikan tombol switch **"Demo"** dalam posisi aktif (akan menampilkan badge ungu **SIMULASI**).
3. Tekan **"MULAI PEMINDAIAN"**. Aplikasi akan memindai beberapa suar perangkat virtual (Earbuds Pro, Smart Watch, AirTag, dll) dengan fluktuasi sinyal realistis.
4. Ketuk salah satu perangkat untuk masuk ke **Radar View**. Perhatikan sapuan jarum radar berputar 360°, target blip bergerak radial mengikuti zona jarak, serta grafik sparkline bergerak secara waktu nyata.
5. Kembali ke dashboard dan buka ikon **Riwayat** di App Bar atas untuk memeriksa data yang tersimpan di Room DB.

### 2. Menguji di Perangkat Fisik (Hardware BLE Asli):
1. Instal file APK yang telah di-build ke ponsel Android Anda.
2. Pastikan Bluetooth dan GPS di ponsel aktif.
3. Buka aplikasi dan berikan izin Bluetooth saat diminta.
4. Pastikan toggle **"Demo"** dalam posisi nonaktif (badge hijau **HARDWARE**).
5. Tekan **"MULAI PEMINDAIAN"**. Perangkat BLE aktif di sekitar Anda (smartwatch, headphone, beacon) akan langsung muncul.

### 3. Menguji Fitur Filter, Pencarian & Database Riwayat:
1. **Filter Threshold**: Geser slider RSSI minimum (misal ke $\ge -70$ dBm) untuk memastikan hanya perangkat berjarak dekat/sinyal kuat yang tampil.
2. **Pencarian Instan**: Ketik nama atau MAC address pada search bar untuk menyaring daftar secara waktu nyata.
3. **Database Riwayat (Room DB)**: Buka ikon jam di pojok kanan atas, ketik kata kunci untuk mencari perangkat yang pernah terdeteksi, atau tekan ikon tong sampah untuk menguji dialog konfirmasi penghapusan riwayat lokal.

---

## Asumsi Teknis & Keterbatasan (Known Issues)

1. **Fluktuasi Nilai RSSI di Dunia Nyata**:
   - Sinyal RF 2.4 GHz Bluetooth secara alami rentan terhadap fenomena *multipath fading*, pantulan dinding, dan interferensi tubuh manusia.
   - *Mitigasi yang diterapkan*: Aplikasi menerapkan filter *Exponential Moving Average (EMA)* dan visualisasi rentang zona daripada mengklaim angka jarak presisi absolut hingga sentimeter.
2. **Ketiadaan Arah Sudut (Angle of Arrival - AoA)**:
   - Standar BLE *advertising packets* umum hanya menyediakan intensitas sinyal (skalar dBm), bukan vektor sudut datang (kecuali menggunakan perangkat antena array Bluetooth 5.1 AoA berspesifikasi khusus).
   - *Desain yang diterapkan*: Pada Radar View, titik blip ditempatkan pada kuadran sudut pandang tetap dan bergeser secara radial (jarak dari pusat), merepresentasikan kedekatan relatif secara intuitif bagi pengguna.

---

## Pernyataan Orisinalitas & Penggunaan Alat Bantu AI (AI Disclosure)

Sesuai dengan ketentuan pengumpulan tugas:
> **Pernyataan yang Dipilih:**
> *"Ya, saya menggunakan bantuan AI generatif dan telah mencantumkan rincian penggunaannya (disclosure) di dalam dokumentasi README."*

### Rincian Penggunaan AI Generatif:
Dalam pengerjaan proyek study case ini, alat bantu AI generatif digunakan sebagai asisten pemrograman (*pair programming assistant*) dengan rincian transparansi sebagai berikut:

1. **Alat AI yang Digunakan**:
   - **Antigravity AI Assistant** (Google DeepMind).

2. **Model Kolaborasi (AI-Augmented Development)**:
   - **Peran Pengembang (Ideation, Architecture & Direction)**:
     - Menganalisis kebutuhan tugas *study case* dan merumuskan spesifikasi produk ke dalam dokumen PRD.
     - Menentukan keputusan teknologi utama: memilih Kotlin Native, Jetpack Compose Material 3, Room Database, dan Dagger Hilt.
     - Mengarahkan estetika desain antarmuka, pemilihan font **Plus Jakarta Sans**, dan konsep visualisasi Radar View 5 zona jarak.
     - Memandu alur kerja secara bertahap per fase (mulai dari inisialisasi, domain/data layer, state management, UI, hingga build APK).
     - Melakukan pengujian fungsionalitas aplikasi pada emulator/device untuk memastikan seluruh kriteria penerimaan terpenuhi.
   - **Peran AI Generatif (Code Generation & Synthesis)**:
     - Menghasilkan implementasi kode sumber (*source code generation*) sesuai instruksi dan pola arsitektur yang diarahkan (Clean Architecture + MVVM).
     - Menyusun konfigurasi build Gradle Version Catalog, tabel/DAO Room Database, dan modul Dagger Hilt.
     - Mengimplementasikan formula matematika log-distance path loss, filter eksponensial (EMA), serta animasi kanvas radar berputar.
     - Membuat skenario pengujian unit (*Unit Tests*) dan dokumen pendukung proyek.

3. **Pemahaman Solusi & Integritas**:
   - Pengungkapan (*disclosure*) ini dicantumkan secara terbuka sebagai bentuk integritas profesional. Pengembang memahami konsep solusi, arsitektur, dan alur kerja aplikasi yang dibangun, serta terbuka untuk mendiskusikan pendekatan teknis yang diterapkan dalam studi kasus ini.
