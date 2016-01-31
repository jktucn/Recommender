package recommender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author jktu
 */
class Item {
    public int userId, movieId;
    public double rating;
    public final double RATINGFORUNKNOWN = -999;
    public Item(int u, int m, double r) {
        userId = u;
        movieId = m;
        rating = r;
    }
    public Item(int u, int m) {
        userId = u;
        movieId = m;
        rating = RATINGFORUNKNOWN;
    }
    public static Comparator<Item> userIdComparator = new Comparator<Item>() {
    @Override
    public int compare(Item i1, Item i2) {
        if(i1.userId != i2.userId)
            return i1.userId - i2.userId;
        else
        return i1.movieId - i2.movieId;
    }
    };
    public static Comparator<Item> movieIdComparator = new Comparator<Item>() {
    @Override
    public int compare(Item i1, Item i2) {
        if(i1.movieId != i2.movieId)
            return i1.movieId - i2.movieId;
        else
            return i1.userId - i2.userId;
    }
    };
    @Override
    public String toString() {
        return "[ " + userId + ", " + movieId + ", " + rating + " ]";
    }
}
class User {
    // for gender F=0, M=1;
    public int userId, age, gender, zipcode;
    public String occupation;
    public User(int userId, int age, int gender, int zipcode, String occupation) {
        this.userId = userId;
        this.age = age;
        this.gender = gender;
        this.zipcode = zipcode;
        this.occupation = occupation;
    }
    public static Comparator<User> comparator = new Comparator<User>() {
    @Override
    public int compare(User u1, User u2) {
        return u1.userId - u2.userId;
    }
    };
    @Override
    public String toString() {
        return "[ " + userId + ", " + age + ", " + gender + ", " + zipcode + ", " + occupation + " ]";
    }
}
public class Recommender {
    public ArrayList<Item> dataSortedByUser = new ArrayList<Item>();
    public ArrayList<Item> dataSortedByMovie = new ArrayList<Item>();
    public ArrayList<Item> recommendation = new ArrayList<Item>();
    public ArrayList<User> userData = new ArrayList<User>();
    public HashMap ratingData = new HashMap(30000);
    public HashMap movieCorrelation = new HashMap(30000);
    public HashMap userCorrelation = new HashMap(30000);
    
    public Recommender(String dataFile, String UnknownRatingFile) throws FileNotFoundException {
        Scanner in = new Scanner(new File(dataFile));
        while(in.hasNext()) {
            int userId = in.nextInt();
            int movieId = in.nextInt();
            double rating = in.nextDouble();
            Item i = new Item(userId, movieId, rating);
            dataSortedByUser.add(i);
            dataSortedByMovie.add(i);
            ratingData.put(i.userId + "#" + i.movieId, i.rating);
            in.nextLine();
        }
        Collections.sort(dataSortedByUser, Item.userIdComparator);
        //System.out.println(dataSortedByUser);
        Collections.sort(dataSortedByMovie, Item.movieIdComparator);
        //System.out.println(dataSortedByMovie);
        in = new Scanner(new File(UnknownRatingFile));
        while(in.hasNext()) {
            int userId = in.nextInt();
            int movieId = in.nextInt();
            recommendation.add(new Item(userId, movieId));
        }
    }
    public void slopeOne() {
        int userId, movieId, smallMovieId, bigMovieId, previousUserId, n;
        double predictedRating;
        ArrayList<Integer> movies = new ArrayList<Integer>();
        previousUserId = -1;
        for(int i = 0; i < recommendation.size(); i++) {
            //System.out.println((i+1) + " / " + (recommendation.size()));
            n = 0;
            predictedRating = 0;
            userId = recommendation.get(i).userId;
            movieId = recommendation.get(i).movieId;
            if (previousUserId != userId)
                movies = moviesOfUser(userId);
            for(int j = 0; j < movies.size(); j++) {
                if(movies.get(j) < movieId) {
                    smallMovieId = movies.get(j);
                    bigMovieId = movieId;
                }
                else if(movies.get(j) > movieId) {
                    smallMovieId = movieId;
                    bigMovieId = movies.get(j);
                }
                else
                    throw new IllegalArgumentException("One user have 2 ratings on same movie");
                    //System.out.println("Error one user have 2 ratings on same movie");
                if(!movieCorrelation.containsKey(smallMovieId + "#" + bigMovieId))
                    calculateCorrelation(smallMovieId, bigMovieId);
                if(movieCorrelation.get(smallMovieId + "#" + bigMovieId) != null) {
                    if(movieId == bigMovieId) {
                        predictedRating += (double)ratingData.get(userId + "#" + movies.get(j)) + (double)movieCorrelation.get(smallMovieId + "#" + bigMovieId);
                        n++;
                    }
                    else {
                        predictedRating += (double)ratingData.get(userId + "#" + movies.get(j)) - (double)movieCorrelation.get(smallMovieId + "#" + bigMovieId);
                        n++;
                    }
                }
            }
            if(n != 0)
                recommendation.get(i).rating = predictedRating / n;
            else {
                double sumRating = 0;
                for(int k = 0; k < movies.size(); k++)
                    sumRating += (double)ratingData.get(userId + "#" + movies.get(k));
                recommendation.get(i).rating = sumRating / movies.size();
            }
            previousUserId = userId;
        }
    }
    public void slopeOne2() {
        int userId, movieId, smallMovieId, bigMovieId, previousUserId, n;
        double predictedRating;
        ArrayList<Integer> movies = new ArrayList<Integer>();
        previousUserId = -1;
        for(int i = 0; i < recommendation.size(); i++) {
            //System.out.println((i+1) + " / " + (recommendation.size()));
            n = 0;
            predictedRating = 0;
            userId = recommendation.get(i).userId;
            movieId = recommendation.get(i).movieId;
            if (previousUserId != userId)
                movies = moviesOfUser(userId);
            for(int j = 0; j < movies.size(); j++) {
                if(movies.get(j) < movieId) {
                    smallMovieId = movies.get(j);
                    bigMovieId = movieId;
                }
                else if(movies.get(j) > movieId) {
                    smallMovieId = movieId;
                    bigMovieId = movies.get(j);
                }
                else
                    throw new IllegalArgumentException("One user have 2 ratings on same movie");
                    //System.out.println("Error one user have 2 ratings on same movie");
                //if(!movieCorrelation.containsKey(smallMovieId + "#" + bigMovieId + "#" + userId))
                double avgDifference = calculateCorrelation(smallMovieId, bigMovieId, userId);
                if(avgDifference != 999) {
                    if(movieId == bigMovieId) {
                        predictedRating += (double)ratingData.get(userId + "#" + movies.get(j)) + avgDifference;
                        n++;
                    }
                    else {
                        predictedRating += (double)ratingData.get(userId + "#" + movies.get(j)) - avgDifference;
                        n++;
                    }
                }
            }
            if(n != 0)
                recommendation.get(i).rating = predictedRating / n;
            else {
                double sumRating = 0;
                for(int k = 0; k < movies.size(); k++)
                    sumRating += (double)ratingData.get(userId + "#" + movies.get(k));
                recommendation.get(i).rating = sumRating / movies.size();
            }
            previousUserId = userId;
        }
    }
    public void calculateCorrelation(int smallMovieId, int bigMovieId) {
        double avgDifference, difference = 0;
        ArrayList<Integer> commonUser = commonUserOfMovie(smallMovieId, bigMovieId);
        if(commonUser.size() > 0) {
            for(int i = 0; i < commonUser.size(); i++)
                difference += (double)ratingData.get(commonUser.get(i) + "#" + bigMovieId)
                        - (double)ratingData.get(commonUser.get(i) + "#" + smallMovieId);
            avgDifference = difference / commonUser.size();
            movieCorrelation.put(smallMovieId + "#" + bigMovieId, avgDifference);
        }
        else
            movieCorrelation.put(smallMovieId + "#" + bigMovieId, null);
    }
    public double calculateCorrelation(int smallMovieId, int bigMovieId, int userId) {
        double avgDifference, difference = 0, sumCorrelation = 0;
        ArrayList<Integer> commonUser = commonUserOfMovie(smallMovieId, bigMovieId);
        if(commonUser.size() > 0) {
            for(int i = 0; i < commonUser.size(); i++) {
                int smallUserId, bigUserId;
                if (userId < commonUser.get(i)) {
                    smallUserId = userId;
                    bigUserId = commonUser.get(i);
                }
                else {
                    smallUserId = commonUser.get(i);
                    bigUserId = userId;
                }
                if(!userCorrelation.containsKey(smallUserId + "#" + bigUserId))
                    calculateUserCorrelation(smallUserId, bigUserId);
                difference += ((double)ratingData.get(commonUser.get(i) + "#" + bigMovieId)
                        - (double)ratingData.get(commonUser.get(i) + "#" + smallMovieId)) * 
                        (double)userCorrelation.get(smallUserId + "#" + bigUserId);
                sumCorrelation += (double)userCorrelation.get(smallUserId + "#" + bigUserId);
            }
            avgDifference = difference / sumCorrelation;
            return avgDifference;
        }
        else
            return 999;
    }
    public ArrayList<Integer> commonUserOfMovie(int movie1, int movie2) {
        int index1, index2;
        ArrayList<Integer> users1, users2, commonUser;
        users1 = userOfMovie(movie1);
        users2 = userOfMovie(movie2);
        commonUser = new ArrayList<Integer>();
        index1 = index2 = 0;
        while(index1 < users1.size() && index2 < users2.size()) {
            if(users1.get(index1) < users2.get(index2))
                index1++;
            else if(users1.get(index1) > users2.get(index2))
                index2++;
            else {
                commonUser.add(users1.get(index1));
                index1++;
                index2++;
            }
        }
        return commonUser;
    }
    public ArrayList<Integer> userOfMovie(int movieId) {
        int index, leftIndex, rightIndex;
        ArrayList<Integer> users = new ArrayList<Integer>();
        index = binarySearchMovie(dataSortedByMovie, movieId, 0, dataSortedByMovie.size() - 1);
        if(index == -1)
            return users;
        leftIndex = rightIndex = index;
        do {
            leftIndex--;
        }while(leftIndex >=0 && dataSortedByMovie.get(leftIndex).movieId == movieId);
        leftIndex++;
        do {
            rightIndex++;
        }while(rightIndex <= dataSortedByMovie.size() - 1 && dataSortedByMovie.get(rightIndex).movieId == movieId);
        rightIndex--;
        for(int i = leftIndex; i <= rightIndex; i++) {
            users.add(dataSortedByMovie.get(i).userId);
        }
        return users;
    }
    public ArrayList<Integer> moviesOfUser(int userId) {
        int index, leftIndex, rightIndex;
        ArrayList<Integer> movies = new ArrayList<Integer>();
        index = binarySearchUser(dataSortedByUser, userId, 0, dataSortedByUser.size() - 1);
        leftIndex = rightIndex = index;
        do {
            leftIndex--;
        }while(leftIndex >=0 && dataSortedByUser.get(leftIndex).userId == userId);
        leftIndex++;
        do {
            rightIndex++;
        }while(rightIndex <= dataSortedByUser.size() - 1 && dataSortedByUser.get(rightIndex).userId == userId);
        rightIndex--;
        for(int i = leftIndex; i <= rightIndex; i++) {
            movies.add(dataSortedByUser.get(i).movieId);
        }
        return movies;
    }
    public int binarySearchUser(ArrayList<Item> al, int key, int i, int j) {
        if(i > j)
            return -1;
        int k = (i + j) / 2;
        if(al.get(k).userId > key)
            return binarySearchUser(al, key, i, k - 1);
        if(al.get(k).userId < key)
            return binarySearchUser(al, key, k + 1, j);
        return k;
    }
    public int binarySearchMovie(ArrayList<Item> al, int key, int i, int j) {
        if(i > j)
            return -1;
        int k = (i + j) / 2;
        if(al.get(k).movieId > key)
            return binarySearchMovie(al, key, i, k - 1);
        if(al.get(k).movieId < key)
            return binarySearchMovie(al, key, k + 1, j);
        return k;
    }
    public void getUserInfo(String userInfoFile) throws FileNotFoundException {
        int userId, age, gender, zipcode;
        String occupation;
        final int NOVAL = -99999;
        /*
        final int AGEDIFF_1 = 5;
        final double AGEDIFF_1_COEFF = 0.3;
        final int AGEDIFF_2 = 10;
        final double AGEDIFF_2_COEFF = 0.2;
        final double GENDERMATCH = 0.5;
        final double OCCUPATIONMATCH = 0.5;
        final int ZIPCODEDIFF = 500;
        final double ZIPCODEDIFF_CONEFF = 0.5;
        */
        Scanner in = new Scanner(new File(userInfoFile));
        while(in.hasNextLine()) {
            String line = in.nextLine();
            int index[] = new int[4];
            int j = 0;
            for(int i = 0; i < line.length(); i++) 
                if (line.substring(i, i+1).compareTo("|") == 0)
                    index[j++] = i;
            try{
                userId = Integer.parseInt(line.substring(0, index[0]));
            }catch(NumberFormatException e){
                System.out.println("wrong userId");
                userId = NOVAL;
            }
            try{
                age = Integer.parseInt(line.substring(index[0]+1, index[1]));
            }catch(NumberFormatException e){
                System.out.println("wrong age");
                age = NOVAL;
            }
            if(line.substring(index[1]+1,index[2]).compareTo("M") == 0)
                gender = 1;
            else
                gender = 0;
            occupation = line.substring(index[2]+1,index[3]);
            try{
                zipcode = Integer.parseInt(line.substring(index[3]+1, line.length()));
            }catch(NumberFormatException e){
                zipcode = NOVAL;
            }
            userData.add(new User(userId, age, gender, zipcode, occupation));
        }
        Collections.sort(userData, User.comparator);
        /*
        for(int i = 0; i < userData.size() - 1; i++) {
            for(int j = i + 1; j < userData.size(); j++) {
                double correlation = 1;
                if(Math.abs(userData.get(i).age - userData.get(j).age) <= AGEDIFF_1)
                    correlation += AGEDIFF_1_COEFF;
                if(Math.abs(userData.get(i).age - userData.get(j).age) <= AGEDIFF_2)
                    correlation += AGEDIFF_2_COEFF;
                if(userData.get(i).gender == userData.get(j).gender)
                    correlation += GENDERMATCH;
                if(userData.get(i).occupation.compareTo(userData.get(j).occupation) == 0)
                    correlation += OCCUPATIONMATCH;
                if(Math.abs(userData.get(i).zipcode - userData.get(j).zipcode) <= ZIPCODEDIFF && userData.get(i).zipcode != NOVAL)
                    correlation += ZIPCODEDIFF_CONEFF;
                userCorrelation.put(userData.get(i).userId + "#" + userData.get(j).userId, correlation);
            }
        }
        */
    }
    public void calculateUserCorrelation(int smallUserId, int bigUserId) {
        final int NOVAL = -99999;
        final int AGEDIFF_1 = 5;
        final double AGEDIFF_1_COEFF = 0.3;
        final int AGEDIFF_2 = 10;
        final double AGEDIFF_2_COEFF = 0.2;
        final double GENDERMATCH = 0.5;
        final double OCCUPATIONMATCH = 0.5;
        final int ZIPCODEDIFF = 500;
        final double ZIPCODEDIFF_CONEFF = 0.5;
        double correlation = 1;
        int i = smallUserId - 1, j = bigUserId - 1;
        if(userData.get(i).userId != smallUserId || userData.get(j).userId != bigUserId)
            System.out.println("Error in calculateUserCorrelation method");
        if(Math.abs(userData.get(i).age - userData.get(j).age) <= AGEDIFF_1)
            correlation += AGEDIFF_1_COEFF;
        if(Math.abs(userData.get(i).age - userData.get(j).age) <= AGEDIFF_2)
            correlation += AGEDIFF_2_COEFF;
        if(userData.get(i).gender == userData.get(j).gender)
            correlation += GENDERMATCH;
        if(userData.get(i).occupation.compareTo(userData.get(j).occupation) == 0)
            correlation += OCCUPATIONMATCH;
        if(Math.abs(userData.get(i).zipcode - userData.get(j).zipcode) <= ZIPCODEDIFF && userData.get(i).zipcode != NOVAL)
            correlation += ZIPCODEDIFF_CONEFF;
        userCorrelation.put(userData.get(i).userId + "#" + userData.get(j).userId, correlation);
    }
    public static void makeUnknownRatingFile(String testFileName, String unknownRatingFileName) throws FileNotFoundException {
        ArrayList<Item> array = new ArrayList<Item>();
        Scanner in = new Scanner(new File(testFileName));
        while(in.hasNext()) {
            int userId = in.nextInt();
            int movieId = in.nextInt();
            array.add(new Item(userId, movieId));
            in.nextLine();
        }
        PrintWriter fout = new PrintWriter(unknownRatingFileName);
        fout.printf("%d\t%d", array.get(0).userId, array.get(0).movieId);
        for(int i = 1; i < array.size(); i++) {
            fout.println();
            fout.printf("%d\t%d", array.get(i).userId, array.get(i).movieId);
        }
        fout.close();
    }
    public void makeOutputFile(String fileName) throws FileNotFoundException {
        PrintWriter fout = new PrintWriter(fileName);
        fout.printf("%d\t%d\t%f", recommendation.get(0).userId, recommendation.get(0).movieId, recommendation.get(0).rating);
        for(int i = 1; i < recommendation.size(); i++) {
            fout.println();
            fout.printf("%d\t%d\t%f", recommendation.get(i).userId, recommendation.get(i).movieId, recommendation.get(i).rating);
        }
        fout.close();
    }
    public static double[] measurePerformance(String testFileName, String predictionFileName) throws FileNotFoundException {
        double[] measurement = new double[2];
        ArrayList<Item> test = new ArrayList<Item>();
        ArrayList<Item> prediction = new ArrayList<Item>();
        Scanner in = new Scanner(new File(testFileName));
        while(in.hasNext()) {
            int userId = in.nextInt();
            int movieId = in.nextInt();
            double rating = in.nextDouble();
            test.add(new Item(userId, movieId, rating));
            in.nextLine();
        }
        in = new Scanner(new File(predictionFileName));
        while(in.hasNext()) {
            int userId = in.nextInt();
            int movieId = in.nextInt();
            double rating = in.nextDouble();
            prediction.add(new Item(userId, movieId, rating));
        }
        int n = 0, flip = 0;
        double mse, mfc, sumSquare = 0; 
        if (test.size() != prediction.size())
            System.out.println("test file and prediction file are not match");
        for (int i = 0; i < test.size(); i++) {
            if (test.get(i).userId != prediction.get(i).userId || test.get(i).movieId != prediction.get(i).movieId)
                System.out.println("test file and prediction file are not match");
            sumSquare += Math.pow((test.get(i).rating - prediction.get(i).rating), 2);
            if (Math.abs(test.get(i).rating - prediction.get(i).rating) >= 2)
                flip++;
        }
        mse = sumSquare / test.size();
        mfc = 1.0 * flip / test.size();
        System.out.println("Measurement for Test file: " + testFileName + " and Prediction file: " + predictionFileName);
        System.out.printf("Mean Square Error (MSE) is %f\n", mse);
        System.out.printf("Mean Flip Count (MFC) is %f\n", mfc);
        measurement[0] = mse;
        measurement[1] = mfc;
        return measurement;
    }
    /*
    public static ArrayList<Item> readFile(String fileName) throws FileNotFoundException {
        ArrayList<Item> array = new ArrayList<Item>();
        Scanner in = new Scanner(new File(fileName));
        while(in.hasNext()) {
            int userId = in.nextInt();
            int movieId = in.nextInt();
            double rating = in.nextDouble();
            array.add(new Item(userId, movieId, rating));
            in.nextLine();
        }
        return array;
    }
    */
    public static void main(String[] args) throws FileNotFoundException {
        double m1[], sumMse1 = 0, sumMfc1 = 0;
        double m2[], sumMse2 = 0, sumMfc2 = 0;
        int n = 0;
        
        makeUnknownRatingFile("u1.test", "u1.test.UnknownRating");
        Recommender u1 = new Recommender("u1.base", "u1.test.UnknownRating");
        u1.slopeOne();
        u1.makeOutputFile("u1.test.Prediction");
        m1 = measurePerformance("u1.test", "u1.test.Prediction");
        sumMse1 += m1[0];
        sumMfc1 += m1[1];
        u1.getUserInfo("u.user");
        u1.slopeOne2();
        u1.makeOutputFile("u1.test.Prediction2");
        m2 = measurePerformance("u1.test", "u1.test.Prediction2");
        sumMse2 += m2[0];
        sumMfc2 += m2[1];
        n++;
        u1 = null;
        System.gc();
        
        makeUnknownRatingFile("u2.test", "u2.test.UnknownRating");
        Recommender u2 = new Recommender("u2.base", "u2.test.UnknownRating");
        u2.slopeOne();
        u2.makeOutputFile("u2.test.Prediction");
        m1 = measurePerformance("u2.test", "u2.test.Prediction");
        sumMse1 += m1[0];
        sumMfc1 += m1[1];
        u2.getUserInfo("u.user");
        u2.slopeOne2();
        u2.makeOutputFile("u2.test.Prediction2");
        m2 = measurePerformance("u2.test", "u2.test.Prediction2");
        sumMse2 += m2[0];
        sumMfc2 += m2[1];
        n++;
        u2 = null;
        System.gc();
        
        makeUnknownRatingFile("u3.test", "u3.test.UnknownRating");
        Recommender u3 = new Recommender("u3.base", "u3.test.UnknownRating");
        u3.slopeOne();
        u3.makeOutputFile("u3.test.Prediction");
        m1 = measurePerformance("u3.test", "u3.test.Prediction");
        sumMse1 += m1[0];
        sumMfc1 += m1[1];
        u3.getUserInfo("u.user");
        u3.slopeOne2();
        u3.makeOutputFile("u3.test.Prediction2");
        m2 = measurePerformance("u3.test", "u3.test.Prediction2");
        sumMse2 += m2[0];
        sumMfc2 += m2[1];
        n++;
        u3 = null;
        System.gc();
        
        makeUnknownRatingFile("u4.test", "u4.test.UnknownRating");
        Recommender u4 = new Recommender("u4.base", "u4.test.UnknownRating");
        u4.slopeOne();
        u4.makeOutputFile("u4.test.Prediction");
        m1 = measurePerformance("u4.test", "u4.test.Prediction");
        sumMse1 += m1[0];
        sumMfc1 += m1[1];
        u4.getUserInfo("u.user");
        u4.slopeOne2();
        u4.makeOutputFile("u4.test.Prediction2");
        m2 = measurePerformance("u4.test", "u4.test.Prediction2");
        sumMse2 += m2[0];
        sumMfc2 += m2[1];
        n++;
        u4 = null;
        System.gc();
        
        makeUnknownRatingFile("u5.test", "u5.test.UnknownRating");
        Recommender u5 = new Recommender("u5.base", "u5.test.UnknownRating");
        u5.slopeOne();
        u5.makeOutputFile("u5.test.Prediction");
        m1 = measurePerformance("u5.test", "u5.test.Prediction");
        sumMse1 += m1[0];
        sumMfc1 += m1[1];
        u5.getUserInfo("u.user");
        u5.slopeOne2();
        u5.makeOutputFile("u5.test.Prediction2");
        m2 = measurePerformance("u5.test", "u5.test.Prediction2");
        sumMse2 += m2[0];
        sumMfc2 += m2[1];
        n++;
        
        System.out.println();
        System.out.println("Slope One Method:");
        System.out.printf("Average Mean Square Error (MSE) is %f\n", sumMse1/n);
        System.out.printf("Average Mean Flip Count (MFC) is %f\n", sumMfc1/n);
        System.out.println();
        System.out.println("Slope One Modified Method:");
        System.out.printf("Average Mean Square Error (MSE) is %f\n", sumMse2/n);
        System.out.printf("Average Mean Flip Count (MFC) is %f\n", sumMfc2/n);
    }
    
}
