package com.example.musicstore.repositories;// ProductService.java
import com.example.musicstore.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.musicstore.models.enums.OrderStatus;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(Long id);

}