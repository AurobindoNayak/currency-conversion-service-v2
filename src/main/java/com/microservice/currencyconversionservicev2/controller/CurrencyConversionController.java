package com.microservice.currencyconversionservicev2.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.microservice.currencyconversionservicev2.entity.CurrencyConversion;
import com.microservice.currencyconversionservicev2.entity.ExchangeValue;
import com.microservice.currencyconversionservicev2.feignproxy.CurrencyExchangeServiceFeignProxy;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;



@RestController
public class CurrencyConversionController {

	@Autowired
	private CurrencyExchangeServiceFeignProxy proxy;

	@GetMapping("/getAmount/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion getAmount(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {
		Map<String, String> uriValues = new HashMap<>();
		uriValues.put("from", from);
		uriValues.put("to", to);

		ResponseEntity<ExchangeValue> responseEntity = new RestTemplate()
				.getForEntity("http://localhost:8000/get/from/{from}/to/{to}", ExchangeValue.class, uriValues);
		ExchangeValue exchangeValue = responseEntity.getBody();
		return new CurrencyConversion(exchangeValue.getId(), from, to, exchangeValue.getConversionMultiple(), quantity,
				quantity.multiply(exchangeValue.getConversionMultiple()), exchangeValue.getPort());
	}

	//@HystrixCommand(fallbackMethod = "getData")
	@GetMapping("/proxy/from/{from}/to/{to}/quantity/{quantity}")
	//@Retry(name = "default",fallbackMethod = "getData")
	@CircuitBreaker(name = "default",fallbackMethod = "getData")
	public CurrencyConversion getAmountByProxy(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {
		ExchangeValue exchangeValue = proxy.getExchangeAmount(from, to);
		return new CurrencyConversion(exchangeValue.getId(), from, to, exchangeValue.getConversionMultiple(), quantity,
				quantity.multiply(exchangeValue.getConversionMultiple()), exchangeValue.getPort());
	}

	//Need to pass the Exception in the method if we are using resilience @retry 
	public CurrencyConversion getData(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity, Exception ex) {

		return new CurrencyConversion(1, from, to, BigDecimal.valueOf(60), quantity,
				quantity.multiply(BigDecimal.valueOf(60)), 8080);
	}

}
