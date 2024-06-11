package com.krishna.repository;

import com.krishna.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    // here spring data will automatically know that we are trying to fetch data by email (using the method name)
    // and this concept is called DERIVED METHOD NAME QUERY
    List<Customer> findByEmail(String email);
}
