package com.example.secondhand.domain.catetory.repository;

import com.example.secondhand.domain.catetory.entity.Category;
import java.util.Optional;
import javax.swing.text.html.Option;
import net.bytebuddy.pool.TypePool.Empty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByCategoryName(String categoryName);
	void deleteByCategoryName(String categoryName);
}
