package com.selbovi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Applications main model representing bank account.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    /**
     * Name of the owner, also represents primary key.
     */
    @Id
    private String owner;

    /**
     * Current balance of this account.
     */
    private double balance;
}
