package clustering;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClusteringAlgos {
    int mxmid = 3953;   // actually 3952
    int mxuid = 6041;   // actually 6040

    List < List < Integer >> userCluster = new ArrayList < List < Integer >> (mxuid);
    List < List < Integer >> userClusterTest = new ArrayList < List < Integer >> (mxuid);
    List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);

    double[][] rat = new double[mxuid][mxmid];
    double[][] Rat = new double[mxuid][mxmid];
    double[][] diff = new double[mxuid][mxuid];

    String inputPathPrefix, outputPathPrefix, prefix;
    ArrayList < Integer > clusterCentroids;

    ClusteringAlgos(String prefix, String inPrefix, String outPrefix) {
        this.prefix = prefix;
        this.inputPathPrefix = inPrefix;
        this.outputPathPrefix = outPrefix;
        init();
    }

    void init() {
        for (int i = 0; i < mxuid; i++) {
            userCluster.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxuid; i++) {
            userClusterTest.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }
    }

    // Reading the train.csv file
    public void takeTrainData() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(inputPathPrefix + "train.csv"));
        String text;
        String[] cut;
        int uid = 0, mid = 0, r = 0, t = 0;
        while ((text = in .readLine()) != null) {
            cut = text.split(",");
            uid = Integer.parseInt(cut[0]);
            mid = Integer.parseInt(cut[1]);
            r = Integer.parseInt(cut[2]);
            t = Integer.parseInt(cut[3]);
            // System.out.println(uid+" + "+ mid +" + "+ r +" + "+t);
            rat[uid][mid] = r;

            userCluster.get(uid).add(mid);
        }
        // Reading the test.csv file
        in = new BufferedReader(new FileReader(inputPathPrefix + "test.csv"));
        while ((text = in .readLine()) != null) {
            cut = text.split(",");
            uid = Integer.parseInt(cut[0]);
            mid = Integer.parseInt(cut[1]);
            r = Integer.parseInt(cut[2]);
            t = Integer.parseInt(cut[3]);
            //System.out.println(uid+" + "+ mid +" + "+ r +" + "+t);
            Rat[uid][mid] = r;
            userClusterTest.get(uid).add(mid);
        }
    }

    // Normalizing values
    double normalize(double val) {
        double new_min = 0.2;
        double new_max = 1;
        double minVal = 1;
        double maxVal = 5;
        double newVal = 0;
        newVal = ((val - minVal) * (new_max - new_min)) / (maxVal - minVal) + new_min;
        return newVal;
    }

    // Distance Calculator
    void calculateDistance() throws IOException {
        File file = new File("F:/ThesisWorks/Datasets/90_10/userDiffs.csv");
        boolean exists = file.exists();
        if (exists) {
            System.out.println("[userDiffs.csv] File Exist!");
            BufferedReader in = new BufferedReader(new FileReader(inputPathPrefix + "userDiffs.csv"));
            String text;
            String[] cut;
            int u = 0, v = 0;
            while ((text = in .readLine()) != null) {
                cut = text.split(",");
                u = Integer.parseInt(cut[0]);
                v = Integer.parseInt(cut[1]);
                diff[u][v] = Double.parseDouble(cut[2]);
                System.out.println("Distance between " + u + " and " + v + " is " + diff[u][v]);
            }
        } else {
            System.out.println("[userDiffs.csv] File Does Not Exist!");
            PrintWriter out = new PrintWriter(new FileWriter(outputPathPrefix + "userDiffs.csv"));
            for (int u = 1; u < mxuid; u++) {
                for (int v = 1; v < mxuid; v++) {
                    List < Integer > userList = userCluster.get(u);
                    int itemSize = userList.size();
                    int commonCounter = 0;
                    for (int movieIndex = 0; movieIndex < itemSize; movieIndex++) {
                        int movieId = userList.get(movieIndex);
                        if (rat[v][movieId] != 0) {
                            commonCounter++;
                            //  System.out.println("movieId: "+ movieId); // common movie they both watched
                            diff[u][v] += Math.abs(normalize(rat[u][movieId]) - normalize(rat[v][movieId]));
                        }
                    }
                    if (commonCounter != 0) {
                        diff[u][v] = diff[u][v] / commonCounter;
                    } else {
                        diff[u][v] = 1;
                    }
                    // save in file
                    out.println(u + "," + v + "," + diff[u][v]);
                    out.flush();

                    System.out.println("Diff of " + u + " & " + v + " = " + diff[u][v]);
                }
            }
            out.close();
        }
    }

    // Display distance between 2 user objects
    void distanceCalculator(int u, int v) {
        System.out.println("Distance between " + u + " and " + v + " is " + diff[u][v]);
    }

    // Choose random 1 centroid from every 100 objects orderly. 6040 users So, K = 61
    ArrayList < Integer > randomInRange(ArrayList < Integer > clusterCentroids) {
        Random r = new Random();
        for (int userId = 1; userId + 100 < mxuid; userId = userId + 100) {
            int Low = userId;
            int High = userId + 100;
            int Result = r.nextInt(High - Low + 1) + Low; // rand.nextInt((max - min) + 1) + min;
            clusterCentroids.add(Result);
        }
        // As, 6001-6040 there is only 40 objects
        int Low = 6001;
        int High = 6040;
        int Result = r.nextInt(High - Low + 1) + Low;
        clusterCentroids.add(Result);

        return clusterCentroids;
    }

    // Choose K unique centroids randomly from the dataset. Here K = 61
    ArrayList < Integer > uniqueRandomInRange(ArrayList < Integer > clusterCentroids) {
        int numofCluster = 61;
        Random rand = new Random();
        while (numofCluster > 0) {
            int n = rand.nextInt(6040 - 1 + 1) + 1; // rand.nextInt((max - min) + 1) + min;
            if (clusterCentroids.size() == 0) {
                clusterCentroids.add(n);
                numofCluster--;
            } else {
                boolean alreadyIncluded = false;
                for (int j = 0; j < clusterCentroids.size(); j++) {
                    if (clusterCentroids.get(j) == n) {
                        alreadyIncluded = true;
                        break;
                    }
                }
                if (alreadyIncluded == false) {
                    clusterCentroids.add(n);
                    numofCluster--;
                }
            }
        }
        return clusterCentroids;
    }

    // Display cluster centroids
    void displayClusterCentroids() {
        System.out.println();
        System.out.println("Centroids of clusters:");
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | " + centroid);
        }
    }

    // Display Clusters
    void displayClusters() {
        System.out.println();
        System.out.println("Clusters:");
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + centroid); // displays centroid
            for (int p = 0; p < arrayListofClusters.get(centroid).size(); p++) {
                System.out.print(arrayListofClusters.get(centroid).get(p) + ", ");
            }
            System.out.println("\n total objects: " + arrayListofClusters.get(centroid).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
    }

    // ============================================================ //
    // K-Means Clustering
    // ============================================================ //
    void K_MeansClustering() throws FileNotFoundException, IOException {
        clusterCentroids = new ArrayList < Integer > ();
        // Choosing 1 centroid from every 100 objects orderly.
        clusterCentroids = randomInRange(clusterCentroids);

        // Display randomly choosen centroids (K = 61)
        displayClusterCentroids();

        //  Populate each cluster with closest objects to its centroid
        for (int i = 1; i < mxuid; i++) { // i = current item
            // Check if item itself is centroid
            // Because sometimes 2 users' diff may be 0.0
            boolean isCentroid = false;
            for (int j = 0; j < clusterCentroids.size(); j++) {
                int centroid = clusterCentroids.get(j);
                if (i == centroid) { // If item itself is centroid
                    arrayListofClusters.get(i).add(i); // Add centroid to its own cluster
                    isCentroid = true;
                    break;
                }
            }

            if (!isCentroid) {
                double tempMax = 10000;
                int tempCentroid = 0;
                double distance = 0;

                // finding nearest centroid to i
                for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 61
                    int currentCentroid = clusterCentroids.get(k);
                    distance = diff[i][currentCentroid];
                    if (distance < tempMax) {
                        tempMax = distance; // tempMax will contain the closest centroid distance from a object
                        tempCentroid = currentCentroid; // The closest centroid
                    }
                }

                arrayListofClusters.get(tempCentroid).add(i);
            }
        }

        // //// for debugging purpose
        // // Display initial clusters
        System.out.println("initial clusters:");
        //displayClusters();
        int totalObjects = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + clusterCentroids.get(i)); // displays centroid
            for (int j = 0; j < arrayListofClusters.get(centroid).size(); j++) {
                System.out.print(arrayListofClusters.get(centroid).get(j) + ", ");
                totalObjects++;
            }
            System.out.println("\n total objects in cluster: " + arrayListofClusters.get(centroid).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
        System.out.println("\n Total Objects: " + totalObjects);

        // Iterations of finding new centroids and populating
        ArrayList < Integer > newClusterCentroids;
        ArrayList < Integer > oldClusterCentroids;
        int iterator = 1;

        do {
            List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
            for (int i = 0; i < mxuid; i++) {
                arrayListofTempClusters.add(new ArrayList < Integer > ());
            }
            newClusterCentroids = new ArrayList < Integer > ();

            // Find new centroid from the cluster
            for (int i = 0; i < clusterCentroids.size(); i++) {
                int currentCentroid = clusterCentroids.get(i);
                double diffSum = 10000;
                int newCentroid = 0;
                for (int j = 0; j < arrayListofClusters.get(currentCentroid).size(); j++) { // arrayListofClusters.get(currentCentroid).size() = cluster size of current centroid
                    int currentItem = arrayListofClusters.get(currentCentroid).get(j); // current item of the cluster
                    double diffSumTemp = 0;
                    for (int k = 0; k < arrayListofClusters.get(currentCentroid).size(); k++) {
                        int nextItem = arrayListofClusters.get(currentCentroid).get(k); // next item of the cluster
                        diffSumTemp += diff[currentItem][nextItem];
                    }

                    if (diffSumTemp < diffSum) {
                        diffSum = diffSumTemp; // store smallest diffSumTemp
                        newCentroid = currentItem; // store the item as new centroid
                    }
                }
                newClusterCentroids.add(newCentroid);
            }

            // storing clusterCentroids
            oldClusterCentroids = clusterCentroids;

            // For new centroids: Again populating each cluster with closest objects to its centroid
            for (int i = 1; i < mxuid; i++) { // i = current item
                // Check if item itself is centroid
                // Because sometimes 2 users' diff may be 0.0
                boolean isNewCentroid = false;
                for (int j = 0; j < newClusterCentroids.size(); j++) {
                    int centroid = newClusterCentroids.get(j);
                    if (i == centroid) { // If item itself is centroid
                        arrayListofTempClusters.get(i).add(i); // Add centroid to its own cluster
                        isNewCentroid = true;
                        break;
                    }
                }

                if (!isNewCentroid) {
                    double tempMax = 10000;
                    int tempCentroid = 0;
                    double distance = 0;

                    // for newCentroids, finding nearest centroid to i
                    for (int k = 0; k < newClusterCentroids.size(); k++) { // Here, newClusterCentroids.size() = 61
                        int currentCentroid = newClusterCentroids.get(k);
                        distance = diff[i][currentCentroid];
                        if (distance < tempMax) {
                            tempMax = distance; // tempMax will contain the closest centroid distance from a object
                            tempCentroid = currentCentroid; // The closest centroid
                        }
                    }

                    arrayListofTempClusters.get(tempCentroid).add(i);
                }
            }

            // updating arrayListofClusters
            arrayListofClusters = arrayListofTempClusters;
            clusterCentroids = newClusterCentroids;
            System.out.println("Current Iteration Number : " + iterator);
            iterator++;
        } while (!newClusterCentroids.equals(oldClusterCentroids)); // k-means convergence

        System.out.println();
        System.out.println("=========================================================================");
        System.out.println("For New Clusters:");
        System.out.println("=========================================================================\n");

        displayClusterCentroids();

        System.out.println();
        System.out.println("+++++++++++++++++++++++++++++");
        System.out.println("Final Clusters after K-Means:");
        System.out.println("+++++++++++++++++++++++++++++");
        System.out.println();

        totalObjects = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + clusterCentroids.get(i)); // displays centroid
            for (int j = 0; j < arrayListofClusters.get(centroid).size(); j++) {
                System.out.print(arrayListofClusters.get(centroid).get(j) + ", ");
                totalObjects++;
            }
            System.out.println("\n total objects in cluster: " + arrayListofClusters.get(centroid).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
        System.out.println("\n Total Objects: " + totalObjects);
    }


    // ============================================================ //
    // K-Medoids Clustering
    // ============================================================ //
    void K_MedoidsClustering() throws FileNotFoundException, IOException {
        boolean[] isUsedCentroid = new boolean[mxuid];

        for (int i = 1; i < mxuid; i++) {
            isUsedCentroid[i] = false;
        }

        clusterCentroids = new ArrayList < Integer > (); // Arraylist of initial centroids

        // Find 61 random centroids (K) within dataset
        clusterCentroids = uniqueRandomInRange(clusterCentroids);

        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            isUsedCentroid[centroid] = true; // flag for already used as centroid
        }

        // // Display initial K-Medoids centroids
        // displayClusterCentroids();

        //  Populate each cluster with closest objects to its centroid
        for (int i = 1; i < mxuid; i++) { // i = current item
            // Check if item itself is centroid
            // Because sometimes 2 users' diff may be 0.0
            boolean isCentroid = false;
            for (int j = 0; j < clusterCentroids.size(); j++) {
                int centroid = clusterCentroids.get(j);
                if (i == centroid) { // If item itself is centroid
                    arrayListofClusters.get(i).add(i); // Add centroid to its own cluster
                    isCentroid = true;
                    break;
                }
            }

            if (!isCentroid) {
                double tempMax = 10000;
                int tempCentroid = 0;
                double distance = 0;

                // finding nearest centroid to i
                for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 61
                    int currentCentroid = clusterCentroids.get(k);
                    distance = diff[i][currentCentroid];
                    if (distance < tempMax) {
                        tempMax = distance; // tempMax will contain the closest centroid distance from a object
                        tempCentroid = currentCentroid; // The closest centroid
                    }
                }

                arrayListofClusters.get(tempCentroid).add(i);
            }
        }

        // Display initial clusters
        displayClusters();

        // Calculate total cost of initial clusters
        double totalCostofCurrentInitCluster = 0;
        double totalCostofInitClusters = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int currentCentroid = clusterCentroids.get(i);
            for (int p = 0; p < arrayListofClusters.get(currentCentroid).size(); p++) {
                int currentItem = arrayListofClusters.get(currentCentroid).get(p);
                totalCostofCurrentInitCluster += diff[currentCentroid][currentItem];
            }
            totalCostofInitClusters += totalCostofCurrentInitCluster;
        }

        System.out.println("Total Cost of Initial Clusters: " + totalCostofInitClusters);

        double oldCost = totalCostofInitClusters; // Saving initial total cost

        //================
        // Iterations for Finding best Medoids
        //================
        int iterationNum = 1;

        boolean allUsedAsCentroid = false;
        while (!allUsedAsCentroid) {
            System.out.println("iteration: " + iterationNum);

            List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
            for (int i = 0; i < mxuid; i++) {
                arrayListofTempClusters.add(new ArrayList < Integer > ());
            }

            // Randomly select a centroid to remove from clusterCentroids array
            Random randRem = new Random();
            int randomSelectedCentroidIndex = randRem.nextInt(clusterCentroids.size());
            int randomSelectedCentroid = clusterCentroids.get(randomSelectedCentroidIndex);
            //////System.out.println("Delete centroid: " + randomSelectedCentroid);

            // New clusterCentroids after removing randomly choosen centroid
            ArrayList < Integer > tempClusterCentroids = new ArrayList < Integer > ();
            for (int i = 0; i < clusterCentroids.size(); i++) { // Here, clusterCentroids.size() = 61
                int currentCentroid = clusterCentroids.get(i);
                if (currentCentroid != randomSelectedCentroid) {
                    tempClusterCentroids.add(currentCentroid);
                }
            }

            // //// for debugging purpose
            // // Display tempCentroids
            // System.out.println("tempCentroids of clusters after deleting a random centroid: ");
            // for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //     System.out.println((i + 1) + "| " + tempClusterCentroids.get(i));
            // }

            // Randomly selected a new unique centroid from all other users
            Random randSel = new Random();
            int newRandomCentroid = 0;

            boolean isUnique = false;
            while (!isUnique) {
                newRandomCentroid = randSel.nextInt(6040 - 2 + 1) + 2; // rand.nextInt((max - min) + 1) + min;
                if (!isUsedCentroid[newRandomCentroid]) {
                    isUsedCentroid[newRandomCentroid] = true;
                    isUnique = true;
                }
            }

            // System.out.println("New centroid to be added: " + newRandomCentroid);

            // Add new centroid to tempClusterCentroids
            tempClusterCentroids.add(newRandomCentroid);

            // //// for debugging purpose
            // // Display tempCentroids
            // System.out.println("tempCentroids of clusters after adding a random user as centroid: ");
            // for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //     System.out.println((i + 1) + "| " + tempClusterCentroids.get(i));
            // }

            //  Populate each cluster with closest objects to its centroid
            for (int i = 1; i < mxuid; i++) { // i = current item
                // Check if item itself is centroid
                // Because sometimes 2 users' diff may be 0.0
                boolean isNewCentroid = false;
                for (int j = 0; j < tempClusterCentroids.size(); j++) {
                    int centroid = tempClusterCentroids.get(j);
                    if (i == centroid) { // If item itself is centroid
                        arrayListofTempClusters.get(i).add(i); // Add centroid to its own cluster
                        isNewCentroid = true;
                        break;
                    }
                }

                if (!isNewCentroid) {
                    double tempMax = 10000;
                    int tempCentroid = 0;
                    double distance = 0;

                    // finding nearest centroid to i
                    for (int k = 0; k < tempClusterCentroids.size(); k++) { // Here, tempClusterCentroids.size() = 61
                        int currentCentroid = tempClusterCentroids.get(k);
                        distance = diff[i][currentCentroid];
                        if (distance < tempMax) {
                            tempMax = distance; // tempMax will contain the closest centroid distance from a object
                            tempCentroid = currentCentroid; // The closest centroid
                        }
                    }

                    arrayListofTempClusters.get(tempCentroid).add(i);
                }
            }

            // //// for debugging purpose
            // // Display objects of temp clusters
            // System.out.println("tempClusters after swapping a centroid first time:");
            // for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //     int centroid = tempClusterCentroids.get(i);
            //     System.out.println((i + 1) + " | centroid: " + tempClusterCentroids.get(i)); // displays centroid
            //     for (int p = 0; p < arrayListofTempClusters.get(centroid).size(); p++) {
            //         System.out.print(arrayListofTempClusters.get(centroid).get(p) + ", ");
            //     }
            //     System.out.println("\n total objects: " + arrayListofTempClusters.get(centroid).size()); // displays total objects
            //     System.out.println();
            //     System.out.println("================================");
            // }

            // Calculate total cost of tempClusters
            double totalCostofCurrentTempCluster = 0;
            double totalCostofAllTempClusters = 0;
            for (int i = 0; i < tempClusterCentroids.size(); i++) {
                int currentCentroid = tempClusterCentroids.get(i);
                for (int p = 0; p < arrayListofTempClusters.get(currentCentroid).size(); p++) {
                    int currentItem = arrayListofTempClusters.get(currentCentroid).get(p);
                    totalCostofCurrentTempCluster += diff[currentCentroid][currentItem];
                }
                totalCostofAllTempClusters += totalCostofCurrentTempCluster;
            }

            // total cost of with new centroid
            System.out.println("Total Cost of Temp Clusters: " + totalCostofAllTempClusters + " for iteration no: " + iterationNum);

            double newCost = totalCostofAllTempClusters;

            double s = newCost - oldCost;
            if (s < 0) {
                clusterCentroids = tempClusterCentroids;
                arrayListofClusters = arrayListofTempClusters;
                oldCost = newCost;
            }

            // check if all users are used centroid
            int usedAsCentroidCount = 0;
            for (int i = 1; i < mxuid; i++) {
                if (!isUsedCentroid[i]) { // if any user not yet used as centroid
                    usedAsCentroidCount = 1;
                    break;
                }
            }

            if (usedAsCentroidCount == 0) {
                allUsedAsCentroid = true;
            }

            iterationNum++;
        }

        System.out.println("+++++++++++++++++++++++++++++++");
        System.out.println("Final Clusters after K-Medoids:");
        System.out.println("+++++++++++++++++++++++++++++++");
        displayClusterCentroids();
        displayClusters();
        System.out.println("Final Cost: " + oldCost);
    }


    /// ============================================================ //
    // DBSCAN Clustering
    // ============================================================ //
    void DBSCANClustering() throws FileNotFoundException, IOException {
        double eps = 0.05; // minimum epsilon
        int minPts = 50; // minimum number of points
        boolean[] flagForVisited = new boolean[mxuid]; // Mark all object as unvisited
        boolean[] isInCluster = new boolean[mxuid];
        boolean[] isNoise = new boolean[mxuid];

        for (int i = 1; i < mxuid; i++) { // Mark all object as unvisited
            flagForVisited[i] = false;
        }

        for (int i = 1; i < mxuid; i++) { // Mark all object as is not in any cluster
            isInCluster[i] = false;
        }

        for (int i = 1; i < mxuid; i++) { // Mark all object as not noise
            isNoise[i] = false;
        }

        ArrayList < Integer > neighborObjects = new ArrayList < Integer > (); // candidate set N
        ArrayList < Integer > neighborObjectsOfp = new ArrayList < Integer > ();
        ArrayList < Integer > coreObjects = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        for (int i = 1; i < mxuid; i++) {
            if (flagForVisited[i] == false) {
                flagForVisited[i] = true; // Mark i as visited

                for (int j = 1; j < mxuid; j++) {
                    if (diff[i][j] <= eps) {
                        neighborObjects.add(j); // N
                    }
                }

                if (neighborObjects.size() >= minPts) {
                    coreObjects.add(i); // i is a core object
                    isInCluster[i] = true;
                    arrayListofClusters.get(i).add(i);

                    for (int k = 0; k < neighborObjects.size(); k++) {
                        int p = neighborObjects.get(k);

                        if (isInCluster[p] == false) {
                            isInCluster[p] = true;
                            arrayListofClusters.get(i).add(p);
                        }

                        if (flagForVisited[p] == false) {
                            flagForVisited[p] = true;
                            //arrayListofClusters.get(i).add(p);
                            for (int l = 1; l < mxuid; l++) {
                                if (diff[p][l] <= eps) {
                                    neighborObjectsOfp.add(l);
                                }
                            }

                            // add neighborhood points of p to neighborObjects
                            if (neighborObjectsOfp.size() >= minPts) {
                                for (int m = 0; m < neighborObjectsOfp.size(); m++) {
                                    int n = neighborObjectsOfp.get(m);
                                    //neighborObjects.add(n); ////// may add duplicates

                                    if (isInCluster[n] == false) {
                                        isInCluster[n] = true;
                                        neighborObjects.add(n);
                                        arrayListofClusters.get(i).add(n);
                                    }
                                }
                            } else {
                                isNoise[p] = true;
                            }

                            // empty neighborObjectsOfp arraylist
                            neighborObjectsOfp.clear();
                        }
                    }

                    //clusterPosition++;
                } else {
                    isNoise[i] = true;
                }

                // empty neighborObjects arraylist
                neighborObjects.clear();
            }
        }

        // Display clusters
        //System.out.println("Clusters after DBSCAN with coreObjects: ");
        for (int i = 0; i < coreObjects.size(); i++) {
            System.out.println((i + 1) + " | Core object : " + coreObjects.get(i));
            // for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
            //     System.out.print(arrayListofClusters.get(i).get(j) + ", ");
            // }
            // System.out.println("\n total objects in the cluster: " + arrayListofClusters.get(i).size());
            // System.out.println();
            // System.out.println("================================");
        }

        // Display clusters
        System.out.println("Clusters after DBSCAN:");
        int totalClusters = 0;
        for (int i = 0; i < arrayListofClusters.size(); i++) {
            if (arrayListofClusters.get(i).size() > 0) {
                for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
                totalClusters++;
            }
        }
        System.out.println("\n total clusters: " + totalClusters);
    }


    // ============================================================ //
    // Mean Shift Clustering
    // ============================================================ //
    void MeanShiftClustering() throws FileNotFoundException, IOException {
        double radius = 0.05; // radius
        boolean[] flagForVisited = new boolean[mxuid];
        for (int i = 1; i < mxuid; i++) { // Mark all object as unvisited
            flagForVisited[i] = false;
        }

        ArrayList < Integer > coreObjects = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        int newCentroid = 0;
        int oldCentroid = 0;

        for (int i = 1; i < mxuid; i++) {
            if (flagForVisited[i] == false) {
                flagForVisited[i] = true;

                newCentroid = i;

                coreObjects.add(i);

                // int coreObjectPosition = 0; // coreObject position in arraylist
                // for (int q = 0; q < coreObjects.size(); q++) {
                //     int matchCoreObject = coreObjects.get(q);
                //     if (matchCoreObject == i) {
                //         coreObjectPosition = q;
                //         break;
                //     }
                // }

                //add i to final cluster
                arrayListofClusters.get(i).add(i);

                // initial tempCluster
                ArrayList < Integer > tempCluster = new ArrayList < Integer > ();

                for (int j = 1; j < mxuid; j++) {
                    if (diff[i][j] <= radius) {
                        tempCluster.add(j);
                    }
                }

                do {
                    oldCentroid = newCentroid;
                    //  find mean object (newCentroid)
                    double diffSum = 1000000;

                    for (int k = 0; k < tempCluster.size(); k++) {
                        int currentItem = tempCluster.get(k); // current item of the cluster
                        double diffSumTemp = 0;
                        for (int l = 0; l < tempCluster.size(); l++) {
                            int tempItem = tempCluster.get(l); // next item of the cluster
                            diffSumTemp += diff[currentItem][tempItem];
                        }

                        if (diffSumTemp < diffSum) {
                            diffSum = diffSumTemp; // store smallest diffSumTemp
                            newCentroid = currentItem; // store the item as new centroid
                        }
                    }

                    // if new mean object (centroid) was not visitied before, then add to cluster
                    if (flagForVisited[newCentroid] == false) {
                        flagForVisited[newCentroid] = true;

                        //add to final cluster
                        arrayListofClusters.get(i).add(newCentroid);

                        ////// for debugging purpose
                        System.out.println("newCentroid / new mean: " + newCentroid);

                        //form cluster
                        ArrayList < Integer > newTempCluster = new ArrayList < Integer > ();
                        for (int m = 1; m < mxuid; m++) {
                            if (diff[newCentroid][m] <= radius) {
                                newTempCluster.add(m);
                            }
                        }

                        tempCluster = newTempCluster;

                        // // empty newTempCluster
                        // newTempCluster.clear();
                    } else { // if newCentroid is a previously visited centroid

                        // searching the coreObject position of the oldCentroid
                        int coreObjectPositionOfOldCentroid = 0; // cluster position in arraylist

                        loopForOldCentroid:
                        for (int n = 0; n < coreObjects.size(); n++) {
                            for (int p = 0; p < arrayListofClusters.get(n).size(); p++) {
                                int matchCentroid = arrayListofClusters.get(n).get(p);
                                if (matchCentroid == oldCentroid) {
                                    coreObjectPositionOfOldCentroid = n;
                                    break loopForOldCentroid;
                                }
                            }
                        }

                        // deleting coreObject of the previously visited centroid from coreObject arraylist
                        coreObjects.remove(coreObjectPositionOfOldCentroid);

                        // searching the coreObject position of the newCentroid (previously visited centroid)
                        int coreObjectPositionOfVisitedCentroid = 0;

                        loopForNewCentroid:
                        for (int n = 0; n < coreObjects.size(); n++) {
                            for (int p = 0; p < arrayListofClusters.get(n).size(); p++) {
                                int matchCentroid = arrayListofClusters.get(n).get(p);
                                if (matchCentroid == newCentroid) {
                                    coreObjectPositionOfVisitedCentroid = n;
                                    break loopForNewCentroid;
                                }
                            }
                        }

                        // adding all objects of the old cluster to the appropriate cluster
                        for (int r = 0; r < arrayListofClusters.get(coreObjectPositionOfOldCentroid).size(); r++) {
                            int s = arrayListofClusters.get(coreObjectPositionOfOldCentroid).get(r);
                            arrayListofClusters.get(coreObjectPositionOfVisitedCentroid).add(s);

                        }

                        // deleting the cluster of oldCentroid (old cluster)
                        arrayListofClusters.remove(coreObjectPositionOfOldCentroid);
                    }
                } while (newCentroid != oldCentroid);

                // // empty tempCluster
                // tempCluster.clear();
            }
        }

        System.out.println("Clusters after MeanShift:");
        for (int i = 0; i < coreObjects.size(); i++) {
            System.out.println((i + 1) + " | Core point : " + coreObjects.get(i));
            for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
                System.out.print(arrayListofClusters.get(i).get(j) + ", ");
            }
            System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
    }


    // ============================================================ //
    // Divisive Clustering
    // ============================================================ //
    void DivisiveClustering() throws FileNotFoundException, IOException {

        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofTempClusters.add(new ArrayList < Integer > ());
        }

        ArrayList < Integer > tempClusterX = new ArrayList < Integer > ();
        ArrayList < Integer > tempClusterY = new ArrayList < Integer > ();

        // finding 2 most furthest users in the cluster
        double maxDistance = 0;
        double distance = 0;
        int x = 0;
        int y = 0;
        for (int i = 1; i < mxuid; i++) {
            for (int j = i + 1; j < mxuid; j++) {
                distance = diff[i][j];
                if (distance > maxDistance) {
                    maxDistance = distance;
                    x = i;
                    y = j;
                }
            }
        }

        ////// for debugging purpose
        System.out.println("2 most furthest users are: " + x + " and " + y);

        for (int i = 1; i < mxuid; i++) {
            if (diff[i][x] <= diff[i][y]) {
                arrayListofTempClusters.get(x).add(i);
            } else {
                arrayListofTempClusters.get(y).add(i);
            }
        }

        ////// for debugging purpose
        // Display tempClusters
        System.out.println("Clusters after first div:");
        int initClusterCounter = 1;
        for (int i = 0; i < arrayListofTempClusters.size(); i++) {
            if (arrayListofTempClusters.get(i).size() > 0) {
                System.out.println("Cluster #" + initClusterCounter);
                initClusterCounter++;
                for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                    System.out.print(arrayListofTempClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofTempClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
            }
        }

        //==============
        // iterations
        //==============
        //  number of clusters will be max iterations + 1
        for (int iterator = 1; iterator < 60; iterator++) { // we will get 61 clusters
            maxDistance = 0;
            int clusterToDiv = 0; // the cluster where we found the 2 most furthest users. So we can devide that

            for (int i = 0; i < arrayListofTempClusters.size(); i++) { // finding in all clusters
                // finding 2 most furthest users in current cluster
                for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                    int m = arrayListofTempClusters.get(i).get(j);
                    for (int k = j + 1; k < arrayListofTempClusters.get(i).size(); k++) {
                        int n = arrayListofTempClusters.get(i).get(k);
                        distance = diff[m][n];
                        if (distance > maxDistance) {
                            maxDistance = distance;
                            x = m;
                            y = n;
                            clusterToDiv = i;
                        }
                    }
                }
            }

            ////// for debugging purpose
            System.out.println("2 most furthest users are: " + x + " and " + y);

            for (int i = 0; i < arrayListofTempClusters.get(clusterToDiv).size(); i++) {
                int o = arrayListofTempClusters.get(clusterToDiv).get(i);
                if (diff[o][x] <= diff[o][y]) {
                    tempClusterX.add(o);
                } else {
                    tempClusterY.add(o);
                }
            }

            ////// for debugging purpose
            System.out.println("tempClusterX, tempClusterY completed.");

            // empty the arraylist where x and y was.
            arrayListofTempClusters.get(clusterToDiv).clear();

            ////// for debugging purpose
            System.out.println("empty parent for avoiding duplicate");

            // adding to own clusters
            arrayListofTempClusters.get(x).addAll(tempClusterX);
            // empty tempClusterX
            tempClusterX.clear();
            // adding to own clusters
            arrayListofTempClusters.get(y).addAll(tempClusterY);
            // empty tempClusterY
            tempClusterY.clear();

            ////// for debugging purpose
            System.out.println("empty tempClusterX, tempClusterY");

            ////// for debugging purpose
            // Display tempClusters after iteration
            System.out.println("Clusters after iteration #" + iterator + " :");
            int tempClusterCounter = 1;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    System.out.println("Cluster #" + tempClusterCounter);
                    tempClusterCounter++;
                    for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                        System.out.print(arrayListofTempClusters.get(i).get(j) + ", ");
                    }
                    System.out.println("\n total objects: " + arrayListofTempClusters.get(i).size()); // displays total objects
                    System.out.println();
                    System.out.println("================================");
                }
            }
        }

        arrayListofClusters = arrayListofTempClusters;
    }


    // ============================================================ //
    // Agglomerative Clustering
    // ============================================================ //
    //---------------------------//
    // Single-linkage clustering //
    //---------------------------//
    void SingleLinkageClustering() throws FileNotFoundException, IOException {
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofTempClusters.add(new ArrayList < Integer > ());
        }

        // Initially creating separate clusters for each user
        for (int i = 1; i < mxuid; i++) {
            arrayListofTempClusters.get(i).add(i);
        }

        int numOfClusters = 10000;
        while (numOfClusters > 61) { // to get 61 clusters
            double minDistance = 10000;
            double distance = 0;
            int x = 0;
            int y = 0;
            int clusterPositionX = 0;
            int clusterPositionY = 0;

            // finding 2 most nearest users of 2 different clusters
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                    int m = arrayListofTempClusters.get(i).get(j);
                    for (int p = i + 1; p < arrayListofTempClusters.size(); p++) {
                        for (int q = 0; q < arrayListofTempClusters.get(p).size(); q++) {
                            int n = arrayListofTempClusters.get(p).get(q);
                            distance = diff[m][n];
                            if (distance < minDistance) {
                                minDistance = distance;
                                x = m;
                                y = n;
                                clusterPositionX = i;
                                clusterPositionY = p;
                            }
                        }
                    }
                }
            }

            //// for debugging purpose
            System.out.println("----------------");
            System.out.println("x: " + x + " & y: " + y);

            // merging clusterPositionY in clusterPositionX
            arrayListofTempClusters.get(clusterPositionX).addAll(arrayListofTempClusters.get(clusterPositionY));

            // empty the cluster at clusterPositionY
            arrayListofTempClusters.get(clusterPositionY).clear();

            // to get desired number of clusters
            int clusterCounter = 0;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    clusterCounter++; // determines number of clusters
                }
            }
            //System.out.println(clusterCounter);
            numOfClusters = clusterCounter;
            System.out.println("Number of Clusters: " + numOfClusters);
        }

        arrayListofClusters = arrayListofTempClusters;
    }


    //-----------------------------//
    // Complete-linkage clustering //
    //-----------------------------//
    void CompleteLinkageClustering() throws FileNotFoundException, IOException {
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofTempClusters.add(new ArrayList < Integer > ());
        }

        // Initially creating separate clusters for each user
        for (int i = 1; i < mxuid; i++) {
            arrayListofTempClusters.get(i).add(i);
        }

        double distance = 0;
        // int tempX = 0;
        // int tempY = 0;
        // int x = 0;
        // int y = 0;
        int tempClusterPositionX = 0;
        int tempClusterPositionY = 0;
        int clusterPositionX = 0;
        int clusterPositionY = 0;

        int numOfClusters = 10000;
        while (numOfClusters > 6036) { // to get 61 clusters  //61
            double minOfMax = 10000;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                for (int p = i + 1; p < arrayListofTempClusters.size(); p++) { /////
                    // finding 2 most nearest users of 2 different clusters
                    double maxDistance = 0;
                    for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                        int m = arrayListofTempClusters.get(i).get(j);
                        for (int q = 0; q < arrayListofTempClusters.get(p).size(); q++) {
                            int n = arrayListofTempClusters.get(p).get(q);
                            distance = diff[m][n];
                            if (distance > maxDistance) {
                                maxDistance = distance;
                                // tempX = m;
                                // tempY = n;
                                tempClusterPositionX = i;
                                tempClusterPositionY = p;
                            }
                        }
                    }

                    //  findind 2 most nearest clusters.
                    //  2 clusters that contains the the minimum distance of the furthest users in between clusters
                    if (maxDistance < minOfMax) {
                        minOfMax = maxDistance;
                        // x = tempX;
                        // y = tempY;
                        clusterPositionX = tempClusterPositionX;
                        clusterPositionY = tempClusterPositionY;
                    }
                }
            }

            // merging clusterPositionY in clusterPositionX
            arrayListofTempClusters.get(clusterPositionX).addAll(arrayListofTempClusters.get(clusterPositionY));

            // for (int i = 0; i < arrayListofTempClusters.get(clusterPositionY).size(); i++) {
            //     int y = arrayListofTempClusters.get(clusterPositionY).get(i);
            //     arrayListofTempClusters.get(clusterPositionX).add(y);
            // }

            // empty the cluster at clusterPositionY
            arrayListofTempClusters.get(clusterPositionY).clear();

            // to get desired number of clusters
            int clusterCounter = 0;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    clusterCounter++; // determines number of clusters
                }
            }

            numOfClusters = clusterCounter;
            System.out.println("Number of Clusters: " + numOfClusters);
        }

        arrayListofClusters = arrayListofTempClusters;

        // Display clusters
        System.out.println("Final Clusters after Complete-Linkage:");
        int totalClusters = 0;
        for (int i = 0; i < arrayListofClusters.size(); i++) {
            if (arrayListofClusters.get(i).size() > 0) {
                for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
                totalClusters++;
            }
        }
        System.out.println("\n total clusters: " + totalClusters);
    }


    //----------------------------//
    // Average-linkage clustering //
    //----------------------------//
    void AverageLinkageClustering() throws FileNotFoundException, IOException {
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofTempClusters.add(new ArrayList < Integer > ());
        }

        // Initially creating separate clusters for each user
        for (int i = 1; i < mxuid; i++) {
            arrayListofTempClusters.get(i).add(i);
        }

        int tempClusterPositionX = 0;
        int tempClusterPositionY = 0;
        int clusterPositionX = 0;
        int clusterPositionY = 0;

        int numOfClusters = 10000;
        while (numOfClusters > 6036) { // to get 61 clusters  ///61
            double minOfAverage = 10000;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                for (int p = i + 1; p < arrayListofTempClusters.size(); p++) { /////
                    // finding 2 most nearest users of 2 different clusters
                    double averageDistance = 0;
                    double sumOfDistance = 0;
                    int r = arrayListofTempClusters.get(i).size();
                    int s = arrayListofTempClusters.get(p).size();
                    for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                        int m = arrayListofTempClusters.get(i).get(j);
                        //r++;
                        for (int q = 0; q < arrayListofTempClusters.get(p).size(); q++) {
                            int n = arrayListofTempClusters.get(p).get(q);
                            //s++;
                            sumOfDistance += diff[m][n];
                        }
                    }
                    tempClusterPositionX = i;
                    tempClusterPositionY = p;

                    averageDistance = sumOfDistance / (r * s);

                    //  findind 2 most nearest clusters.
                    //  2 clusters that contains the the minimum distance of the furthest users in between clusters
                    if (averageDistance < minOfAverage) {
                        minOfAverage = averageDistance;
                        // x = tempX;
                        // y = tempY;
                        clusterPositionX = tempClusterPositionX;
                        clusterPositionY = tempClusterPositionY;
                    }
                }
            }

            // merging clusterPositionY in clusterPositionX
            arrayListofTempClusters.get(clusterPositionX).addAll(arrayListofTempClusters.get(clusterPositionY));

            // for (int i = 0; i < arrayListofTempClusters.get(clusterPositionY).size(); i++) {
            //     int y = arrayListofTempClusters.get(clusterPositionY).get(i);
            //     arrayListofTempClusters.get(clusterPositionX).add(y);
            // }

            // empty the cluster at clusterPositionY
            arrayListofTempClusters.get(clusterPositionY).clear();

            // to get desired number of clusters
            int clusterCounter = 0;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    clusterCounter++; // determines number of clusters
                }
            }

            numOfClusters = clusterCounter;
            System.out.println("Number of Clusters: " + numOfClusters);
        }

        arrayListofClusters = arrayListofTempClusters;

        // Display clusters
        System.out.println("Final Clusters after Average-Linkage:");
        int totalClusters = 0;
        for (int i = 0; i < arrayListofClusters.size(); i++) {
            if (arrayListofClusters.get(i).size() > 0) {
                for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
                totalClusters++;
            }
        }
        System.out.println("\n total clusters: " + totalClusters);
    }
}
