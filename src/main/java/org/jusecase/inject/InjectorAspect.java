package org.jusecase.inject;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.ConstructorSignature;

@Aspect
public class InjectorAspect {

    @Pointcut("within(@org.jusecase.inject.Component *)")
    public void typeAnnotatedWithComponent() {
    }

    @Pointcut("execution(public new(..))")
    public void constructor() {
    }

    @Before("typeAnnotatedWithComponent() && constructor()")
    public void inject(JoinPoint joinPoint) {
        ConstructorSignature signature = (ConstructorSignature) joinPoint.getStaticPart().getSignature();
        Injector.getInstance().inject(joinPoint.getThis(), signature.getDeclaringType());
    }

}
