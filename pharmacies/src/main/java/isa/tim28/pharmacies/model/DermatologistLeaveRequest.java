package isa.tim28.pharmacies.model;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DermatologistLeaveRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Dermatologist dermatologist;
	
	@Column(name = "startDate", nullable = false)
	private LocalDate startDate;
	
	@Column(name = "endDate", nullable = false)
	private LocalDate endDate;
	
	@Column(name = "type", nullable = false)
	private LeaveType type;
	
	@Column(name = "confirmed", nullable = false)
	private boolean confirmed;
	
	@Column(name = "reasonDenied", nullable = false)
	private String reasonDenied;

	public DermatologistLeaveRequest() {
		super();
	}
	
	public DermatologistLeaveRequest(long id, Dermatologist dermatologist, LocalDate startDate, LocalDate endDate,
			LeaveType type, boolean confirmed, String reasonDenied) {
		super();
		this.id = id;
		this.dermatologist = dermatologist;
		this.startDate = startDate;
		this.endDate = endDate;
		this.type = type;
		this.confirmed = confirmed;
		this.reasonDenied = reasonDenied;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Dermatologist getDermatologist() {
		return dermatologist;
	}

	public void setDermatologist(Dermatologist dermatologist) {
		this.dermatologist = dermatologist;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public LeaveType getType() {
		return type;
	}

	public void setType(LeaveType type) {
		this.type = type;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getReasonDenied() {
		return reasonDenied;
	}

	public void setReasonDenied(String reasonDenied) {
		this.reasonDenied = reasonDenied;
	}
}
