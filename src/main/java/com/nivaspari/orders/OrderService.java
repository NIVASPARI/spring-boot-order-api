package com.nivaspari.orders;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service @Transactional
public class OrderService {
 private final OrderRepository repository;
 public OrderService(OrderRepository repository) { this.repository=repository; }
 public OrderApi.OrderView create(OrderApi.CreateOrder request) { return OrderApi.OrderView.from(repository.save(new PurchaseOrder(request))); }
 @Transactional(readOnly=true)
 public OrderApi.OrderView get(UUID id) { return OrderApi.OrderView.from(find(id)); }
 @Transactional(readOnly=true)
 public OrderApi.OrderPage list(int page,int size) {
  Page<PurchaseOrder> results=repository.findAll(PageRequest.of(page,size,Sort.by(Sort.Order.desc("createdAt"),Sort.Order.desc("id"))));
  return new OrderApi.OrderPage(results.map(OrderApi.OrderView::from).getContent(),page,size,results.getTotalElements(),results.getTotalPages());
 }
 public OrderApi.OrderView transition(UUID id,PurchaseOrder.Status target) {
  PurchaseOrder order=find(id);
  boolean allowed=switch(order.status) {
   case PENDING -> target==PurchaseOrder.Status.CONFIRMED || target==PurchaseOrder.Status.CANCELLED;
   case CONFIRMED -> target==PurchaseOrder.Status.SHIPPED || target==PurchaseOrder.Status.CANCELLED;
   case SHIPPED, CANCELLED -> false;
  };
  if(!allowed) throw new ResponseStatusException(HttpStatus.CONFLICT,"Cannot transition from "+order.status+" to "+target);
  order.status=target;
  return OrderApi.OrderView.from(repository.saveAndFlush(order));
 }
 private PurchaseOrder find(UUID id) { return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found")); }
}
