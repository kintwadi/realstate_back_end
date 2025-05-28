package com.imovel.api.security.validation.pointcut;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;

public class PointCuts {

    @Pointcut("execution(* com.imovel.api.controller.AuthController.register(..))")
    public static void registerValidation(){

    }

    @Pointcut("execution(* com.imovel.api.controller.AuthController.login(..))")
    public static void loginValidation(){

    }



}
