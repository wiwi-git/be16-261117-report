package com.be16261117report.global.aspect;

import com.be16261117report.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// 컨트롤러 액션이 RsData를 반환하면, resultCode에서 파싱한 statusCode를
// HTTP 응답 상태 코드로 설정한다. (예: resultCode "201-1" → 201 Created)
@Aspect
@Component
public class RsDataAspect {
    @Around("""
            within(@org.springframework.web.bind.annotation.RestController *)
            && execution(public com.be16261117report.global.rsData.RsData *(..))
            """)
    public Object setResponseStatus(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof RsData<?> rsData
                && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletResponse response = attributes.getResponse();

            if (response != null) {
                response.setStatus(rsData.statusCode());
            }
        }

        return result;
    }
}
