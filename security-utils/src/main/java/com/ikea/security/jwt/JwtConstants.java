package com.ikea.security.jwt;

public class JwtConstants {

    private JwtConstants() {}

    public static final String ISSUER =
            System.getenv().getOrDefault("JWT_ISSUER", "ikea-client");

    public static final String SECRET =
            System.getenv().getOrDefault("JWT_SECRET", "change-me");

    public static final long EXPIRATION = 3600000;
}