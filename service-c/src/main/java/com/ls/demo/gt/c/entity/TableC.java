package com.ls.demo.gt.c.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "table_c")
@Data
public class TableC implements Serializable {

  public TableC() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "f1")
  private String f1;
}
