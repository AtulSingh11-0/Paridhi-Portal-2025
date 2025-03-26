package com.megatronix.paridhi.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.megatronix.paridhi.repository.BlackListedTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${jwt.secret}")
  private String SECRET_KEY;

  @Value("${jwt.expiration}")
  private long EXPIRATION_TIME;

  private final BlackListedTokenRepository blackListedTokenRepository;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts
      .builder()
      .claims(extraClaims)
      .subject(userDetails.getUsername())
      .issuedAt(new Date(System.currentTimeMillis()))
      .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
      .signWith(getSigninKey())
      .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return ( 
      username.equals(userDetails.getUsername()) 
      && !isTokenExpired(token) 
      && !isTokenBlacklisted(token) 
    );
  }

  public boolean isTokenBlacklisted(String token) {
    return blackListedTokenRepository.existsByToken(token);
  }

  public LocalDateTime getExpirationDateFromToken(String token) {
    var expirationDate = extractExpiration(token);
    return LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault());
  }

  public boolean isTokenExpired(String token) {
    try {
			return extractExpiration(token).before(new Date());
		} catch (Exception e) {
			return true;
		}
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
      .parser()
      .verifyWith(getSigninKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  private SecretKey getSigninKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
