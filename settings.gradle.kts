rootProject.name = "soundbind-backend"
include(
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