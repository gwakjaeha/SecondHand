package com.example.secondhand.domain.catetory.repository;

import com.example.secondhand.domain.catetory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
