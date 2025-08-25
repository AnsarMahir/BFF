package com.med4all.bff.config;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
// This annotation is used to inject the current authenticated user into controller methods.
