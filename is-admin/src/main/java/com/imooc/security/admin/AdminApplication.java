/**
 * 
 */
package com.imooc.security.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jojo
 *
 */
@SpringBootApplication
@RestController
@EnableZuulProxy
@Slf4j
public class AdminApplication {
	
	private RestTemplate restTemplate = new RestTemplate();
	
	@GetMapping("/me")
	public TokenInfo me(HttpServletRequest request) {
		return (TokenInfo)request.getSession().getAttribute("token");
	}

	@GetMapping("/oauth/callback")
	public void callback (@RequestParam String code, String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String oauthServiceUrl = "http://gateway.imooc.com:9070/token/oauth/token";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setBasicAuth("admin", "123456");
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("code", code);
		params.add("grant_type", "authorization_code");
		params.add("redirect_uri", "http://admin.imooc.com:8080/oauth/callback");
		
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
		
		ResponseEntity<TokenInfo> token = restTemplate.exchange(oauthServiceUrl, HttpMethod.POST, entity, TokenInfo.class);
		log.info("token info: " + token.getBody().toString());
		request.getSession().setAttribute("token", token.getBody().init());
		
		response.sendRedirect("/");
	}
	
	@PostMapping("/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getSession().invalidate();
	}
	
	/**
	 * 
	 * @author jojo
	 * 2019年8月25日
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}

}