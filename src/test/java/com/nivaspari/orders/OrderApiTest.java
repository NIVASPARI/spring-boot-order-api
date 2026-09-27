package com.nivaspari.orders;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class OrderApiTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired OrderRepository repository;
 @BeforeEach void clean() { repository.deleteAll(); }
 String create() throws Exception {
  String body=mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
   {"customer":"Sample Customer","product":"Keyboard","quantity":3,"unitPrice":19.99}
   """)).andExpect(status().isCreated()).andExpect(header().exists("Location"))
   .andExpect(jsonPath("$.total").value(59.97)).andExpect(jsonPath("$.status").value("PENDING"))
   .andReturn().getResponse().getContentAsString();
  return json.readTree(body).get("id").asText();
 }
 @Test void createsReadsAndListsOrder() throws Exception {
  String id=create();
  mvc.perform(get("/api/orders/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.product").value("Keyboard"));
  mvc.perform(get("/api/orders?page=0&size=5")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(id));
 }
 @Test void validatesInputWithoutPersisting() throws Exception {
  mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
   {"customer":" ","product":"Keyboard","quantity":0,"unitPrice":-2}
   """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isArray());
  Assertions.assertEquals(0,repository.count());
 }
 @Test void rejectsExcessPrecision() throws Exception {
  mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
   {"customer":"Sample","product":"Keyboard","quantity":1,"unitPrice":1.999}
   """)).andExpect(status().isBadRequest());
 }
 @Test void transitionsThroughFulfilment() throws Exception {
  String id=create();
  change(id,"CONFIRMED",200); change(id,"SHIPPED",200); change(id,"CANCELLED",409);
  mvc.perform(get("/api/orders/"+id)).andExpect(jsonPath("$.status").value("SHIPPED"));
 }
 @Test void cancellationIsTerminal() throws Exception {
  String id=create(); change(id,"CANCELLED",200); change(id,"CONFIRMED",409);
 }
 @Test void rejectsSkippingConfirmation() throws Exception { change(create(),"SHIPPED",409); }
 @Test void returnsNotFound() throws Exception { mvc.perform(get("/api/orders/"+UUID.randomUUID())).andExpect(status().isNotFound()); }
 @Test void rejectsInvalidPageAndSize() throws Exception {
  mvc.perform(get("/api/orders?page=-1")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/orders?size=101")).andExpect(status().isBadRequest());
 }
 @Test void rejectsMalformedIdAndStatus() throws Exception {
  mvc.perform(get("/api/orders/not-a-uuid")).andExpect(status().isBadRequest());
  change(create(),"UNKNOWN",400);
 }
 void change(String id,String value,int expected) throws Exception {
  mvc.perform(patch("/api/orders/"+id+"/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\""+value+"\"}"))
   .andExpect(status().is(expected));
 }
}
