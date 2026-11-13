package com.hackathon.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * /api 요청마다 "메서드 경로 → 상태코드 (ms)" 한 줄을 남긴다.
 * 프론트가 "안 돼요" 할 때 배포 로그에서 실제로 뭐가 왔는지 바로 확인하는 용도.
 * 본문은 건강 정보가 섞일 수 있어 남기지 않는다.
 */
@Slf4j
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            String query = request.getQueryString();
            log.info("{} {}{} → {} ({}ms)", request.getMethod(), request.getRequestURI(),
                    query == null ? "" : "?" + query, response.getStatus(), System.currentTimeMillis() - start);
        }
    }
}
