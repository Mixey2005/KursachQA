package com.example.kursach.security; // Убедитесь, что пакет правильный

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Импорт UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // Для проверки строки
import org.springframework.web.filter.OncePerRequestFilter; // Базовый класс фильтра


import java.io.IOException;

@Component // Делаем бином Spring, чтобы его можно было добавить в цепочку фильтров
@RequiredArgsConstructor // Внедряем зависимости через конструктор Lombok
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Наследуемся от базового фильтра Spring

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil; // Внедряем утилиту для работы с JWT
    private final UserDetailsService userDetailsService; // Внедряем сервис для загрузки UserDetails

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Извлечь токен из запроса
            String jwt = parseJwt(request);

            // 2. Проверить, есть ли токен и валиден ли он
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // 3. Извлечь имя пользователя из токена
                String username = jwtUtil.getUsernameFromToken(jwt);

                // 4. Загрузить UserDetails из базы данных
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Создать объект Authentication
                // Мы доверяем токену, поэтому создаем аутентификацию напрямую
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, // Principal
                        null,       // Credentials (не нужны при аутентификации по токену)
                        userDetails.getAuthorities()); // Authorities (роли/права)

                // 6. Добавить детали аутентификации (IP, сессия и т.д.) к объекту Authentication
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Установить объект Authentication в SecurityContextHolder
                // Это означает, что пользователь аутентифицирован для данного запроса
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Аутентификация пользователя '{}' по JWT токену установлена.", username);
            }
        } catch (Exception e) {
            // Логируем ошибку, но позволяем запросу идти дальше (доступ будет определен позже)
            log.error("Не удалось установить аутентификацию пользователя из JWT: {}", e.getMessage());
        }

        // 8. Передать запрос и ответ дальше по цепочке фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT токен из заголовка Authorization.
     * @param request HTTP запрос.
     * @return Строка токена (без префикса "Bearer ") или null, если заголовок отсутствует или некорректен.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(HEADER_AUTHORIZATION);

        // Проверяем, что заголовок не пустой и начинается с "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(TOKEN_PREFIX)) {
            // Извлекаем сам токен, удаляя префикс
            return headerAuth.substring(TOKEN_PREFIX.length());
        }

        // Если заголовок некорректен, возвращаем null
        log.trace("JWT токен не найден в заголовке Authorization");
        return null;
    }
}