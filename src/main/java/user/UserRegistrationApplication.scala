package user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import dust.DustViewResolver
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Primary}
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import scala.collection.JavaConversions._

@Configuration
@EnableAutoConfiguration
@ComponentScan
class UserRegistrationConfiguration {
  @Bean
  @Primary
  def scalaObjectMapper() = new ScalaObjectMapper

  @Bean
  def dustViewResolver = {
    val resolver = new DustViewResolver
    resolver.setPrefix("/WEB-INF/views/")
    resolver.setSuffix(".dust")

    resolver
  }

  @Bean
  def restTemplate = {
    val restTemplate = new RestTemplate()
    restTemplate.getMessageConverters foreach{
      case mc: MappingJackson2HttpMessageConverter => mc.setObjectMapper(scalaObjectMapper())
      case _ =>
    }

    restTemplate
  }
}

class ScalaObjectMapper extends ObjectMapper {
  registerModule(DefaultScalaModule)
}

object UserRegistrationApplication{
  def main(args: Array[String]): Unit = SpringApplication.run(classOf[UserRegistrationConfiguration], args:_*)
}
