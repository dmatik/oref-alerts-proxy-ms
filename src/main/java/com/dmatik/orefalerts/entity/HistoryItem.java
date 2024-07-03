package com.dmatik.orefalerts.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryItem {

    private String alertDate;
    private String title;
    private String data;
    private int category;
}
