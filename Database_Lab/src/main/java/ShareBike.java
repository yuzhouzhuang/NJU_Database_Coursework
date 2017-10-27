import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShareBike {

    private Connection conn;

    public static void main(String[] args) {
        System.out.println("!!Start ShareBike Mission!!");
        ShareBike dormitoryManager = new ShareBike();
        long startTime = 0;
        long endTime = 0;
        dormitoryManager.initConnection();

        System.out.println("Step 1----建表");
        startTime = System.currentTimeMillis();
        dormitoryManager.createTables();
        endTime = System.currentTimeMillis();
        System.out.println("End Step 1----Using " + (endTime - startTime) + " ms");

        System.out.println("Step 2----插入所有数据");
        startTime = System.currentTimeMillis();
        dormitoryManager.insertData();
        endTime = System.currentTimeMillis();
        System.out.println("End Step 2----Using  " + (endTime - startTime) + " ms");

        System.out.println("Step 3----添加家庭住址");
        startTime = System.currentTimeMillis();
        dormitoryManager.addHomePosition();
        endTime = System.currentTimeMillis();
        System.out.println("End Step 3----Using  " + (endTime - startTime) + " ms");

        System.out.println("Step 4----更新单车维护表");
        startTime = System.currentTimeMillis();
        dormitoryManager.updateRepairInfo();
        endTime = System.currentTimeMillis();
        System.out.println("End Step 4----Using  " + (endTime - startTime) + " ms");

    }

    private void updateRepairInfo() {
    }

    private void addHomePosition() {

    }


    private void initConnection() {
        System.out.println("连接到数据库");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/course_lab", "root", "");
            System.out.println("连接成功");
        } catch (Exception e) {
            System.out.println("连接失败!");
            e.printStackTrace();
        }
    }

    private void createTables() {
        String bike_check = "drop table if exists bike;";
        String bike_create = "CREATE TABLE bike(\n" +
                "  `bid` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`bid`)\n" +
                ");";

        String user_check = "drop table if exists user;";
        String user_create = "CREATE TABLE user(\n" +
                "  `uid` INT(11) NOT NULL,\n" +
                "  `uname` VARCHAR(11) NOT NULL,\n" +
                "  `phone` VARCHAR(11) NOT NULL,\n" +
                "  `money` DOUBLE NOT NULL,\n" +
                "  `home` VARCHAR(255),\n" +
                "  PRIMARY KEY (`uid`)\n" +
                ");";

        String location_check = "drop table if exists location;";
        String location_create = "CREATE TABLE location(\n" +
                "  `lid` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `lname` VARCHAR(255) NOT NULL,\n" +
                "  PRIMARY KEY (`lid`)\n" +
                ");";

        String rent_check = "drop table if exists rent;";
        String rent_create = "CREATE TABLE rent(\n" +
                "  `bid` INT(11) NOT NULL,\n" +
                "  `uid` INT(11) NOT NULL,\n" +
                "  `start_lid` INT(11) NOT NULL,\n" +
                "  `start_time` DATETIME NOT NULL,\n" +
                "  `end_lid` INT(11) NOT NULL,\n" +
                "  `end_time` DATETIME NOT NULL,\n" +
                "  `charge` INT(11)\n" +
                ");";

        String repair_check = "drop table if exists repair;";
        String repair_create = "CREATE TABLE repair(\n" +
                "  `bid` INT(11) NOT NULL,\n" +
                "  `lid` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`bid`)\n" +
                ");";
        executeSQLS(new String[]{bike_check, user_check, location_check, rent_check, repair_check});
        executeSQLS(new String[]{user_create, location_create, bike_create, rent_create, repair_create});
    }

    private void executeSQLS(String[] sqlList) {
        try {
            Statement statement = conn.createStatement();
            for (String sql : sqlList) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeSQL(String sql) {
        try {
            Statement statement = conn.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void insertData() {
        ArrayList<String[]> bikeData = readFile("src/main/resources/bike.txt");
        StringBuffer bikeSQL = new StringBuffer();
        bikeSQL.append("INSERT INTO bike(bid) VALUES ");
        for (String[] bikeRecord : bikeData) {
            bikeSQL.append("('" + bikeRecord[0] + "'),");
        }
        bikeSQL.deleteCharAt(bikeSQL.length() - 1);
        executeSQLS(new String[]{bikeSQL.toString()});

        ArrayList<String[]> userData = readFile("src/main/resources/user.txt");
        List<String[]> tempData = new ArrayList<String[]>();
        int mark = 0;
        while (mark < userData.size()) {
            if (mark + 10000 >= userData.size()) {
                tempData = userData.subList(mark, userData.size());
            } else {
                tempData = userData.subList(mark, mark + 10000);
            }
            mark += 10000;

            StringBuffer userSQL = new StringBuffer();
            userSQL.append("INSERT INTO user(uid, uname, phone, money) VALUES ");
            for (String[] userRecord : tempData) {
                String[] record = userRecord[0].split(";");
                userSQL.append("('" + record[0] + "','" + record[1] + "','" + record[2] + "','" + record[3] + "'),");
            }
            userSQL.deleteCharAt(userSQL.length() - 1);
            executeSQLS(new String[]{userSQL.toString()});
        }

        ArrayList<String[]> recordData = readFile("src/main/resources/record.txt");
        ArrayList<String> locationList = new ArrayList<String>();
        for (String[] recordRecord : recordData) {
            String[] record = recordRecord[0].split(";");
            if (!locationList.contains(record[2])) {
                locationList.add(record[2]);
            }
            if (!locationList.contains(record[4])) {
                locationList.add(record[4]);
            }
        }
        StringBuffer locationSQL = new StringBuffer();
        locationSQL.append("INSERT INTO location(lname) VALUES ");
        for (String location : locationList) {
            locationSQL.append("('" + location + "'),");
        }
        locationSQL.deleteCharAt(locationSQL.length() - 1);
        executeSQL(locationSQL.toString());

        mark = 0;
        while (mark < recordData.size()) {
            if (mark + 10000 >=recordData.size()) {
                tempData = recordData.subList(mark, recordData.size());
            } else {
                tempData = recordData.subList(mark, mark + 10000);
            }
            mark += 10000;

            StringBuffer recordSQL = new StringBuffer();
            recordSQL.append("INSERT INTO rent(bid, uid, start_lid, start_time, end_lid, end_time) VALUES ");
            for (String[] recordRecord : tempData) {
                String[] record = recordRecord[0].split(";");
                recordSQL.append("('" + record[1] + "','" + record[0] + "','" + (locationList.indexOf(record[2]) + 1) +
                        "',STR_TO_DATE('" + record[3] + "','%Y/%m/%d-%H:%i:%s'),'" + (locationList.indexOf(record[4]) + 1) + "',STR_TO_DATE('" + record[5] + "','%Y/%m/%d-%H:%i:%s')),");
            }
            recordSQL.deleteCharAt(recordSQL.length() - 1);
            executeSQL(recordSQL.toString());
        }

    }

    private ArrayList<String[]> readFile(String filePath) {
        ArrayList<String[]> list = new ArrayList<String[]>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filePath), "utf-8"));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                list.add(temp.split("\t"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
