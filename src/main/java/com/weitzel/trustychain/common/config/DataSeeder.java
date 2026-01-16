package com.weitzel.trustychain.common.config;

import com.weitzel.trustychain.actor.Actor;
import com.weitzel.trustychain.actor.ActorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(ActorRepository actorRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (actorRepository.findByUsername("user").isEmpty()) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);
                KeyPair kp = kpg.generateKeyPair();
                String publicKey = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

                Actor user = new Actor();
                user.setName("Standard User");
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRole("USER");
                user.setPublicKey(publicKey);
                
                actorRepository.save(user);
                System.out.println(">>> Seeded default user: user / password");
            }
        };
    }
}
