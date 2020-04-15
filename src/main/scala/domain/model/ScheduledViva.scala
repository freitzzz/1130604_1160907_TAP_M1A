package domain.model

import java.time.LocalDateTime

case class ScheduledViva(viva: Viva,
                         start: LocalDateTime,
                         end: LocalDateTime,
                         scheduledPreference: Int)
