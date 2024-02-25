package com.list.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.list.dao.ContactRepository;
import com.list.dao.ContactService;
import com.list.dao.UserRepository;
import com.list.dao.UserService;
import com.list.entities.Contact;
import com.list.entities.User;
import com.list.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService uservice;

	@Autowired
	private UserRepository repo;

	@Autowired
	private ContactRepository crepo;

	@Autowired
	private ContactService cservice;

	// Dashboard

	@GetMapping("/index")
	public String dashboard(Model m, Principal principal) {

		String username = principal.getName();

		User user = repo.getUserByUserName(username);

		m.addAttribute("title", "Dashboard");
		m.addAttribute(user);

		return "normal/dashboard";
	}

	// Open add contacts form

	@GetMapping("/addcontact")
	public String addContactHandler(Model m, Principal principal) {

		User user = repo.getUserByUserName(principal.getName());

		m.addAttribute("title", "Add Contacts");
		m.addAttribute("contact", new Contact());
		m.addAttribute(user);

		return "normal/addcontact";
	}

	// Adding contacts to database

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Principal principal, Model m, HttpSession session) {

		String username = principal.getName();
		User user = this.repo.getUserByUserName(username);

		if (result.hasErrors()) {
			m.addAttribute(user);
			return "normal/addcontact";
		}

		else {
			try {

				/* Adding File to folder */

				if (file.isEmpty()) {

					contact.setImage(contact.getName() + "contact.png");
				}

				else {

					contact.setImage(contact.getName() + file.getOriginalFilename());

					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + contact.getName()
							+ file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				}

				/* Saving contacts and user */

				contact.setUser(user);

				user.getContacts().add(contact); // means list.add()
				this.repo.save(user);

				session.setAttribute("message", new Message("Your contact is saved successfully!!", "success"));

				return "normal/addcontact";

			} catch (Exception e) {
				e.printStackTrace();

				session.setAttribute("message", new Message("Something went wrong!! Try again..", "danger"));

				return "normal/addcontact";
			}
		}

	}

	// Show all contacts

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {

		String username = principal.getName();
		User user = this.uservice.getUser(username);

		Pageable pageable = PageRequest.of(page, 4);
		Page<Contact> contacts = this.crepo.findContactsByUser(user.getId(), pageable);

		m.addAttribute("title", "User Contacts");
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		m.addAttribute(user);

		return "normal/contacts";
	}

	// Show single contact details

	@GetMapping("/show-contacts/contact/{cid}")
	public String getContact(@PathVariable("cid") Integer cid, Model m, Principal principal) {

		Contact contact = this.cservice.getContact(cid);

		String username = principal.getName();
		User user = this.uservice.getUser(username);

		try {

			if (contact.getUser().getId() == user.getId()) {
				m.addAttribute("title", "Contact Details");
				m.addAttribute("contact", contact);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		m.addAttribute(user);

		return "normal/showcontact";
	}

	// Delete single contact

	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid") Integer cid, Principal principal, HttpSession session, Model m) {

		Contact contact = this.cservice.getContact(cid);

		String username = principal.getName();
		User user = this.uservice.getUser(username);

		try {
			if (contact.getUser().getId() == user.getId()) {

				// Deleting the image from folder
				File imageFile = new ClassPathResource("static/img").getFile();
				File file = new File(imageFile, contact.getImage());
				file.delete();

				// Deleting the contact fro db
				this.crepo.delete(contact);

				// In case the upper statement won't work use:
				/*
				 * user.getContacts().remove(contact); this.repo.save(user);
				 */

				session.setAttribute("message", new Message("Contact Deleted Successfully....", "success"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		m.addAttribute(user);

		return "redirect:/user/show-contacts/0";
	}

	// Updating the contact details

	@PostMapping("/update/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model m) {

		Contact contact = this.cservice.getContact(cid);

		m.addAttribute("title", "Update Contact");
		m.addAttribute(contact);

		return "normal/updatecontact";
	}

	// Process updated form

	@PostMapping("/process-updatecontact")
	public String processUpdatedForm(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Principal principal, Model m, HttpSession session) {

		if (result.hasErrors()) {
			return "normal/updatecontact";
		} else {
			try {

				// old contact
				Contact oldContact = this.cservice.getContact(contact.getCid());

				if (!file.isEmpty()) {

					// If image doesn't have any value

					if (oldContact.getImage() == null) {

						// Adding new Image

						contact.setImage(contact.getName() + file.getOriginalFilename());

						File newFile = new ClassPathResource("static/img").getFile();

						Path path = Paths.get(newFile.getAbsolutePath() + File.separator + contact.getName()
								+ file.getOriginalFilename());

						Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					}

					// If image has value

					else {

						// Deleting old Image

						File oldFile = new ClassPathResource("static/img").getFile();
						File file1 = new File(oldFile, oldContact.getImage());
						file1.delete();

						// Adding new Image

						contact.setImage(contact.getName() + file.getOriginalFilename());

						File newFile = new ClassPathResource("static/img").getFile();

						Path path = Paths.get(newFile.getAbsolutePath() + File.separator + contact.getName()
								+ file.getOriginalFilename());

						Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					}

				}

				else {
					if (oldContact.getImage() == null) {
						contact.setImage(contact.getName() + "contact.png");
					} else {
						contact.setImage(oldContact.getImage());
					}
				}

				User user = this.repo.getUserByUserName(principal.getName());

				contact.setUser(user);
				this.cservice.updateContact(contact);

				session.setAttribute("message", new Message("Contact Updated Successfully....", "success"));

			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "redirect:/user/show-contacts/contact/" + contact.getCid();
	}

	// User Profile Handler

	@GetMapping("/profile")
	public String profileHandler(Model m, Principal principal) {
		
		User user = this.repo.getUserByUserName(principal.getName());
		
		m.addAttribute(user);
		m.addAttribute("title", "User Profile");
		
		return "normal/userprofile";
	}

}
