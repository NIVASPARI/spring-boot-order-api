package com.nivaspari.orders;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="purchase_orders")
public class PurchaseOrder {
 @Id UUID id;
 @Column(nullable=false,length=120) String customer;
 @Column(nullable=false,length=80) String product;
 @Column(nullable=false) int quantity;
 @Column(nullable=false,precision=12,scale=2) BigDecimal unitPrice;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) Status status;
 @Column(nullable=false) Instant createdAt;
 @Version long version;
 protected PurchaseOrder() {}
 PurchaseOrder(OrderApi.CreateOrder request) {
  id=UUID.randomUUID(); customer=request.customer().trim(); product=request.product().trim();
  quantity=request.quantity(); unitPrice=request.unitPrice(); status=Status.PENDING; createdAt=Instant.now();
 }
 public enum Status { PENDING, CONFIRMED, SHIPPED, CANCELLED }
}
