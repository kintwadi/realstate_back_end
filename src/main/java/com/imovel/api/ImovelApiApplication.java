package com.imovel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import java.util.TimeZone;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ImovelApiApplication {

	public static void main(String[] args) {
		//TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"));
		SpringApplication.run(ImovelApiApplication.class, args);
	}

}
