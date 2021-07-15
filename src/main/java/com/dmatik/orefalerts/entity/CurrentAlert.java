package com.dmatik.orefalerts.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.ArrayList;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentAlert {

    private Long id;
    private String title;
    private ArrayList<String> data;
}
