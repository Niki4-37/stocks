package ru.redcarpet.stocks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.redcarpet.stocks.enums.SecurityType;

@Entity
@Table(name = "securities")
@Getter
@Setter
@NoArgsConstructor
public class Security {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker", length = 20)
    private String ticker;

    @Column(name = "isin", length = 12)
    private String isin;

    @Column(name = "figi", length = 12)
    private String figi;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_type", nullable = false, length = 32)
    private SecurityType securityType;

    @Column(name = "exchange", length = 32)
    private String exchange;
}
