package com.mpos.lottery.te.merchant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DEVICES")
public class Device implements java.io.Serializable {
  private static final long serialVersionUID = 7096647691505429659L;
  public static final int STATUS_ACTIVE = 0;
  public static final int STATUS_BLOCKED = 1;
  public static final int STATUS_PAYOUT_DENIED = 2;

  @Id
  @Column(name = "DEV_ID")
  private long id;

  @Column(name = "DPY_ID")
  private String departmentId;

  @Column(name = "DT_ID")
  private String type;

  @Column(name = "SERIAL_NO")
  private String serialNo;

  @Column(name = "HARDWARE_ID")
  private String hardwareId;

  @Column(name = "LOGIC_STATUS")
  private int status;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(String departmentId) {
    this.departmentId = departmentId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSerialNo() {
    return serialNo;
  }

  public void setSerialNo(String serialNo) {
    this.serialNo = serialNo;
  }

  public String getHardwareId() {
    return hardwareId;
  }

  public void setHardwareId(String hardwareId) {
    this.hardwareId = hardwareId;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

}
