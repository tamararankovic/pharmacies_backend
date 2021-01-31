package isa.tim28.pharmacies.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import isa.tim28.pharmacies.dtos.PharmacistProfileDTO;
import isa.tim28.pharmacies.exceptions.BadNameException;
import isa.tim28.pharmacies.exceptions.BadNewEmailException;
import isa.tim28.pharmacies.exceptions.BadSurnameException;
import isa.tim28.pharmacies.exceptions.UserDoesNotExistException;
import isa.tim28.pharmacies.model.Role;
import isa.tim28.pharmacies.model.User;
import isa.tim28.pharmacies.service.PharmacistService;

@RestController
@RequestMapping(value = "pharm")
public class PharmacistController {

	private PharmacistService pharmacistService;
	
	@Autowired
	public PharmacistController(PharmacistService pharmacistService) {
		super();
		this.pharmacistService = pharmacistService;
	}
	
	/*
	 url: GET localhost:8081/pharm/get
	 HTTP request for pharmacist profile
	 returns ResponseEntity object
	*/
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PharmacistProfileDTO> getPharmacist(HttpSession session) {
		
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if(loggedInUser == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No logged in user!");
		}
		if(loggedInUser.getRole() != Role.PHARMACIST) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only pharmacist can view his profile data.");
		}
		
		User pharmacist;
		try {
			pharmacist = pharmacistService.getUserPart(loggedInUser.getId());
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacist with given id doesn't exist!");
		}
		
		return new ResponseEntity<>(new PharmacistProfileDTO(pharmacist.getName(), pharmacist.getSurname(), pharmacist.getEmail()), HttpStatus.OK);
	}
	
	
	/*
	 url: GET localhost:8081/pharm/update
	 HTTP request for changing pharmacist personal info
	 returns ResponseEntity object
	*/
	@PostMapping(value="update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PharmacistProfileDTO> updatePharmacist(@RequestBody PharmacistProfileDTO newPharmacist, HttpSession session) {
		
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if(loggedInUser == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No logged in user!");
		}
		if(loggedInUser.getRole() != Role.PHARMACIST) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only pharmacist can change his profile data.");
		}
		
		User pharmacist;
		try {
			pharmacist = pharmacistService.updatePharmacist(newPharmacist, loggedInUser.getId());
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacist with given id doesn't exist!");
		} catch (BadNameException e1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad name!");
		} catch (BadSurnameException e2) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad surname!");
		} catch (BadNewEmailException e3) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad email!");
		}
		return new ResponseEntity<>(new PharmacistProfileDTO(pharmacist.getName(), pharmacist.getSurname(), pharmacist.getEmail()), HttpStatus.OK);
	}
}
