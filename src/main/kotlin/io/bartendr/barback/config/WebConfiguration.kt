package io.bartendr.barback.config

import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.tomcat.util.descriptor.web.SecurityCollection
import org.apache.tomcat.util.descriptor.web.SecurityConstraint
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry

import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter




@Configuration
class WebConfiguration : WebMvcConfigurerAdapter() {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/{spring:^[a-zA-Z\\d-_]+}")
                .setViewName("forward:/")
        registry.addViewController("/**/{spring:^[a-zA-Z\\d-_]+}")
                .setViewName("forward:/")
        registry.addViewController("/{spring:^[a-zA-Z\\d-_]+}/**{spring:?!(\\.js|\\.css)$}")
                .setViewName("forward:/")
    }

    @Bean
    fun servletContainer(): ServletWebServerFactory? {
        val tomcat: TomcatServletWebServerFactory = object : TomcatServletWebServerFactory() {
            override fun postProcessContext(context: Context) {
                val securityConstraint = SecurityConstraint()
                securityConstraint.userConstraint = "CONFIDENTIAL"
                val collection = SecurityCollection()
                collection.addPattern("/*")
                securityConstraint.addCollection(collection)
                context.addConstraint(securityConstraint)
            }
        }
        tomcat.addAdditionalTomcatConnectors(redirectConnector())
        return tomcat
    }

    private fun redirectConnector(): Connector? {
        val connector = Connector("org.apache.coyote.http11.Http11NioProtocol")
        connector.setScheme("http")
        connector.setPort(80)
        connector.setSecure(false)
        connector.setRedirectPort(443)
        return connector
    }
}