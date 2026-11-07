package com.batpay.batpay.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.ServletRequestPathUtils;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    public static final String USUARIO_LOGADO = "usuarioLogado";

    private final String[] origensPermitidas;

    public CorsConfig(@Value("${BATPAY_CORS_ORIGINS:http://localhost:[*],http://127.0.0.1:[*]}") String[] origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Idempotency-Key")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (CorsUtils.isPreFlightRequest(request)) {
                    return true;
                }
                response.setHeader("Cache-Control", "no-store");
                String caminho = ServletRequestPathUtils.getParsedRequestPath(request).pathWithinApplication().value();
                if (HttpMethod.POST.matches(request.getMethod())
                        && (caminho.equals("/usuarios") || caminho.equals("/usuarios/login"))) {
                    return true;
                }
                var sessao = request.getSession(false);
                if (sessao == null || sessao.getAttribute(USUARIO_LOGADO) == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para acessar o BatPay.");
                }
                return true;
            }
        }).addPathPatterns("/usuarios", "/usuarios/**", "/clientes", "/clientes/**",
                "/cobrancas", "/cobrancas/**", "/pagamentos", "/pagamentos/**");
    }
}
