package com.livestock.modules.product.middlewares;

import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.exceptions.ProductNotFoundException;
import com.livestock.modules.user.exceptions.UserInputException;
import com.livestock.modules.user.exceptions.UserNotAuthenticatedException;
import com.livestock.modules.user.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@ControllerAdvice
public class ProductExceptionMiddleware {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleProductNotFoundException(ProductNotFoundException ex) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("errorCode", "404");
        mav.addObject("timestamp", LocalDateTime.now());


        mav.setViewName("error");
        return mav;
    }

}
