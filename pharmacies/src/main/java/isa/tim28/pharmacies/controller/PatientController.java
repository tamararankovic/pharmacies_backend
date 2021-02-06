package isa.tim28.pharmacies.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import isa.tim28.pharmacies.dtos.PasswordChangeDTO;
import isa.tim28.pharmacies.dtos.PatientProfileDTO;
import isa.tim28.pharmacies.dtos.PharmacyAddAdminDTO;
import isa.tim28.pharmacies.exceptions.BadNameException;
import isa.tim28.pharmacies.exceptions.BadSurnameException;
import isa.tim28.pharmacies.exceptions.PasswordIncorrectException;
import isa.tim28.pharmacies.exceptions.PharmacyNotFoundException;
import isa.tim28.pharmacies.exceptions.UserDoesNotExistException;
import isa.tim28.pharmacies.model.Patient;
import isa.tim28.pharmacies.model.Role;
import isa.tim28.pharmacies.model.User;
import isa.tim28.pharmacies.service.PatientService;
import isa.tim28.pharmacies.service.SubscriptionService;

@RestController
@RequestMapping(value = "patient")
public class PatientController {

	private PatientService patientService;
	private SubscriptionService subscriptionService;

	@Autowired
	public PatientController(PatientService patientService, SubscriptionService subscriptionService) {
		super();
		this.patientService = patientService;
		this.subscriptionService = subscriptionService;
	}

	/*
	 * url: GET localhost:8081/patient/get HTTP request for patient profile returns
	 * ResponseEntity object
	 */
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PatientProfileDTO> getPatient(HttpSession session) {

		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No logged in user!");
		}
		if (loggedInUser.getRole() != Role.PATIENT) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patient can view his profile data.");
		}

		User user;
		try {
			user = patientService.getUserPart(loggedInUser.getId());
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with given id doesn't exist!");
		}

		Patient patient = new Patient();
		try {
			patient = patientService.getPatientById(loggedInUser.getId());
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> allergy = patientService.getAllAllergies(patient);

		return new ResponseEntity<PatientProfileDTO>(new PatientProfileDTO(user.getName(), user.getSurname(),
				user.getEmail(), patient.getAddress(), patient.getCity(), patient.getCountry(), patient.getPhone(),
				patient.getPoints(), patient.getCategory().toString(),allergy), HttpStatus.OK);
	}

	@PostMapping(value = "edit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PatientProfileDTO> editPatient(@RequestBody PatientProfileDTO newPatient,
			HttpSession session) throws BadNameException, BadSurnameException {

		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No logged in user!");
		}
		if (loggedInUser.getRole() != Role.PATIENT) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patient can change his profile data.");
		}

		Patient patient;
		try {
			patient = patientService.editPatient(newPatient, loggedInUser.getId());
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient with given id doesn't exist!");
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}
	
	/*
	 url: POST localhost:8081/derm/changePassword
	 HTTP request for changing dermatologist password
	 returns ResponseEntity object
	*/
	@PostMapping(value="changePassword", produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> changePassword(@RequestBody PasswordChangeDTO dto, HttpSession session) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		
		if(loggedInUser == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No logged in user!");
		}
		if(loggedInUser.getRole() != Role.PATIENT) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patient can change his password.");
		}
		if(!dto.isValid()) return new ResponseEntity<>("Passwords are not valid.", HttpStatus.BAD_REQUEST);
		
		try {
			if(patientService.checkOldPassword(loggedInUser.getId(), dto.getOldPassword()));
		} catch (UserDoesNotExistException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (PasswordIncorrectException e1) {
			return new ResponseEntity<>(e1.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		try {
			patientService.changePassword(loggedInUser.getId(), dto.getNewPassword());
		} catch (UserDoesNotExistException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping(value = "/medicine", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ArrayList<String>> getMedicine(HttpSession session) {

		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No logged in user!");
		}
		if (loggedInUser.getRole() != Role.PATIENT) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patient can view his profile data.");
		}

		User user;
		try {
			user = patientService.getUserPart(loggedInUser.getId());
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with given id doesn't exist!");
		}

		Patient patient = new Patient();
		try {
			patient = patientService.getPatientById(loggedInUser.getId());
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> medicine = patientService.getAllMedicine(patient);

		return new ResponseEntity<ArrayList<String>>(medicine, HttpStatus.OK);
	}
	
	@GetMapping(value = "benefits/{id}")
	public ResponseEntity<String> subscribeToPharmacy(@PathVariable long id, HttpSession session) {
		
		User user = (User) session.getAttribute("loggedInUser");
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not logged in!");
		}
		if (user.getRole() != Role.PATIENT) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		try {
			if(subscriptionService.alreadySubscribed(user.getId(), id)==false) {
				return new ResponseEntity<>("You have already subscribed to this pharmacy's actions and benefits", HttpStatus.BAD_REQUEST);
			}
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		} catch (PharmacyNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found");
		}
		
		try {
			subscriptionService.subscribe(user.getId(), id);
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		} catch (PharmacyNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found");
		}
		return new ResponseEntity<>("", HttpStatus.OK);
	}
	
	@GetMapping(value = "/getSubscribedPharmacies", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PharmacyAddAdminDTO>> getAllSubscribedPharmacies(HttpSession session){
		
		User user = (User) session.getAttribute("loggedInUser");
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not logged in!");
		}
		if (user.getRole() != Role.PATIENT) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		List<PharmacyAddAdminDTO> pharmacies;
		try {
			pharmacies = subscriptionService.getAllSubscribedPharmacies(user.getId());
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		
		return new ResponseEntity<>(pharmacies, HttpStatus.OK);
	
	}
	@GetMapping(value = "cancel/{id}")
	public ResponseEntity<String> cancelSubscription(@PathVariable long id, HttpSession session) {
		
		User user = (User) session.getAttribute("loggedInUser");
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not logged in!");
		}
		if (user.getRole() != Role.PATIENT) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		try {
			subscriptionService.cancelSubscription(user.getId(), id);
		} catch (UserDoesNotExistException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		} catch (PharmacyNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found");
		}
		
		return new ResponseEntity<>("", HttpStatus.OK);
	}
	
}
