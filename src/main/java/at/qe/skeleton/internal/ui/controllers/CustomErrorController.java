package at.qe.skeleton.internal.ui.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

    if (status != null) {
      int statusCode = Integer.parseInt(status.toString());

      if (statusCode == 403) {
        return "error/access_denied.xhtml";
      } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        return "error/server_error.xhtml";
      }
    }
    return "error/error.xhtml";
  }
}
