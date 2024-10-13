rootProject.name = "soundbind-backend"
include(
    ":global",
    ":eureka-server",
    ":config-server",
    ":kafka-server",
    ":api-gateway",
    ":auth-service",
    ":music-service",
    ":review-service",
    ":notification-service",
    ":pay-service"
)