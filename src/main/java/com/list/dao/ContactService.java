package com.list.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.list.entities.Contact;

@Service
public class ContactService {
	
	@Autowired
	private ContactRepository crepo;
	
	public Contact getContact(int cid) {
		
		Optional<Contact> coptional = this.crepo.findById(cid);
		Contact contact  = coptional.get();
		
		return contact;
	}
	
	public void updateContact(Contact contact) {
		this.crepo.save(contact);
	}
}
