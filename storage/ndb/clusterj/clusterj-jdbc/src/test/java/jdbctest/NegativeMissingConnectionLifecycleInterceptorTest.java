/*
 *  Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
 */

package jdbctest;

import java.sql.SQLException;

import com.myblockchain.clusterj.ClusterJUserException;

import testsuite.clusterj.AbstractClusterJTest;

public class NegativeMissingConnectionLifecycleInterceptorTest extends AbstractClusterJTest {
    public void test() {
        try {
            getConnection("target/test-classes/bad-connection-no-connection-lifecycle-interceptor.properties");
            connection.prepareStatement("SELECT 1 FROM DUAL");
            fail("Missing connection lifecycle interceptor should fail with ClusterJUserException.");
        } catch (SQLException e) {
            // getConnection doesn't throw SQLException but wraps its exception in RuntimeException
        } catch (RuntimeException runtimeException) {
            Throwable sqlException = runtimeException.getCause();
            if (sqlException instanceof SQLException) {
                Throwable clusterJException = sqlException.getCause();
                if (clusterJException instanceof ClusterJUserException) {
                    if (clusterJException.getMessage().contains("connectionLifecycleInterceptors")) {
                    // good catch
                    } else {
                        fail("Exception should contain the string 'connectionLifecycleInterceptors'");
                        if (getDebug()) sqlException.printStackTrace();
                    }
                } else {
                    fail("Exception should be a ClusterJUserException wrapped in a SQLException.");
                    if (getDebug()) sqlException.printStackTrace();
                }
            } else {
                fail("Exception should be a ClusterJUserException wrapped in a SQLException.");
                if (getDebug()) sqlException.printStackTrace();
            }
        }
    }

}
