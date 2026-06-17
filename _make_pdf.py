# -*- coding: utf-8 -*-
from fpdf import FPDF

TEAL=(14,135,112); DARK=(26,27,31); GREY=(95,96,104); LIGHT=(232,246,242)
CODEBG=(245,245,247); KHAIRI=(31,90,55); REVALDO=(180,83,9); NAUFAL=(27,67,119)

def san(s):
    rep={"→":"->","←":"<-","—":"-","–":"-","•":"-","‘":"'","’":"'","“":'"',"”":'"',"…":"...","·":"-","×":"x"}
    for k,v in rep.items(): s=s.replace(k,v)
    return s.encode("latin-1","ignore").decode("latin-1")

class PDF(FPDF):
    def header(self):
        if self.page_no()==1: return
        self.set_font("Helvetica","B",9); self.set_text_color(*GREY)
        self.cell(0,8,san("ALIRIN - Skrip Presentasi Lengkap"),align="L")
        self.cell(0,8,san("GEMASTIK Smart City"),align="R",new_x="LMARGIN",new_y="NEXT")
        self.set_draw_color(*LIGHT); self.line(15,20,195,20); self.ln(6)
    def footer(self):
        self.set_y(-12); self.set_font("Helvetica","",8); self.set_text_color(*GREY)
        self.cell(0,8,san(f"Halaman {self.page_no()}"),align="C")

pdf=PDF(format="A4"); pdf.set_auto_page_break(True,15); pdf.set_margins(15,15,15)

def section_title(num,title,color):
    pdf.ln(1); pdf.set_fill_color(*color); pdf.set_text_color(255,255,255); pdf.set_font("Helvetica","B",13)
    pdf.cell(0,11,san(f"  {num} - {title}"),fill=True,new_x="LMARGIN",new_y="NEXT")
    pdf.set_text_color(*DARK); pdf.ln(3)
def page_head(title,files):
    pdf.set_x(pdf.l_margin); pdf.set_font("Helvetica","B",11.5); pdf.set_text_color(*TEAL)
    pdf.multi_cell(0,6.5,san("> "+title),new_x="LMARGIN",new_y="NEXT")
    pdf.set_font("Courier","I",8); pdf.set_text_color(*GREY)
    pdf.multi_cell(0,4.6,san("file: "+files),new_x="LMARGIN",new_y="NEXT")
    pdf.set_text_color(*DARK); pdf.ln(1)
def speaker(name,color):
    pdf.ln(0.5); pdf.set_font("Helvetica","B",11); pdf.set_text_color(*color)
    pdf.cell(0,7,san(name+":"),new_x="LMARGIN",new_y="NEXT"); pdf.set_text_color(*DARK)
def say(t):
    pdf.set_x(pdf.l_margin); pdf.set_font("Helvetica","",10.5); pdf.set_text_color(*DARK)
    pdf.multi_cell(0,5.6,san(t),new_x="LMARGIN",new_y="NEXT"); pdf.ln(1.2)
def cue(t):
    pdf.set_x(pdf.l_margin); pdf.set_font("Helvetica","I",9.5); pdf.set_text_color(*GREY)
    pdf.multi_cell(0,5,san("[ "+t+" ]"),new_x="LMARGIN",new_y="NEXT"); pdf.set_text_color(*DARK); pdf.ln(1.2)
def code(lines,file=None):
    if file:
        pdf.set_x(pdf.l_margin); pdf.set_font("Helvetica","BI",8.5); pdf.set_text_color(*TEAL)
        pdf.multi_cell(0,5,san("# "+file),new_x="LMARGIN",new_y="NEXT")
    pdf.set_fill_color(*CODEBG); pdf.set_text_color(*DARK); pdf.set_font("Courier","",8.0)
    for ln in lines:
        pdf.set_x(pdf.l_margin)
        pdf.multi_cell(0,4.6,san(ln) or " ",fill=True,border=0,new_x="LMARGIN",new_y="NEXT")
    pdf.ln(2)
def point(label,text):
    pdf.set_x(pdf.l_margin); pdf.set_font("Helvetica","B",10.5); pdf.set_text_color(*TEAL)
    pdf.cell(6,5.6,san("-")); pdf.cell(36,5.6,san(label))
    pdf.set_font("Helvetica","",10.5); pdf.set_text_color(*DARK)
    pdf.multi_cell(0,5.6,san(text),new_x="LMARGIN",new_y="NEXT"); pdf.ln(0.4)

# ---------- COVER ----------
pdf.add_page(); pdf.set_fill_color(*TEAL); pdf.rect(0,0,210,297,"F")
pdf.set_text_color(255,255,255); pdf.set_y(60)
pdf.set_font("Helvetica","B",42); pdf.cell(0,18,"ALIRIN",align="C",new_x="LMARGIN",new_y="NEXT")
pdf.set_font("Helvetica","",13)
pdf.cell(0,9,san("Aplikasi Pelaporan & Prediksi Drainase Mikro"),align="C",new_x="LMARGIN",new_y="NEXT")
pdf.cell(0,9,san("Kota Bandar Lampung - Smart City"),align="C",new_x="LMARGIN",new_y="NEXT")
pdf.ln(12)
pdf.set_font("Helvetica","B",15); pdf.cell(0,10,san("SKRIP PRESENTASI LENGKAP (per Halaman)"),align="C",new_x="LMARGIN",new_y="NEXT")
pdf.ln(14); pdf.set_font("Helvetica","",12); pdf.cell(0,8,san("Pembagian tugas:"),align="C",new_x="LMARGIN",new_y="NEXT"); pdf.ln(2)
for nm,part in [("KHAIRI","Pembuka, Arsitektur, Splash/Onboarding, Login & Beranda"),
                ("REVALDO","Lapor, Peta Risiko & Status Laporan"),
                ("NAUFAL","Navigasi/Peran, Sisi Staff, Tentang, Web & Penutup")]:
    pdf.set_font("Helvetica","B",14); pdf.cell(0,9,san(nm),align="C",new_x="LMARGIN",new_y="NEXT")
    pdf.set_font("Helvetica","",10); pdf.cell(0,6.5,san(part),align="C",new_x="LMARGIN",new_y="NEXT"); pdf.ln(2)
pdf.set_text_color(*DARK)

# ============================================================ ARSITEKTUR (Khairi opening)
pdf.add_page()
section_title("PEMBUKA","KHAIRI: Produk, Tech Stack & Arsitektur",KHAIRI)
speaker("Khairi",KHAIRI)
say("Assalamualaikum / selamat pagi Bapak/Ibu dosen. Perkenalkan, kami tim ALIRIN. Kami "
    "akan mempresentasikan aplikasi mobile bernama ALIRIN: sistem pelaporan sekaligus "
    "prediksi drainase mikro untuk Kota Bandar Lampung.")
say("Latar belakangnya: genangan dan banjir kecil di kota sering terjadi bukan hanya karena "
    "hujan, tapi karena drainase (got) mampet, tertutup sampah, atau tidak terawat. "
    "Penanganannya selama ini reaktif - hujan turun, banjir, baru warga lapor, baru "
    "ditangani. ALIRIN mengubah pola ini jadi PREVENTIF: laporan warga dan data cuaca "
    "diolah menjadi prioritas aksi dan prediksi risiko.")
say("Ada tiga peran pengguna dalam sistem ini:")
point("Warga","melapor drainase bermasalah, melihat peta risiko, dan memantau status.")
point("Staff","memvalidasi dan menindaklanjuti laporan - semuanya di dalam aplikasi.")
point("Admin","mengelola data lewat website; aplikasi mobile mengarahkan ke web.")
say("Dari sisi teknologi, aplikasi dibangun native Android dengan Kotlin dan Jetpack "
    "Compose untuk antarmukanya. Kami memakai 3 sumber data dari internet:")
point("BMKG","API resmi pemerintah untuk prakiraan cuaca per kelurahan.")
point("GROQ AI","model AI (Llama) untuk memprediksi kondisi udara, suhu, curah hujan, debit air.")
point("dummyjson","layanan autentikasi untuk login staff dan admin.")
say("Pola arsitektur yang kami pakai adalah MVVM (Model-View-ViewModel) dengan pemisahan "
    "lapisan. Setiap lapisan punya tugas sendiri dan tidak saling mencampuri:")
code([
    "UI (Jetpack Compose)   -> hanya menampilkan, tidak tahu soal API",
    "        | StateFlow (data mengalir otomatis)",
    "ViewModel              -> menyimpan & menyiapkan state untuk UI",
    "        |",
    "Repository             -> memproses & menggabungkan data",
    "        |",
    "Network (Retrofit)     -> Service / ApiClient / DTO  -> API",
])
say("Keuntungan pemisahan ini: mudah dirawat dan diuji. Mengganti tampilan tidak mengubah "
    "logika; mengganti sumber data tidak mengubah tampilan. Struktur foldernya mengikuti "
    "tanggung jawab tadi:")
code([
    "data/network/      -> service/ (endpoint), dto/ (model JSON), ApiClient",
    "data/repository/   -> Auth, Weather, Prediction, Report",
    "data/local/        -> DataStore (menyimpan token login)",
    "feature/           -> layar per fitur (auth, beranda, lapor, peta, staff)",
    "ui/theme/          -> warna, font, ukuran (satu sumber design system)",
    "ui/components/     -> komponen dipakai ulang (Pill, Avatar, Button, Card)",
    "nav/               -> pengatur navigasi & peran pengguna",
])
say("Sekarang saya lanjut menjelaskan halaman-halaman awal aplikasi.")

# ============================================================ BAGIAN 1 KHAIRI
pdf.add_page()
section_title("BAGIAN 1","KHAIRI: Splash, Onboarding, Login & Beranda",KHAIRI)

page_head("Splash Screen","feature/auth/SplashScreen.kt")
speaker("Khairi",KHAIRI)
say("Saat aplikasi dibuka, yang pertama muncul adalah Splash Screen: logo tetesan air "
    "ALIRIN dengan latar hijau-teal selama sekitar 1,8 detik. Ini juga mencegah layar putih "
    "berkedip saat aplikasi dingin (cold start) karena kami pakai SplashScreen API resmi "
    "Android.")
code([
    "LaunchedEffect(Unit) {",
    "    delay(1800)        // tahan 1.8 detik",
    "    onDone()           // lanjut ke layar berikutnya",
    "}",
],file="feature/auth/SplashScreen.kt")

page_head("Onboarding (3 slide)","feature/auth/OnboardingScreen.kt")
say("Setelah splash, muncul Onboarding berisi 3 slide perkenalan: (1) Lapor Cepat 10 detik, "
    "(2) Verifikasi gotong-royong, (3) Prediksi cuaca BMKG. Slide bisa digeser. Statusnya "
    "disimpan permanen di DataStore, jadi onboarding hanya muncul sekali - di buka "
    "berikutnya langsung lewat.")
code([
    "!onboardingDone -> OnboardingScreen(onDone = { authVm.finishOnboarding() })",
    "// finishOnboarding() menyimpan flag ke DataStore",
],file="nav/AlirinNavHost.kt")
cue("peragakan: Splash -> geser Onboarding -> Mulai pakai ALIRIN")

page_head("Welcome Screen","feature/auth/WelcomeScreen.kt")
say("Halaman Welcome adalah pintu masuk. Warga bisa langsung menekan 'Mulai sebagai Warga' "
    "tanpa perlu daftar atau login - sesuai prinsip pelaporan publik yang cepat. Sementara "
    "petugas menekan tautan kecil 'Masuk sebagai Staff' di bawahnya. Pembedaan ini "
    "disengaja: warga harus semudah mungkin, akses staff sedikit tersembunyi.")
code([
    "WelcomeScreen(",
    "    onWarga = { authVm.chooseAnonymous() },  // masuk anonim",
    "    onStaff = { showStaffLogin = true },      // buka form login",
    ")",
],file="nav/AlirinNavHost.kt")

page_head("Login Staff","feature/auth/LoginScreen.kt + LoginContent.kt")
say("Untuk login, kami pisahkan menjadi dua file sebagai contoh pola pemisahan: LoginScreen "
    "menangani logika dan state, sedangkan LoginContent murni tampilan - hanya menerima data "
    "dan aksi lewat parameter. Dengan begitu tampilannya bisa dipratinjau dan diuji "
    "terpisah dari logika.")
say("Proses login memanggil API lewat AuthViewModel. State login (loading, sukses, gagal) "
    "dibungkus rapi sehingga UI tinggal bereaksi.")
code([
    "fun login(username, password) {",
    "    ui.value = Submitting",
    "    viewModelScope.launch {",
    "        runCatching { repository.login(username, password) }",
    "            .onSuccess { ui.value = Ok(it) }",
    "            .onFailure { ui.value = Failed(it.message) }",
    "    }",
    "}",
],file="feature/ViewModels.kt (AuthViewModel)")
say("Token hasil login disimpan di DataStore (penyimpanan lokal aman), dan dipakai otomatis "
    "untuk permintaan berikutnya. Username menentukan peran: emilys=staff, michaelw=admin.")

page_head("Beranda + Kartu Prediksi AI","feature/beranda/BerandaScreen.kt")
say("Inilah halaman utama warga. Di atas ada sapaan + nama kecamatan terpilih, lalu grid "
    "menu: Lapor Cepat, Lapor Lengkap, Peta Risiko, dan Status. Di bawahnya ada strip cuaca "
    "BMKG, dan komponen andalan kami: Kartu Prediksi AI.")
say("Kartu prediksi menampilkan ringkasan, kondisi udara, suhu, curah hujan 3 jam ke depan, "
    "estimasi debit air drainase, plus rekomendasi tindakan - semuanya hasil olahan AI dari "
    "data BMKG. Datanya reaktif memakai StateFlow, jadi begitu pengguna ganti kelurahan, "
    "kartunya otomatis menghitung ulang.")
code([
    "val prediction by vm.prediction.collectAsStateWithLifecycle()",
    "// AiForecast: kondisiUdara, suhuCelsius, curahHujanMm,",
    "//            debitAirMs, ringkasan, rekomendasi[]",
    "if (p == null) PredictionCardSkeleton()  // loading",
    "else PredictionCardContent(model = p)    // tampilkan hasil",
],file="feature/beranda/BerandaScreen.kt")
say("Detail teknis bagaimana AI memprosesnya ada di slide khusus nanti. Saya serahkan ke "
    "Revaldo untuk halaman Lapor, Peta, dan Status.")

# ============================================================ BAGIAN 2 REVALDO
pdf.add_page()
section_title("BAGIAN 2","REVALDO: Lapor, Peta Risiko & Status",REVALDO)
speaker("Revaldo",REVALDO)
say("Terima kasih Khairi. Saya menjelaskan tiga aktivitas inti warga: membuat laporan, "
    "melihat peta risiko, dan memantau status laporan.")

page_head("Alur Lapor - dibuka sebagai drawer","feature/lapor/LaporFlowScreen.kt")
say("Saat tombol Lapor (tombol + di menu bawah) ditekan, form lapor muncul sebagai drawer "
    "yang naik dari bawah layar - bukan pindah halaman penuh - sehingga terasa ringan. Alur "
    "isinya 4 langkah berurutan yang dikelola satu state machine.")
code([
    "when (step) {",
    "    Choice  -> ChoiceStep(...)   // pilih Cepat / Lengkap",
    "    Lokasi  -> LokasiStep(...)   // GPS / pin lokasi",
    "    Detail  -> DetailStep(...)   // kategori + keparahan",
    "    Foto    -> FotoStep(...)     // foto (Lengkap saja)",
    "    Success -> SuccessScreen(...)// nomor laporan keluar",
    "}",
],file="feature/lapor/LaporFlowScreen.kt")

page_head("Langkah 1-2: Pilih Mode & Lokasi","feature/lapor/ChoiceStep.kt, LokasiStep.kt")
say("Langkah pertama memilih Lapor Cepat (tanpa foto, 10 detik) atau Lapor Lengkap (dengan "
    "foto, skor lebih tinggi). Langkah kedua menentukan lokasi: pengguna menekan 'Gunakan "
    "lokasi saya' yang meminta izin GPS lalu mengisi koordinat otomatis; kecamatan dan "
    "kelurahan mengikuti pilihan pengguna.")
code([
    "locVm.fetchOnce { loc ->",
    "    if (loc != null)",
    "        onUpdate(form.copy(lat=loc.lat, lng=loc.lng,",
    "                           accuracyMeters=loc.accuracyMeters))",
    "}",
],file="feature/lapor/LokasiStep.kt")

page_head("Langkah 3-4: Detail & Foto (watermark)","feature/lapor/DetailStep.kt, FotoStep.kt")
say("Langkah ketiga memilih kategori masalah (6 pilihan) dan tingkat keparahan (4 tingkat). "
    "Langkah keempat (hanya untuk Lapor Lengkap) menambah foto. Foto dari kamera otomatis "
    "diberi watermark berisi koordinat dan waktu - supaya tidak bisa dipalsukan dengan foto "
    "lama. Foto dari galeri ditandai 'tidak diverifikasi lokasi'.")
code([
    "PhotoStore.saveCameraBitmap(ctx, bitmap, lat, lng)",
    "// menambahkan teks 'lat, lng + waktu' di pojok foto",
],file="data/PhotoStore.kt")

page_head("Kirim Laporan & Success","feature/lapor/SuccessScreen.kt")
say("Saat dikirim, ViewModel menghitung skor risiko dari tingkat keparahan dan mode, lalu "
    "menyimpan laporan ke ReportRepository. Pengguna mendapat nomor laporan unik "
    "(ALR-2026-xxxx) dan ringkasan. Laporan baru langsung muncul di daftar status.")
code([
    "fun submit() {",
    "    val result = repository.submitReport(form.value, mode.value)",
    "    submitted.value = result.code      // ALR-2026-xxxx",
    "}",
],file="feature/ViewModels.kt (LaporViewModel)")
cue("peragakan: Lapor Lengkap -> lokasi -> kategori -> foto -> Kirim -> nomor laporan")

page_head("Peta Risiko","feature/peta/PetaScreen.kt + OsmMapView.kt")
say("Peta memakai osmdroid dengan peta OpenStreetMap - gratis, tanpa API key, tanpa biaya. "
    "Di atas peta ada kartu 'Area kamu' yang dihitung dari kelurahan terpilih dan titik di "
    "sekitarnya. Setiap titik rawan jadi marker berwarna sesuai level risiko. Ada kolom "
    "pencarian, filter sumber data, tombol zoom, dan legenda warna.")
say("Marker digambar langsung di peta sehingga ikut bergeser saat peta digeser/di-zoom. Tap "
    "marker membuka detail di bottom sheet: kategori, level, skor, status, dan tombol "
    "'Tambah laporan di sini'.")
code([
    "OsmMapView(",
    "    hotspots = filtered,            // hasil filter + pencarian",
    "    selectedId = selected?.id,",
    "    onHotspotTap = { selected = it },",
    "    onMapReady = { mapRef = it },   // referensi utk tombol zoom",
    ")",
],file="feature/peta/PetaScreen.kt")

page_head("Status Laporan (daftar)","feature/status/StatusListScreen.kt")
say("Halaman ini menampilkan laporan milik pengguna. Ada pencarian (berdasarkan nomor, "
    "kategori, atau kelurahan) dan filter Semua / Aktif / Selesai yang langsung menyaring "
    "daftar. Tiap kartu menampilkan nomor, kategori, lokasi, badge status, dan progres mini.")
code([
    "val filtered = reports",
    "    .filter { sesuaiFilter(it) }         // Semua/Aktif/Selesai",
    '    .filter { it.code.contains(query) || it.category... }',
],file="feature/status/StatusListScreen.kt")

page_head("Detail Status (timeline)","feature/status/StatusDetailScreen.kt")
say("Detail laporan menampilkan informasi lengkap dan timeline 6 tahap: Menunggu -> "
    "Diverifikasi -> Dijadwalkan -> Ditangani -> Selesai (atau Ditolak). Setiap tahap "
    "menampilkan waktu dan catatan dari petugas. Ada juga tombol Bagikan yang memakai share "
    "sheet Android. Saya lanjut ke Naufal untuk sisi staff.")

# ============================================================ BAGIAN 3 NAUFAL
pdf.add_page()
section_title("BAGIAN 3","NAUFAL: Navigasi/Peran, Sisi Staff, Tentang & Web",NAUFAL)
speaker("Naufal",NAUFAL)
say("Terima kasih Revaldo. Saya mulai dari bagaimana aplikasi menentukan tampilan "
    "berdasarkan peran pengguna, lalu menjelaskan seluruh halaman sisi staff.")

page_head("Navigasi & Pembagian Peran","nav/AlirinNavHost.kt")
say("Pusat navigasi menentukan layar berdasarkan status: belum splash, belum onboarding, "
    "sudah login (peran apa), atau anonim. Admin langsung diarahkan ke halaman web; warga "
    "dan staff masuk ke aplikasi utama dengan menu bawah yang berbeda.")
code([
    "when {",
    "  !splashDone        -> SplashScreen()",
    "  !onboardingDone    -> OnboardingScreen()",
    "  role == Admin      -> AdminWallScreen()   // arahkan ke web",
    "  login / anonim     -> MainShell()         // warga / staff",
    "  else               -> WelcomeScreen()",
    "}",
],file="nav/AlirinNavHost.kt")
say("Transisi antar layar dibuat halus bergaya iOS (slide), dan menu bawah muncul/hilang "
    "dengan animasi mulus mengikuti lifecycle.")

page_head("Admin Wall","feature/auth/AdminWallScreen.kt")
say("Jika yang login adalah admin, aplikasi tidak menampilkan dashboard di HP, melainkan "
    "halaman yang menjelaskan bahwa pengelolaan admin dilakukan lewat website. Ini sesuai "
    "pembagian: mobile fokus untuk warga dan staff lapangan.")

page_head("Inbox Verifikasi (Staff)","feature/staff/PersetujuanListScreen.kt")
say("Setelah login sebagai staff, menu bawah berganti - muncul tab Verifikasi. Halaman ini "
    "berisi kartu statistik (jumlah menunggu, jumlah kritis, rata-rata respon), filter "
    "(Semua/Kritis/Dengan foto/Tanpa foto), dan daftar laporan yang menunggu validasi. "
    "Laporan kritis ditandai border merah agar menonjol.")

page_head("Detail & Aksi Validasi","feature/staff/PersetujuanDetailScreen.kt")
say("Di detail, staff melihat foto bukti, deskripsi, dan 'sinyal validasi': berapa warga "
    "yang melapor, ada/tidaknya hotspot historis, dan skor risiko sistem. Aksinya lewat "
    "drawer: Verifikasi (beri catatan), Tolak (pilih alasan), atau Jadwalkan (pilih tim dan "
    "waktu). Ada juga tombol Navigasi yang membuka aplikasi peta ke lokasi.")
code([
    "fun transition(reportId, newStatus, note) {",
    "    repository.updateReportStatus(reportId, newStatus,",
    "        note, actorLabel)   // status + catatan masuk riwayat",
    "}",
],file="feature/ViewModels.kt (StaffViewModel)")
say("Karena memakai StateFlow, perubahan status oleh staff ini LANGSUNG terlihat di halaman "
    "Status milik warga - tanpa perlu refresh. Inilah inti alur end-to-end aplikasi.")
cue("peragakan: login emilys/emilyspass -> Verifikasi -> pilih laporan -> Verifikasi -> sukses")

page_head("Tindak Lanjut, Statistik, Profil","feature/staff/StaffTindakLanjutScreen.kt, StaffStatistikScreen.kt, StaffProfilScreen.kt")
say("Tab Lanjut menampilkan pekerjaan tim (berjalan/selesai) dengan tombol Tutup untuk "
    "menyelesaikan. Tab Stats berisi grafik 14 hari, pecahan kategori, dan top kelurahan - "
    "dihitung otomatis dari data laporan, bukan angka tetap. Tab Profil berisi info staff "
    "dan tombol 'Ganti peran' untuk keluar dan kembali ke pemilihan peran.")
code([
    "// pecahan kategori dihitung dari daftar laporan, bukan hardcode",
    "val categoryBreakdown = reports",
    "    .groupingBy { it.category }.eachCount()",
    "    .entries.sortedByDescending { it.value }",
],file="feature/staff/StaffStatistikScreen.kt")

page_head("Tentang & ALIRIN Web","feature/tentang/TentangScreen.kt, feature/web/WebViewScreen.kt")
say("Halaman Tentang menjelaskan cara kerja sistem, empat sumber data, dan kebijakan privasi "
    "(anonim per perangkat). Ada tautan 'Buka ALIRIN Web' yang membuka situs di dalam "
    "aplikasi menggunakan WebView, lengkap dengan indikator loading dan tombol kembali.")

pdf.ln(1); speaker("Naufal",NAUFAL)
say("Sebagai penutup, rencana pengembangan kami: backend real-time agar laporan tersinkron "
    "lintas perangkat, integrasi sensor IoT ketinggian air, dan notifikasi dini cuaca. "
    "Demikian presentasi dari tim kami. Terima kasih, kami buka sesi tanya jawab.")

# ============================================================ TECH DEEP-DIVE
pdf.add_page()
section_title("LAMPIRAN","Detail Teknis (untuk pertanyaan mendalam)",DARK)
page_head("Lapisan Network - 3 file","data/network/")
say("Koneksi API kami pisah jadi tiga tanggung jawab dengan Retrofit:")
point("Service","interface daftar endpoint - APA yang dipanggil.")
point("ApiClient","atur header, token, timeout, base URL - BAGAIMANA memanggil.")
point("DTO","data class @Serializable - bentuk JSON yang diterima.")
code([
    "interface BmkgService {",
    '    @GET("publik/prakiraan-cuaca")',
    '    suspend fun forecast(@Query("adm4") adm4: String)',
    "        : BmkgForecastResponse",
    "}",
],file="data/network/service/BmkgService.kt")

page_head("Repository - menggabungkan BMKG + AI","data/repository/PredictionRepository.kt")
say("Repository inilah otak penggabungan. Data cuaca BMKG disusun jadi prompt, dikirim ke "
    "GROQ, lalu jawabannya (JSON) diubah jadi objek AiForecast.")
code([
    "val res = api.groqService.chat(ChatCompletionRequest(",
    "  messages = listOf(",
    '    ChatMessage("system", "prediksi cuaca + debit air, JSON..."),',
    '    ChatMessage("user", "curah hujan: $precip mm, suhu: $temp C"),',
    "  )))",
    "return json.decodeFromString<AiForecast>(rawJson)",
])
say("Pengaman: jika API key tidak ada atau internet mati, sistem otomatis pakai perhitungan "
    "rule-based - aplikasi tetap menampilkan prediksi, tidak error.")
code([
    "if (!api.groqConfigured) return fallback(forecast)",
    "return runCatching { callGroq(...) }",
    "         .getOrElse { fallback(forecast) }",
])

# ============================================================ Q&A
pdf.add_page()
section_title("TANYA-JAWAB","Bocoran Pertanyaan Dosen + Jawaban",DARK)
qa=[
 ("Datanya dari mana?",
  "Cuaca: API resmi BMKG. Prediksi: AI GROQ (model Llama). Login: dummyjson. Data laporan "
  "saat ini contoh (dummy) di memori - arsitektur sudah siap dihubungkan ke backend nyata "
  "tanpa mengubah tampilan."),
 ("Cara kerja prediksi AI-nya bagaimana?",
  "Data cuaca BMKG (curah hujan, suhu, kelembaban 3 jam ke depan) disusun jadi prompt, "
  "dikirim ke model AI GROQ. AI mengembalikan JSON berisi kondisi udara, suhu, curah hujan, "
  "estimasi debit air drainase, dan rekomendasi tindakan."),
 ("Kalau tidak ada internet / API gagal?",
  "Ada perhitungan cadangan (rule-based). Aplikasi tetap menampilkan prediksi, tidak error."),
 ("Cara mengambil data dari API gimana?",
  "Memakai Retrofit, dipisah 3 file: Service (endpoint), ApiClient (header/token/timeout), "
  "DTO (bentuk JSON). Repository memproses dan menggabungkan hasilnya, lalu dialirkan ke UI "
  "lewat ViewModel."),
 ("Kenapa memakai MVVM?",
  "Memisahkan tampilan, logika, dan data. Mudah dirawat, diuji, dan diganti per bagian "
  "tanpa merusak bagian lain."),
 ("Kok datanya update otomatis?",
  "Memakai StateFlow. UI 'berlangganan' data; ketika data berubah (mis. staff memvalidasi), "
  "tampilan otomatis tergambar ulang tanpa refresh manual."),
 ("Peta pakai apa? Berbayar?",
  "osmdroid dengan OpenStreetMap - gratis, tanpa API key, tanpa biaya."),
 ("Bagaimana keamanan API key AI?",
  "Disimpan di local.properties (di-gitignore), dibaca lewat BuildConfig. Tidak ikut "
  "ter-upload ke repository GitHub."),
 ("Bedanya Lapor Cepat dan Lengkap?",
  "Cepat: tanpa foto, ringan, skor bukti lebih rendah. Lengkap: dengan foto ber-watermark, "
  "skor lebih tinggi sehingga lebih diprioritaskan petugas."),
 ("Apa itu verifikasi gotong-royong?",
  "Jika 3 warga atau lebih melapor titik yang sama dalam radius dan waktu berdekatan, "
  "laporan otomatis dianggap terverifikasi oleh warga."),
 ("Foto bisa dipalsukan tidak?",
  "Foto dari kamera otomatis diberi watermark koordinat + waktu. Foto dari galeri ditandai "
  "'tidak diverifikasi lokasi'."),
 ("Kenapa skor risiko bisa beda tiap laporan?",
  "Dihitung dari tingkat keparahan dikali pengali mode (Lengkap lebih tinggi dari Cepat), "
  "menghasilkan level Normal/Waspada/Tinggi/Kritis."),
]
for q,a in qa:
    pdf.set_x(pdf.l_margin); pdf.set_font("Helvetica","B",10.5); pdf.set_text_color(*NAUFAL)
    pdf.multi_cell(0,5.6,san("T: "+q),new_x="LMARGIN",new_y="NEXT")
    pdf.set_x(pdf.l_margin); pdf.set_font("Helvetica","",10.5); pdf.set_text_color(*DARK)
    pdf.multi_cell(0,5.6,san("J: "+a),new_x="LMARGIN",new_y="NEXT"); pdf.ln(2.5)

out="ALIRIN_Skrip_Presentasi.pdf"; pdf.output(out); print("written",out)
