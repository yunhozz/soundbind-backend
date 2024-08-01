package com.auth_service.domain.application

import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.entity.UserProfile
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.domain.persistence.repository.UserRepository
import com.auth_service.global.auth.enums.LoginType
import com.auth_service.global.auth.enums.Role
import com.auth_service.global.auth.oauth.OAuth2Provider
import com.auth_service.global.auth.oauth.OAuth2Registrar
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Random

@Service
class OAuth2UserCustomService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository
): OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val registration = userRequest.clientRegistration

        val attributes = oAuth2User.attributes
        val registrationId = registration.registrationId
        val userNameAttributeName = registration.providerDetails.userInfoEndpoint.userNameAttributeName

        val provider = OAuth2Provider.of(
            OAuth2Registrar.valueOf(registrationId),
            userNameAttributeName,
            attributes
        )

        val user: User = userProfileRepository.findWithUserByEmail(provider.email)?.let { up ->
            up.updateBySocialLogin(provider.name, LoginType.SOCIAL)
            up.user
        } ?: run {
            val socialUser = User.createWithTypes(LoginType.SOCIAL, Role.USER)
            val socialUserProfile = UserProfile.create(
                socialUser,
                provider.email,
                provider.name,
                createRandomNickname(),
                provider.imageUrl
            )
            userRepository.save(socialUser)
            userProfileRepository.save(socialUserProfile)
            socialUser
        }

        return DefaultOAuth2User(
            AuthorityUtils.createAuthorityList(user.role.name),
            attributes,
            userNameAttributeName
        )
    }

    private fun createRandomNickname(): String {
        var randomNickname: StringBuilder
        do {
            randomNickname = StringBuilder()
            val random = Random(System.currentTimeMillis())
            for (i in 0 until 8) {
                val index = random.nextInt(3)
                when (index) {
                    0 -> randomNickname.append((random.nextInt(26) + 97).toChar()) // a~z
                    1 -> randomNickname.append((random.nextInt(26) + 65).toChar()) // A~Z
                    2 -> randomNickname.append(random.nextInt(10)) // 0~9
                }
            }
        } while (userProfileRepository.existsByNickname(randomNickname.toString()))

        return randomNickname.toString()
    }
}