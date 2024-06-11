package com.krishna.config;

import com.krishna.model.Customer;
import com.krishna.repository.CustomerRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EasyBankUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        System.out.println("Inside authenticate of custom authentication provider==========================");

        List<Customer> customers = customerRepository.findByEmail(username);

        if(customers == null || customers.isEmpty()) throw new BadCredentialsException("Username not found");
        else {
            try{
                if(passwordEncoder.matches(password, customers.get(0).getPwd())){
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(customers.get(0).getRole()));
                    return new UsernamePasswordAuthenticationToken(username, password, authorities);
                } else {
                    throw new BadCredentialsException("Invalid password");
                }
            } catch (Exception e){
                throw new BadCredentialsException("Something went wrong!");
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
