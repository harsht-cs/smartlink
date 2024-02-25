package com.list.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.list.dao.UserService;
import com.list.entities.User;
import com.list.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	public BCryptPasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String homeHanlder(Model m) {
		m.addAttribute("title", "Home");
		return "home";
	}

	@GetMapping("/about")
	public String aboutHanlder(Model m) {
		m.addAttribute("title", "About");
		return "about";
	}

	@GetMapping("/signup")
	public String signupHanlder(Model m) {
		m.addAttribute("title", "SignUp");

		m.addAttribute("user", new User());

		return "signup";
	}

	// register handler

	@Autowired
	private UserService regService;

	@PostMapping("/register")
	public String registerHandler(@Valid @ModelAttribute User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {

		try {
			if (!agreement) {
				throw new Exception("You have not agreed to the terms and conditions");
			}
			
			if(result.hasErrors()) {
				m.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setImageUrl("default.png");
			
			this.regService.addUser(user);

			session.setAttribute("message", new Message("Successfully Registered!!", "alert-success"));
			
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("something went wrong!!" + e.getMessage(), "alert-danger"));
			
			return "signup";
		}

	}
	
	
	// login handler
	
	@GetMapping("/signin")
	public String loginHandler(Model m) {
		m.addAttribute("title", "Login");
		return "login";
	}

}
