package com.gokcank.curalis.domain.model

/** Bir ölçüm türü için "bunu ölçmeyi unutma" hatırlatıcısı — ilaç hatırlatıcısından
 *  bağımsız, kullanıcı isteğe bağlı olarak her ölçüm türü için ayrı kurar.
 *  [daysOfWeek] boşsa her gün anlamına gelir; doluysa yalnızca o günlerde (1=Pzt..7=Paz). */
data class VitalReminderSetting(
    val type: VitalType,
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
    val daysOfWeek: List<Int> = emptyList()
)
