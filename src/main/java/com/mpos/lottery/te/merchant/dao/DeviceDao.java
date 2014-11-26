package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.merchant.Device;

public interface DeviceDao {

  Device findById(long id);

  Device findBySerialNo(String serialNo);

  Device findByHardwareId(String hardwareId);

  Device update(Device entity);

  void remove(Device entity);
}
