package com.ecommerce.customer.infrastructure.adapter.web;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.customer.application.dto.CustomerDto;
import com.ecommerce.customer.application.dto.CreateCustomerRequest;
import com.ecommerce.customer.application.dto.UpdateCustomerRequest;
import com.ecommerce.customer.application.port.in.CustomerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for customer management
 * Provides HTTP endpoints for customer operations
 */
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {
    
    private final CustomerUseCase customerUseCase;
    
    public CustomerController(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }
    
    @PostMapping
    @Operation(summary = "Register a new customer", description = "Creates a new customer account")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Customer created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Customer already exists")
    })
    public ResponseEntity<ApiResponse<CustomerDto>> registerCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        
        CustomerDto customer = customerUseCase.registerCustomer(request);
        
        ApiResponse<CustomerDto> response = ApiResponse.success(
            customer, 
            "Customer registered successfully"
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID", description = "Retrieves customer information by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        CustomerDto customer = customerUseCase.getCustomer(customerId);
        
        ApiResponse<CustomerDto> response = ApiResponse.success(
            customer, 
            "Customer retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieves customer information by email address")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerByEmail(
            @Parameter(description = "Customer email") @PathVariable String email) {
        
        CustomerDto customer = customerUseCase.getCustomerByEmail(email);
        
        ApiResponse<CustomerDto> response = ApiResponse.success(
            customer, 
            "Customer retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer", description = "Updates customer information")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        
        CustomerDto customer = customerUseCase.updateCustomer(customerId, request);
        
        ApiResponse<CustomerDto> response = ApiResponse.success(
            customer, 
            "Customer updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{customerId}/deactivate")
    @Operation(summary = "Deactivate customer", description = "Deactivates a customer account")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer deactivated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<Void>> deactivateCustomer(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        customerUseCase.deactivateCustomer(customerId);
        
        ApiResponse<Void> response = ApiResponse.success(
            null, 
            "Customer deactivated successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{customerId}/activate")
    @Operation(summary = "Activate customer", description = "Activates a customer account")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer activated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<Void>> activateCustomer(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        customerUseCase.activateCustomer(customerId);
        
        ApiResponse<Void> response = ApiResponse.success(
            null, 
            "Customer activated successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{customerId}/login")
    @Operation(summary = "Record customer login", description = "Records a customer login event")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login recorded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<Void>> recordLogin(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        customerUseCase.recordLogin(customerId);
        
        ApiResponse<Void> response = ApiResponse.success(
            null, 
            "Login recorded successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Searches customers by name")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<ApiResponse<List<CustomerDto>>> searchCustomers(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        
        List<CustomerDto> customers = customerUseCase.searchCustomers(searchTerm);
        
        ApiResponse<List<CustomerDto>> response = ApiResponse.success(
            customers, 
            "Search completed successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/city/{city}")
    @Operation(summary = "Get customers by city", description = "Retrieves customers in a specific city")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getCustomersByCity(
            @Parameter(description = "City name") @PathVariable String city) {
        
        List<CustomerDto> customers = customerUseCase.getCustomersByCity(city);
        
        ApiResponse<List<CustomerDto>> response = ApiResponse.success(
            customers, 
            "Customers retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{customerId}/can-place-orders")
    @Operation(summary = "Check if customer can place orders", description = "Validates if customer can place orders")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<Boolean>> canPlaceOrders(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        boolean canPlace = customerUseCase.canPlaceOrders(customerId);
        
        ApiResponse<Boolean> response = ApiResponse.success(
            canPlace, 
            "Order placement validation completed"
        );
        
        return ResponseEntity.ok(response);
    }
}