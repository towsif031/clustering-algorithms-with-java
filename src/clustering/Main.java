package clustering;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        String pathPrefix = "F:\\ThesisWorks\\Datasets\\90_10\\";
        String inputPathPrefix = "F:\\ThesisWorks\\Datasets\\90_10\\";
        String outputPathPrefix = "F:\\ThesisWorks\\Datasets\\90_10\\";
        ClusteringAlgos ca = new ClusteringAlgos(pathPrefix, inputPathPrefix, outputPathPrefix);
        ca.takeTrainData();
        ca.calculateDistance();

        ca.K_MeansClustering();
        ca.K_MedoidsClustering();
        ////ca.DBSCANClustering();
        ////ca.MeanShiftClustering();
        ca.DivisiveClustering();
        ca.SingleLinkageClustering();
        ca.CompleteLinkageClustering();
        ca.AverageLinkageClustering();
    }
}
