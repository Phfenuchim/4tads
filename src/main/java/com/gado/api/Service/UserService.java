package com.gado.api.service;

import com.gado.api.domain.user.Usr;
import com.gado.api.repositories.UsrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UsrRepository usrRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUser(Usr usr) {
        usr.setPassword(passwordEncoder.encode(usr.getPassword()));
        usrRepository.save(usr);
    }
}
