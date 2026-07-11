package com.ikea.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtGenerator {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("System.getenv(JWT_ISSUER): " + System.getenv("JWT_ISSUER"));
        System.out.println("System.getenv(JWT_SECRET): " + System.getenv("JWT_SECRET"));
        System.out.println("JwtConstants.ISSUER      : " + JwtConstants.ISSUER);
        System.out.println("JwtConstants.SECRET      : " + JwtConstants.SECRET);
        System.out.println("==============================================");

        SecretKey key =
                Keys.hmacShaKeyFor(JwtConstants.SECRET.getBytes());

        Date issuedAt = new Date();

        Date expiry =
                new Date(System.currentTimeMillis()
                        + JwtConstants.EXPIRATION);

        String token =
                Jwts.builder()
                        .issuer(JwtConstants.ISSUER)
                        .issuedAt(issuedAt)
                        .expiration(expiry)
                        .signWith(key)
                        .compact();

        System.out.println("\n==============================================");
        System.out.println("JWT Successfully Generated");
        System.out.println("==============================================");

        System.out.println("Issuer      : " + JwtConstants.ISSUER);
        System.out.println("Issued At   : " + issuedAt);
        System.out.println("Expires At  : " + expiry);
        System.out.println("Valid For   : "
                + (JwtConstants.EXPIRATION / 1000)
                + " seconds");

        System.out.println("\nJWT Token:");
        System.out.println(token);

        System.out.println("==============================================");
    }
}