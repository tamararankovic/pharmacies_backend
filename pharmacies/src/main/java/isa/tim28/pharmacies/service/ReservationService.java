package isa.tim28.pharmacies.service;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isa.tim28.pharmacies.dtos.ReservationDTO;
import isa.tim28.pharmacies.exceptions.UserDoesNotExistException;
import isa.tim28.pharmacies.model.CancelledReservation;

import isa.tim28.pharmacies.model.Reservation;
import isa.tim28.pharmacies.model.ReservationStatus;
import isa.tim28.pharmacies.repository.CancelledReservationRepository;
import isa.tim28.pharmacies.repository.ReservationRepository;
import isa.tim28.pharmacies.service.interfaces.IMedicineService;
import isa.tim28.pharmacies.service.interfaces.IPatientService;
import isa.tim28.pharmacies.service.interfaces.IPharmacyService;
import isa.tim28.pharmacies.service.interfaces.IReservationService;

@Service
public class ReservationService implements IReservationService {
	
	private ReservationRepository reservationRepository;
	private IPatientService patientService;
	private IPharmacyService pharmacyService;
	private CancelledReservationRepository cancelledReservationRepository;
	private IMedicineService medicineService;
	private EmailService emailService;
	
	@Autowired
	public ReservationService(ReservationRepository reservationRepository, IPatientService patientService,IPharmacyService pharmacyService
			,IMedicineService medicineService,CancelledReservationRepository cancelledReservationRepository, EmailService emailService) {
		super();
		this.reservationRepository = reservationRepository;
		this.patientService = patientService;
		this.cancelledReservationRepository = cancelledReservationRepository;
		this.medicineService = medicineService;
		this.pharmacyService = pharmacyService;
		this.emailService = emailService;
	}
	
	@Override
	public List<ReservationDTO> getReservationByPatient(long userId) {
		
		long id = 0;
		try {
			id = patientService.getPatientById(userId).getId();
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<ReservationDTO> result = new ArrayList<ReservationDTO>();
		List<Reservation> reservations = reservationRepository.findByPatient_Id(id);
		List<CancelledReservation> canReservations = cancelledReservationRepository.findByPatient_Id(id);
		
		for(CancelledReservation cr : canReservations) {
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"); 
			String date = cr.getDueDate().format(formatter);
				
			ReservationDTO dto = new ReservationDTO(cr.getId(),cr.getMedicine(),cr.getPharmacy(),date,cr.getStatus().toString(),false);
			result.add(dto);
		}
		
		for (Reservation r : reservations) {
			String res;
			if(r.isReceived()) {
				res = "RECEIVED";
			}else {
				res = "NOT RECEIVED";
			}
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"); 
			String date = r.getDueDate().format(formatter);
			
			boolean cancellable = isCancellable(r);
			ReservationDTO dto = new ReservationDTO(r.getId(),r.getMedicine().getName(),r.getPharmacy().getName(),date,res,cancellable);
			result.add(dto);
		}
		return result;
	}
	
	public boolean isCancellable(Reservation r) {
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime checkDate = r.getDueDate();
		
		if(today.isBefore(checkDate.minus(Period.ofDays(1))) && !r.isReceived()) {
			return true;
		}
		return false;
	}

	@Override
	public List<ReservationDTO> cancelReservation(ReservationDTO dto, long id) {
		
		CancelledReservation cancelled  = new CancelledReservation();
		cancelled.setMedicine(dto.getMedicine());
		cancelled.setPharmacy(dto.getPharmacy());
		try {
			cancelled.setPatient(patientService.getPatientById(id));
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"); 
		LocalDateTime dateTime = LocalDateTime.parse(dto.getDate(), formatter);

		cancelled.setDueDate(dateTime);
		if(dto.getReceived().equals("RECEIVED"))
			cancelled.setReceived(true);
		cancelled.setReceived(false);
		cancelled.setStatus(ReservationStatus.CANCELLED);
		
		cancelledReservationRepository.save(cancelled);		
		reservationRepository.deleteById(dto.getId());;
		
		return getReservationByPatient(id);
	}
	
	@Override
	public Reservation makeReservation(ReservationDTO dto, long id) {
		Reservation res = new Reservation();
		res.setMedicine(medicineService.getByName(dto.getMedicine()));
		try {
			res.setPatient(patientService.getPatientById(id));
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		res.setPharmacy(pharmacyService.getByName(dto.getPharmacy()));
		res.setReceived(false);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"); 
		LocalDateTime dateTime = LocalDateTime.parse(dto.getDate(), formatter);
		
		res.setDueDate(dateTime);
		
		Reservation reservation = reservationRepository.save(res);
		try {
			emailService.sendReservationMadeEmailAsync(dto.getMedicine(), "vukbarisic1996@gmail.com", reservation.getId());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
		
	}
	
	
	
	


}
