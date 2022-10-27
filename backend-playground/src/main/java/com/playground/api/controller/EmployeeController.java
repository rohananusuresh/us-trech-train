package com.playground.api.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playground.api.dto.EmployeeDto;
import com.playground.api.dto.ResponseDto;
import com.playground.api.model.Employee;
import com.playground.api.model.Manager;
import com.playground.api.model.User;
import com.playground.api.repositories.EmployeeRepository;
import com.playground.api.repositories.ManagerRepository;
import com.playground.api.repositories.UserRepository;

@RestController
@RequestMapping("/api/employee")
 public class EmployeeController {
	
	@Autowired
	ManagerRepository managerRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder encoder; 
	
	@Autowired
	private ResponseDto responseDto;
	
	@PostMapping("/add")
	public ResponseEntity<ResponseDto> addEmployee(@RequestBody EmployeeDto dto) {
		 
		/* managerEmail is valid */
		Manager manager = managerRepository.getByEmail(dto.getManagerEmail());
		responseDto.setMsg("Manager Email Invalid");
		if(manager == null)
				return ResponseEntity
						.status(HttpStatus.BAD_REQUEST)
						.body(responseDto);
		
		/* Employee email is not already present */
		Employee employee = employeeRepository.getByEmail(dto.getEmail());
		responseDto.setMsg("Employee Already Exists");
		if(! (employee == null) )
				return ResponseEntity
						.status(HttpStatus.BAD_REQUEST)
						.body(responseDto);
		
		/* Password is encrypted */
		 String encryptedPassword =encoder.encode(dto.getPassword());
		
		/* Save record in employee and user table */
		User user = new User();
		user.setUsername(dto.getEmail());
		user.setPassword(encryptedPassword);
		user.setRole("EMPLOYEE");
		user.setEnabled(false);
		user = userRepository.save(user);
		
		employee = new Employee();
		employee.setUser(user);
		employee.setName(dto.getName());
		employee.setJobTitle(dto.getJobTitle());
		employee.setCreatedOn(LocalDate.now());
		employee.setManager(manager);
		employeeRepository.save(employee);
		
		responseDto.setMsg("Employee Record Added");
		
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(responseDto);
	}
	
	@GetMapping("/all")
	public List<EmployeeDto> getAllEmployeeByManagerEmail(Principal principal) {
		String managerEmail = principal.getName();
		List<Employee> list = employeeRepository.getAllEmployeeByManager(managerEmail);
		List<EmployeeDto> listDto = EmployeeDto.convertToDto(list);
		return listDto; 
	}
	
	@GetMapping("/access")
	public List<EmployeeDto> getEmployeeHavingAccess(Principal principal) {
		String username=principal.getName();
		List<Employee>  list = employeeRepository.getEmployeeHavingAccess(username,false);
		List<EmployeeDto> listDto =  EmployeeDto.convertToDto(list);
		return listDto;
	}
}
