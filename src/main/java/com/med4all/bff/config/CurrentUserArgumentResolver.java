package com.med4all.bff.config;

import com.med4all.bff.dto.CurrentUserDetails;
import com.med4all.bff.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    public CurrentUserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                (parameter.getParameterType().equals(Long.class) ||
                        parameter.getParameterType().equals(String.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // sub from JWT

        if (parameter.getParameterType().equals(String.class)) {
            // If @CurrentUser String param → return email directly
            return email;
        }

        if (parameter.getParameterType().equals(Long.class)) {
            // If @CurrentUser Long param → resolve ID from DB
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"))
                    .getId();
        }

        throw new IllegalArgumentException("@CurrentUser not supported for type: " + parameter.getParameterType());
    }
}


