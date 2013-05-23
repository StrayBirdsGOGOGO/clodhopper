package org.battelle.clodhopper.gmeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.battelle.clodhopper.AbstractClusterSplitter;
import org.battelle.clodhopper.Cluster;
import org.battelle.clodhopper.ClusterStats;
import org.battelle.clodhopper.kmeans.KMeansClusterer;
import org.battelle.clodhopper.kmeans.KMeansParams;
import org.battelle.clodhopper.seeding.PreassignedSeeder;
import org.battelle.clodhopper.tuple.ArrayTupleList;
import org.battelle.clodhopper.tuple.FilteredTupleList;
import org.battelle.clodhopper.tuple.TupleList;
import org.battelle.clodhopper.tuple.TupleMath;

public class GMeansClusterSplitter extends AbstractClusterSplitter {

	private static final Logger logger = Logger.getLogger(GMeansClusterSplitter.class);
	
	private TupleList tuples;
	private GMeansParams params;
	
	public GMeansClusterSplitter(TupleList tuples, GMeansParams params) {
		if (tuples == null || params == null) {
			throw new NullPointerException();
		}
		this.tuples = tuples;
		this.params = params;
	}
	
	@Override
	public boolean prefersSplit(Cluster origCluster, List<Cluster> splitClusters) {
		return !TupleMath.andersonDarlingGaussianTest(projectToLineBetweenChildren(
				origCluster, splitClusters));
	}

	@Override
	public List<Cluster> performSplit(Cluster cluster) {
		TupleList seeds = createTwoSeeds(cluster);
		return runLocalKMeans(cluster, seeds);
	}

	/**
     * Projects the data in a cluster to the line connecting its two children's
     * centers.
     */
    private double[] projectToLineBetweenChildren(Cluster cluster,
            Collection<Cluster> children) {
        double[] projectedData = null;
        if (children.size() == 2) {
            Iterator<Cluster> it = children.iterator();
            double[] center1 = it.next().getCenter();
            double[] center2 = it.next().getCenter();
            int dim = center1.length;
            double[] projection = new double[dim];
            for (int i = 0; i < dim; i++) {
                projection[i] = center1[i] - center2[i];
            }
            projectedData = projectToVector(cluster, projection);
        }
        return projectedData;
    }
    
    /**
     * Projects all data in a cluster to one dimension, via the dot product with
     * a projection vector.
     */
    private double[] projectToVector(Cluster cluster, double[] projection) {
        int n = cluster.getMemberCount();
        int dim = tuples.getTupleLength();
        double[] projectedData = new double[n];
        double[] coords = new double[dim];
        for (int i = 0; i < n; i++) {
            tuples.getTuple(cluster.getMember(i), coords);
            projectedData[i] = TupleMath.dotProduct(coords, projection);
        }
        return projectedData;
    }

    /**
     * Create two cluster seeds by going +/- one standard deviation from the
     * cluster's center.
     * 
     * @return TupleList containing two seeds
     */
    private TupleList createTwoSeeds(Cluster cluster) {
        
    	int dim = tuples.getTupleLength();

        double[][] stats = ClusterStats.computeMeanAndVariance(tuples, cluster);

        TupleList seeds = new ArrayTupleList(dim, 2);

        double[] seed1 = new double[dim];
        double[] seed2 = new double[dim];
        
        for (int i = 0; i < dim; i++) {
            double center = stats[i][0];
            double sdev = Math.sqrt(stats[i][1]);
            seed1[i] = center - sdev;
            seed2[i] = center + sdev;
        }
        
        seeds.setTuple(0, seed1);
        seeds.setTuple(1, seed2);

        return seeds;
    }

    protected List<Cluster> runLocalKMeans(Cluster cluster, TupleList seeds) {

        FilteredTupleList fcs = new FilteredTupleList(cluster.getMembers().toArray(), tuples);
        
        KMeansParams kparams = new KMeansParams.Builder()
        	.clusterCount(seeds.getTupleCount())
        	.maxIterations(Integer.MAX_VALUE)
        	.movesGoal(0)
        	.workerThreadCount(1)
        	.replaceEmptyClusters(false)
        	.distanceMetric(params.getDistanceMetric())
        	.clusterSeeder(new PreassignedSeeder(seeds)).
        	build();
        
        KMeansClusterer kmeans = new KMeansClusterer(fcs, kparams);
        kmeans.run();
        
        List<Cluster> clusters;
		try {
			clusters = kmeans.get();
		} catch (Exception e) {
			logger.error("error splitting cluster", e);
			return null;
		}
        
        int n = clusters.size();
        List<Cluster> clusterList = new ArrayList<Cluster>(n);
        for (int i = 0; i < n; i++) {
            Cluster c = clusters.get(i);
            int memCount = c.getMemberCount();
            int[] indexes = new int[memCount];
            for (int j=0; j<memCount; j++) {
            	indexes[j] = fcs.getFilteredIndex(c.getMember(j));
            }
            clusterList.add(new Cluster(indexes, c.getCenter()));
        }

        return clusterList;
    }

}
