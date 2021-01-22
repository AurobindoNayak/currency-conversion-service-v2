package com.microservice.currencyconversionservicev2.feignproxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.microservice.currencyconversionservicev2.entity.ExchangeValue;

//@FeignClient(name="currency-exchange-service",url="http://localhost:8000")
@FeignClient(name="currency-exchange-service")
public interface CurrencyExchangeServiceFeignProxy {
	
	@GetMapping("/get/from/{from}/to/{to}")
	public ExchangeValue getExchangeAmount(@PathVariable String from, @PathVariable String to);

}
