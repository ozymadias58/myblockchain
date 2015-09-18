/*
   Copyright 2010 Sun Microsystems, Inc.
   All rights reserved. Use is subject to license terms.

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

package testsuite.clusterj.model;

import com.myblockchain.clusterj.annotation.Column;
import com.myblockchain.clusterj.annotation.Index;
import com.myblockchain.clusterj.annotation.Indices;
import com.myblockchain.clusterj.annotation.PersistenceCapable;
import com.myblockchain.clusterj.annotation.PrimaryKey;
import java.math.BigDecimal;

/** Schema
 *
drop table if exists decimaltypes;
create table decimaltypes (
 id int not null primary key,

 decimal_null_hash decimal(10,5),
 decimal_null_btree decimal(10,5),
 decimal_null_both decimal(10,5),
 decimal_null_none decimal(10,5)

) ENGINE=ndbcluster DEFAULT CHARSET=latin1;

create unique index idx_decimal_null_hash using hash on decimaltypes(decimal_null_hash);
create index idx_decimal_null_btree on decimaltypes(decimal_null_btree);
create unique index idx_decimal_null_both on decimaltypes(decimal_null_both);

 */
@Indices({
    @Index(name="idx_decimal_null_both", columns=@Column(name="decimal_null_both"))
})
@PersistenceCapable(table="decimaltypes")
@PrimaryKey(column="id")
public interface DecimalTypes extends IdBase {

    int getId();
    void setId(int id);

    // Decimal
    @Column(name="decimal_null_hash")
    @Index(name="idx_decimal_null_hash")
    BigDecimal getDecimal_null_hash();
    void setDecimal_null_hash(BigDecimal value);

    @Column(name="decimal_null_btree")
    @Index(name="idx_decimal_null_btree")
    BigDecimal getDecimal_null_btree();
    void setDecimal_null_btree(BigDecimal value);

    @Column(name="decimal_null_both")
    BigDecimal getDecimal_null_both();
    void setDecimal_null_both(BigDecimal value);

    @Column(name="decimal_null_none")
    BigDecimal getDecimal_null_none();
    void setDecimal_null_none(BigDecimal value);

}
