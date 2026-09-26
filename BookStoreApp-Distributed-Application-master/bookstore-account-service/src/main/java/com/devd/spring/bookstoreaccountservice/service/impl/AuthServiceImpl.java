package com.devd.spring.bookstoreaccountservice.service.impl;

import com.devd.spring.bookstoreaccountservice.repository.OAuthClientRepository;
import com.devd.spring.bookstoreaccountservice.repository.RoleRepository;
import com.devd.spring.bookstoreaccountservice.repository.UserRepository;
import com.devd.spring.bookstoreaccountservice.repository.dao.OAuthClient;
import com.devd.spring.bookstoreaccountservice.repository.dao.Role;
import com.devd.spring.bookstoreaccountservice.service.AuthService;
import com.devd.spring.bookstoreaccountservice.web.CreateOAuthClientRequest;
import com.devd.spring.bookstoreaccountservice.web.CreateOAuthClientResponse;
import com.devd.spring.bookstoreaccountservice.web.CreateUserResponse;
import com.devd.spring.bookstoreaccountservice.web.SignUpRequest;
import com.devd.spring.bookstoreaccountservice.web.GoogleLoginRequest;
import com.devd.spring.bookstoreaccountservice.web.JwtAuthenticationResponse;
import com.devd.spring.bookstorecommons.exception.RunTimeExceptionPlaceHolder;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author: Devaraj Reddy, Date : 2019-06-30
 */
@Service
public class AuthServiceImpl implements AuthService {

  @Autowired
  BCryptPasswordEncoder passwordEncoder;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  OAuthClientRepository oAuthClientRepository;

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  DefaultTokenServices tokenServices;

  @Value("${google.client-id}")
  private String googleClientId;

  @Value("${security.oauth.client-id:93ed453e-b7ac-4192-a6d4-c45fae0d99ac}")
  private String oauthClientId;

  @Value("${security.jwt.key-store}")
  private Resource keyStore;

  @Value("${security.jwt.key-store-password}")
  private String keyStorePassword;

  @Value("${security.jwt.key-pair-alias}")
  private String keyPairAlias;

  @Value("${security.jwt.key-pair-password}")
  private String keyPairPassword;

  @Value("${security.jwt.public-key}")
  private Resource publicKey;

  @Override
  public CreateOAuthClientResponse createOAuthClient(
      CreateOAuthClientRequest createOAuthClientRequest) {

    //Generate client secret.
    String clientSecret = UUID.randomUUID().toString();
    String encode = passwordEncoder.encode(clientSecret);

    OAuthClient oAuthClient = OAuthClient.builder()
        .client_secret(encode)
        .authorities(String.join(",", createOAuthClientRequest.getAuthorities()))
        .authorized_grant_types(
            String.join(",", createOAuthClientRequest.getAuthorized_grant_types()))
        .scope(String.join(",", createOAuthClientRequest.getScope()))
        .resource_ids(String.join(",", createOAuthClientRequest.getResource_ids()))
        .build();

    OAuthClient saved = oAuthClientRepository.save(oAuthClient);

    return CreateOAuthClientResponse.builder()
        .client_id(saved.getClient_id())
        .client_secret(clientSecret)
        .build();

  }

  @Override
  public CreateUserResponse registerUser(SignUpRequest signUpRequest) {

    if (userRepository.existsByUserName(signUpRequest.getUserName())) {
      throw new RunTimeExceptionPlaceHolder("Username is already taken!!");
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      throw new RunTimeExceptionPlaceHolder("Email address already in use!!");
    }

    // Creating user's account
    com.devd.spring.bookstoreaccountservice.repository.dao.User user =
        new com.devd.spring.bookstoreaccountservice.repository.dao.User(
            signUpRequest.getUserName(),
            signUpRequest.getPassword(),
            signUpRequest.getFirstName(),
            signUpRequest.getLastName(),
            signUpRequest.getEmail());

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    Role userRole = roleRepository.findByRoleName("STANDARD_USER")
        .orElseThrow(() -> new RuntimeException("User Role not set."));

    user.setRoles(Collections.singleton(userRole));

    com.devd.spring.bookstoreaccountservice.repository.dao.User savedUser =
        userRepository.save(user);

    return CreateUserResponse.builder()
        .userId(savedUser.getUserId())
        .userName(savedUser.getUserName())
        .build();

  }

  @Override
  public JwtAuthenticationResponse loginWithGoogle(GoogleLoginRequest googleLoginRequest) {

    if (googleClientId == null || googleClientId.trim().isEmpty()) {
      throw new RunTimeExceptionPlaceHolder(
              "Google authentication is not configured"
      );
    }

    try {

      // 1. Verify Google ID token
      GoogleIdTokenVerifier verifier =
              new GoogleIdTokenVerifier.Builder(
                      new NetHttpTransport(),
                      JacksonFactory.getDefaultInstance()
              )
                      .setAudience(
                              Collections.singletonList(googleClientId)
                      )
                      .build();

      GoogleIdToken googleIdToken =
              verifier.verify(googleLoginRequest.getIdToken());

      if (googleIdToken == null) {
        throw new RunTimeExceptionPlaceHolder(
                "Invalid Google ID token"
        );
      }

      // 2. Check whether Google email is verified
      if (!Boolean.TRUE.equals(
              googleIdToken.getPayload().getEmailVerified())) {

        throw new RunTimeExceptionPlaceHolder(
                "Google account could not be verified"
        );
      }

      // 3. Get Google user information
      GoogleIdToken.Payload payload =
              googleIdToken.getPayload();

      String email = payload.getEmail();

      String userName =
              "google_" + payload.getSubject();

      // 4. Find existing user or create a new user
      com.devd.spring.bookstoreaccountservice.repository.dao.User user =
              userRepository
                      .findByUserNameOrEmail(userName, email)
                      .orElseGet(() -> {

                        String fullName =
                                payload.get("name") == null
                                        ? email
                                        : payload.get("name").toString();

                        String[] nameParts =
                                fullName.trim().split("\\s+", 2);

                        String firstName =
                                nameParts.length > 0
                                        ? nameParts[0]
                                        : email;

                        String lastName =
                                nameParts.length > 1
                                        ? nameParts[1]
                                        : "";

                        Role role =
                                roleRepository
                                        .findByRoleName("STANDARD_USER")
                                        .orElseThrow(() ->
                                                new RuntimeException(
                                                        "User Role not set."
                                                )
                                        );

                        com.devd.spring.bookstoreaccountservice.repository.dao.User newUser =
                                new com.devd.spring.bookstoreaccountservice.repository.dao.User(
                                        userName,
                                        passwordEncoder.encode(
                                                UUID.randomUUID().toString()
                                        ),
                                        firstName,
                                        lastName,
                                        email
                                );

                        newUser.setRoles(
                                new HashSet<>(
                                        Collections.singletonList(role)
                                )
                        );

                        return userRepository.save(newUser);
                      });

      // 5. Convert user roles into Spring Security authorities
      List<GrantedAuthority> authorities =
              new ArrayList<>();

      user.getRoles().forEach(role ->
              authorities.add(
                      new SimpleGrantedAuthority(
                              role.getRoleName()
                      )
              )
      );

      // 6. Create OAuth2 request
      OAuth2Request oauth2Request =
              new OAuth2Request(
                      Collections.singletonMap(
                              "grant_type",
                              "google"
                      ),
                      oauthClientId,
                      authorities,
                      true,
                      Collections.singleton("read"),
                      Collections.singleton("web"),
                      null,
                      Collections.singleton("token"),
                      Collections.emptyMap()
              );

      // 7. Create Authentication object
      UsernamePasswordAuthenticationToken userAuthentication =
              new UsernamePasswordAuthenticationToken(
                      user.getUserName(),
                      user.getPassword(),
                      authorities
              );

      // 8. Create OAuth2 Authentication
      OAuth2Authentication authentication =
              new OAuth2Authentication(
                      oauth2Request,
                      userAuthentication
              );

      // 9. Generate access token
      OAuth2AccessToken token =
              tokenServices.createAccessToken(
                      authentication
              );

      // 10. Return response
      return new JwtAuthenticationResponse(
              token.getValue(),
              token.getTokenType(),
              token.getRefreshToken() == null
                      ? null
                      : token.getRefreshToken().getValue(),
              Long.valueOf(token.getExpiresIn())
      );

    } catch (RunTimeExceptionPlaceHolder exception) {

      throw exception;

    } catch (Exception exception) {

      throw new RunTimeExceptionPlaceHolder(
              "Google authentication failed"
      );
    }
  }
}
