package com.ls.demo.gt.b.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "table_b")
@Data
public class TableB implements Serializable {

  public TableB() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "f1")
  private String f1;
}
