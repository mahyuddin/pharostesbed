package pharoslabut.logger.analyzer.cbl.rssidist;

/**
 * Performs linear interpolation to convert RSSI measurements into distance
 * for Mission 15, Experiment 9.
 * 
 * From the plot of average distance vs. RSSI, we see that any RSSI reading less than about -86dBm
 * has very poor correlation with distance.
 * 
 * @author Chien-Liang Fok
 * @see http://pharos.ece.utexas.edu/wiki/index.php/Mission_15_-_February_15,_2011_-_Dell_Diamond#Experiment_9
 */
public class RSSIToDistM15Exp9All extends RSSItoDist {
	
	/**
	 * This two-dimensional array contains the mapping between 
	 * RSSI and distance.
	 */
	static double[][] dataTable = {
			{2.705427186,	-51.91609036},
			{8.165026428,	-67.39759036},
			{12.77426567,	-72.33022388},
			{17.44822566,	-77.08044383},
			{22.59823366,	-79.47840532},
			{27.40954091,	-82.37201365},
			{32.49723747,	-83.91745283},
			{37.27787083,	-85.05109489},
			{42.50559412,	-86.19905213},
			{46.96632652,	-87.65517241}};
	
	public RSSIToDistM15Exp9All() {
		super(dataTable);
	}
	
//	public static void main(String[] args) {
//		RSSIToDistM15Exp9All t = new RSSIToDistM15Exp9All();
//		System.out.println("RSSI\tDist");
//		for (int rssi = -50; rssi > -90; rssi--) {
//			System.out.println(rssi + "\t" + t.getDist(rssi));
//		}
//	}
}
