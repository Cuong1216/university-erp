package com.wiz.universityerpapi.repository;

import com.wiz.universityerpapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query(value = """
            SELECT cd.he_so_cd as heSoCd, COALESCE(hv.he_so_hv, 1.00) as heSoHv FROM giang_vien gv 
            LEFT JOIN chuc_danh cd ON gv.ma_cd = cd.ma_cd 
            LEFT JOIN hoc_vi hv ON gv.ma_hv = hv.ma_hv 
            WHERE gv.ma_gv = :maGv OR gv.user_id = (SELECT u.id FROM users u WHERE u.username = :maGv)
            LIMIT 1
            """, nativeQuery = true)
    Optional<com.wiz.universityerpapi.repository.projection.GiangVienHeSoView> findHeSoByMaGv(@Param("maGv") String maGv);
}
