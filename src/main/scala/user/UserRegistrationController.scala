package user

import javax.validation.Valid
import javax.validation.constraints.{Size, NotNull}

import org.hibernate.validator.constraints.Email
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{RequestParam, ModelAttribute, RequestMethod, RequestMapping}
import org.springframework.web.client.{HttpClientErrorException, RestTemplate}
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import scala.beans.BeanProperty

@Controller
class UserRegistrationController @Autowired()(restTemplate: RestTemplate){
  @Value("${user_registration_url}")
  var userRegistrationUrl: String = _

  @RequestMapping(value = Array("/register.html"), method = Array(RequestMethod.GET))
  def beginRegister = "register"

  @RequestMapping(value = Array("/register.html"), method = Array(RequestMethod.POST))
  def register(@Valid() @ModelAttribute("registration") request: RegistrationRequest, bindingResult: BindingResult, redirectAttributes: RedirectAttributes): String = {
    if (bindingResult.getErrorCount != 0)
      return "register"

    val response = try
      restTemplate.postForEntity(userRegistrationUrl, RegistrationBackendRequest(request.getEmailAddress, request.getPassword), classOf[RegistrationBackendResponse])
    catch{
      case e: HttpClientErrorException
        if e.getStatusCode == HttpStatus.CONFLICT => bindingResult.rejectValue("emailAddress", "duplicate.email.address", "Email address already registered")
        return "register"
    }

    response.getStatusCode match {
      case HttpStatus.OK =>
        redirectAttributes.addAttribute("emailAddress", request.getEmailAddress)
        "redirect:registrationconfirmation.html"
    }
  }

  @RequestMapping(value = Array("/registrationconfirmation.html"), method = Array(RequestMethod.GET))
  def registrationConfirmation(@RequestParam emailAddress: String, model: Model) = {
    model.addAttribute("emailAddress", emailAddress)
    "registrationconfirmation"
  }
}

class RegistrationRequest {
  @BeanProperty
  @Email
  @NotNull
  var emailAddress: String = _

  @BeanProperty
  @NotNull
  @Size(min = 8, max = 30)
  var password: String = _
}

case class RegistrationBackendRequest(emailAddress: String, password: String)

case class RegistrationBackendResponse(id: String, emailAddress: String)
