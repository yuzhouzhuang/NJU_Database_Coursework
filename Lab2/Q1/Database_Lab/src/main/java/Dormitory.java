import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Dormitory {

    private Connection conn;

    public static void main(String[] args) {
        System.out.println("!!Start Dormitory Mission!!");
        Dormitory dormitoryManager = new Dormitory();
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

        System.out.println("Step 3----查询“王小星”同学所在宿舍楼的所有院系");
        startTime = System.currentTimeMillis();
        dormitoryManager.findWangXiaoxing();
        endTime = System.currentTimeMillis();
        System.out.println("End Step 3----Using  " + (endTime - startTime) + " ms");

        System.out.println("Step 4----陶园一舍的住宿费用提高至1200元,修改数据库");
        startTime = System.currentTimeMillis();
        dormitoryManager.modifyCharge();
        endTime = System.currentTimeMillis();
        System.out.println("End Step 4----Using  " + (endTime - startTime) + " ms");

        System.out.println("Step 5----软件学院男女研究生互换宿舍楼,修改数据库");
        startTime = System.currentTimeMillis();
        dormitoryManager.exchangeDormitory();
        endTime = System.currentTimeMillis();
        System.out.println("End Step 5----Using  " + (endTime - startTime) + " ms");
    }

    private void exchangeDormitory() {
        executeSQL(new String[]{"UPDATE student s1\n" +
                "  JOIN student s2\n" +
                "  JOIN institution i ON s1.gender = '男' AND s2.gender = '女' AND s1.iid = s2.iid AND s1.iid = i.iid AND i.iname = '软件学院'\n" +
                "SET s1.did = s2.did, s2.did = s1.did"});
    }

    private void modifyCharge() {
        executeSQL(new String[]{"UPDATE dormitory SET charge = '1200' WHERE dname = '陶园1舍'"});
    }

    private void findWangXiaoxing() {
        String sql = "SELECT DISTINCT i.iname\n" +
                "FROM institution i, student s\n" +
                "WHERE i.iid = s.iid AND s.did =\n" +
                "                        (SELECT did\n" +
                "                         FROM student\n" +
                "                         WHERE sname = '王小星')";
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String student_check = "drop table if exists student;";
        String student_create = "CREATE TABLE student (\n" +
                "  `sid` varchar(11) NOT NULL,\n" +
                "  `sname` varchar(255) NOT NULL,\n" +
                "  `gender` varchar(11) NOT NULL,\n" +
                "  `iid` INT(11) NOT NULL,\n" +
                "  `did` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`sid`),\n" +
                "  FOREIGN KEY (`iid`) REFERENCES institution(`iid`),\n" +
                "  FOREIGN KEY (`did`) REFERENCES dormitory(`did`)\n" +
                ");";

        String institution_check = "drop table if exists institution;";
        String institution_create = "CREATE TABLE institution (\n" +
                "  `iid` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `iname` varchar(255) NOT NULL,\n" +
                "  PRIMARY KEY (`iid`)\n" +
                ");";

        String dormitory_check = "drop table if exists dormitory;";
        String dormitory_create = "CREATE TABLE dormitory (\n" +
                "  `did` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `dname` varchar(255) NOT NULL,\n" +
                "  `charge` INT(11) NOT NULL,\n" +
                "  `campus` VARCHAR(11) NOT NULL,\n" +
                "  `phone` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`did`)\n" +
                ");";
        executeSQL(new String[]{student_check, institution_check, dormitory_check});
        executeSQL(new String[]{institution_create, dormitory_create, student_create});
    }

    private void executeSQL(String[] sqlList) {
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


    private void insertData() {
        boolean mark = true;

        ArrayList<String[]> dormitoryData = readFile("src/main/resources/电话.txt");
        ArrayList<String[]> studentData = readFile("src/main/resources/分配方案.csv");

        ArrayList<String> institutionList = new ArrayList<String>();
        for (String[] institutionRecord : studentData.subList(1, studentData.size())) {
            String[] record = institutionRecord[0].split(",");
            if (!institutionList.contains(record[0])) {
                institutionList.add(record[0]);
            }
        }
        StringBuffer institutionSQL = new StringBuffer();
        institutionSQL.append("INSERT INTO institution(iname) VALUES ");
        for (String record : institutionList) {
            if (mark){
                mark = false;
            } else {
                institutionSQL.append(",");
            }
            institutionSQL.append("('" + record + "')");
        }
        System.out.println(institutionSQL.toString());
        executeSQL(new String[]{institutionSQL.toString()});


        HashMap<String, String[]> dormitoryMap = new HashMap<String, String[]>();
        for (String[] dormitoryRecord : studentData.subList(1, studentData.size())) {
            String[] record = dormitoryRecord[0].split(",");
            if (!dormitoryMap.containsKey(record[5])) {
                dormitoryMap.put(record[5], new String[]{record[4], record[6]});
            }
        }
        ArrayList<String> dormitoryList = new ArrayList<String>();
        for (String[] dormitoryRecord : dormitoryData.subList(1, dormitoryData.size())) {
            String[] record = dormitoryRecord[0].split(";");
            if (!dormitoryList.contains(record[0])) {
                dormitoryList.add(record[0]);
            }
        }
        mark = true;
        StringBuffer dormitorySQL = new StringBuffer();
        dormitorySQL.append("INSERT INTO dormitory(dname, phone, charge,campus) VALUES ");
        for (String[] dormitoryRecord : dormitoryData.subList(1, dormitoryData.size())) {
            if (mark){
                mark = false;
            } else {
                dormitorySQL.append(",");
            }
            String[] record = dormitoryRecord[0].split(";");
            dormitorySQL.append("('" + record[0] + "','" +
                    record[1] + "','" + dormitoryMap.get(record[0])[1] + "','" + dormitoryMap.get(record[0])[0] + "')");
        }
        executeSQL(new String[]{dormitorySQL.toString()});

        StringBuffer studentSQL = new  StringBuffer();
        studentSQL.append("INSERT INTO student(sid, sname, gender, iid, did) VALUES ");

        mark = true;
        for (String[] studentRecord : studentData.subList(1, studentData.size())) {
            if (mark){
                mark = false;
            } else {
                studentSQL.append(",");
            }
            String[] record = studentRecord[0].split(",");
            studentSQL.append("('" + record[1] + "','" +
                    record[2] + "','" + record[3] + "','" + (institutionList.indexOf(record[0]) + 1) + "','" +
                    (dormitoryList.indexOf(record[5]) + 1) + "')");
        }
        executeSQL(new String[]{studentSQL.toString()});
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
