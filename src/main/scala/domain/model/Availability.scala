package domain.model

import java.time.LocalDateTime

case class Availability(start: LocalDateTime,
                        end: LocalDateTime,
                        preference: Int)
