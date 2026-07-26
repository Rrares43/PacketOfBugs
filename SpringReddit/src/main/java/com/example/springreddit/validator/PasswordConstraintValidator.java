package com.example.springreddit.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context){
        if(password == null){
            return false;
        }

        boolean correctLength = password.length() >= 8 && password.length() < 40;
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        boolean hasWhitespace = false;

        for(int i = 0; i < password.length(); i++){
            char ch = password.charAt(i);
            if(Character.isDigit(ch)){
                hasNumber = true;
            }
            else if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            }
            else if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            }
            else if (Character.isWhitespace(ch)) {
                hasWhitespace = true;
            }
            else if (!Character.isLetterOrDigit(ch)) {
                hasSpecial = true;
            }
        }

        context.disableDefaultConstraintViolation();

        if(hasWhitespace){
            context.buildConstraintViolationWithTemplate("Password cannot contain whitespace")
                    .addConstraintViolation();
            return false;
        }
        if (!correctLength) {
            context.buildConstraintViolationWithTemplate("Password must be between 8 and 40 characters")
                    .addConstraintViolation();
            return false;
        }

        if (!(hasUppercase && hasLowercase && hasNumber && hasSpecial)) {
            context.buildConstraintViolationWithTemplate("Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
