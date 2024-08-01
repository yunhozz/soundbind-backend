package com.auth_service.global.auth.oauth

data class OAuth2Provider private constructor(
    val email: String,
    val name: String,
    val imageUrl: String,
    val userNameAttributeName: String,
    val attributes: Map<String, Any>
) {

    companion object {
        fun of(
            registrar: OAuth2Registrar,
            userNameAttributeName: String,
            attributes: Map<String, Any>
        ): OAuth2Provider =
            when (registrar) {
                OAuth2Registrar.GOOGLE -> ofGoogle(userNameAttributeName, attributes)
                OAuth2Registrar.KAKAO -> ofKakao(userNameAttributeName, attributes)
                OAuth2Registrar.NAVER -> ofNaver(userNameAttributeName, attributes)
            }

        private fun ofGoogle(userNameAttributeName: String, attributes: Map<String, Any>): OAuth2Provider =
            OAuth2Provider(
                attributes["email"] as String,
                attributes["name"] as String,
                attributes["picture"] as String,
                userNameAttributeName,
                attributes
            )

        private fun ofKakao(userNameAttributeName: String, attributes: Map<String, Any>): OAuth2Provider {
            val account = attributes["kakao_account"] as Map<String, Any>
            val profile = attributes["profile"] as Map<String, Any>
            return OAuth2Provider(
                account["email"] as String,
                profile["nickname"] as String,
                profile["profile_img_url"] as String,
                userNameAttributeName,
                attributes
            )
        }

        private fun ofNaver(userNameAttributeName: String, attributes: Map<String, Any>): OAuth2Provider {
            val response = attributes["response"] as Map<String, Any>
            return OAuth2Provider(
                response["email"] as String,
                response["name"] as String,
                response["profile_image"] as String,
                userNameAttributeName,
                attributes
            )
        }
    }
}