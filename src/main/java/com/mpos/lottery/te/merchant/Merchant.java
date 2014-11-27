package com.mpos.lottery.te.merchant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MERCHANT")
public class Merchant implements java.io.Serializable{
  private static final long serialVersionUID = -8656618688566015743L;

  @Id
  @Column(name = "MERCHANT_ID")
  private long id;

  @Column(name = "status")
  private int status;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

}
