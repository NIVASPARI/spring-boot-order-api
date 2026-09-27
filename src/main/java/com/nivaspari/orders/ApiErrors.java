package com.nivaspari.orders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.*;
import org.springframework.dao.OptimisticLockingFailureException;
@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {
 @Override protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,HttpHeaders headers,HttpStatusCode status,WebRequest request) {
  ProblemDetail problem=ProblemDetail.forStatusAndDetail(status,"Correct the invalid request fields.");
  problem.setProperty("errors",ex.getBindingResult().getFieldErrors().stream().map(e->new FieldError(e.getField(),e.getDefaultMessage())).toList());
  return handleExceptionInternal(ex,problem,headers,status,request);
 }
 @ExceptionHandler(OptimisticLockingFailureException.class)
 public ProblemDetail conflict(OptimisticLockingFailureException ex) { return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"The order changed concurrently. Reload it before retrying."); }
 record FieldError(String field,String message) {}
}
