package com.nivaspari.orders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.server.ResponseStatusException;
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
 @Mock OrderRepository repository;
 @InjectMocks OrderService service;
 @Test void invalidTransitionDoesNotSave() {
  PurchaseOrder order=new PurchaseOrder(new OrderApi.CreateOrder("Customer","Product",2,new BigDecimal("10.00")));
  when(repository.findById(order.id)).thenReturn(Optional.of(order));
  assertThrows(ResponseStatusException.class,()->service.transition(order.id,PurchaseOrder.Status.SHIPPED));
  verify(repository,never()).saveAndFlush(any());
 }
 @Test void confirmationSavesNewState() {
  PurchaseOrder order=new PurchaseOrder(new OrderApi.CreateOrder("Customer","Product",2,new BigDecimal("10.00")));
  when(repository.findById(order.id)).thenReturn(Optional.of(order));
  when(repository.saveAndFlush(order)).thenReturn(order);
  assertEquals(PurchaseOrder.Status.CONFIRMED,service.transition(order.id,PurchaseOrder.Status.CONFIRMED).status());
  verify(repository).saveAndFlush(order);
 }
}
