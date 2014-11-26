package com.mpos.lottery.te.merchant.dao;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.mpos.lottery.te.merchant.Device;
import com.mpos.lottery.te.test.BaseTransactionalIntegrationTest;

public class JpaDeviceDaoIntegrationTest extends BaseTransactionalIntegrationTest {
  @Resource(name = "jpaDeviceDao")
  private DeviceDao deviceDao;

  @Before
  public void clearCache() {
    Device device = this.getDeviceDao().findBySerialNo("88080222");
    /**
     * May get a exception as below,
     * <p>
     * java.lang.IllegalArgumentException: Removing a detached instance
     * com.mpos.lottery.te.merchant.Device#112
     * <p>
     * As the instance of <code>Device</code> is returned from Redis cache, this exception will be
     * thrown out when try to remove, so here we call #update() to merge it into JPA context first.
     */
    device = this.getDeviceDao().update(device);
    this.getDeviceDao().remove(device);
  }

  @Test
  public void testFindByHardwareId() {
    this.printMethod();
    // the call to #findBySerialNo() will hit database
    Device device = this.getDeviceDao().findBySerialNo("88080222");
    device.setHardwareId("88080999");
    /**
     * the call to #update will update Redis cache as well...However I don't recommend the use of
     * <code>@CachePut</code>, as you have to consider to coordinate the database transaction with
     * cache provider. For example, when we update a entity in a JPA transaction, and then update
     * the cache, however finally error occurs when commit transaction. What can we do? clear the
     * cache when get transaction exception? it will complicate your design.
     * <p>
     * The simple solution is use <code>@CacheEvict</code> no matter updating or removing a entity,
     * then the query will load the entity from underlying database next time.
     * */
    this.getDeviceDao().update(device);
    /**
     * this call to #findBySerialNo() won't hit the underlying database...why? they are different
     * methods! That is due to the key generate only considers the method arguments, so even
     * #findBySerialNo() and #findByHardwareId() are different methods, when pass the same argument
     * "88080222", the cache manager will lookup the cache value by same key
     * */
    device = this.getDeviceDao().findByHardwareId("88080333");
    assertEquals("88080999", device.getHardwareId());
  }

  public DeviceDao getDeviceDao() {
    return deviceDao;
  }

  public void setDeviceDao(DeviceDao deviceDao) {
    this.deviceDao = deviceDao;
  }

}
