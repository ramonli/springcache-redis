package com.mpos.lottery.te.test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * This test will be ran against <code>DispatchServlet</code> directly, that says we must support
 * lookup <code>ApplicationContext</code> from <code>ServletContext</code>, refer to
 * {@link org.springframework.web.context.support.WebApplicationContextUtils}
 * <p>
 * Spring TestContext Framework. If extending from
 * <code>AbstractTransactionalJUnit4SpringContextTests</code>, you don't need to declare
 * <code>@RunWith</code>, <code>TestExecutionListeners(3 default listeners)</code> and
 * <code>@Transactional</code>. Refer to {@link AbstractTransactionalJUnit4SpringContextTests} for
 * more information.
 * <p>
 * Legacy JUnit 3.8 class hierarchy is deprecated. Under new sprint test context framework, a field
 * of property must be annotated with <code>@Autowired</code> or <code>@Resource</code>(
 * <code>@Autowired</code> in conjunction with <code>@Qualifier</code>) explicitly to let spring
 * inject dependency automatically.
 * <p>
 * Reference:
 * <ul>
 * <li>https://jira.springsource.org/browse/SPR-5243</li>
 * <li>
 * http://forum.springsource.org/showthread.php?86124-How -to-register-
 * BeanPostProcessor-programaticaly</li>
 * </ul>
 * 
 * @author Ramon Li
 */

// Make sure loading a web application context.
@ContextConfiguration(locations = { "/spring-core.xml" })
// this annotation defines the transaction manager for each test case.
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
// As our TEST extending from AbstractTransactionalJUnit4SpringContextTests,
// below 3 listeners have been registered by default, and it will be inherited
// by subclass.
// @TestExecutionListeners(listeners = {ShardAwareTestExecutionListener.class})
// @Transactional
public class BaseTransactionalIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
  protected static Log logger = LogFactory.getLog(BaseTransactionalIntegrationTest.class);
  // SPRING DEPENDENCIES
  /**
   * Always auto wire the data source to a javax.sql.DataSource with name 'dataSource' even there
   * are multiple data sources. It means there must be a DataSource bean named 'dataSource' and a
   * <code>PlatformTransactionManager</code> named 'transactionManager'.
   * <p>
   * 
   * @see AbstractTransactionalJUnit4SpringContextTests#setDataSource(javax.sql.DataSource)
   */
  @PersistenceContext
  protected EntityManager entityManager;

  protected String dateFormat = "yyyyMMddHHmmss";

  // run once for current test suite.
  @BeforeClass
  public static void beforeClass() {
    logger.trace("@BeforeClass:beforeClass()");
  }

  /**
   * Set up test data within the transaction.
   * <p>
   * The @Before methods of superclass will be run before those of the current class. No other
   * ordering is defined.
   * <p>
   * NOTE: Any before methods (for example, methods annotated with JUnit 4's <code>@Before</code>)
   * and any after methods (such as methods annotated with JUnit 4's <code>@After</code>) are
   * executed within a transaction.
   */
  @Before
  public void setUpTestDataWithinTransaction() {
    logger.trace("@Before:setUpTestDataWithinTransaction()");
  }

  /**
   * execute "tear down" logic within the transaction.
   * <p>
   * The @After methods declared in superclass will be run after those of the current class.
   */
  @After
  public void tearDownWithinTransaction() throws Exception {
    logger.trace("@After:tearDownWithinTransaction()");
  }

  /**
   * logic to verify the final state after transaction has rolled back.
   * <p>
   * The @AfterTransaction methods declared in superclass will be run after those of the current
   * class.
   */
  @AfterTransaction
  public void verifyFinalDatabaseState() throws Exception {
    logger.trace("@AfterTransaction:verifyFinalDatabaseState()");
  }

  @AfterClass
  public static void afterClass() {
    logger.trace("@AfterClass:afterClass()");
  }

  // ----------------------------------------------------------------
  // HELPER METHODS
  // ----------------------------------------------------------------

  protected void printMethod() {
    StringBuffer lineBuffer = new StringBuffer("+");
    for (int i = 0; i < 120; i++) {
      lineBuffer.append("-");
    }
    lineBuffer.append("+");
    String line = lineBuffer.toString();

    // Get the test method. If index=0, it means get current method.
    StackTraceElement eles[] = new Exception().getStackTrace();
    // StackTraceElement eles[] = new Exception().getStackTrace();
    // for (StackTraceElement ele : eles){
    // System.out.println("class:" + ele.getClassName());
    // System.out.println("method:" + ele.getMethodName());
    // }
    String className = eles[1].getClassName();
    int index = className.lastIndexOf(".");
    className = className.substring((index == -1 ? 0 : (index + 1)));

    String method = className + "." + eles[1].getMethodName();
    StringBuffer padding = new StringBuffer();
    for (int i = 0; i < line.length(); i++) {
      padding.append(" ");
    }
    logger.info(line);
    String methodSig = (method + padding.toString()).substring(0, line.length() - 3);
    logger.info("| " + methodSig + "|");
    logger.info(line);
  }

  protected String uuid() {
    UUID uuid = UUID.randomUUID();
    String uuidStr = uuid.toString();
    return uuidStr.replace("-", "");
  }

  /**
   * Execute SQL in a new transaction, won't be affected by Spring test transaction.
   */
  protected void executeSqlInNewTransaction(String... sqls) throws Exception {
    DataSource dataSource = (DataSource) this.applicationContext.getBean("dataSource");
    Connection conn = dataSource.getConnection();

    try {
      conn.setAutoCommit(false);
      for (String sql : sqls) {
        Statement statement = conn.createStatement();
        statement.execute(sql);
        statement.close();
      }
      conn.commit();
    } catch (Exception e) {
      conn.rollback();
    } finally {
      if (conn != null)
        conn.close();
    }
  }

  // ----------------------------------------------------------------
  // SPRINT DEPENDENCIES INJECTION
  // ----------------------------------------------------------------

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }
}
