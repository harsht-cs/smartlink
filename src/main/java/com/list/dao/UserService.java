package com.list.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.list.entities.User;

@Service
public class UserService {
	@Autowired
	private UserRepository repo;
	
	public void addUser(User user) {
		this.repo.save(user);
	}
	
	public User getUser(String username) {
		return repo.getUserByUserName(username);
	}
}
