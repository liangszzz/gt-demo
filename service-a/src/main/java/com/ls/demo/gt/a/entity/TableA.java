package com.ls.demo.gt.a.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "table_a")
@Data
public class TableA implements Serializable {

  public TableA() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "f1")
  private String f1;
}
