/*
 *  Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

package com.myblockchain.clusterj.tie;

import java.util.List;

import com.myblockchain.clusterj.core.store.Column;

import com.myblockchain.clusterj.core.util.I18NHelper;
import com.myblockchain.clusterj.core.util.Logger;
import com.myblockchain.clusterj.core.util.LoggerFactoryService;
import com.myblockchain.clusterj.tie.DbImpl.BufferManager;

import com.myblockchain.ndbjtie.ndbapi.NdbScanOperation;
import com.myblockchain.ndbjtie.ndbapi.NdbOperationConst.LockMode;

/**
 *
 */
class ScanResultDataImpl extends ResultDataImpl {

    /** My message translator */
    static final I18NHelper local = I18NHelper
            .getInstance(ScanResultDataImpl.class);

    /** My logger */
    static final Logger logger = LoggerFactoryService.getFactory()
            .getInstance(ScanResultDataImpl.class);

    private NdbScanOperation ndbScanOperation = null;
    private ClusterTransactionImpl clusterTransaction = null;
    /** The number to skip */
    protected long skip = 0;

    /** The limit */
    protected long limit = Long.MAX_VALUE;

    /** The record counter during the scan */
    protected long recordCounter = 0;

    /** True if any records have been locked while scanning the cache */
    boolean recordsLocked = false;

    /** Flags for iterating a scan */
    protected final int RESULT_READY = 0;
    protected final int SCAN_FINISHED = 1;
    protected final int CACHE_EMPTY = 2;

    public ScanResultDataImpl(ClusterTransactionImpl clusterTransaction, 
            NdbScanOperation ndbScanOperation, List<Column> storeColumns,
            int maximumColumnId, int bufferSize, int[] offsets, int[] lengths, int maximumColumnLength,
            BufferManager bufferManager, long skip, long limit) {
        super(ndbScanOperation, storeColumns, maximumColumnId, bufferSize, offsets, lengths,
                bufferManager, false);
        this.clusterTransaction = clusterTransaction;
        this.ndbScanOperation = ndbScanOperation;
        this.skip = skip;
        this.limit = limit;
    }

    /** If any locks were taken over, execute the takeover operations
     */
    private void executeIfRecordsLocked() {
        if (recordsLocked) {
            clusterTransaction.executeNoCommit(true, true);
            recordsLocked = false;
        }
    }

    @Override
    public boolean next() {
        if (recordCounter >= limit) {
            // the next record is past the limit; we have delivered all the rows
            executeIfRecordsLocked();
            ndbScanOperation.close(true, true);
            return false;
        }
        // NdbScanOperation may have many results.
        boolean done = false;
        boolean fetch = false;
        boolean force = true; // always true for scans
        while (!done) {
            int result = ndbScanOperation.nextResult(fetch, force);
            switch (result) {
                case RESULT_READY:
                    if (++recordCounter > skip) {
                        // this record is past the skip
                        // if scanning with locks, grab the lock for the current transaction
                        if (ndbScanOperation.getLockMode() != LockMode.LM_CommittedRead) { 
                            // TODO: remember the operations and check them at SCAN_FINISHED and CACHE_EMPTY
                            // check for result code 499: scan moved on and you forgot to execute the takeover op
                            ndbScanOperation.lockCurrentTuple();
                            recordsLocked = true;
                        }
                        return true;
                    } else {
                        // skip this record
                        break;
                    }
                case SCAN_FINISHED:
                    executeIfRecordsLocked();
                    ndbScanOperation.close(true, true);
                    return false;
                case CACHE_EMPTY:
                    executeIfRecordsLocked();
                    fetch = true;
                    break;
                default:
                    Utility.throwError(result, ndbScanOperation.getNdbError());
            }
        }
        return true; // this statement is needed to make the compiler happy but it's never executed
    }

}
