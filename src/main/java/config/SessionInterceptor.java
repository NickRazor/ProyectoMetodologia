package config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(@SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null") HttpServletResponse response, @SuppressWarnings("null") Object handler) {
        HttpSession session = request.getSession();
        if (session != null) {
            // Extender el tiempo de la sesión
            session.setMaxInactiveInterval(30 * 60); // 30 minutos
        }
        return true;
    }
}