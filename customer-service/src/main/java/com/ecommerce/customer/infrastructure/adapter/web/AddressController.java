package com.ecommerce.customer.infrastructure.adapter.web;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.customer.application.dto.AddressDto;
import com.ecommerce.customer.application.dto.CreateAddressRequest;
import com.ecommerce.customer.application.dto.UpdateAddressRequest;
import com.ecommerce.customer.application.port.in.AddressManagementUseCase;
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
 * REST controller for address management
 * Provides HTTP endpoints for customer address operations
 */
@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
@Tag(name = "Address Management", description = "APIs for managing customer addresses")
public class AddressController {
    
    private final AddressManagementUseCase addressManagementUseCase;
    
    public AddressController(AddressManagementUseCase addressManagementUseCase) {
        this.addressManagementUseCase = addressManagementUseCase;
    }
    
    @PostMapping
    @Operation(summary = "Add address to customer", description = "Creates a new address for the customer")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Address created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Valid @RequestBody CreateAddressRequest request) {
        
        AddressDto address = addressManagementUseCase.addAddress(customerId, request);
        
        ApiResponse<AddressDto> response = ApiResponse.success(
            address, 
            "Address added successfully"
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get customer addresses", description = "Retrieves all addresses for the customer")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Addresses retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<List<AddressDto>>> getCustomerAddresses(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        List<AddressDto> addresses = addressManagementUseCase.getCustomerAddresses(customerId);
        
        ApiResponse<List<AddressDto>> response = ApiResponse.success(
            addresses, 
            "Addresses retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/primary")
    @Operation(summary = "Get primary address", description = "Retrieves the customer's primary address")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Primary address retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer or primary address not found")
    })
    public ResponseEntity<ApiResponse<AddressDto>> getPrimaryAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        AddressDto address = addressManagementUseCase.getPrimaryAddress(customerId);
        
        ApiResponse<AddressDto> response = ApiResponse.success(
            address, 
            "Primary address retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{addressId}")
    @Operation(summary = "Update address", description = "Updates an existing customer address")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer or address not found")
    })
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Parameter(description = "Address ID") @PathVariable String addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        
        AddressDto address = addressManagementUseCase.updateAddress(customerId, addressId, request);
        
        ApiResponse<AddressDto> response = ApiResponse.success(
            address, 
            "Address updated successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{addressId}")
    @Operation(summary = "Remove address", description = "Removes an address from the customer")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address removed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer or address not found")
    })
    public ResponseEntity<ApiResponse<Void>> removeAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Parameter(description = "Address ID") @PathVariable String addressId) {
        
        addressManagementUseCase.removeAddress(customerId, addressId);
        
        ApiResponse<Void> response = ApiResponse.success(
            null, 
            "Address removed successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{addressId}/set-primary")
    @Operation(summary = "Set primary address", description = "Sets an address as the customer's primary address")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Primary address set successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer or address not found")
    })
    public ResponseEntity<ApiResponse<Void>> setPrimaryAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Parameter(description = "Address ID") @PathVariable String addressId) {
        
        addressManagementUseCase.setPrimaryAddress(customerId, addressId);
        
        ApiResponse<Void> response = ApiResponse.success(
            null, 
            "Primary address set successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/validate-taipei")
    @Operation(summary = "Validate Taipei address", description = "Validates if an address is a valid Taipei address")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address validation completed")
    })
    public ResponseEntity<ApiResponse<Boolean>> validateTaipeiAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Valid @RequestBody CreateAddressRequest request) {
        
        boolean isValid = addressManagementUseCase.validateTaipeiAddress(request);
        
        ApiResponse<Boolean> response = ApiResponse.success(
            isValid, 
            "Taipei address validation completed"
        );
        
        return ResponseEntity.ok(response);
    }
}