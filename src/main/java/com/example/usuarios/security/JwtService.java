package com.example.usuarios.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration-ms}")
	private long expirationMs;

	public String generateToken(UsuarioPrincipal principal) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(principal.getEmail())
				.claims(Map.of(
						"uid", principal.getId(),
						"rol", principal.getRol()))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(signingKey())
				.compact();
	}

	public String extractEmail(String token) {
		return parseClaims(token).getSubject();
	}

	public Long extractUserId(String token) {
		Object uid = parseClaims(token).get("uid");
		if (uid instanceof Integer i) {
			return i.longValue();
		}
		if (uid instanceof Long l) {
			return l;
		}
		return Long.valueOf(uid.toString());
	}

	public String extractRol(String token) {
		return String.valueOf(parseClaims(token).get("rol"));
	}

	public boolean isTokenValid(String token, UserDetailsLike user) {
		String email = extractEmail(token);
		return email.equals(user.getUsername()) && !isExpired(token);
	}

	public boolean isTokenValid(String token) {
		try {
			return !isExpired(token);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isExpired(String token) {
		return parseClaims(token).getExpiration().before(new Date());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey signingKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/** Minimal interface to avoid coupling filter validation to UserDetails only. */
	public interface UserDetailsLike {
		String getUsername();
	}
}
