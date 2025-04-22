package com.example.finalproject.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    public static boolean isValidEmail(String email){
        String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,24}$";
        if (email == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber){
        String PHONE_REGEX = "^\\+?[0-9]{1,3}?[-.\\s]?\\(?[0-9]{3}\\)?[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{2}[-.\\s]?[0-9]{2}$";
        if (phoneNumber == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public static boolean isValidWebsite(String website) {
        String WEBSITE_REGEX = "^(https?://)?([\\p{L}0-9-@]+\\.)+[\\p{L}]{2,24}(/.*)?/?$";
        if (website == null || website.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(WEBSITE_REGEX);
        Matcher matcher = pattern.matcher(website);
        return matcher.matches();
    }

    public static boolean isValidSlug(String slug){

        if(!Character.isLetter(slug.charAt(0)))return false;
        for(char c: slug.toCharArray())if(!Character.isLetterOrDigit(c) && c!=' ' && c!='-' && c!='_')return false;
        return true;
    }

}
