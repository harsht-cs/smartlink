package com.list.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.list.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	@Query("from Contact as c where c.user.id =:id")
	public Page<Contact> findContactsByUser(@Param("id")int id, Pageable pega);
	
	@Query(value="SELECT * FROM Contact as c WHERE c.cid=:id", nativeQuery=true)
	public Contact getContactById(int id);
}
