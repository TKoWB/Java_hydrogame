/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.security_service;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
/**
 *
 * @author Admin
 */
public class DecryptionService {
    Argon2PasswordEncoder decoder = new Argon2PasswordEncoder(72, 72, 7, 27272, 7 );
    public boolean Decrypt(String password, String hash){
        return decoder.matches(password, hash);
    }
}
