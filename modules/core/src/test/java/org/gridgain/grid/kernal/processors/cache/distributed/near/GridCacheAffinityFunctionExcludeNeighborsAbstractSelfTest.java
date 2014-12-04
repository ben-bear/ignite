/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.cache.distributed.near;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.product.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.affinity.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.spi.discovery.tcp.*;
import org.gridgain.grid.spi.discovery.tcp.internal.*;
import org.gridgain.grid.spi.discovery.tcp.ipfinder.*;
import org.gridgain.grid.spi.discovery.tcp.ipfinder.vm.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;

import static org.gridgain.grid.cache.GridCacheMode.*;
import static org.gridgain.grid.cache.GridCachePreloadMode.*;

/**
 * Partitioned affinity test.
 */
@SuppressWarnings({"PointlessArithmeticExpression", "FieldCanBeLocal"})
public abstract class GridCacheAffinityFunctionExcludeNeighborsAbstractSelfTest extends GridCommonAbstractTest {
    /** Number of backups. */
    private int backups = 2;

    /** */
    private int gridInstanceNum;

    /** */
    private GridTcpDiscoveryIpFinder ipFinder = new GridTcpDiscoveryVmIpFinder(true);

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(final String gridName) throws Exception {
        IgniteConfiguration c = super.getConfiguration(gridName);

        GridTcpDiscoverySpi spi = new GridTcpDiscoverySpi() {
            @Override public void setNodeAttributes(Map<String, Object> attrs, IgniteProductVersion ver) {
                super.setNodeAttributes(attrs, ver);

                // Set unique mac addresses for every group of three nodes.
                String macAddrs = "MOCK_MACS_" + (gridInstanceNum / 3);

                attrs.put(GridNodeAttributes.ATTR_MACS, macAddrs);

                gridInstanceNum++;
            }
        };

        spi.setIpFinder(ipFinder);

        c.setDiscoverySpi(spi);

        GridCacheConfiguration cc = defaultCacheConfiguration();

        cc.setCacheMode(PARTITIONED);

        cc.setBackups(backups);

        cc.setAffinity(affinityFunction());

        cc.setPreloadMode(NONE);

        c.setCacheConfiguration(cc);

        return c;
    }

    /**
     * @return Affinity function for test.
     */
    protected abstract GridCacheAffinityFunction affinityFunction();

    /**
     * @param ignite Grid.
     * @return Affinity.
     */
    static GridCacheAffinity<Object> affinity(Ignite ignite) {
        return ignite.cache(null).affinity();
    }

    /**
     * @param aff Affinity.
     * @param key Key.
     * @return Nodes.
     */
    private static Collection<? extends ClusterNode> nodes(GridCacheAffinity<Object> aff, Object key) {
        return aff.mapKeyToPrimaryAndBackups(key);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAffinityMultiNode() throws Exception {
        int grids = 9;

        startGrids(grids);

        try {
            Object key = 12345;

            int copies = backups + 1;

            for (int i = 0; i < grids; i++) {
                final Ignite g = grid(i);

                GridCacheAffinity<Object> aff = affinity(g);

                List<GridTcpDiscoveryNode> top = new ArrayList<>();

                for (ClusterNode node : g.cluster().nodes())
                    top.add((GridTcpDiscoveryNode) node);

                Collections.sort(top);

                assertEquals(grids, top.size());

                int idx = 1;

                for (ClusterNode n : top) {
                    assertEquals(idx, n.order());

                    idx++;
                }

                Collection<? extends ClusterNode> affNodes = nodes(aff, key);

                info("Affinity picture for grid [i=" + i + ", aff=" + U.toShortString(affNodes));

                assertEquals(copies, affNodes.size());

                Set<String> macs = new HashSet<>();

                for (ClusterNode node : affNodes)
                    macs.add((String)node.attribute(GridNodeAttributes.ATTR_MACS));

                assertEquals(copies, macs.size());
            }
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testAffinitySingleNode() throws Exception {
        Ignite g = startGrid();

        try {
            Object key = 12345;

            Collection<? extends ClusterNode> affNodes = nodes(affinity(g), key);

            info("Affinity picture for grid: " + U.toShortString(affNodes));

            assertEquals(1, affNodes.size());
        }
        finally {
            stopAllGrids();
        }
    }
}
