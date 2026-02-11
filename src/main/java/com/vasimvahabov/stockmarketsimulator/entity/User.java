package com.vasimvahabov.stockmarketsimulator.entity;

import com.vasimvahabov.stockmarketsimulator.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "gen_user_id", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "gen_user_id", sequenceName = "seq_user_id")
    Long id;

    @Column(name = "username")
    String username;

    @Column(name = "password")
    String password;

    @Column(name = "role_id")
    Role role;

    @OneToOne
    @JoinColumn(
            name = "trader_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_trader")
    )
    Trader trader;

}
