package com.example.chatfirebase;


import android.util.Patterns;

//Estou validando se o email está no formato correto
public class Validation {


    public boolean isEmptyFields(String name, String email, String password) {
        if (name == null || name.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isEmailValid(String email) {
        boolean validation = (Patterns.EMAIL_ADDRESS.matcher(email).matches());
        return validation;
    }

    public boolean isPswValid(String password) {

        if (password.length() >= 6) {
            return true;
        }
        return false;
    }



}
