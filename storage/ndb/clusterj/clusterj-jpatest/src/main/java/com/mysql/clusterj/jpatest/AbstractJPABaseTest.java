/*
   Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; version 2 of the License.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
*/

package com.myblockchain.clusterj.jpatest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.Query;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

import com.myblockchain.clusterj.jpatest.model.Employee;
import com.myblockchain.clusterj.jpatest.model.IdBase;

/**
 *
 */
public abstract class AbstractJPABaseTest extends SingleEMTestCase {

    /** The local system default time zone, which is reset by resetLocalSystemDefaultTimeZone */
    protected static TimeZone localSystemTimeZone = TimeZone.getDefault();

    /** The connection to the blockchain */
    protected Connection connection;

    /** The column descriptors */
    private ColumnDescriptor[] columnDescriptors = getColumnDescriptors();

    /** The instances used in the tests, generated by generateInstances */
    List<IdBase> instances = new ArrayList<IdBase>();

    /** List of expected results, generated by generateInstances */
    private List<Object[]> expected = null;

    /** Debug flag */
    protected static boolean debug;

    public AbstractJPABaseTest() {
        debug = getDebug();
    }

    protected void getConnection() {
        connection = (Connection) ((OpenJPAEntityManager)em).getConnection();
        setAutoCommit(connection, false);
    }

    protected void setAutoCommit(Connection connection, boolean b) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("setAutoCommit failed", e);
        }
    }

    public void deleteAll() {
        // delete all instances without verifying
        em = emf.createEntityManager();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = em.find(Employee.class, i);
            if (e != null) {
                em.remove(e);
            }
        }
        commit();
        em.close();
    }
    public void verifyDeleteAll() {
        em = emf.createEntityManager();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = em.find(Employee.class, i);
            if (e != null) {
                error("Entity exists after being removed: " + i);
            }
        }
        commit();
        failOnError();
        em.close();
    }

    public void createEmployeeInstances() {
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            System.out.println("AbstractJPABaseTest creating Employee " + i);
            Employee e = new Employee();
            e.setId(i);
            e.setAge(i);
            e.setMagic(i);
            e.setName("Employee " + i);
            em.persist(e);
        }
    }

    public void createAll() {
        em = emf.createEntityManager();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = new Employee();
            e.setId(i);
            e.setAge(i);
            e.setMagic(i);
            e.setName("Employee " + i);
            em.persist(e);
        }
        commit();
        em.close();
    }

    public void findAll() {
        em = emf.createEntityManager();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = em.find(Employee.class, i);
            verifyEmployee(e, 0);
        }
        commit();
        em.close();
    }

    public void updateThenVerifyAll() {
        em = emf.createEntityManager();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = em.find(Employee.class, i);
            e.setAge(i + 1);
            verifyEmployee(e, 1);
        }
        commit();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = em.find(Employee.class, i);
            verifyEmployee(e, 1);
        }
        commit();
        em.close();
    }

    public void deleteThenVerifyAll() {
        em = emf.createEntityManager();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = em.find(Employee.class, i);
            verifyEmployee(e, 1);
            em.remove(e);
        }
        commit();
        begin();
        for (int i = 0; i < getNumberOfEmployees(); ++i) {
            Employee e = em.find(Employee.class, i);
            if (e != null) {
                error("Entity exists after being removed: " + i);
            }
        }
        commit();
        em.close();
    }

    protected void verifyEmployee(Employee e, int updateOffset) {
        int i = e.getId();
        errorIfNotEqual("Error in age", i + updateOffset, e.getAge().intValue());
        errorIfNotEqual("Error in magic", i, e.getMagic());
        errorIfNotEqual("Error in name", "Employee " + i, e.getName());
    }
    protected int getNumberOfEmployees() {
        return 1;
    }

    /** Convert year, month, day, hour, minute, second into milliseconds after the Epoch, UCT.
     * @param year the year
     * @param month the month (0 for January)
     * @param day the day of the month
     * @param hour the hour of the day
     * @param minute the minute
     * @param second the second
     * @return
     */
    protected static long getMillisFor(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        long result = calendar.getTimeInMillis();
        return result;
    }

    /** Convert year, month, day into milliseconds after the Epoch, UCT.
     * Set hours, minutes, seconds, and milliseconds to zero.
     * @param year the year
     * @param month the month (0 for January)
     * @param day the day of the month
     * @return
     */
    protected static long getMillisFor(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long result = calendar.getTimeInMillis();
        return result;
    }

    /** Convert days, hours, minutes, and seconds into milliseconds after the Epoch, UCT.
     * Date is index origin 1 so add one to the number of days. Default year and month,
     * as these are assumed by Calendar to be the Epoch.
     * @param day the number of days
     * @param hour the hour (or number of hours)
     * @param minute the minute (or number of minutes)
     * @param second the second (or number of seconds)
     * @return millis past the Epoch UCT
     */
    protected static long getMillisFor(int days, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.DATE, days + 1);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        long result = calendar.getTimeInMillis();
        return result;
    }

    /** Reset the local system default time zone to the time zone used
     * by the MyBlockchain server. This guarantees that there is no time zone
     * offset between the time zone in the client and the time zone
     * in the server.
     * @param connection2 
     */
    protected static void resetLocalSystemDefaultTimeZone(Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement("select @@global.time_zone, @@global.system_time_zone, @@session.time_zone");
            ResultSet rs = statement.executeQuery();
            // there are two columns in the result
            rs.next();
            String globalTimeZone = rs.getString(1);
            String globalSystemTimeZone = rs.getString(2);
            String sessionTimeZone = rs.getString(3);
            if (debug) System.out.println("Global time zone: " + globalTimeZone + 
                    " Global system time zone: " + globalSystemTimeZone +" Session time zone: " + sessionTimeZone);
            connection.commit();
            if ("SYSTEM".equalsIgnoreCase(globalTimeZone)) {
                globalTimeZone = globalSystemTimeZone;
            } else {
                globalTimeZone = "GMT" + globalTimeZone;
            }
            localSystemTimeZone = TimeZone.getTimeZone(globalTimeZone);
            if (debug) System.out.println("Local system time zone set to: " + globalTimeZone + "(" + localSystemTimeZone + ")");
            TimeZone.setDefault(localSystemTimeZone);
            // get a new connection after setting local default time zone
            // because a connection contains a session calendar used to create Timestamp instances
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("setServerTimeZone failed", e);
        }
    }

    /** This class describes columns and fields for a table and model class. 
     * A subclass will instantiate instances of this class and provide handlers to
     * read and write fields and columns via methods defined in the instance handler.
     */
    protected static class ColumnDescriptor {

        private String columnName;
    
        protected InstanceHandler instanceHandler;
    
        public String getColumnName() {
            return columnName;
        }
    
        public Object getResultSetValue(ResultSet rs, int j) throws SQLException {
            return instanceHandler.getResultSetValue(rs, j);
        }
    
        public Object getFieldValue(IdBase instance) {
            return instanceHandler.getFieldValue(instance);
        }
    
        public void setFieldValue(IdBase instance, Object value) {
            this.instanceHandler.setFieldValue(instance, value);
        }
    
        public void setPreparedStatementValue(PreparedStatement preparedStatement, int j, Object value)
                throws SQLException {
            instanceHandler.setPreparedStatementValue(preparedStatement, j, value);
        }
    
        protected ColumnDescriptor(String name, InstanceHandler instanceHandler) {
            this.columnName = name;
            this.instanceHandler = instanceHandler;
        }
    }

    protected interface InstanceHandler {
        void setFieldValue(IdBase instance, Object value);
        Object getResultSetValue(ResultSet rs, int j)
                throws SQLException;
        Object getFieldValue(IdBase instance);
        public void setPreparedStatementValue(PreparedStatement preparedStatement, int j, Object value)
                throws SQLException;
    }

    /** Subclasses can override this method to get debugging info printed to System.out */
    protected boolean getDebug() {
        return false;
    }

    /** Subclasses usually should not override this method to provide the list of expected results */
    protected List<Object[]> getExpected() {
        return expected;
    }

    /** Subclasses must override this method to provide the name of the table for the test */
    protected String getTableName() {
        return null;
    }

    /** Subclasses must override this method to provide the number of instances to create */
    protected int getNumberOfInstances() {
        return 0;
    }

    /** Subclasses must override this method to provide the column descriptors for the test */
    protected ColumnDescriptor[] getColumnDescriptors() {
        return null;
    }

    /** Subclasses must override this method to implement the model factory for the test */
    protected IdBase getNewInstance(Class<? extends IdBase> modelClass) {
        return null;
    }

    /** Subclasses must override this method to provide the model class for the test */
    protected Class<? extends IdBase> getModelClass() {
        return null;
    }

    /** Subclasses must override this method to provide values for rows (i) and columns (j) */
    protected Object getColumnValue(int i, int j) {
        return null;
    }

    /** Generated instances to persist. When using JDBC, the data is obtained from the instance
     * via the column descriptors. As a side effect (!) create the list of expected results from read.
     * @param columnDescriptors the column descriptors
     * @return the generated instances
     */
    protected void generateInstances(ColumnDescriptor[] columnDescriptors) {
        Class<? extends IdBase> modelClass = getModelClass();
        expected = new ArrayList<Object[]>();
        instances = new ArrayList<IdBase>();
        IdBase instance = null;
        int numberOfInstances = getNumberOfInstances();
        for (int i = 0; i < numberOfInstances; ++i) {
            // create the instance
            instance = getNewInstance(modelClass);
            instance.setId(i);
            // create the expected result row
            int j = 0;
            for (ColumnDescriptor columnDescriptor: columnDescriptors) {
                Object value = getColumnValue(i, j);
                // set the column value in the instance
                columnDescriptor.setFieldValue(instance, value);
                // set the column value in the expected result
                if (debug) System.out.println("generateInstances set field " + columnDescriptor.getColumnName() + " to value "  + value);
                ++j;
            }
            instances.add(instance);
            Object[] expectedRow = createRow(columnDescriptors, instance);
            expected.add(expectedRow);
        }
        if (debug) System.out.println("Created " + instances.size() + " instances.");
    }

    /** Verify that the actual results match the expected results. If not, use the multiple error
     * reporting method errorIfNotEqual defined in the superclass.
     * @param where the location of the verification of results, normally the name of the test method
     * @param expecteds the expected results
     * @param actuals the actual results
     */
    protected void verify(String where, List<Object[]> expecteds, List<Object[]> actuals) {
        for (int i = 0; i < expecteds.size(); ++i) {
            Object[] expected = expecteds.get(i);
            Object[] actual = actuals.get(i);
            errorIfNotEqual(where + " got failure on id for row " + i, i, actual[0]);
            for (int j = 1; j < expected.length; ++j) {
                errorIfNotEqual(where + " got failure to match column data for row "
                        + i + " column " + j,
                        expected[j], actual[j]);
            }
        }
    }

    protected void removeAll(Class<?> modelClass) {
        Query query = em.createQuery("DELETE FROM " + modelClass.getSimpleName());
        em.getTransaction().begin();
        query.executeUpdate();
        em.getTransaction().commit();
    }

    private void removeAll(String tableName) {
        getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName);
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** Write data via JDBC and read back the data via JPA */
    protected void writeJDBCreadJPA() {
        generateInstances(columnDescriptors);
        removeAll(getTableName());
        List<Object[]> result = null;
        writeToJDBC(columnDescriptors, instances);
        result = readFromJPA(columnDescriptors);
        verify("writeJDBCreadJPA", getExpected(), result);
    }

    /** Write data via JDBC and read back the data via JDBC */
    protected void writeJDBCreadJDBC() {
        generateInstances(columnDescriptors);
        removeAll(getTableName());
        List<Object[]> result = null;
        writeToJDBC(columnDescriptors, instances);
        result = readFromJDBC(columnDescriptors);
        verify("writeJDBCreadJDBC", getExpected(), result);
    }

    /** Write data via JPA and read back the data via JPA */
    protected void writeJPAreadJPA() {
        generateInstances(columnDescriptors);
        removeAll(getModelClass());
        List<Object[]> result = null;
        writeToJPA(columnDescriptors, instances);
        result = readFromJPA(columnDescriptors);
        verify("writeJPAreadJPA", getExpected(), result);
    }

    /** Write data via JPA and read back the data via JDBC */
    protected void writeJPAreadJDBC() {
        generateInstances(columnDescriptors);
        removeAll(getTableName());
        List<Object[]> result = null;
        writeToJPA(columnDescriptors, instances);
        result = readFromJDBC(columnDescriptors);
        verify("writeJPAreadJDBC", getExpected(), result);
    }

    /** Write data via JPA */
    protected void writeToJPA(ColumnDescriptor[] columnDescriptors, List<IdBase> instances) {
        em.getTransaction().begin();
        for (IdBase instance: instances) {
            em.persist(instance);
        }
        em.getTransaction().commit();
    }

    /** Write data to JDBC. */
    protected void writeToJDBC(ColumnDescriptor[] columnDescriptors, List<IdBase> instances) {
        String tableName = getTableName();
        StringBuffer buffer = new StringBuffer("INSERT INTO ");
        buffer.append(tableName);
        buffer.append(" (id");
        for (ColumnDescriptor columnDescriptor: columnDescriptors) {
            buffer.append(", ");
            buffer.append(columnDescriptor.getColumnName());
        }
        buffer.append(") VALUES (?");
        for (ColumnDescriptor columnDescriptor: columnDescriptors) {
            buffer.append(", ?");
        }
        buffer.append(")");
        String statement = buffer.toString();
        if (debug) System.out.println(statement);
    
        PreparedStatement preparedStatement = null;
        int i = 0;
        try {
            preparedStatement = connection.prepareStatement(statement);
            if (debug) System.out.println(preparedStatement.toString());
            for (i = 0; i < instances.size(); ++i) {
                IdBase instance = instances.get(i);
                preparedStatement.setInt(1, instance.getId());
                int j = 2;
                for (ColumnDescriptor columnDescriptor: columnDescriptors) {
                    Object value = columnDescriptor.getFieldValue(instance);
                    columnDescriptor.setPreparedStatementValue(preparedStatement, j++, value);
                    if (debug) System.out.println("writeToJDBC set column: " + columnDescriptor.getColumnName() + " to value: " + value);
                }
                preparedStatement.execute();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert " + tableName + " at instance " + i, e);
        }
    }

    /** Read data via JPA */
    protected List<Object[]> readFromJPA(ColumnDescriptor[] columnDescriptors) {
        Class<? extends IdBase> modelClass = getModelClass();
        List<Object[]> result = new ArrayList<Object[]>();
        em.getTransaction().begin();
        for (int i = 0; i < getNumberOfInstances() ; ++i) {
            IdBase instance = em.find(modelClass, i);
            if (instance != null) {
                Object[] row = createRow(columnDescriptors, instance);
                result.add(row);
            }
        }
        em.getTransaction().commit();
        if (debug) System.out.println("readFromJPA: " + dump(result));
        return result;
    }

    /** Read data via JDBC */
    protected List<Object[]> readFromJDBC(ColumnDescriptor[] columnDescriptors) {
        String tableName = getTableName();
        List<Object[]> result = new ArrayList<Object[]>();
        StringBuffer buffer = new StringBuffer("SELECT id");
        for (ColumnDescriptor columnDescriptor: columnDescriptors) {
            buffer.append(", ");
            buffer.append(columnDescriptor.getColumnName());
        }
        buffer.append(" FROM ");
        buffer.append(tableName);
        buffer.append(" ORDER BY ID");
        String statement = buffer.toString();
        if (debug) System.out.println(statement);
        PreparedStatement preparedStatement = null;
        int i = 0;
        try {
            preparedStatement = connection.prepareStatement(statement);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Object[] row = new Object[columnDescriptors.length + 1];
                int j = 1;
                row[0] = rs.getInt(1);
                for (ColumnDescriptor columnDescriptor: columnDescriptors) {
                    row[j] = columnDescriptor.getResultSetValue(rs, j + 1);
                    ++j;
                }
                ++i;
                result.add(row);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read " + tableName + " at instance " + i, e);
        }
        if (debug) System.out.println("readFromJDBC: " + dump(result));
        return result;
    }

    /** Create row data from an instance.
     * @param columnDescriptors the column descriptors describing the data
     * @param instance the instance to extract data from
     * @return the row data representing the instance
     */
    private Object[] createRow(ColumnDescriptor[] columnDescriptors,
            IdBase instance) {
        Object[] row = new Object[columnDescriptors.length + 1];
        row[0] = instance.getId();
        int j = 1;
        for (ColumnDescriptor columnDescriptor: columnDescriptors) {
            row[j++] = columnDescriptor.getFieldValue(instance);
        }
        return row;
    }

    /** Dump the contents of the expected or actual results of the operation */
    private String dump(List<Object[]> results) {
        StringBuffer result = new StringBuffer(results.size() + " rows\n");
        for (Object[] row: results) {
            result.append("Id: ");
            for (Object column: row) {
                result.append(column);
                result.append(' ');
            }
            result.append('\n');
        }
        return result.toString();
    }

}
