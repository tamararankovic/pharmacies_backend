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
	
	@Column(name = "state", nullable = false)
	private LeaveRequestState state = LeaveRequestState.WAITING_REVIEW;

	public DermatologistLeaveRequest() {
		super();
	}
	
	public DermatologistLeaveRequest(long id, Dermatologist dermatologist, LocalDate startDate, LocalDate endDate,
			LeaveType type, LeaveRequestState state) {
		super();
		this.id = id;
		this.dermatologist = dermatologist;
		this.startDate = startDate;
		this.endDate = endDate;
		this.type = type;
		this.state = state;
	}

	public DermatologistLeaveRequest(long id, Dermatologist dermatologist, LocalDate startDate, LocalDate endDate,
			LeaveType type) {
		super();
		this.id = id;
		this.dermatologist = dermatologist;
		this.startDate = startDate;
		this.endDate = endDate;
		this.type = type;
		this.state = LeaveRequestState.WAITING_REVIEW;
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

	public LeaveRequestState getState() {
		return state;
	}

	public void setState(LeaveRequestState state) {
		this.state = state;
	}
	
	public boolean isConfirmed() {
		return state == LeaveRequestState.ACCEPTED;
	}
	
	public boolean isWaitingOnReview() {
		return state == LeaveRequestState.WAITING_REVIEW && startDate.isAfter(LocalDate.now());
	}
	
	public void decline() {
		state = LeaveRequestState.DECLINED;
	}
	
	public void accept() {
		state = LeaveRequestState.ACCEPTED;
	}
}
