package com.lp.criticabackend.security;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
public class SessionUtil {


    public final String secretKey = "L0LuZ4gLX9pVUyjo7hkfvup72EngczEnlqK/6dKmLJU=";

    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    Key key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
