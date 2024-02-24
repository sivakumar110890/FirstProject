/**
 *
 */
package com.emagine.ussd.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author udaykapavarapu
 *
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.emagine")
public class AppConfig {

}
