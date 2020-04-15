package org.opengroup.osdu.legal.middleware;

import com.google.gson.Gson;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class GlobalErrorHandler implements ErrorController {

  private static final Gson gson = new Gson();

  @RequestMapping("/error")
  public ResponseEntity<Object> handleError(final HttpServletRequest request,
                                                  final HttpServletResponse response) {

    Object exception = request.getAttribute("javax.servlet.error.exception");
    Object statusCode = request.getAttribute("javax.servlet.error.status_code");
    Object servletMessage = request.getAttribute("javax.servlet.error.message");
    if(exception instanceof AppException){
      AppException  appException = (AppException)exception;
      String message = appException.getError().getMessage();
      return new ResponseEntity<Object>(appException.getError(), HttpStatus.resolve(appException.getError().getCode()));
    }
    else if (statusCode != null) {
      String message = servletMessage != null ?  servletMessage + ", status: " + statusCode : "status: " + statusCode;
      return new ResponseEntity<Object>(gson.toJson(message), HttpStatus.resolve(Integer.parseInt(statusCode.toString())));
    }
    throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error", "An unknown error has occurred.");
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }

}