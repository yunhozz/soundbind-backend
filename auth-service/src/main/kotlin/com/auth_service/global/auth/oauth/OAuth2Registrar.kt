package com.auth_service.global.auth.oauth

enum class OAuth2Registrar(val registrationId: String) {
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver");
}