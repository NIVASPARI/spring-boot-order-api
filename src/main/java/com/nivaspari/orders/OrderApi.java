package com.nivaspari.orders;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders")
public class OrderApi {
 private final OrderService service;
 public OrderApi(OrderService service) { this.service=service; }
 public record CreateOrder(@NotBlank @Size(max=120) String customer, @NotBlank @Size(max=80) String product,
  @Min(1) @Max(1000) int quantity, @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal unitPrice) {}
 public record ChangeStatus(@NotNull PurchaseOrder.Status status) {}
 public record OrderView(UUID id,String customer,String product,int quantity,BigDecimal unitPrice,BigDecimal total,
  PurchaseOrder.Status status,Instant createdAt,long version) {
  static OrderView from(PurchaseOrder o) { return new OrderView(o.id,o.customer,o.product,o.quantity,o.unitPrice,o.unitPrice.multiply(BigDecimal.valueOf(o.quantity)),o.status,o.createdAt,o.version); }
 }
 public record OrderPage(List<OrderView> content,int page,int size,long totalElements,int totalPages) {}
 @PostMapping public ResponseEntity<OrderView> create(@Valid @RequestBody CreateOrder request) {
  OrderView order=service.create(request); return ResponseEntity.created(URI.create("/api/orders/"+order.id())).body(order);
 }
 @GetMapping("/{id}") public OrderView get(@PathVariable UUID id) { return service.get(id); }
 @GetMapping public OrderPage list(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return service.list(page,size); }
 @PatchMapping("/{id}/status") public OrderView status(@PathVariable UUID id,@Valid @RequestBody ChangeStatus request) { return service.transition(id,request.status()); }
}
