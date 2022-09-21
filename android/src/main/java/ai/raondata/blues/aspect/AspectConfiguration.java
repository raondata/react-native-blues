package ai.raondata.blues.aspect;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectConfiguration {
    
    @Pointcut("execution(* ai.raondata.blues.core.Blues.*(..))")
    public void bluesMethods() {}
    
    @Before("bluesMethods()")
    public void before(JoinPoint jp) {
        String methodName = jp.getSignature().getName();
        Log.d(methodName, String.format(">> %s()", methodName));
    }
}
