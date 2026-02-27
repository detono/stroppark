package fyi.tono.stroppark

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform