package br.edu.fsa.planner.domain.model

@JvmInline
value class AccentColor(val rgb: Int) {
    init { require(rgb in 0..0xFFFFFF) }
    val hex get() = "#%06X".format(java.util.Locale.ROOT, rgb)
    companion object {
        val Default = AccentColor(0x5B735B)
        fun parse(value: String): AccentColor? = value.trim().removePrefix("#").takeIf { it.matches(Regex("[0-9a-fA-F]{6}")) }
            ?.toIntOrNull(16)?.let(::AccentColor)
    }
}

enum class AccentPreset(val color: AccentColor) {
    SAGE(AccentColor.Default), TERRACOTTA(AccentColor(0xAD664E)), LAVENDER(AccentColor(0x8D75B1)),
    BLUE(AccentColor(0x447D9B)), ROSE(AccentColor(0xB56780)), AMBER(AccentColor(0xB28329)), GRAPHITE(AccentColor(0x515C65))
}
