package com.livestock.modules.user.middlewares;

import com.livestock.modules.user.exceptions.UserInputException;
import com.livestock.modules.user.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@ControllerAdvice
public class UserExceptionMiddleware {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleUserNotFoundException(UserNotFoundException ex) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("errorCode", "404");
        mav.addObject("timestamp", LocalDateTime.now());


        mav.setViewName("error/user-not-found");
        return mav;
    }

    @ExceptionHandler(UserInputException.class)
    public String handleUserInputException(UserInputException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorCode", "404");
        redirectAttributes.addFlashAttribute("timestamp", LocalDateTime.now());

        return "redirect:/admin/create-user";
    }

}
