package br.edu.fsa.planner.domain.model

import java.time.LocalDate

object RolloverRules {
    fun eligible(item: PlannerItem, today: LocalDate) = !item.isCompleted && !item.isInbox && !item.isDiscarded &&
        item.type != ItemType.EVENT && item.date < today
    fun shouldOffer(preferences: PlannerPreferences, today: LocalDate, items: List<PlannerItem>) =
        preferences.ready && preferences.gentleRolloverEnabled && preferences.lastRolloverReviewDate != today &&
            items.any { eligible(it, today) }
}
