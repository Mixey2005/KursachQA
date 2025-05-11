package com.example.kursach.security; // Убедитесь, что пакет правильный

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys; // Для генерации безопасного ключа
import io.jsonwebtoken.security.SignatureException; // Для обработки ошибок подписи
import jakarta.annotation.PostConstruct; // Для инициализации ключа
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // Для чтения из application.properties
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Base64; // Для кодирования/декодирования ключа
import java.util.Date;

@Component // Делаем бином Spring, чтобы его можно было внедрять
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // Читаем секретный ключ и время жизни токена из application.properties
    @Value("${jwt.secret}")
    private String jwtSecretString;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    private Key key; // Ключ для подписи токенов (HMAC-SHA)

    @PostConstruct // Метод выполнится после создания бина и внедрения зависимостей (@Value)
    protected void init() {
        // Важно: В реальном приложении используйте ГОРАЗДО более сильный секрет,
        // сгенерированный и хранящийся безопасно (например, в переменных окружения).
        // Длина ключа должна быть достаточной для выбранного алгоритма (HS256, HS384, HS512).
        // Если jwtSecretString закодирован в Base64, декодируем его:
        // byte[] keyBytes = Base64.getDecoder().decode(jwtSecretString);
        // Если нет, используем как есть (но убедитесь, что он достаточно длинный и случайный):
        if (jwtSecretString == null || jwtSecretString.length() < 32) { // Проверка длины для HS256
            log.warn("!!! Секретный ключ JWT слишком короткий или не задан! Используется ключ по умолчанию (НЕБЕЗОПАСНО) !!!");
            // Генерируем безопасный ключ при старте (только для разработки, т.к. он будет разным при каждом рестарте)
            // this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            // Или используем строку по умолчанию (НЕБЕЗОПАСНО для продакшена!)
            this.key = Keys.hmacShaKeyFor("ОченьОченьОченьОченьОченьОченьСекретныйКлючДляJWTНеХранитеЕгоЗдесьВРеальномПроекте".getBytes());
        } else {
            // Преобразуем строку секрета в ключ
            byte[] keyBytes = jwtSecretString.getBytes(); // Предполагаем, что секрет не в Base64
            this.key = Keys.hmacShaKeyFor(keyBytes);
            log.info("Секретный ключ JWT успешно загружен.");
        }
    }

    /**
     * Генерирует JWT токен для указанного имени пользователя.
     * @param username Имя пользователя.
     * @return Сгенерированный JWT токен.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username) // Устанавливаем имя пользователя как "subject" токена
                .setIssuedAt(now) // Время выдачи токена
                .setExpiration(expiryDate) // Время истечения срока действия токена
                .signWith(key, SignatureAlgorithm.HS256) // Подписываем токен ключом и алгоритмом HS256
                // Можно добавить дополнительные claims (полезную информацию), например, роли:
                // .claim("roles", /* список ролей пользователя */)
                .compact(); // Собираем токен в строку
    }

    /**
     * Извлекает имя пользователя из JWT токена.
     * @param token JWT токен.
     * @return Имя пользователя.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key) // Указываем ключ для проверки подписи
                .build()
                .parseClaimsJws(token) // Парсим и проверяем токен
                .getBody(); // Получаем полезную нагрузку (claims)

        return claims.getSubject(); // Извлекаем "subject" (имя пользователя)
    }

    /**
     * Проверяет валидность JWT токена.
     * @param token JWT токен.
     * @return true, если токен валиден, иначе false.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // Если парсинг прошел успешно (подпись верна, срок не истек), токен валиден
        } catch (SignatureException ex) {
            log.error("Неверная подпись JWT: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Невалидный JWT токен: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Срок действия JWT токена истек: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Неподдерживаемый JWT токен: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Строка claims JWT пуста: {}", ex.getMessage());
        }
        return false; // Если возникло любое исключение, токен невалиден
    }
}