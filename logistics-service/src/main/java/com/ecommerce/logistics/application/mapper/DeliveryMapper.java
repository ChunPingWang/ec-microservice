package com.ecommerce.logistics.application.mapper;

import com.ecommerce.logistics.application.dto.AddressDto;
import com.ecommerce.logistics.application.dto.DeliveryDto;
import com.ecommerce.logistics.domain.model.Address;
import com.ecommerce.logistics.domain.model.DeliveryRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 配送資料轉換器
 * 遵循 SRP 原則 - 只負責配送相關的資料轉換
 */
@Component
public class DeliveryMapper {
    
    /**
     * 將領域實體轉換為DTO
     */
    public DeliveryDto toDto(DeliveryRequest deliveryRequest) {
        if (deliveryRequest == null) {
            return null;
        }
        
        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(deliveryRequest.getDeliveryId());
        dto.setOrderId(deliveryRequest.getOrderId());
        dto.setCustomerId(deliveryRequest.getCustomerId());
        dto.setDeliveryAddress(toAddressDto(deliveryRequest.getDeliveryAddress()));
        dto.setDeliveryType(deliveryRequest.getDeliveryType());
        dto.setStatus(deliveryRequest.getStatus());
        dto.setDeliveryFee(deliveryRequest.getDeliveryFee());
        dto.setEstimatedDeliveryDate(deliveryRequest.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(deliveryRequest.getActualDeliveryDate());
        dto.setTrackingNumber(deliveryRequest.getTrackingNumber());
        dto.setDeliveryNotes(deliveryRequest.getDeliveryNotes());
        dto.setFailureReason(deliveryRequest.getFailureReason());
        dto.setCreatedAt(deliveryRequest.getCreatedAt());
        dto.setUpdatedAt(deliveryRequest.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * 將DTO列表轉換為領域實體列表
     */
    public List<DeliveryDto> toDtoList(List<DeliveryRequest> deliveryRequests) {
        if (deliveryRequests == null) {
            return null;
        }
        
        return deliveryRequests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 將地址領域物件轉換為DTO
     */
    public AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        
        return new AddressDto(
            address.getCity(),
            address.getDistrict(),
            address.getStreet(),
            address.getPostalCode(),
            address.getRecipientName(),
            address.getRecipientPhone()
        );
    }
    
    /**
     * 將地址DTO轉換為領域物件
     */
    public Address toAddressDomain(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }
        
        return new Address(
            addressDto.getCity(),
            addressDto.getDistrict(),
            addressDto.getStreet(),
            addressDto.getPostalCode(),
            addressDto.getRecipientName(),
            addressDto.getRecipientPhone()
        );
    }
}