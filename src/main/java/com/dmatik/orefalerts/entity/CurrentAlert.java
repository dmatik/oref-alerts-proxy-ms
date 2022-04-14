package com.dmatik.orefalerts.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentAlert {

    private String id;
    private String cat;
    private String title;
    private String[] data;
    private String desc;
}
