package isa.tim28.pharmacies.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isa.tim28.pharmacies.dtos.DermatologistAppointmentDTO;
import isa.tim28.pharmacies.dtos.DermatologistReportDTO;
import isa.tim28.pharmacies.dtos.MedicineDTO;
import isa.tim28.pharmacies.dtos.MedicineQuantityCheckDTO;
import isa.tim28.pharmacies.dtos.TherapyDTO;
import isa.tim28.pharmacies.exceptions.UserDoesNotExistException;
import isa.tim28.pharmacies.model.DermatologistAppointment;
import isa.tim28.pharmacies.model.DermatologistReport;
import isa.tim28.pharmacies.model.Medicine;
import isa.tim28.pharmacies.model.MedicineMissingNotification;
import isa.tim28.pharmacies.model.MedicineQuantity;
import isa.tim28.pharmacies.model.Patient;
import isa.tim28.pharmacies.model.Pharmacy;
import isa.tim28.pharmacies.model.Reservation;
import isa.tim28.pharmacies.model.Therapy;
import isa.tim28.pharmacies.repository.DermatologistAppointmentRepository;
import isa.tim28.pharmacies.repository.DermatologistReportRepository;
import isa.tim28.pharmacies.repository.MedicineMissingNotificationRepository;
import isa.tim28.pharmacies.repository.MedicineQuantityRepository;
import isa.tim28.pharmacies.repository.MedicineRepository;
import isa.tim28.pharmacies.repository.PatientRepository;
import isa.tim28.pharmacies.repository.ReservationRepository;
import isa.tim28.pharmacies.service.interfaces.IDermatologistAppointmentService;

@Service
public class DermatologistAppointmentService implements IDermatologistAppointmentService {

	private DermatologistAppointmentRepository appointmentRepository;
	private PatientRepository patientRepository;
	private MedicineRepository medicineRepository;
	private DermatologistReportRepository dermatologistReportRepository;
	private MedicineQuantityRepository medicineQuantityRepository;
	private ReservationRepository reservationRepository;
	private MedicineMissingNotificationRepository medicineMissingNotificationRepository;
	
	@Autowired
	public DermatologistAppointmentService(DermatologistAppointmentRepository appointmentRepository, MedicineRepository medicineRepository, 
			DermatologistReportRepository dermatologistReportRepository, PatientRepository patientRepository, MedicineQuantityRepository medicineQuantityRepository,
			ReservationRepository reservationRepository, MedicineMissingNotificationRepository medicineMissingNotificationRepository) {
		super();
		this.appointmentRepository = appointmentRepository;
		this.medicineRepository = medicineRepository;
		this.dermatologistReportRepository = dermatologistReportRepository;
		this.patientRepository = patientRepository;
		this.medicineQuantityRepository = medicineQuantityRepository;
		this.reservationRepository = reservationRepository;
		this.medicineMissingNotificationRepository = medicineMissingNotificationRepository;
	}

	@Override
	public Set<DermatologistAppointment> findAllAvailableByPharmacyId(long pharmacyId) {
		return appointmentRepository.findAll().stream()
				.filter(a -> a.getPharmacy().getId() == pharmacyId)
				.filter(a -> a.getStartDateTime().isAfter(LocalDateTime.now()))
				.filter(a -> !a.isScheduled()).collect(Collectors.toSet());
	}

	@Override
	public DermatologistAppointmentDTO getAppointmentDTOById(long appointmentId) {
		DermatologistAppointment appointment = appointmentRepository.findById(appointmentId).get();
		if(appointment == null) return new DermatologistAppointmentDTO();
		else return new DermatologistAppointmentDTO(appointment.getId(), appointment.getPatient().getId(), appointment.getPatient().getUser().getFullName());
	}

	@Override
	public List<MedicineDTO> getMedicineList() {
		List<MedicineDTO> dtos = new ArrayList<MedicineDTO>();
		for (Medicine m : medicineRepository.findAll()) {
			dtos.add(new MedicineDTO(m.getId(), m.getName(), m.getManufacturer()));
		}
		return dtos;
	}

	@Override
	public String fillReport(DermatologistReportDTO dto) {
		DermatologistReport report = new DermatologistReport();
		DermatologistAppointment app = appointmentRepository.findById(dto.getAppointmentId()).get();
		report.setAppointment(app);
		report.setDiagnosis(dto.getDiagnosis());
		Set<Therapy> therapies = new HashSet<Therapy>();
		String reservationCodes = "";
		
		for(TherapyDTO t : dto.getTherapies()) {
			Therapy therapy = new Therapy();
			Medicine medicine = medicineRepository.findById(t.getMedicineId()).get();
			therapy.setDurationInDays(t.getDurationInDays());
			therapy.setMedicine(medicine);
			therapies.add(therapy);
			reservationCodes = reservationCodes + medicine.getCode() + "; ";
			updateMedicineQuantity(medicine.getId(), app.getId());
			/*
			Reservation reservation = new Reservation();
			reservation.setMedicine(medicine);
			reservation.setPatient(app.getPatient());
			reservation.setPharmacy(app.getPharmacy());
			reservation.setReceived(false);
			reservation.setAppointment(app.getId());
			reservation.setDueDate(LocalDate.now().plusDays(1));
			reservationRepository.save(reservation);
			*/
		}
		report.setTherapies(therapies);
		dermatologistReportRepository.save(report);
		
		/*
		Set<Reservation> reservations = reservationRepository.findAllByAppointment(app.getId());
		String reservationCodes = "";
		for(Reservation res : reservations) {
			reservationCodes = reservationCodes + res.getId() + "; ";
		}*/
		return reservationCodes;
	}

	@Override
	public boolean checkAllergies(long patientId, long medicineId) throws UserDoesNotExistException {
		Patient patient = patientRepository.findById(patientId).get();
		if(patient == null) throw new UserDoesNotExistException("Patient with given id doesn't exist.");
		Set<Medicine> allergies = patient.getAllergies();
		for (Medicine allergy : allergies) 
			if (allergy.getId() == medicineId) return false;
		return true;
	}

	@Override
	public DermatologistAppointment getAppointmentById(long appointmentId) {
		return appointmentRepository.findById(appointmentId).get();
	}

	@Override
	public String getMedicineCodeById(long medicineId) {
		return medicineRepository.findById(medicineId).get().getCode();
	}

	@Override
	public MedicineQuantityCheckDTO checkIfMedicineIsAvailable(long medicineId, long appointmentId) {
		Pharmacy pharmacy = appointmentRepository.findById(appointmentId).get().getPharmacy();
		Set<MedicineQuantity> medicines = pharmacy.getMedicines();
		for (MedicineQuantity mq : medicines) {
			if (mq.getMedicine().getId() == medicineId) {
				MedicineQuantityCheckDTO dto = new MedicineQuantityCheckDTO(mq.getQuantity());
				if(!dto.isAvailable()) {
					MedicineMissingNotification notification = new MedicineMissingNotification();
					notification.setMedicine(mq.getMedicine());
					notification.setPharmacy(pharmacy);
					medicineMissingNotificationRepository.save(notification);
				}
				return dto;
			}
		}
		return new MedicineQuantityCheckDTO(0);
	}
	
	private void updateMedicineQuantity(long medicineId, long appointmentId) {
		Set<MedicineQuantity> medicines = appointmentRepository.findById(appointmentId).get().getPharmacy().getMedicines();
		for (MedicineQuantity mq : medicines) {
			if (mq.getMedicine().getId() == medicineId) {
				mq.setQuantity(mq.getQuantity() - 1);
				medicineQuantityRepository.save(mq);
				return;
			}
		}
	}

	@Override
	public List<MedicineDTO> compatibleMedicine(long medicineId) {
		Medicine medicine = medicineRepository.findById(medicineId).get();
		List<MedicineDTO> compatible = new ArrayList<MedicineDTO>();
		for(String code : medicine.getCompatibleMedicineCodes()) {
			Medicine m = medicineRepository.findOneByCode(code);
			if(m != null) compatible.add(new MedicineDTO(m.getId(), m.getName(), m.getManufacturer()));
		}
		return compatible;
	}
	
}
