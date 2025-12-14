package com.voting.votingproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class VotingprojectApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

        // Export to Spring environment
        System.setProperty("MONGO_URL", dotenv.get("MONGO_URL"));
        System.setProperty("PORT", dotenv.get("PORT"));
        System.setProperty("DATABASE", dotenv.get("DATABASE"));
        System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
        System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
        System.setProperty("FRONTEND_URL", dotenv.get("FRONTEND_URL"));
        System.setProperty("PYTHON_BACKEND_URL", dotenv.get("PYTHON_BACKEND_URL"));

        
		SpringApplication.run(VotingprojectApplication.class, args);
	}

}
