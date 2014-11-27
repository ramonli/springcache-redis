package com.mpos.lottery.te.merchant.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.mpos.lottery.te.merchant.Device;

@Repository("jpaDeviceDao")
public class JpaDeviceDao implements DeviceDao {
  private Log logger = LogFactory.getLog(JpaDeviceDao.class);
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Device findById(long id) {
    logger.debug("call #findById");
    return this.getEntityManager().find(Device.class, id);
  }

  @Cacheable("devices")
  @Override
  public Device findBySerialNo(String serialNo) {
    logger.debug("call #findBySerialNo");
    Query query = this.getEntityManager().createQuery("from Device d where d.serialNo=:serialNo");
    query.setParameter("serialNo", serialNo);
    Device device = (Device) query.getSingleResult();
    device.getMerchant().getStatus();
    return device;
  }

  @Cacheable("devices")
  @Override
  public Device findByHardwareId(String hardwareId) {
    logger.debug("call #findByHardwareId");
    Query query = this.getEntityManager().createQuery(
            "from Device d where d.hardwareId=:hardwareId");
    query.setParameter("hardwareId", hardwareId);
    return (Device) query.getSingleResult();
  }

  @Cacheable("department")
  @Override
  public Device findByDepartmentId(String departmentId) {
    logger.debug("call #findByDepartmentId");
    Query query = this.getEntityManager().createQuery(
            "from Device d where d.departmentId=:departmentId");
    query.setParameter("departmentId", departmentId);
    
    return (Device) query.getSingleResult();
  }

  
  @CachePut(value="devices", key="#entity.serialNo")
  @Override
  public Device update(Device entity) {
    return this.getEntityManager().merge(entity);
  }

  @CacheEvict(value = "devices", allEntries = true)
  @Override
  public void remove(Device entity) {
    this.getEntityManager().remove(entity);
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

}
