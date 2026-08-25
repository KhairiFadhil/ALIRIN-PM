package com.example.alirinmobile.data

// Urutan status laporan. Kembar dengan C:\ALIRIN\app\src\domain\status.js dan
// fungsi alirin_allowed_status_transition di Supabase.
//
// Sebelumnya hanya web yang menegakkan urutan ini. Mobile menulis status apa pun
// tanpa memeriksa status sebelumnya, sehingga tercatat lompatan
// masuk -> dijadwalkan dan dijadwalkan -> selesai di database. Sekarang trigger
// Supabase menolaknya, dan pemeriksaan di sini mencegah aksi yang pasti gagal
// sampai ke jaringan.
object StatusMachine {

    private val TRANSITIONS: Map<ReportStatus, Set<ReportStatus>> = mapOf(
        ReportStatus.Pending to setOf(ReportStatus.Verified, ReportStatus.Rejected),
        ReportStatus.Verified to setOf(ReportStatus.Scheduled, ReportStatus.Rejected, ReportStatus.Pending),
        ReportStatus.Scheduled to setOf(ReportStatus.InProgress, ReportStatus.Verified, ReportStatus.Rejected),
        ReportStatus.InProgress to setOf(ReportStatus.Completed, ReportStatus.Scheduled, ReportStatus.Verified),
        ReportStatus.Completed to emptySet(),
        ReportStatus.Rejected to emptySet(),
    )

    fun canTransition(from: ReportStatus, to: ReportStatus): Boolean =
        from == to || TRANSITIONS[from].orEmpty().contains(to)

    fun nextStatuses(from: ReportStatus): Set<ReportStatus> = TRANSITIONS[from].orEmpty()

    fun isFinal(status: ReportStatus): Boolean =
        status == ReportStatus.Completed || status == ReportStatus.Rejected

    // Penutupan pekerjaan wajib disertai bukti. Trigger Supabase menolak
    // status 'selesai' tanpa foto penyelesaian.
    fun requiresCompletionPhoto(to: ReportStatus): Boolean = to == ReportStatus.Completed

    fun rejectionReason(from: ReportStatus, to: ReportStatus): String =
        "Transisi status tidak valid: ${from.label} ke ${to.label}."
}
