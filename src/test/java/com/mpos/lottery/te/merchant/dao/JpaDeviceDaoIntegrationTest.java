package com.mpos.lottery.te.merchant.dao;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

import com.mpos.lottery.te.merchant.Device;
import com.mpos.lottery.te.test.BaseTransactionalIntegrationTest;

public class JpaDeviceDaoIntegrationTest extends BaseTransactionalIntegrationTest {
  @Resource(name = "jpaDeviceDao")
  private DeviceDao deviceDao;

  @Before
  public void clearCache() {
    Jedis jedis = new Jedis("192.168.2.158", 6379);
    jedis.flushDB();
  }

  @Test
  public void testByFindByDepartment() throws Exception {
    String departmentId = "88080111";
    this.jdbcTemplate.update("update devices set DPY_ID='" + departmentId + "' where dev_id=113");

    // 1st call to #findByDepartmentId will hit database
    Device device = this.getDeviceDao().findByDepartmentId(departmentId);
    assertEquals("88080666", device.getSerialNo());

    // 2nd call to #findByDepartmentId will hit redis cache
    device = this.getDeviceDao().findByDepartmentId(departmentId);
    assertEquals("88080666", device.getSerialNo());

    /**
     * Very strange, seem the call to #findByHardwareId won't hit underlying database too, it will
     * get the same cached instance with the 2nd call to #findByDepartmentId...So the cache value
     * 'department' of <code>@Cacheable("department")</code> doesn't make any sense.
     * <p>
     * Except that when you set a cache name to <code>@Cacheable</code>, a new key($CacheName~keys)
     * with value of type sorted set will be created. For example a
     * <code>@Cacheable("department")</code> will create a key with name 'department~keys'. However
     * seem it doesn't make any sense, as the key of real cache item is only a single '8808111'
     * <p>
     * Found the
     * solution(http://stackoverflow.com/questions/18145372/is-it-possible-to-create-multiple
     * -cache-stores-using-springs-cache-abstraction), actualy the javadoc of
     * {@link org.springframework.data.redis.cache.RedisCacheManager} has said that: By default
     * saves the keys directly, without appending a prefix (which acts as a namespace). To avoid
     * clashes, it is recommended to change this (by setting 'usePrefix' to 'true').
     * <p>
     * Actually there is another solution. You can implement your own
     * {@link org.springframework.cache.interceptor.KeyGenerator}, as the default one
     * {@link org.springframework.cache.interceptor.SimpleKeyGenerator} only consider the method
     * arguments, your own implementation can generate a key (by cacheName+methodName+arguments).
     */
    // 3rd call to #findByHardwareId will hit database
    device = this.getDeviceDao().findByHardwareId(departmentId);
    // in my understanding ,it should be 88080222
    assertEquals("88080222", device.getSerialNo());
  }

  /**
   * Imagine a scenario that in a transaction we call #findBySerialNo("88080111"), the entity is
   * loaded, and store to redis cache, then the transaction commit. So far so good. A new
   * transaction call #findBySerialNo("88080111") again, the <code>Device</code> install is loaded
   * from redis cache, no database query needed, however when you try to call
   * <code>device.getMerchant().getStatus()</code>, a exception
   * "org.hibernate.LazyInitializationException: could not initialize proxy - no Session" will be
   * thrown out. It is easy to understand, the 'device.getMerchant()' will return a hibernate proxy,
   * the proxy can work only in its original transaction.
   * <p>
   * To solve this problem, we get 2 approaches.
   * <h3>1. load the joined entity eagerly</h3>
   * For example in the implementation of <code>JpaDeviceDao.findBySerialNo(String)</code>, we can
   * explicitly access the joined entity which will make entity manager to load the lazy entity.
   * 
   * <pre>
   * public Device findBySerialNo(String serialNo) {
   *  logger.debug("call #findBySerialNo");
   *  Query query = this.getEntityManager().createQuery("from Device d where d.serialNo=:serialNo");
   *  query.setParameter("serialNo", serialNo);
   *  Device device = (Device) query.getSingleResult();
   *  // explicitly access the fields of lazily loaded entity.
   *  device.getMerchant().getStatus()
   *  return device;
   * }
   * </pre>
   */
  @Test
  public void testJoinColumn() throws Exception {
    Device device = this.getDeviceDao().findBySerialNo("88080111");
    assertEquals(1, device.getMerchant().getStatus());
  }

  /**
   * In database, there are 3 Device records,
   * <table border="1">
   * <tr>
   * <td>id</td>
   * <td>seriaNo</td>
   * <td>hardwareId</td>
   * </tr>
   * <tr>
   * <td>111</td>
   * <td>88080111</td>
   * <td>88080222</td>
   * </tr>
   * <tr>
   * <td>112</td>
   * <td>88080222</td>
   * <td>88080111</td>
   * </tr>
   * <tr>
   * <td>113</td>
   * <td>88080666</td>
   * <td>88080999</td>
   * </tr>
   * </table>
   */
  @Test
  public void testFindBetweenDifferentMethods() {
    this.printMethod();
    // the call to #findBySerialNo() will hit database
    Device device = this.getDeviceDao().findBySerialNo("88080111");
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

    device = this.getDeviceDao().findByHardwareId("88080111");
    /**
     * this call to #findByHardwareId() won't hit the underlying database...why? It is looking up a
     * brand new entity(id=112), it is different method from #findBySerialNo! That is due to the key
     * generate only considers the method arguments, so pass the same argument "88080222", the cache
     * manager will lookup the cache value by same key, method name doesn't make any sense to cache
     * manager.
     * */
    assertEquals("88080999", device.getHardwareId());
  }

  public DeviceDao getDeviceDao() {
    return deviceDao;
  }

  public void setDeviceDao(DeviceDao deviceDao) {
    this.deviceDao = deviceDao;
  }

}
