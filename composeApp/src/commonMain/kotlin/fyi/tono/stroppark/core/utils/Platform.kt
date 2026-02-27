package fyi.tono.stroppark.core.utils

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform