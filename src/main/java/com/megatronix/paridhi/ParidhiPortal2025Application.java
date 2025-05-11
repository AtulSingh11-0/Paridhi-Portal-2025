package com.megatronix.paridhi;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@EnableWebMvc
@RestController
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
public class ParidhiPortal2025Application {
	public static void main(String[] args) {
		SpringApplication.run(ParidhiPortal2025Application.class, args);
		log.info("Portal successfully running on http://localhost:8080");
	}

	@GetMapping(produces = "application/json;charset=UTF-8")
	public Map<String, String> home() {
		Map<String, String> response = new TreeMap<>();
    response.put("chaupai-1", "जा पर कृपा राम की होई, ता पर कृपा करहिं सब कोई");
    response.put("chaupai-2", "राम भगति मनि उर बस जाकें, दु:ख लवलेस न सपनेहुँ ताकें");
    response.put("chaupai-3", "कवन सो काज कठिन जग माही, जो नहीं होइ तात तुम पाहीं");
		
		return response;
	}
}
